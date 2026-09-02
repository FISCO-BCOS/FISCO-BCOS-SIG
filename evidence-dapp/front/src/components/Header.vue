<template>
  <div class="app-header">
    <div class="header-left">
      <i class="el-icon-connection header-logo"></i>
      <span class="header-title">数字存证 DApp</span>
    </div>
    <div class="header-right">
      <el-breadcrumb separator="/" class="header-breadcrumb">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="parentRoute">{{ parentRoute }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Header',
  computed: {
    currentTitle() {
      const map = { '/home': '概览', '/evidence/create': '新建存证', '/evidence/list': '存证列表', '/verify': '存证验证', '/individual': '个人中心' }
      return map[this.$route.path] || ''
    },
    parentRoute() {
      const p = this.$route.path
      if (p.startsWith('/evidence')) return '存证管理'
      return ''
    }
  }
}
</script>

<style scoped>
.app-header {
  height: var(--header-height);
  background: #fff;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: relative;
  z-index: 100;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.header-logo { font-size: 22px; color: var(--primary); }
.header-title { font-size: 17px; font-weight: 700; color: var(--text-primary); letter-spacing: 0.5px; }
.header-right .header-breadcrumb { font-size: 13px; }
.header-right /deep/ .el-breadcrumb__inner.is-link { color: var(--text-secondary); font-weight: normal; }
.header-right /deep/ .el-breadcrumb__inner.is-link:hover { color: var(--primary); }
.header-right /deep/ .el-breadcrumb__item:last-child .el-breadcrumb__inner { color: var(--text-primary); font-weight: 600; }
</style>
