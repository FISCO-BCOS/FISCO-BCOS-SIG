<template>
  <div class="home-page">
    <div class="stats-row">
      <div class="stat-card" v-for="(item, index) in statItems" :key="index" :style="{ animationDelay: index * 0.1 + 's' }">
        <div class="stat-icon" :style="{ background: item.gradient }">
          <i :class="item.icon"></i>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ item.value }}</span>
          <span class="stat-label">{{ item.label }}</span>
        </div>
      </div>
    </div>

    <el-card class="recent-card">
      <div slot="header" class="card-header">
        <span class="card-title"><i class="el-icon-time"></i> 最近存证</span>
        <el-button type="text" class="view-all-btn" @click="$router.push('/evidence/list')">查看全部 →</el-button>
      </div>
      <el-table :data="recentList" border stripe size="medium">
        <el-table-column prop="evidenceNo" label="存证编号" width="180"></el-table-column>
        <el-table-column prop="workTitle" label="作品标题" show-overflow-tooltip></el-table-column>
        <el-table-column prop="workCategory" label="分类" width="90">
          <template slot-scope="scope">
            <el-tag size="small" :type="categoryTagType(scope.row.workCategory)">{{ categoryLabel(scope.row.workCategory) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="170">
          <template slot-scope="scope">{{ fmtTime(scope.row.evidenceTime) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template slot-scope="scope">
            <el-tag size="small" :type="statusTagType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "Home",
  data() {
    return {
      stats: { totalEvidences: 0, monthlyNew: 0, totalVerifications: 0, latestBlockNumber: 0 },
      recentList: []
    };
  },
  computed: {
    statItems() {
      return [
        { icon: "el-icon-document", label: "总存证数", value: this.stats.totalEvidences || 0, gradient: "linear-gradient(135deg, #2b5aed, #4e72f0)" },
        { icon: "el-icon-plus", label: "本月新增", value: this.stats.monthlyNew || 0, gradient: "linear-gradient(135deg, #00c853, #69f0ae)" },
        { icon: "el-icon-view", label: "验证次数", value: this.stats.totalVerifications || 0, gradient: "linear-gradient(135deg, #ff9800, #ffb74d)" },
        { icon: "el-icon-connection", label: "最新区块", value: this.stats.latestBlockNumber || 0, gradient: "linear-gradient(135deg, #f56c6c, #fab6b6)" }
      ];
    }
  },
  methods: {
    loadStats() {
      this.axios.get("/api/statistics/summary").then((response) => {
        if (response.data.code == 200) this.stats = response.data.data;
      });
    },
    loadRecent() {
      this.axios.get("/api/evidence/list", {
        params: { pageNum: 1, pageSize: 5 }
      }).then((response) => {
        if (response.data.code == 200) this.recentList = response.data.data.list;
      });
    },
    fmtTime(t) { if (!t) return "-"; return t.replace("T", " ").substring(0, 19); },
    categoryLabel(c) { const m = { image: "图片", document: "文档", audio: "音频", video: "视频", code: "代码" }; return m[c] || c; },
    categoryTagType(c) { const m = { image: "", document: "success", audio: "warning", video: "danger", code: "info" }; return m[c] || ""; },
    statusLabel(s) { const m = { 0: "待上链", 1: "已上链", 2: "失败" }; return m[s] || "未知"; },
    statusTagType(s) { const m = { 0: "warning", 1: "success", 2: "danger" }; return m[s] || ""; }
  },
  mounted() {
    this.loadStats();
    this.loadRecent();
  }
};
</script>

<style scoped>
.home-page { background: var(--bg-body); padding: 24px; }

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}
.stat-card {
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 18px;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
  animation: fadeInUp 0.5s ease both;
  cursor: default;
}
.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}
.stat-icon {
  width: 52px; height: 52px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.stat-icon i { font-size: 24px; color: #fff; }
.stat-info { display: flex; flex-direction: column; }
.stat-value {
  font-size: 28px; font-weight: 800; color: var(--text-primary);
  line-height: 1.2; font-family: 'SF Pro Display', 'Segoe UI', monospace;
}
.stat-label { font-size: 13px; color: var(--text-secondary); margin-top: 4px; }

.recent-card { border-radius: var(--radius-lg); }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { display: flex; align-items: center; gap: 8px; font-weight: 600; font-size: 16px; color: var(--text-primary); }
.card-title i { color: var(--primary); }
.view-all-btn { font-weight: 500; font-size: 14px; }

@media (max-width: 1200px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
}
</style>
