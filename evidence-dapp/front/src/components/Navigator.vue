<template>
  <div class="app-aside">
    <div class="aside-top">
      <el-menu :default-active="activeIndex" class="aside-menu" @select="handleSelect">
        <el-menu-item index="/home"><i class="el-icon-s-home"></i><span>首页概览</span></el-menu-item>
        <el-menu-item index="/evidence/create"><i class="el-icon-edit-outline"></i><span>新建存证</span></el-menu-item>
        <el-menu-item index="/evidence/list"><i class="el-icon-document"></i><span>存证列表</span></el-menu-item>
        <el-menu-item index="/verify"><i class="el-icon-circle-check"></i><span>存证验证</span></el-menu-item>
        <el-menu-item index="/individual"><i class="el-icon-user"></i><span>个人中心</span></el-menu-item>
      </el-menu>
    </div>
    <div class="aside-bottom">
      <div class="user-card">
        <div class="user-avatar">{{ avatarLetter }}</div>
        <div class="user-detail">
          <el-tooltip :content="username" placement="top" :disabled="username.length <= 8">
            <span class="user-name">{{ username }}</span>
          </el-tooltip>
          <span class="user-role">{{ userTypeLabel }}</span>
        </div>
        <el-dropdown trigger="click" @command="handleCommand" class="user-actions">
          <i class="el-icon-more-outline more-btn"></i>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="profile"><i class="el-icon-user"></i> 个人中心</el-dropdown-item>
            <el-dropdown-item command="logout" divided><i class="el-icon-switch-button"></i> 退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Navigator',
  computed: {
    activeIndex() {
      const path = this.$route.path
      if (path === '/home') return '/home'
      if (path.startsWith('/evidence/create')) return '/evidence/create'
      if (path.startsWith('/evidence/list')) return '/evidence/list'
      if (path === '/verify') return '/verify'
      if (path === '/individual') return '/individual'
      return ''
    },
    username() { return this.$cookies.get('username') || '用户' },
    userType() { return this.$cookies.get('userType') || '0' },
    userTypeLabel() {
      const m = { '0': '个人用户', '1': '企业用户', '2': '机构用户' }
      return m[this.userType] || '个人用户'
    },
    avatarLetter() {
      const name = this.username
      return name ? name.charAt(0).toUpperCase() : '?'
    }
  },
  methods: {
    handleSelect(index) {
      if (this.$route.path !== index) this.$router.push(index)
    },
    handleCommand(cmd) {
      if (cmd === 'logout') this.logout()
      else if (cmd === 'profile') this.$router.push('/individual')
    },
    logout() {
      this.$confirm('确认退出登录？', '提示', {
        confirmButtonText: '确定退出',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$accessToken.set(null)
        this.$cookies.remove('token')
        this.$cookies.remove('userId')
        this.$cookies.remove('username')
        this.$cookies.remove('userType')
        this.$cookies.remove('realName')
        this.$message.success('已退出登录')
        this.$router.push('/login')
      }).catch(() => {})
    }
  },
  mounted() {
    if (!this.$cookies.get('userId')) this.$router.push('/login')
  }
}
</script>

<style scoped>
.app-aside {
  height: 100%;
  background: #fff;
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
}
.aside-top { flex: 1; overflow-y: auto; display: flex; flex-direction: column; }
.aside-bottom { flex-shrink: 0; }

.aside-menu { border-right: none !important; padding: 12px 0; }
.aside-menu .el-menu-item {
  height: 46px; line-height: 46px; margin: 2px 10px;
  border-radius: var(--radius-sm); color: var(--text-regular);
  font-size: 14px; font-weight: 500; transition: all 0.2s ease;
}
.aside-menu .el-menu-item i { font-size: 16px; margin-right: 10px; color: var(--text-secondary); transition: color 0.2s; width: 17px; text-align: center; }
.aside-menu .el-menu-item:hover { background-color: var(--primary-bg) !important; color: var(--primary); }
.aside-menu .el-menu-item:hover i { color: var(--primary); }
.aside-menu .el-menu-item.is-active {
  background-color: var(--primary-bg) !important; color: var(--primary) !important; font-weight: 600;
  position: relative;
}
.aside-menu .el-menu-item.is-active::before {
  content: ''; position: absolute; left: 0; top: 50%; transform: translateY(-50%);
  width: 3px; height: 20px; background: var(--primary); border-radius: 0 3px 3px 0;
  transition: opacity 0.2s ease, height 0.15s ease;
}
.aside-menu .el-menu-item.is-active i { color: var(--primary); }

.user-card {
  display: flex; align-items: center; gap: 10px;
  padding: 14px 16px;
  margin: 12px 10px;
  background: var(--bg-body);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  transition: box-shadow 0.2s;
}
.user-card:hover { box-shadow: var(--shadow-sm); }
.user-avatar {
  width: 36px; height: 36px; border-radius: 50%;
  background: linear-gradient(135deg, var(--primary), var(--accent-cyan));
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 15px; font-weight: 700; flex-shrink: 0;
}
.user-detail { display: flex; flex-direction: column; min-width: 0; flex: 1; }
.user-name { font-size: 13px; font-weight: 600; color: var(--text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; cursor: default; }
.user-role { font-size: 11px; color: var(--text-secondary); margin-top: 2px; }
.more-btn {
  font-size: 18px; color: var(--text-secondary); cursor: pointer;
  padding: 4px; border-radius: 50%; transition: all 0.2s;
  flex-shrink: 0;
}
.more-btn:hover { color: var(--primary); background: var(--primary-bg); }
</style>
