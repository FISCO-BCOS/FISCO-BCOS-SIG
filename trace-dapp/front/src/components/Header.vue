<template>
  <div class="top-header">
    <div class="header-left">
      <div class="brand">
        <span class="brand-icon">&#x1F33E;</span>
        <span class="brand-text">农产品溯源系统</span>
      </div>
    </div>
    <div class="header-center" v-if="userInfo">
      <div class="user-info">
        <el-avatar :size="32" :icon="'el-icon-user-solid'" class="user-avatar"></el-avatar>
        <span class="user-name">{{ userInfo.realName || userInfo.username || '用户' }}</span>
        <el-tag size="small" :type="roleTagType" effect="dark" class="role-tag">{{ roleText }}</el-tag>
      </div>
    </div>
    <div class="header-right">
      <el-button type="text" class="logout-btn" @click="logout">
        <i class="el-icon-switch-button"></i> 退出登录
      </el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: "Header",
  data() {
    return {
      userInfo: null
    }
  },
  computed: {
    roleText() {
      const map = { 0: '监管者', 1: '农户', 2: '加工商', 3: '检测机构', 4: '物流商' };
      return map[this.userInfo?.role] || '未知';
    },
    roleTagType() {
      const map = { 0: '', 1: 'success', 2: 'warning', 3: 'danger', 4: 'info' };
      return map[this.userInfo?.role] || 'info';
    }
  },
  methods: {
    logout() {
      this.$cookies.remove("token");
      this.$cookies.remove("userInfo");
      this.$router.push("/login");
    }
  },
  mounted() {
    const info = this.$cookies.get("userInfo");
    if (info && typeof info === 'object') {
      this.userInfo = info;
    } else {
      this.$message.warning("请先登录");
      this.$router.push("/login");
    }
  }
};
</script>

<style scoped>
.top-header {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-left .brand {
  display: flex;
  align-items: center;
  gap: 8px;
}
.brand-icon {
  font-size: 22px;
}
.brand-text {
  color: #fff;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 1px;
}
.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-avatar {
  background: rgba(255, 255, 255, 0.2) !important;
  border: none !important;
  opacity: 0.9;
}
.user-name {
  color: rgba(255, 255, 255, 0.92);
  font-size: 14px;
  font-weight: 500;
}
.role-tag {
  border-radius: 10px;
  font-size: 11px;
  padding: 0 8px;
  height: 20px;
  line-height: 20px;
  background: rgba(255, 255, 255, 0.18) !important;
  border: 1px solid rgba(255, 255, 255, 0.25) !important;
  color: #fff !important;
}
.header-right .logout-btn {
  color: rgba(255, 255, 255, 0.85) !important;
  font-size: 13px;
  padding: 6px 14px;
  border-radius: 6px;
  transition: all 0.25s;
}
.header-right .logout-btn:hover {
  color: #fff !important;
  background: rgba(255, 255, 255, 0.15);
}
</style>
