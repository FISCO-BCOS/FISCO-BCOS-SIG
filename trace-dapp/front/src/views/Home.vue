<template>
  <div id="app">
    <el-container>
      <el-header><Header /></el-header>
      <el-container>
        <el-aside width="220px"><navigator></navigator></el-aside>
        <el-main class="home-main">
          <!-- 欢迎横幅 -->
          <div class="welcome-banner fade-in-up">
            <div class="banner-text">
              <h2>{{ greeting }}，{{ userName }}</h2>
              <p>欢迎使用农产品溯源管理系统，以下是今日数据概览</p>
            </div>
            <div class="banner-actions">
              <el-button v-if="userRole === 1" type="primary" icon="el-icon-plus" @click="$router.push('/products/create')">录入产品</el-button>
              <el-button icon="el-icon-search" @click="$router.push('/trace/query')">溯源查询</el-button>
            </div>
          </div>

          <!-- 统计卡片 -->
          <el-row :gutter="24" class="stat-row fade-in-up" style="animation-delay: 0.1s;">
            <el-col :span="6" v-for="(card, index) in statCards" :key="card.label">
              <div class="stat-card" :style="{ '--accent': card.color }">
                <div class="stat-icon-wrap" :style="{ background: card.bgColor }">
                  <i :class="card.icon"></i>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ card.value }}</div>
                  <div class="stat-label">{{ card.label }}</div>
                </div>
              </div>
            </el-col>
          </el-row>

          <!-- 最近产品表格 -->
          <div class="section-card fade-in-up" style="animation-delay: 0.2s;">
            <div class="section-header">
              <h3><i class="el-icon-goods"></i> 最近产品</h3>
              <el-button type="text" @click="$router.push('/products/list')">查看全部 <i class="el-icon-arrow-right"></i></el-button>
            </div>
            <el-table :data="productList" style="width: 100%" size="medium">
              <el-table-column prop="productName" label="产品名称" min-width="140"></el-table-column>
              <el-table-column prop="category" label="品类" width="100" align="center">
                <template slot-scope="scope">
                  <el-tag size="small" :type="getCategoryTag(scope.row.category)">{{ scope.row.category }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="productNo" label="产品编号" width="160">
                <template slot-scope="scope">
                  <code class="mono-text">{{ scope.row.productNo }}</code>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="170"></el-table-column>
              <el-table-column label="操作" width="80" align="center">
                <template slot-scope="scope">
                  <el-button type="text" @click="$router.push('/products/detail/' + scope.row.id)">查看</el-button>
                </template>
              </el-table-column>
            </el-table>
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
  name: 'Home',
  components: { Navigator, Header },
  data() {
    return {
      userRole: 0,
      userName: '用户',
      statCards: [
        { label: '产品总数', value: 0, icon: 'el-icon-goods', color: '#409EFF', bgColor: '#ecf5ff' },
        { label: '今日新增', value: 0, icon: 'el-icon-plus', color: '#67C23A', bgColor: '#f0f9eb' },
        { label: '待上链记录', value: 0, icon: 'el-icon-upload2', color: '#E6A23C', bgColor: '#fdf6ec' },
        { label: '总扫码量', value: 0, icon: 'el-icon-view', color: '#909399', bgColor: '#f4f4f5' }
      ],
      productList: []
    };
  },
  computed: {
    greeting() {
      const h = new Date().getHours();
      if (h < 6) return '夜深了';
      if (h < 12) return '早上好';
      if (h < 14) return '中午好';
      if (h < 18) return '下午好';
      return '晚上好';
    }
  },
  methods: {
    getCategoryTag(cat) {
      const m = { '粮食': '', '蔬菜': 'success', '水果': 'warning', '肉禽': 'danger', '水产': 'info' };
      return m[cat] || '';
    },
    query() {
      this.axios.get('/api/admin/dashboard', {
        headers: { Authorization: 'Bearer ' + this.$cookies.get('token') }
      }).then((response) => {
        if (response.data.code == 200 && response.data.data) {
          let data = response.data.data;
          this.statCards[0].value = data.totalProducts || 0;
          this.statCards[1].value = data.todayNew || 0;
          this.statCards[2].value = data.pendingRecords || 0;
          this.statCards[3].value = data.totalScans || 0;
        }
      }).catch(() => {});
      this.axios.get('/api/products/list?pageNum=1&pageSize=10', {
        headers: { Authorization: 'Bearer ' + this.$cookies.get('token') }
      }).then((response) => {
        if (response.data.code == 200) {
          const data = response.data.data;
          this.productList = data.list || data.rows || (Array.isArray(data) ? data : []);
        }
      });
    }
  },
  mounted() {
    const info = this.$cookies.get('userInfo');
    if (info && typeof info === 'object') {
      this.userRole = info.role || 0;
      this.userName = info.realName || info.username || '用户';
    }
    this.query();
  }
};
</script>

<style scoped>
.home-main { padding: 20px !important; }

/* 欢迎横幅 */
.welcome-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #409EFF 0%, #3a8ee6 100%);
  border-radius: var(--radius-md);
  padding: 24px 28px;
  color: #fff;
  margin-bottom: 20px;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.25);
}
.banner-text h2 { font-size: 20px; font-weight: 600; margin: 0 0 6px; }
.banner-text p { font-size: 13px; opacity: 0.85; margin: 0; }
.banner-actions .el-button--default { background: rgba(255,255,255,0.95) !important; color: #303133 !important; border: none !important; }
.banner-actions .el-button--default:hover { background: #fff !important; }

/* 统计卡片 */
.stat-row { margin-bottom: 20px; }
.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  border-radius: var(--radius-md);
  padding: 16px 18px;
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: default;
  min-width: 0;
}
.stat-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }
.stat-icon-wrap {
  width: 42px; height: 42px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.stat-icon-wrap i { font-size: 20px; color: var(--accent); }
.stat-info { display: flex; flex-direction: column; min-width: 0; }
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}
.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 2px;
  white-space: nowrap;
}

/* 内容区卡片 */
.section-card {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 20px 24px;
  box-shadow: var(--shadow-sm);
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.section-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 6px;
}
.section-header h3 i { color: var(--primary); }
.mono-text {
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, monospace;
  font-size: 12px;
  color: var(--text-secondary);
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
}
</style>
