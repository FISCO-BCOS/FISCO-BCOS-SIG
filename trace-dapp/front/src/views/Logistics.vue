<template>
  <div id="app">
    <el-container>
      <el-header class="header"><Header /></el-header>
      <el-container>
        <el-aside width="220px"><navigator></navigator></el-aside>
        <el-main class="page-main">
          <div class="page-title-bar">
            <h2><i class="el-icon-truck"></i> 物流信息录入</h2>
            <span class="title-desc">录入产品的物流轨迹，包括运输方式、节点记录等</span>
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
              <el-form-item label="运输方式" required>
                <el-select v-model="form.transportMode" placeholder="请选择运输方式" style="width:200px">
                  <el-option label="公路" value="公路"></el-option>
                  <el-option label="铁路" value="铁路"></el-option>
                  <el-option label="航空" value="航空"></el-option>
                  <el-option label="水路" value="水路"></el-option>
                  <el-option label="冷链" value="冷链"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="车辆/设备信息">
                <el-input v-model="form.vehicleInfo" placeholder="请输入车辆或设备信息"></el-input>
              </el-form-item>
              <el-divider></el-divider>
              <h3 style="display:inline-block">物流节点记录</h3>
              <el-button type="primary" @click="addNode" style="margin-left:16px;margin-bottom:12px">添加</el-button>
              <el-table :data="form.logisticsNodes" border style="width:100%;margin-bottom:16px">
                <el-table-column label="节点位置" width="180">
                  <template slot-scope="scope">
                    <el-input v-model="scope.row.location" placeholder="请输入位置信息"></el-input>
                  </template>
                </el-table-column>
                <el-table-column label="到达时间" width="200">
                  <template slot-scope="scope">
                    <el-date-picker v-model="scope.row.arriveTime" type="datetime" value-format="yyyy-MM-dd HH:mm" placeholder="选择到达时间" style="width:180px"></el-date-picker>
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="140">
                  <template slot-scope="scope">
                    <el-select v-model="scope.row.status" placeholder="选择状态" style="width:120px">
                      <el-option label="待发货" value="待发货"></el-option>
                      <el-option label="已发出" value="已发出"></el-option>
                      <el-option label="运输中" value="运输中"></el-option>
                      <el-option label="已到达" value="已到达"></el-option>
                      <el-option label="已签收" value="已签收"></el-option>
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="80" fixed="right">
                  <template slot-scope="scope">
                    <el-button type="danger" size="mini" @click="removeNode(scope.$index)">移除</el-button>
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
  name: 'Logistics',
  components: { Navigator, Header },
  data() {
    return {
      submitLoading: false,
      form: {
        productId: '',
        productName: '',
        transportMode: '',
        vehicleInfo: '',
        logisticsNodes: [{ location: '', arriveTime: '', status: '' }]
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
    addNode() {
      this.form.logisticsNodes.push({ location: '', arriveTime: '', status: '' });
    },
    removeNode(index) {
      if (this.form.logisticsNodes.length > 1) {
        this.form.logisticsNodes.splice(index, 1);
      }
    },
    handleSubmit() {
      if (!this.form.productId) {
        this.$message.warning('请选择产品');
        return;
      }
      if (!this.form.transportMode) {
        this.$message.warning('请选择运输方式');
        return;
      }
      this.submitLoading = true;
      const submitData = {
        productId: this.form.productId,
        transportMode: this.form.transportMode,
        vehicleInfo: this.form.vehicleInfo,
        logisticsNodes: JSON.stringify(this.form.logisticsNodes)
      };
      this.axios.post('/api/trace/logistics', submitData, {
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
        transportMode: '',
        vehicleInfo: '',
        logisticsNodes: [{ location: '', arriveTime: '', status: '' }]
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
