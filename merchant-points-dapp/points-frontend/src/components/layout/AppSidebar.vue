<template>
  <el-menu
    :default-active="route.path"
    class="sidebar-menu"
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#409eff"
    :collapse="false"
    :router="true"
    @select="handleSelect"
  >
    <el-menu-item :index="'/dashboard'">
      <el-icon><Odometer /></el-icon>
      <span>仪表盘</span>
    </el-menu-item>
    <el-menu-item :index="'/merchants'">
      <el-icon><Shop /></el-icon>
      <span>商户管理</span>
    </el-menu-item>
    <el-sub-menu :index="'/points'">
      <template #title>
        <el-icon><Coin /></el-icon>
        <span>积分管理</span>
      </template>
      <el-menu-item :index="'/points/issue'">积分发行</el-menu-item>
      <el-menu-item :index="'/points/transfer'">积分转账</el-menu-item>
      <el-menu-item :index="'/points/consume'">积分消费</el-menu-item>
    </el-sub-menu>
    <el-menu-item :index="'/transactions'">
      <el-icon><List /></el-icon>
      <span>交易流水</span>
    </el-menu-item>
    <el-menu-item :index="'/profile'">
      <el-icon><User /></el-icon>
      <span>个人中心</span>
    </el-menu-item>
  </el-menu>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const handleSelect = (index: string) => {
  if (!index || !index.startsWith('/')) {
    return
  }
  if (route.path !== index) {
    router.push(index)
  }
}
</script>

<style scoped>
.sidebar-menu {
  height: 100%;
  border-right: none;
}
</style>