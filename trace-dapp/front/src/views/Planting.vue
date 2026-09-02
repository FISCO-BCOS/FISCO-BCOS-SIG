<template>
  <div id="app">
    <el-container>
      <el-header class="header"><Header /></el-header>
      <el-container>
        <el-aside width="220px"><navigator></navigator></el-aside>
        <el-main class="page-main">
          <div class="page-title-bar">
            <h2><i class="el-icon-s-promotion"></i> 种植记录录入</h2>
            <span class="title-desc">录入产品的种植信息，包括基地、品种、农事操作等</span>
          </div>
          <div class="form-card fade-in-up">
            <el-form ref="form" :model="form" label-position="top" size="medium">
              <el-form-item label="选择产品" required>
                <el-select v-model="form.productId" filterable placeholder="请选择或搜索产品" style="width:100%" @change="onProductChange">
                  <el-option v-for="item in products" :key="item.id" :label="item.productName + ' (' + (item.productNo || '') + ')'" :value="item.id"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="产品名称">
                <el-input v-model="form.productName" disabled></el-input>
              </el-form-item>
              <el-form-item label="种植基地名称" required>
                <el-input v-model="form.baseName" placeholder="请输入种植基地名称"></el-input>
              </el-form-item>
              <el-form-item label="基地地址" required>
                <el-input v-model="form.baseAddress" placeholder="请输入基地地址"></el-input>
              </el-form-item>
              <el-form-item label="种植面积">
                <el-input-number v-model="form.areaSize" :min="0" :precision="2" style="width:200px"></el-input-number>
                <span style="margin-left:8px">亩</span>
              </el-form-item>
              <el-form-item label="作物品种" required>
                <el-input v-model="form.cropVariety" placeholder="请输入作物品种"></el-input>
              </el-form-item>
              <el-form-item label="播种日期">
                <el-date-picker v-model="form.sowDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" style="width:200px"></el-date-picker>
              </el-form-item>
              <el-form-item label="预期产量">
                <el-input-number v-model="form.expectedYield" :min="0" :precision="2" style="width:200px"></el-input-number>
                <span style="margin-left:8px">公斤</span>
              </el-form-item>
              <el-divider></el-divider>
              <h3 style="display:inline-block">农事操作记录 <span class="required-mark">*</span></h3>
              <el-button type="primary" @click="addFarmOp" style="margin-left:16px;margin-bottom:12px">添加</el-button>
              <el-table :data="form.farmOps" border style="width:100%;margin-bottom:16px">
                <el-table-column label="操作日期" width="140">
                  <template slot-scope="scope">
                    <el-date-picker v-model="scope.row.date" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" style="width:120px"></el-date-picker>
                  </template>
                </el-table-column>
                <el-table-column label="操作类型" width="120">
                  <template slot-scope="scope">
                    <el-select v-model="scope.row.type" placeholder="选择类型" style="width:100px">
                      <el-option label="播种" value="播种"></el-option>
                      <el-option label="施肥" value="施肥"></el-option>
                      <el-option label="浇水" value="浇水"></el-option>
                      <el-option label="除草" value="除草"></el-option>
                      <el-option label="打药" value="打药"></el-option>
                      <el-option label="采收" value="采收"></el-option>
                      <el-option label="其他" value="其他"></el-option>
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="操作内容">
                  <template slot-scope="scope">
                    <el-input v-model="scope.row.content" placeholder="请输入操作内容"></el-input>
                  </template>
                </el-table-column>
                <el-table-column label="操作人员" width="120">
                  <template slot-scope="scope">
                    <el-input v-model="scope.row.operator" placeholder="操作人员"></el-input>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="80" fixed="right">
                  <template slot-scope="scope">
                    <el-button type="danger" size="mini" @click="removeFarmOp(scope.$index)">移除</el-button>
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
  name: 'Planting',
  components: { Navigator, Header },
  data() {
    return {
      submitLoading: false,
      form: {
        productId: '',
        productName: '',
        baseName: '',
        baseAddress: '',
        areaSize: 0,
        cropVariety: '',
        sowDate: '',
        expectedYield: 0,
        farmOps: []
      },
      products: []
    };
  },
  methods: {
    onProductChange(val) {
      var selected = this.products.find(function(p) { return p.id === val; });
      this.form.productName = selected ? selected.productName : '';
    },
    fetchProducts(query) {
      this.axios.get('/api/products/list', {
        params: { keyword: query, pageSize: 20 },
        headers: { Authorization: 'Bearer ' + this.$cookies.get('token') }
      }).then((response) => {
        if (response.data.code == 200) {
          this.products = response.data.data.list || [];
        }
      });
    },
    addFarmOp() {
      this.form.farmOps.push({ date: '', type: '', content: '', operator: '' });
    },
    removeFarmOp(index) {
      this.form.farmOps.splice(index, 1);
    },
    handleSubmit() {
      if (!this.form.productId) {
        this.$message.warning('请选择产品');
        return;
      }
      if (!this.form.baseName) {
        this.$message.warning('请输入种植基地名称');
        return;
      }
      if (!this.form.baseAddress) {
        this.$message.warning('请输入基地地址');
        return;
      }
      if (!this.form.cropVariety) {
        this.$message.warning('请输入作物品种');
        return;
      }
      if (!this.form.farmOps.length) {
        this.$message.warning('请至少添加一条农事操作记录');
        return;
      }
      this.submitLoading = true;
      this.axios.post('/api/trace/planting', this.form, {
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
        baseName: '',
        baseAddress: '',
        areaSize: 0,
        cropVariety: '',
        sowDate: '',
        expectedYield: 0,
        farmOps: []
      };
    }
  },
  mounted() {
    this.axios.get('/api/products/list', {
      params: { pageSize: 999 },
      headers: { Authorization: 'Bearer ' + this.$cookies.get('token') }
    }).then((response) => {
      if (response.data.code == 200) {
        this.products = response.data.data.list || [];
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
.required-mark { color: #f56c6c; }

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}
.fade-in-up { animation: fadeInUp 0.4s ease-out; }
</style>
