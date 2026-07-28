<template>
  <div id="app">
    <el-container>
      <el-header class="header"><Header /></el-header>
      <el-container>
        <el-aside width="220px"><navigator></navigator></el-aside>
        <el-main class="page-main">
          <div class="page-title-bar">
            <h2><i class="el-icon-success"></i> 检测结果录入</h2>
            <span class="title-desc">录入产品的检测报告，包括检测结果、检测项详情等</span>
          </div>
          <div class="form-card fade-in-up">
            <el-form ref="form" :model="form" label-position="top" size="medium">
              <el-form-item label="选择产品" required>
                <el-select v-model="form.productId" filterable placeholder="请选择或搜索产品" style="width:100%">
                  <el-option v-for="item in products" :key="item.id" :label="item.productName + ' (' + (item.productNo || item.batchNo || '') + ')'" :value="item.id"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="产品名称">
                <el-input v-model="form.productName" disabled></el-input>
              </el-form-item>
              <el-form-item label="检测结果" required>
                <el-radio-group v-model="form.result">
                  <el-radio :label="1">合格</el-radio>
                  <el-radio :label="0">不合格</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="检测日期">
                <el-date-picker v-model="form.testDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" style="width:200px"></el-date-picker>
              </el-form-item>
              <el-form-item label="检测报告">
                <el-upload action="/api/file/upload" accept=".pdf,.doc,.docx,.jpg" :headers="uploadHeaders" :on-success="handleUploadSuccess" :on-remove="handleUploadRemove" :file-list="form.reportFiles">
                  <el-button size="small" type="primary">点击上传</el-button>
                  <div slot="tip" class="el-upload__tip">支持 pdf/doc/docx/jpg 格式</div>
                </el-upload>
              </el-form-item>
              <el-divider></el-divider>
              <h3 style="display:inline-block">检测项记录</h3>
              <el-button type="primary" @click="addItem" style="margin-left:16px;margin-bottom:12px">添加</el-button>
              <el-table :data="form.testItems" border style="width:100%;margin-bottom:16px">
                <el-table-column label="检测项目">
                  <template slot-scope="scope">
                    <el-input v-model="scope.row.itemName" placeholder="检测项目"></el-input>
                  </template>
                </el-table-column>
                <el-table-column label="检测标准">
                  <template slot-scope="scope">
                    <el-input v-model="scope.row.standard" placeholder="检测标准"></el-input>
                  </template>
                </el-table-column>
                <el-table-column label="检测结果">
                  <template slot-scope="scope">
                    <el-input v-model="scope.row.resultValue" placeholder="检测结果"></el-input>
                  </template>
                </el-table-column>
                <el-table-column label="判定" width="100">
                  <template slot-scope="scope">
                    <el-select v-model="scope.row.judgment" placeholder="判定" style="width:80px">
                      <el-option label="合格" value="合格"></el-option>
                      <el-option label="不合格" value="不合格"></el-option>
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="80" fixed="right">
                  <template slot-scope="scope">
                    <el-button type="danger" size="mini" @click="removeItem(scope.$index)">移除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-form-item>
                <el-button type="primary" @click="handleSubmit" :loading="submitLoading" size="large"><i class="el-icon-check"></i> 提交上链</el-button>
                <el-button @click="resetForm">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import Navigator from '@/components/Navigator';
import Header from '@/components/Header';

export default {
  name: 'Testing',
  components: { Navigator, Header },
  data() {
    return {
      submitLoading: false,
      form: {
        productId: '',
        productName: '',
        result: null,
        testDate: '',
        reportUrl: '',
        reportFiles: [],
        testItems: []
      },
      products: []
    };
  },
  computed: {
    uploadHeaders() {
      return { Authorization: 'Bearer ' + this.$cookies.get('token') };
    }
  },
  methods: {
    fetchProducts(query) {
      this.axios.get('/api/products/list', {
        params: { keyword: query, pageSize: 20 },
        headers: { Authorization: 'Bearer ' + this.$cookies.get('token') }
      }).then((response) => {
        if (response.data.code == 200) {
          const data = response.data.data;
          this.products = data.list || data.rows || (Array.isArray(data) ? data : []);
        }
      });
    },
    addItem() {
      this.form.testItems.push({ itemName: '', standard: '', resultValue: '', judgment: '' });
    },
    removeItem(index) {
      this.form.testItems.splice(index, 1);
    },
    handleUploadSuccess(response) {
      if (response.code == 200) {
        this.form.reportUrl = response.data.url || response.data;
      }
    },
    handleUploadRemove() {
      this.form.reportUrl = '';
    },
    handleSubmit() {
      if (!this.form.productId) {
        this.$message.warning('请选择产品');
        return;
      }
      if (this.form.result === null || this.form.result === '') {
        this.$message.warning('请选择检测结果');
        return;
      }
      this.submitLoading = true;
      const params = new URLSearchParams();
      params.append('productId', this.form.productId);
      params.append('testItems', JSON.stringify(this.form.testItems));
      params.append('testResult', Number(this.form.result));
      params.append('testDate', this.form.testDate || '');
      this.axios.post('/api/trace/testing', params, {
        headers: {
          Authorization: 'Bearer ' + this.$cookies.get('token'),
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      }).then((response) => {
        this.submitLoading = false;
        if (response.data.code == 200) {
          this.$message.success('提交成功');
          this.resetForm();
        } else {
          this.$message.error(response.data.msg || '提交失败');
        }
      }).catch(() => {
        this.submitLoading = false;
      });
    },
    resetForm() {
      this.form = {
        productId: '',
        productName: '',
        result: null,
        testDate: '',
        reportUrl: '',
        reportFiles: [],
        testItems: []
      };
    }
  },
  mounted() {
    this.axios.get('/api/products/list', {
      params: { pageSize: 999 },
      headers: { Authorization: 'Bearer ' + this.$cookies.get('token') }
    }).then((response) => {
      if (response.data.code == 200) {
        const data = response.data.data;
        this.products = data.list || data.rows || (Array.isArray(data) ? data : []);
      }
    });
  }
};
</script>

<style scoped>
.page-main { padding: 20px !important; }
.page-title-bar { margin-bottom: 16px; }
.page-title-bar h2 { font-size: 20px; font-weight: 600; color: var(--text-primary); margin: 0 0 4px; display: flex; align-items: center; gap: 8px; }
.page-title-bar h2 i { color: var(--primary); }
.title-desc { font-size: 13px; color: var(--text-secondary); }
.form-card { background: #fff; border-radius: var(--radius-md); padding: 28px 32px; box-shadow: var(--shadow-sm); max-width: 960px; }

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}
.fade-in-up { animation: fadeInUp 0.4s ease-out; }
</style>
