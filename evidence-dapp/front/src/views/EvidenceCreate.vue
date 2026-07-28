<template>
  <div class="evidence-create-page">
    <el-card class="create-card">
      <div slot="header" class="card-header">
        <span class="card-title"><i class="el-icon-edit-outline"></i> 新建存证</span>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" size="medium" label-position="top">
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="作品标题" prop="workTitle">
              <el-input v-model="form.workTitle" placeholder="请输入作品标题" maxlength="200" show-word-limit></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作品分类" prop="workCategory">
              <el-select v-model="form.workCategory" placeholder="请选择分类" style="width:100%">
                <el-option label="🖼 图片" value="image"></el-option>
                <el-option label="📄 文档" value="document"></el-option>
                <el-option label="🎵 音频" value="audio"></el-option>
                <el-option label="🎬 视频" value="video"></el-option>
                <el-option label="💻 代码" value="code"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="作品描述" prop="workDesc">
          <el-input v-model="form.workDesc" type="textarea" :rows="3" placeholder="请输入作品描述（选填）"></el-input>
        </el-form-item>
        <el-form-item label="上传文件">
          <div class="upload-area">
            <el-upload action="" :auto-upload="false" :file-list="fileList" :on-change="handleFileChange" :on-remove="handleFileRemove" :limit="5" :on-exceed="handleExceed" drag multiple>
              <div class="upload-drag-content">
                <i class="el-icon-upload2 upload-icon"></i>
                <p class="upload-text">将文件拖到此处，或<em>点击上传</em></p>
                <p class="upload-tip">最多5个文件，单个文件不超过50MB</p>
              </div>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label=" " class="btn-group">
          <el-button type="primary" size="large" icon="el-icon-connection" @click="submit(true)" :loading="loading" class="submit-btn chain-btn">提交并上链</el-button>
          <el-button size="large" icon="el-icon-folder-opened" @click="submit(false)" :loading="loading" class="submit-btn save-btn">仅保存不上链</el-button>
          <el-button size="large" icon="el-icon-refresh" @click="resetForm" class="reset-btn">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "EvidenceCreate",
  data() {
    return {
      form: { workTitle: "", workCategory: "", workDesc: "" },
      fileList: [],
      rules: {
        workTitle: [{ required: true, message: "请输入作品标题", trigger: "blur" }],
        workCategory: [{ required: true, message: "请选择分类", trigger: "change" }]
      },
      loading: false
    };
  },
  methods: {
    handleFileChange(file, fileList) { this.fileList = fileList; },
    handleFileRemove(file, fileList) { this.fileList = fileList; },
    handleExceed() { this.$message.warning("最多上传5个文件"); },
    submit(uploadToChain) {
      this.$refs.formRef.validate((valid) => {
        if (!valid) return;
        if (this.fileList.length === 0) { this.$message.warning("请至少上传一个文件"); return; }
        let msg = uploadToChain ? "确认提交并上链？数据将写入区块链，不可篡改。" : "确认仅保存？数据仅存储在本地数据库，可稍后手动上链。";
        this.$confirm(msg, "确认", { confirmButtonText: "确定", cancelButtonText: "取消", type: uploadToChain ? "warning" : "info" }).then(() => {
          this.loading = true;
          let formData = new FormData();
          this.fileList.forEach((f) => { formData.append("files", f.raw); });
          formData.append("workTitle", this.form.workTitle);
          formData.append("workCategory", this.form.workCategory);
          formData.append("workDesc", this.form.workDesc || "");
          formData.append("uploadToChain", uploadToChain ? "true" : "false");
          this.axios.post("/api/evidence/save", formData, {
            headers: { "Content-Type": "multipart/form-data" }
          }).then((response) => {
            if (response.data.code == 200 || response.data.code == 201) {
              if (uploadToChain && response.data.data && response.data.data.status !== 1) {
                this.$message.error("存证已保存，但上链失败，可在存证列表中稍后重试");
              } else {
                this.$message.success(uploadToChain ? "存证已上链成功" : "存证已保存，可在个人中心进行上链操作");
              }
              this.$router.push("/evidence/list");
            } else { this.$message.error(response.data.message || "存证创建失败"); }
          }).catch(() => { this.$message.error("网络错误"); }).finally(() => { this.loading = false; });
        }).catch(() => {});
      });
    },
    resetForm() { this.$refs.formRef.resetFields(); this.fileList = []; }
  }
};
</script>

<style scoped>
.evidence-create-page { background: var(--bg-body); padding: 24px; }
.create-card { border-radius: var(--radius-lg); }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { display: flex; align-items: center; gap: 8px; font-weight: 700; font-size: 18px; color: var(--text-primary); }
.card-title i { color: var(--primary); font-size: 20px; }

.upload-area { width: 100%; }
.upload-drag-content { padding: 30px 0; }
.upload-icon { font-size: 48px; color: var(--primary); margin-bottom: 12px; opacity: 0.7; }
.upload-text { color: var(--text-regular); font-size: 15px; margin-bottom: 4px; }
.upload-text em { color: var(--primary); font-style: normal; font-weight: 600; }
.upload-tip { color: var(--text-secondary); font-size: 13px; }

.btn-group { display: flex; gap: 14px; margin-top: 10px; }
.submit-btn { min-width: 150px; border-radius: var(--radius-md); font-weight: 600; letter-spacing: 1px; transition: all 0.25s ease; }
.chain-btn { background: linear-gradient(135deg, #2b5aed, #4e72f0) !important; border: none !important; box-shadow: 0 4px 14px rgba(43, 90, 237, 0.3); }
.chain-btn:hover { transform: translateY(-1px); box-shadow: 0 6px 18px rgba(43, 90, 237, 0.4) !important; }
.save-btn { border-color: var(--accent-green) !important; color: var(--accent-green) !important; background: transparent !important; }
.save-btn:hover { background: rgba(0, 200, 83, 0.08) !important; }
.reset-btn { border-radius: var(--radius-md); }

@media (max-width: 768px) {
  .btn-group { flex-direction: column; }
  .submit-btn { width: 100%; }
}
</style>
