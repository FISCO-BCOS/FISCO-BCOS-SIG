<template>
  <div class="login-page">
    <!-- 左侧品牌区域 -->
    <div class="login-brand">
      <div class="brand-content">
        <div class="brand-icon">&#x1F33E;</div>
        <h1>农产品溯源系统</h1>
        <p>Agricultural Product Traceability System</p>
        <div class="brand-features">
          <div class="feature-item"><i class="el-icon-s-check"></i><span>区块链存证</span></div>
          <div class="feature-item"><i class="el-icon-lock"></i><span>数据不可篡改</span></div>
          <div class="feature-item"><i class="el-icon-view"></i><span>全链路可追溯</span></div>
        </div>
      </div>
      <div class="brand-bg-pattern"></div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-form-wrapper">
      <div class="login-form-card">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>请登录您的账户以继续使用系统</p>
        </div>

        <el-form :model="form" ref="loginForm" @submit.native.prevent>
          <el-form-item>
            <el-input v-model="form.username" prefix-icon="el-icon-user" placeholder="请输入用户名" size="large"></el-input>
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" size="large" @keyup.enter.native="submit"></el-input>
          </el-form-item>
          <el-form-item style="margin-bottom: 8px;">
            <el-checkbox v-model="form.remember">记住我</el-checkbox>
          </el-form-item>
          <el-button type="primary" size="large" style="width: 100%; height: 44px; font-size: 15px;" @click="submit" :loading="loading">登 录</el-button>
        </el-form>

        <div class="form-footer">
          <span class="footer-text">还没有账号？</span>
          <router-link to="/register" class="register-link">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Login",
  data() {
    return {
      form: { username: "", password: "", remember: false },
      loading: false
    };
  },
  methods: {
    submit() {
      if (!this.form.username) { this.$message.warning('请输入用户名'); return; }
      if (!this.form.password) { this.$message.warning('请输入密码'); return; }
      this.loading = true;
      this.axios.post('/api/auth/login', {
        username: this.form.username,
        password: this.form.password
      }).then((response) => {
        if (response.data.code == 200) {
          this.$message.success('登录成功，正在跳转...');
          setTimeout(() => {
            if (this.form.remember) {
              this.$cookies.set("userInfo", JSON.stringify(response.data.data.user), '1d');
              this.$cookies.set("token", response.data.data.token, '1d');
              this.$cookies.set("refreshToken", response.data.data.refreshToken, '7d');
            } else {
              this.$cookies.set("userInfo", JSON.stringify(response.data.data.user));
              this.$cookies.set("token", response.data.data.token);
            }
            this.$router.push('/home');
          }, 500);
        } else {
          this.$message.error(response.data.msg || '登录失败,未知原因');
        }
      }).catch(() => {
        this.$message.error('网络异常，请检查连接');
      }).finally(() => {
        this.loading = false;
      });
    }
  },
  mounted() {
    if (this.$cookies.get('token')) {
      this.$router.push('/home');
    }
  }
};
</script>

<style scoped>
.login-page {
  display: flex;
  height: 100vh;
  width: 100%;
}
.login-brand {
  flex: 0 0 45%;
  background: linear-gradient(135deg, #2d6cb5 0%, #409EFF 40%, #53a3f5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
.brand-content {
  text-align: center;
  color: #fff;
  z-index: 2;
  position: relative;
}
.brand-content .brand-icon { font-size: 64px; margin-bottom: 16px; }
.brand-content h1 { font-size: 28px; font-weight: 700; letter-spacing: 3px; margin: 0 0 10px; }
.brand-content p { font-size: 13px; opacity: 0.75; margin: 0 0 36px; letter-spacing: 1px; }
.brand-features { display: flex; gap: 24px; justify-content: center; }
.feature-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  opacity: 0.85;
  padding: 8px 14px;
  background: rgba(255,255,255,0.12);
  border-radius: 20px;
  backdrop-filter: blur(4px);
}
.feature-item i { font-size: 15px; }
.brand-bg-pattern {
  position: absolute;
  top: -50%; right: -30%;
  width: 500px; height: 500px;
  border-radius: 50%;
  background: rgba(255,255,255,0.06);
}

.login-form-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  padding: 40px;
}
.login-form-card {
  width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 40px 36px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}
.form-header { text-align: center; margin-bottom: 28px; }
.form-header h2 { font-size: 22px; color: #303133; margin: 0 0 8px; font-weight: 600; }
.form-header p { font-size: 13px; color: #909399; margin: 0; }
.form-footer {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}
.footer-text { font-size: 13px; color: #909399; }
.register-link {
  color: #409EFF;
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  margin-left: 4px;
}
.register-link:hover { color: #3a8ee6; text-decoration: underline; }
</style>
