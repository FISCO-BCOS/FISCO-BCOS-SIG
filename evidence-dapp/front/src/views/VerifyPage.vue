<template>
  <div class="verify-page">
    <el-card class="verify-card">
      <div slot="header" class="card-header">
        <span class="card-title"><i class="el-icon-circle-check"></i> 存证验证</span>
      </div>
      <el-tabs v-model="activeTab" class="verify-tabs">
        <el-tab-pane label="Hash验证" name="hash">
          <div class="tab-content">
            <div class="hash-input-wrap">
              <el-input v-model="hashInput" placeholder="请输入64位SHA256哈希值" maxlength="64" size="large" class="hash-input"></el-input>
              <el-button type="primary" size="large" icon="el-icon-search" @click="verifyByHash" :loading="loading" class="verify-btn">验 证</el-button>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="文件验证" name="file">
          <div class="tab-content">
            <el-upload action="" :auto-upload="false" :file-list="fileList" :on-change="handleFileChange" :on-remove="handleFileRemove" :limit="5" :on-exceed="handleExceed" drag multiple class="file-upload">
              <div class="upload-drag-content">
                <i class="el-icon-upload2 upload-icon"></i>
                <p class="upload-text">将文件拖到此处，或<em>点击选择</em></p>
                <p class="upload-tip">最多5个文件，将分别计算哈希并逐个验证</p>
              </div>
            </el-upload>
            <el-button type="primary" size="large" icon="el-icon-check" @click="verifyByFile" :loading="loading" class="verify-btn mt-15">验 证</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>

      <transition name="result-fade">
        <div v-if="verifyResult" class="result-area">
        <el-card :class="['result-card', verifyResult.isExist ? 'result-success' : 'result-error']">
          <div class="result-head">
            <div class="result-icon-wrap" :class="verifyResult.isExist ? 'icon-success' : 'icon-error'">
              <i :class="verifyResult.isExist ? 'el-icon-success' : 'el-icon-error'" :style="{ animation: verifyResult.isExist ? 'pulse 0.6s ease' : 'shake 0.5s ease' }"></i>
            </div>
            <h2 class="result-title">{{ verifyResult.matchResult }}</h2>
            <p class="result-time"><i class="el-icon-time"></i> 验证时间：{{ fmtTime(verifyResult.verifyTime) }}</p>
          </div>
          <div v-if="verifyResult.evidenceInfo" class="evidence-info">
            <el-descriptions :column="1" border size="medium">
              <el-descriptions-item label="存证编号">{{ verifyResult.evidenceInfo.evidenceNo }}</el-descriptions-item>
              <el-descriptions-item label="作品标题">{{ verifyResult.evidenceInfo.workTitle }}</el-descriptions-item>
              <el-descriptions-item label="作品分类">{{ verifyResult.evidenceInfo.workCategory }}</el-descriptions-item>
              <el-descriptions-item label="存证时间">{{ fmtTime(verifyResult.evidenceInfo.evidenceTime) }}</el-descriptions-item>
            </el-descriptions>
          </div>
          <div v-if="verifyResult.multiResults" class="multi-results mt-20">
            <el-table :data="verifyResult.multiResults" border stripe size="small">
              <el-table-column prop="fileName" label="文件名"></el-table-column>
              <el-table-column prop="hash" label="Hash" width="320">
                <template slot-scope="scope">
                  <code class="hash-mini">{{ scope.row.hash ? scope.row.hash.substring(0, 20) + '...' : '-' }}</code>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100" align="center">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.success ? 'success' : 'danger'" size="small" effect="dark">{{ scope.row.success ? '已存证' : '未找到' }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </div>
      </transition>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "VerifyPage",
  data() {
    return {
      activeTab: "hash", hashInput: "", fileList: [], loading: false, verifyResult: null
    };
  },
  methods: {
    handleFileChange(file, fileList) { this.fileList = fileList; },
    handleFileRemove(file, fileList) { this.fileList = fileList; },
    handleExceed() { this.$message.warning("最多上传5个文件"); },
    fmtTime(t) { if (!t) return "-"; return t.replace("T", " ").substring(0, 19); },
    verifyByHash() {
      if (!this.hashInput || this.hashInput.length !== 64) { this.$message.warning("请输入64位SHA256哈希值"); return; }
      this.loading = true; this.verifyResult = null;
      this.axios.get("/api/evidence/verify/" + this.hashInput).then((response) => {
        if (response.data.code == 200) this.verifyResult = response.data.data;
        else this.$message.error(response.data.message || "验证失败");
      }).catch(() => { this.$message.error("网络错误"); }).finally(() => { this.loading = false; });
    },
    async verifyByFile() {
      if (this.fileList.length === 0) { this.$message.warning("请选择文件"); return; }
      this.loading = true; this.verifyResult = null;
      let results = [];
      for (let i = 0; i < this.fileList.length; i++) {
        let file = this.fileList[i].raw;
        try {
          let buffer = await file.arrayBuffer();
          let hashBuffer = await crypto.subtle.digest("SHA-256", buffer);
          let hashArray = Array.from(new Uint8Array(hashBuffer));
          let hashHex = hashArray.map(b => b.toString(16).padStart(2, "0")).join("");
          let resp = await this.axios.get("/api/evidence/verify/" + hashHex);
          results.push({ fileName: file.name, hash: hashHex, success: resp.data.code == 200, data: resp.data.code == 200 ? resp.data.data : null });
        } catch (e) { results.push({ fileName: file.name, hash: null, success: false, data: null }); }
      }
      if (results.length === 1) {
        let r = results[0];
        this.verifyResult = r.success ? r.data : { isExist: false, matchResult: "存证不存在", verifyTime: new Date().toLocaleString() };
      } else {
        let allExist = results.every(r => r.success);
        this.verifyResult = { isExist: allExist, matchResult: allExist ? "全部文件已存证" : "部分文件未找到存证记录", verifyTime: new Date().toLocaleString(), multiResults: results };
      }
      this.loading = false;
    }
  },
  mounted() {
    if (this.$route.query.hash) { this.hashInput = this.$route.query.hash; this.activeTab = "hash"; }
  }
};
</script>

