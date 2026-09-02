<template>
  <el-container class="layout-container">
    <el-header class="layout-header">
      <AppHeader />
    </el-header>
    <el-container>
      <el-aside :width="isCollapse ? '64px' : '200px'" class="layout-aside">
        <AppSidebar />
      </el-aside>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AppHeader from './AppHeader.vue'
import AppSidebar from './AppSidebar.vue'

const isCollapse = ref(false)
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.layout-header {
  background: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  padding: 0 20px;
}
.layout-aside {
  background: #304156;
  transition: width 0.3s;
  overflow: hidden;
}
.layout-main {
  background: #f0f2f5;
  padding: 20px;
}
</style>