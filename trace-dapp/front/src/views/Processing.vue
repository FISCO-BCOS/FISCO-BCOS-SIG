<template>
  <div id="app">
    <el-container>
      <el-header class="header"><Header /></el-header>
      <el-container>
        <el-aside width="220px"><navigator></navigator></el-aside>
        <el-main class="page-main">
          <div class="page-title-bar">
            <h2><i class="el-icon-s-operation"></i> 加工记录录入</h2>
            <span class="title-desc">录入产品的加工步骤信息，包括批次号、加工流程等</span>
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
              <el-form-item label="投入批次号" required>
                <el-input v-model="form.inputBatchNo" placeholder="请输入投入批次号"></el-input>
              </el-form-item>
              <el-form-item label="产出批次号" required>
                <el-input v-model="form.outputBatchNo" placeholder="请输入产出批次号"></el-input>
              </el-form-item>
              <el-divider></el-divider>
              <h3 style="display:inline-block">加工步骤记录</h3>
              <el-button type="primary" @click="addStep" style="margin-left:16px;margin-bottom:12px">添加</el-button>
              <el-table :data="form.processSteps" border style="width:100%;margin-bottom:16px">
                <el-table-column label="步骤名称" width="140">
                  <template slot-scope="scope">
                    <el-input v-model="scope.row.stepName" placeholder="步骤名称"></el-input>
                  </template>
                </el-table-column>
                <el-table-column label="步骤描述">
                  <template slot-scope="scope">
                    <el-input v-model="scope.row.description" placeholder="步骤描述"></el-input>
                  </template>
                </el-table-column>
                <el-table-column label="操作人员" width="120">
                  <template slot-scope="scope">
                    <el-input v-model="scope.row.operator" placeholder="操作人员"></el-input>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="80" fixed="right">
                  <template slot-scope="scope">
                    <el-button type="danger" size="mini" @click="removeStep(scope.$index)">移除</el-button>
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
  name: 'Processing',
  components: { Navigator, Header },
  data() {
    return {
      submitLoading: false,
      form: {
        productId: '',
        productName: '',
        inputBatchNo: '',
        outputBatchNo: '',
        processSteps: [{ stepName: '', description: '', operator: '' }]
      },
      products: []
    };
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
    addStep() {
      this.form.processSteps.push({ stepName: '', description: '', operator: '' });
    },
    removeStep(index) {
      if (this.form.processSteps.length > 1) {
        this.form.processSteps.splice(index, 1);
      }
    },
    handleSubmit() {
      if (!this.form.productId) {
        this.$message.warning('请选择产品');
        return;
      }
      if (!this.form.inputBatchNo) {
        this.$message.warning('请输入投入批次号');
        return;
      }
      if (!this.form.outputBatchNo) {
        this.$message.warning('请输入产出批次号');
        return;
      }
      this.submitLoading = true;
      this.axios.post('/api/trace/processing', this.form, {
        headers: { Authorization: 'Bearer ' + this.$cookies.get('token') }
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
        inputBatchNo: '',
        outputBatchNo: '',
        processSteps: [{ stepName: '', description: '', operator: '' }]
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
