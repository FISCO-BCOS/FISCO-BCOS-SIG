<template>
  <router-view />
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
/* ===== 全局CSS变量 ===== */
:root {
  --primary: #409EFF;
  --primary-light: #66b1ff;
  --primary-dark: #3a8ee6;
  --success: #67C23A;
  --warning: #E6A23C;
  --danger: #F56C6C;
  --info: #909399;
  --bg-page: #f0f2f5;
  --bg-card: #ffffff;
  --text-primary: #303133;
  --text-regular: #606266;
  --text-secondary: #909399;
  --border-color: #e4e7ed;
  --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.06);
  --shadow-md: 0 4px 16px rgba(0, 0, 0, 0.08);
  --shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.12);
  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 16px;
  --header-height: 56px;
  --sidebar-width: 220px;
}

/* ===== 全局重置 ===== */
* { box-sizing: border-box; }
html, body { margin: 0; padding: 0; height: 100%; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif; color: var(--text-primary); background: var(--bg-page); -webkit-font-smoothing: antialiased; }
#app { height: 100%; }

/* ===== 全局布局覆盖 —— 固定顶栏+侧栏 ===== */
.el-container { height: 100%; }

/* 顶层容器：给固定header留出空间 */
.el-container > .el-header {
  position: fixed !important;
  top: 0; left: 0; right: 0;
  z-index: 100;
  padding: 0 20px !important;
  height: var(--header-height) !important;
  line-height: var(--header-height) !important;
  background: linear-gradient(135deg, #409EFF 0%, #3a8ee6 50%, #2d6cb5 100%) !important;
  color: #fff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.3);
}
.el-container > .el-header ~ .el-container,
.el-container > .el-container {
  margin-top: var(--header-height);
  height: calc(100vh - var(--header-height)) !important;
}

/* 侧边栏：sticky固定 */
.el-aside {
  background: #fff !important;
  border-right: 1px solid var(--border-color) !important;
  width: var(--sidebar-width) !important;
  position: sticky;
  top: 0;
  height: calc(100vh - var(--header-height));
  overflow-y: auto;
  overflow-x: hidden;
  flex-shrink: 0;
}

/* 主内容区：独立滚动 */
.el-main {
  background: var(--bg-page) !important;
  padding: 20px !important;
  overflow-y: auto;
  height: 100%;
}

/* ===== 页面标题统一风格 ===== */
.el-main > h2, .el-main > h3 { margin: 0 0 4px 0; font-size: 20px; font-weight: 600; color: var(--text-primary); letter-spacing: 0.5px; }
.el-main > .el-divider { margin: 12px 0 20px !important; }

/* ===== 表格美化 ===== */
.el-table { border-radius: var(--radius-sm); overflow: hidden; box-shadow: var(--shadow-sm); }
.el-table th { background-color: #fafbfc !important; color: var(--text-primary) !important; font-weight: 600 !important; }
.el-table--striped .el-table__body tr.el-table__row--striped td { background-color: #f8f9fb !important; }

/* ===== 卡片美化 ===== */
.el-card { border-radius: var(--radius-md) !important; border: none !important; box-shadow: var(--shadow-sm) !important; transition: transform 0.2s, box-shadow 0.2s; }
.el-card:hover { box-shadow: var(--shadow-md) !important; }

/* ===== 按钮增强 ===== */
.el-button--primary { background: linear-gradient(135deg, var(--primary), var(--primary-dark)) !important; border: none !important; }
.el-button--success { background: linear-gradient(135deg, #67C23A, #4db030) !important; border: none !important; }
.el-button--warning { background: linear-gradient(135deg, #E6A23C, #cf9236) !important; border: none !important; }
.el-button--danger { background: linear-gradient(135deg, #F56C6C, #de5555) !important; border: none !important; }

/* ===== 分页美化 ===== */
.el-pagination.is-background .el-pager li:not(.disabled).active { background: linear-gradient(135deg, var(--primary), var(--primary-dark)) !important; }

/* ===== 对话框美化 ===== */
.el-dialog { border-radius: var(--radius-lg) !important; overflow: hidden; }
.el-dialog__header { background: linear-gradient(135deg, #f8f9fc, #eef1f6); padding: 16px 20px !important; }
.el-dialog__title { font-weight: 600; font-size: 16px; color: var(--text-primary); }
.el-dialog__headerbtn .el-dialog__close { color: var(--text-secondary); }
.el-dialog__body { padding: 20px !important; }

/* ===== 标签页美化 ===== */
.el-tabs--border-card { border-radius: var(--radius-md) !important; border: none !important; box-shadow: var(--shadow-sm) !important; }
.el-tabs--border-card > .el-tabs__header { background: #fafbfc; border-bottom: 1px solid var(--border-color); }

/* ===== 表单美化 ===== */
.el-form-item__label { font-weight: 500; color: var(--text-regular); }
.el-input__inner, .el-textarea__inner { border-radius: var(--radius-sm) !important; transition: border-color 0.25s, box-shadow 0.25s; }
.el-input__inner:focus, .el-textarea__inner:focus { box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.15) !important; }

/* ===== 消息提示美化 ===== */
.el-message { border-radius: var(--radius-md) !important; box-shadow: var(--shadow-md) !important; }

/* ===== 滚动条美化 ===== */
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: #c0c4cc; border-radius: 3px; }
::-webkit-scrollbar-thumb:hover { background: #909399; }

/* ===== 动画 ===== */
@keyframes fadeInUp { from { opacity: 0; transform: translateY(16px); } to { opacity: 1; transform: translateY(0); } }
.fade-in-up { animation: fadeInUp 0.4s ease-out both; }
@keyframes slideInLeft { from { opacity: 0; transform: translateX(-16px); } to { opacity: 1; transform: translateX(0); } }
.slide-in-left { animation: slideInLeft 0.35s ease-out both; }

/* ===== 过渡 ===== */
.page-enter-active, .page-leave-active { transition: opacity 0.25s ease; }
.page-enter, .page-leave-to { opacity: 0; }
</style>
