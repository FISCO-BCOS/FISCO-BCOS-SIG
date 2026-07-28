<template>
  <div id="app">
    <el-container>
      <el-header><Header /></el-header>
      <el-container>
        <el-aside width="220px"><navigator></navigator></el-aside>
        <el-main class="page-main">
          <div class="page-title-bar">
            <h2><i class="el-icon-edit-outline"></i> {{ isEdit ? '编辑产品' : '产品录入' }}</h2>
            <span class="title-desc">{{ isEdit ? '修改已有产品的基本信息' : '填写产品基本信息，录入后自动生成溯源二维码' }}</span>
          </div>

          <div class="form-card fade-in-up">
            <el-form ref="form" :model="form" :rules="rules" label-width="110px" label-position="top" size="medium">
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="产品名称" prop="productName">
                    <el-input v-model="form.productName" placeholder="请输入产品名称" prefix-icon="el-icon-goods"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="产品分类" prop="category">
                    <el-select v-model="form.category" placeholder="请选择产品分类" style="width:100%">
                      <el-option label="粮食" value="粮食"></el-option>
                      <el-option label="蔬菜" value="蔬菜"></el-option>
                      <el-option label="水果" value="水果"></el-option>
                      <el-option label="肉禽" value="肉禽"></el-option>
                      <el-option label="水产" value="水产"></el-option>
                      <el-option label="其他" value="其他"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="产品描述">
                <el-input v-model="form.description" type="textarea" :rows="3" maxlength="1000" show-word-limit placeholder="请输入产品描述（选填）"></el-input>
              </el-form-item>

              <el-form-item label="产品图片">
                <el-upload action="/api/file/upload" list-type="picture-card" :limit="6" accept="image/*" :headers="uploadHeaders" :on-success="handleUploadSuccess" :on-remove="handleUploadRemove">
                  <i class="el-icon-plus"></i>
                </el-upload>
              </el-form-item>

              <el-row :gutter="24">
                <el-col :span="8">
                  <el-form-item label="价格" prop="price">
                    <el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%"></el-input-number>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="单位" prop="unit">
                    <el-input v-model="form.unit" placeholder="如：斤、箱、袋" prefix-icon="el-icon-collection-tag"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="产地地址" prop="originAddress">
                    <el-input v-model="form.originAddress" placeholder="请输入产地地址" prefix-icon="el-icon-location"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="关联种植户">
                    <el-select v-model="form.farmerId" filterable clearable placeholder="请选择种植户（选填）" style="width:100%">
                      <el-option v-for="item in farmerOptions" :key="item.id" :label="item.name" :value="item.id"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="关联加工商">
                    <el-select v-model="form.processorId" filterable clearable placeholder="请选择加工商（选填）" style="width:100%">
                      <el-option v-for="item in processorOptions" :key="item.id" :label="item.name" :value="item.id"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-divider></el-divider>

              <el-form-item>
                <el-button type="primary" @click="handleSubmit" :loading="submitLoading" size="large" style="min-width:120px;">
                  <i class="el-icon-check"></i> {{ isEdit ? '保存修改' : '提交录入' }}
                </el-button>
                <el-button @click="resetForm" size="large">重置表单</el-button>
                <el-button @click="$router.back()" size="large" plain>取消返回</el-button>
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
  name: 'ProductsCreate',
  components: { Navigator, Header },
  data() {
    return {
      submitLoading: false,
      form: { productName: '', category: '', description: '', images: [], price: 0, unit: '', originAddress: '', farmerId: null, processorId: null },
      rules: {
        productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
        category: [{ required: true, message: '请选择产品分类', trigger: 'change' }],
        price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
        unit: [{ required: true, message: '请输入单位', trigger: 'blur' }],
        originAddress: [{ required: true, message: '请输入产地地址', trigger: 'blur' }]
      },
      farmerOptions: [], processorOptions: []
    };
  },
  computed: {
    uploadHeaders() { return { Authorization: 'Bearer ' + this.$cookies.get('token') }; },
    isEdit() { return !!this.$route.query.id; },
    editId() { return this.$route.query.id; }
  },
  methods: {
    handleSubmit() {
      this.$refs.form.validate((valid) => {
        if (!valid) return;
        this.$confirm(this.isEdit ? '确认保存修改？' : '确认录入？将自动生成唯一溯源二维码', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }).then(() => {
          this.submitLoading = true;
          var request = this.isEdit
            ? this.axios.put('/api/products/' + this.editId, this.form, { headers: { Authorization: 'Bearer ' + this.$cookies.get('token') } })
            : this.axios.post('/api/products', this.form, { headers: { Authorization: 'Bearer ' + this.$cookies.get('token') } });
          request.then((response) => {
            this.submitLoading = false;
            if (response.data.code == 200) {
              this.$message.success(this.isEdit ? '产品修改成功' : '产品录入成功');
              var productId = this.isEdit ? this.editId : (response.data.data.productId || response.data.data.id);
              this.$router.push('/products/detail/' + productId);
            } else { this.$message.error(response.data.msg || (this.isEdit ? '修改失败' : '录入失败')); }
          }).catch(() => { this.submitLoading = false; });
        }).catch(() => {});
      });
    },
    resetForm() { this.$refs.form.resetFields(); this.form.images = []; },
    handleUploadSuccess(response, file) { if (response.code == 200) this.form.images.push(response.data.url || response.data); },
    handleUploadRemove(file, fileList) { this.form.images = fileList.map(f => f.response ? (f.response.data.url || f.response.data) : f.url); }
  },
  mounted() {
    if (this.isEdit) {
      this.axios.get('/api/products/' + this.editId, { headers: { Authorization: 'Bearer ' + this.$cookies.get('token') } }).then((response) => {
        if (response.data.code == 200) { var data = response.data.data || {}; var p = data.product || {}; this.form.productName = p.productName || ''; this.form.category = p.category || ''; this.form.price = p.price || 0; this.form.unit = p.unit || ''; this.form.originAddress = p.originAddress || ''; this.form.images = p.images || []; }
      }).catch(() => {});
    }
    this.axios.get('/api/admin/farmers/list', { headers: { Authorization: 'Bearer ' + this.$cookies.get('token') } }).then((response) => { if (response.data.code == 200) this.farmerOptions = response.data.data || []; }).catch(() => {});
    this.axios.get('/api/admin/processors/list', { headers: { Authorization: 'Bearer ' + this.$cookies.get('token') } }).then((response) => { if (response.data.code == 200) this.processorOptions = response.data.data || []; }).catch(() => {});
  }
};
</script>

<style scoped>
.page-main { padding: 20px !important; }
.page-title-bar { margin-bottom: 16px; }
.page-title-bar h2 { font-size: 20px; font-weight: 600; color: var(--text-primary); margin: 0 0 4px; display: flex; align-items: center; gap: 8px; }
.page-title-bar h2 i { color: var(--primary); }
.title-desc { font-size: 13px; color: var(--text-secondary); }
.form-card {
  background: #fff; border-radius: var(--radius-md); padding: 28px 32px;
  box-shadow: var(--shadow-sm); max-width: 900px;
}
</style>
