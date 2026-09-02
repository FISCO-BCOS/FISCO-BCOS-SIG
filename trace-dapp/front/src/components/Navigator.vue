<template>
  <div class="nav-container">
    <!-- Logo区域 -->
    <div class="nav-brand">
      <div class="brand-logo">&#x1F33E;</div>
      <div class="brand-name">溯源系统</div>
      <div class="brand-sub">Trace System</div>
    </div>

    <!-- 菜单 -->
    <el-menu
      class="nav-menu"
      :default-active="activeIndex"
      :collapse="false"
      @select="handleSelect"
      :unique-opened="true"
    >
      <el-menu-item index="home">
        <i class="el-icon-s-home"></i>
        <span slot="title">首页仪表盘</span>
      </el-menu-item>

      <el-submenu index="products">
        <template slot="title">
          <i class="el-icon-goods"></i>
          <span>产品管理</span>
        </template>
        <el-menu-item index="product-list">
          <i class="el-icon-document"></i>
          <span slot="title">产品列表</span>
        </el-menu-item>
        <el-menu-item index="create" v-if="role === 1">
          <i class="el-icon-plus"></i>
          <span slot="title">录入产品</span>
        </el-menu-item>
      </el-submenu>

      <el-menu-item index="planting" v-if="role === 1">
        <i class="el-icon-s-promotion"></i>
        <span slot="title">种植记录</span>
      </el-menu-item>

      <el-menu-item index="processing" v-if="role === 2">
        <i class="el-icon-s-operation"></i>
        <span slot="title">加工记录</span>
      </el-menu-item>

      <el-menu-item index="testing" v-if="role === 3">
        <i class="el-icon-success"></i>
        <span slot="title">检测结果</span>
      </el-menu-item>

      <el-menu-item index="logistics" v-if="role === 4">
        <i class="el-icon-truck"></i>
        <span slot="title">物流信息</span>
      </el-menu-item>

      <el-menu-item index="trace">
        <i class="el-icon-search"></i>
        <span slot="title">溯源查询</span>
      </el-menu-item>

      <el-menu-item index="individual">
        <i class="el-icon-user"></i>
        <span slot="title">个人中心</span>
      </el-menu-item>

      <el-menu-item index="chain-approval" v-if="role === 0">
        <i class="el-icon-s-check"></i>
        <span slot="title">上链审批</span>
      </el-menu-item>
    </el-menu>

    <!-- 底部装饰 -->
    <div class="nav-footer">
      <div class="footer-line"></div>
      <div class="footer-text">v1.0.0</div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Navigator",
  data() {
    return {
      role: 0
    };
  },
  computed: {
    activeIndex() {
      const path = this.$route.path;
      if (path.startsWith('/home')) return 'home';
      if (path.startsWith('/products/create') || path.startsWith('/products/add')) return 'create';
      if (path.startsWith('/products/list') || (path.startsWith('/products') && !path.startsWith('/products/create'))) return 'product-list';
      if (path.startsWith('/trace/planting')) return 'planting';
      if (path.startsWith('/trace/processing')) return 'processing';
      if (path.startsWith('/trace/testing')) return 'testing';
      if (path.startsWith('/trace/logistics')) return 'logistics';
      if (path.startsWith('/trace/query') || path.startsWith('/trace/')) return 'trace';
      if (path.startsWith('/individual')) return 'individual';
      if (path.startsWith('/chain/approval')) return 'chain-approval';
      return 'home';
    }
  },
  methods: {
    handleSelect(key) {
      const routes = {
        home: '/home',
        products: '/products/list',
        'product-list': '/products/list',
        create: '/products/create',
        planting: '/trace/planting',
        processing: '/trace/processing',
        testing: '/trace/testing',
        logistics: '/trace/logistics',
        trace: '/trace/query',
        individual: '/individual',
        'chain-approval': '/chain/approval'
      };
      if (routes[key]) this.$router.push(routes[key]);
    }
  },
  mounted() {
    const info = this.$cookies.get('userInfo');
    if (info && typeof info === 'object') {
      this.role = info.role || 0;
    }
  }
};
</script>

<style scoped>
.nav-container {
  height: 100%;
  background: linear-gradient(180deg, #ffffff 0%, #f8f9fc 100%);
  display: flex;
  flex-direction: column;
  position: relative;
}
.nav-brand {
  padding: 20px 16px 16px;
  text-align: center;
  border-bottom: 1px solid #ebeef5;
  background: linear-gradient(135deg, #409EFF 0%, #3a8ee6 100%);
  color: #fff;
}
.brand-logo { font-size: 32px; margin-bottom: 4px; }
.brand-name { font-size: 15px; font-weight: 700; letter-spacing: 2px; }
.brand-sub { font-size: 10px; opacity: 0.7; margin-top: 2px; letter-spacing: 1px; text-transform: uppercase; }

.nav-menu {
  border-right: none !important;
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
  background: transparent;
}
.nav-menu .el-menu-item {
  height: 46px;
  line-height: 46px;
  font-size: 13px;
  margin: 2px 8px;
  border-radius: 8px;
  transition: all 0.25s ease;
  color: #606266;
}
.nav-menu .el-menu-item:hover {
  background: #ecf5ff !important;
  color: #409EFF;
}
.nav-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, #409EFF, #3a8ee6) !important;
  color: #fff !important;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}
.nav-menu .el-menu-item i { width: 18px; font-size: 16px; margin-right: 8px; }
.nav-menu .el-menu-item.is-active i { color: #fff !important; }

.nav-menu .el-submenu .el-submenu__title {
  height: 44px;
  line-height: 44px;
  font-size: 13px;
  margin: 2px 8px;
  border-radius: 8px;
  color: #606266;
  transition: all 0.25s ease;
}
.nav-menu .el-submenu .el-submenu__title:hover {
  background: #ecf5ff !important;
  color: #409EFF;
}
.nav-menu .el-submenu .el-submenu__title i { width: 18px; font-size: 16px; margin-right: 8px; }
.nav-menu .el-submenu .el-menu-item {
  height: 40px;
  line-height: 40px;
  font-size: 12px;
  min-width: auto;
  margin: 1px 8px 1px 28px;
  padding: 0 12px !important;
  border-radius: 6px;
}

.nav-footer {
  padding: 12px 0;
  text-align: center;
}
.footer-line {
  width: 40px;
  height: 2px;
  background: #e4e7ed;
  border-radius: 1px;
  margin: 0 auto 8px;
}
.footer-text {
  font-size: 11px;
  color: #c0c4cc;
}
</style>
