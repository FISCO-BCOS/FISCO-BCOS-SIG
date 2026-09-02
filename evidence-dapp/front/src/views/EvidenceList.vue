<template>
  <div class="list-page">
    <el-card class="list-card">
      <div slot="header" class="card-header">
        <span class="card-title"><i class="el-icon-document"></i> 存证列表</span>
      </div>
      <div class="search-bar">
        <div class="search-row">
          <el-input v-model="search.keyword" placeholder="搜索标题/编号" clearable prefix-icon="el-icon-search" size="medium" class="search-input"></el-input>
          <el-select v-model="search.category" placeholder="分类" clearable size="medium" class="search-select">
            <el-option label="图片" value="image"></el-option>
            <el-option label="文档" value="document"></el-option>
            <el-option label="音频" value="audio"></el-option>
            <el-option label="视频" value="video"></el-option>
            <el-option label="代码" value="code"></el-option>
          </el-select>
          <el-select v-model="search.status" placeholder="状态" clearable size="medium" class="search-select">
            <el-option label="待上链" :value="0"></el-option>
            <el-option label="已上链" :value="1"></el-option>
            <el-option label="失败" :value="2"></el-option>
          </el-select>
          <el-button type="primary" size="medium" icon="el-icon-search" @click="query">搜索</el-button>
          <el-button size="medium" icon="el-icon-refresh" @click="resetSearch">重置</el-button>
        </div>
      </div>
      <el-table :data="tableData" border stripe class="data-table">
        <el-table-column prop="evidenceNo" label="存证编号" min-width="220"></el-table-column>
        <el-table-column prop="workTitle" label="作品标题" show-overflow-tooltip></el-table-column>
        <el-table-column prop="workCategory" label="分类" width="90">
          <template slot-scope="scope">
            <el-tag size="small" :type="categoryTagType(scope.row.workCategory)">{{ categoryLabel(scope.row.workCategory) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="存证时间" width="170">
          <template slot-scope="scope">{{ fmtTime(scope.row.evidenceTime) }}</template>
        </el-table-column>
        <el-table-column prop="verifyCount" label="验证次数" width="90" align="center"></el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag size="small" :type="statusTagType(scope.row.status)" effect="dark">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template slot-scope="scope">
            <el-button type="primary" plain size="mini" round @click="viewDetail(scope.row.id)" class="detail-btn">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="list-pagination" layout="total, prev, pager, next, jumper"
        @current-change="handlePageChange"
        :current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        background>
      </el-pagination>
    </el-card>

    <el-dialog title="存证详情" :visible.sync="detailVisible" width="70%" custom-class="detail-dialog" :modal-append-to-body="true">
      <div class="detail-content" v-if="detail">
        <div class="section-block">
          <h4 class="section-title"><i class="el-icon-info"></i> 基本信息</h4>
          <el-descriptions :column="2" border size="medium">
            <el-descriptions-item label="存证编号"><code>{{ detail.evidenceNo }}</code></el-descriptions-item>
            <el-descriptions-item label="作品标题">{{ detail.workTitle }}</el-descriptions-item>
            <el-descriptions-item label="作品分类">{{ categoryLabel(detail.workCategory) }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="statusTagType(detail.status)" effect="dark" size="small">{{ statusLabel(detail.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Hash" :span="2"><code class="hash-code">{{ detail.workHash }}</code></el-descriptions-item>
            <el-descriptions-item label="交易Hash" :span="2"><code>{{ detail.txHash || '待上链' }}</code></el-descriptions-item>
            <el-descriptions-item label="区块高度">{{ detail.blockNumber || '-' }}</el-descriptions-item>
            <el-descriptions-item label="验证次数">{{ detail.verifyCount }}</el-descriptions-item>
            <el-descriptions-item label="存证时间">{{ fmtTime(detail.evidenceTime) }}</el-descriptions-item>
            <el-descriptions-item label="存证人">{{ detail.user ? detail.user.username : '-' }}</el-descriptions-item>
            <el-descriptions-item label="作品描述" :span="2">{{ detail.workDesc || '无' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section-block mt-20">
          <h4 class="section-title"><i class="el-icon-folder-opened"></i> 文件列表</h4>
          <el-table :data="detail.files" border size="small" stripe>
            <el-table-column prop="fileName" label="文件名"></el-table-column>
            <el-table-column prop="fileSize" label="大小" width="100">
              <template slot-scope="scope">{{ formatSize(scope.row.fileSize) }}</template>
            </el-table-column>
            <el-table-column prop="fileHash" label="Hash" width="160">
              <template slot-scope="scope">
                <code class="hash-mini">{{ scope.row.fileHash ? scope.row.fileHash.substring(0, 16) + '...' : '' }}</code>
              </template>
            </el-table-column>
            <el-table-column label="上传时间" width="170">
              <template slot-scope="scope">{{ fmtTime(scope.row.uploadTime) }}</template>
            </el-table-column>
          </el-table>
        </div>

        <div class="section-block mt-20">
          <h4 class="section-title"><i class="el-icon-circle-check"></i> 验证记录</h4>
          <el-table v-if="detail.verifyRecords && detail.verifyRecords.length > 0" :data="detail.verifyRecords" border size="small" stripe>
            <el-table-column label="#" width="50" align="center"><template slot-scope="scope">{{ scope.$index + 1 }}</template></el-table-column>
            <el-table-column prop="verifierName" label="验证用户" width="120"></el-table-column>
            <el-table-column label="验证结果" min-width="420">
              <template slot-scope="scope">
                <el-tag :type="scope.row.isExist ? 'success' : 'danger'" size="small">{{ scope.row.matchResult }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="验证时间" width="170">
              <template slot-scope="scope">{{ formatVerifyTime(scope.row.verifyTime) }}</template>
            </el-table-column>
          </el-table>
          <div v-else class="empty-tip">暂无验证记录</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "EvidenceList",
  data() {
    return {
      search: { keyword: "", category: "", status: null },
      tableData: [], pageNum: 1, pageSize: 10, total: 0,
      detailVisible: false, detail: null
    };
  },
  methods: {
    query() {
      this.axios.get("/api/evidence/list", {
        params: { pageNum: this.pageNum, pageSize: this.pageSize, keyword: this.search.keyword, category: this.search.category, status: this.search.status }
      }).then((response) => {
        if (response.data.code == 200) { this.tableData = response.data.data.list; this.total = response.data.data.total; }
      });
    },
    resetSearch() { this.search = { keyword: "", category: "", status: null }; this.pageNum = 1; this.query(); },
    handlePageChange(val) { this.pageNum = val; this.query(); },
    viewDetail(id) {
      this.axios.get("/api/evidence/" + id).then((response) => {
        if (response.data.code == 200) { this.detail = response.data.data; this.detailVisible = true; }
        else this.$message.error("获取详情失败");
      });
    },
    categoryLabel(c) { const m = { image: "图片", document: "文档", audio: "音频", video: "视频", code: "代码" }; return m[c] || c; },
    categoryTagType(c) { const m = { image: "", document: "success", audio: "warning", video: "danger", code: "info" }; return m[c] || ""; },
    statusLabel(s) { const m = { 0: "待上链", 1: "已上链", 2: "失败" }; return m[s] || "未知"; },
    statusTagType(s) { const m = { 0: "warning", 1: "success", 2: "danger" }; return m[s] || ""; },
    formatSize(bytes) { if (!bytes) return "0B"; let k = 1024; let sizes = ["B", "KB", "MB", "GB"]; let i = Math.floor(Math.log(bytes) / Math.log(k)); return (bytes / Math.pow(k, i)).toFixed(2) + sizes[i]; },
    formatVerifyTime(t) { if (!t) return "-"; return t.replace("T", " "); },
    fmtTime(t) { if (!t) return "-"; return t.replace("T", " ").substring(0, 19); }
  },
  mounted() { this.query(); }
};
</script>

<style scoped>
.list-page { background: var(--bg-body); padding: 24px; }
.list-card { border-radius: var(--radius-lg); }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { display: flex; align-items: center; gap: 8px; font-weight: 700; font-size: 18px; color: var(--text-primary); }
.card-title i { color: var(--primary); font-size: 20px; }

.search-bar {
  background: #fafbfc;
  padding: 16px 20px;
  border-radius: var(--radius-md);
  margin-bottom: 18px;
}
.search-row {
  display: flex; align-items: center; gap: 12px; flex-wrap: wrap;
}
.search-input { flex: 1 1 200px; min-width: 0; }
.search-select { width: 120px; flex-shrink: 0; }
.data-table { border-radius: var(--radius-sm); overflow: hidden; }
.list-pagination { margin-top: 18px; padding-bottom: 4px; }
.detail-content { max-height: 60vh; overflow-y: auto; overflow-x: hidden; }

.detail-btn { transition: all 0.2s ease !important; }
.detail-btn:hover {
  background-color: var(--primary) !important;
  color: #fff !important;
  border-color: var(--primary) !important;
}

.section-block { background: #fafbfc; padding: 16px 20px; border-radius: var(--radius-md); }
.section-title {
  display: flex; align-items: center; gap: 8px;
  font-size: 15px; font-weight: 600; color: var(--text-primary);
  margin-bottom: 14px; padding-bottom: 10px;
  border-bottom: 1px solid var(--border-color);
}
.section-title i { color: var(--primary); }
.hash-code { font-family: monospace; font-size: 13px; word-break: break-all; color: var(--text-regular); background: #f5f5f5; padding: 2px 6px; border-radius: 3px; }
.hash-mini { font-family: monospace; font-size: 12px; color: var(--text-secondary); }
.empty-tip { text-align: center; color: var(--text-secondary); padding: 16px; font-size: 14px; }
</style>