<style scoped>
.verify-page { background: var(--bg-body); padding: 24px; }
.verify-card { border-radius: var(--radius-lg); }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { display: flex; align-items: center; gap: 8px; font-weight: 700; font-size: 18px; color: var(--text-primary); }
.card-title i { color: var(--primary); font-size: 20px; }
.tab-content { padding: 16px 0; }

.hash-input-wrap { display: flex; gap: 12px; align-items: stretch; }
.hash-input { flex: 1; font-family: monospace; }
.verify-btn { min-width: 100px; white-space: nowrap; border-radius: var(--radius-md); font-weight: 600; letter-spacing: 2px; }

.upload-drag-content { padding: 28px 0; }
.upload-icon { font-size: 44px; color: var(--primary); margin-bottom: 10px; opacity: 0.7; }
.upload-text { color: var(--text-regular); font-size: 14px; margin-bottom: 4px; }
.upload-text em { color: var(--primary); font-style: normal; font-weight: 600; }
.upload-tip { color: var(--text-secondary); font-size: 13px; }

.result-area { margin-top: 20px; }
.result-fade-enter-active { transition: all 0.4s ease; }
.result-fade-leave-active { transition: all 0.2s ease; }
.result-fade-enter, .result-fade-leave-to { opacity: 0; transform: translateY(12px); }
.result-card { border-left: 4px solid transparent; border-radius: var(--radius-md) !important; overflow: hidden; }
.result-success { border-left-color: var(--accent-green) !important; background: linear-gradient(135deg, #f0fff5, #e8faf5); }
.result-error { border-left-color: var(--accent-red) !important; background: linear-gradient(135deg, #fef5f5, #fff1f0); }

.result-head { text-align: center; padding: 10px 0 20px; }
.result-icon-wrap {
  width: 64px; height: 64px; border-radius: 50%; display: inline-flex;
  align-items: center; justify-content: center; margin-bottom: 12px;
}
.icon-success { background: linear-gradient(135deg, #00c853, #69f0ae); }
.icon-error { background: linear-gradient(135deg, #ff5252, #ff8a80); }
.result-icon-wrap i { font-size: 32px; color: #fff; }

.result-title { font-size: 20px; font-weight: 700; margin-bottom: 6px; }
.result-success .result-title { color: #00a868; }
.result-error .result-title { color: #f56c6c; }

.result-time { color: var(--text-secondary); font-size: 14px; display: flex; align-items: center; justify-content: center; gap: 4px; }

.evidence-info { margin-top: 18px; padding: 16px; background: rgba(255,255,255,0.7); border-radius: var(--radius-md); }
.hash-mini { font-family: monospace; font-size: 12px; color: var(--text-secondary); }
.multi-results { padding: 0 16px 16px; }
</style>
