<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-brand">
        <div class="brand-content">
          <div class="brand-icon-wrap">
            <i class="el-icon-connection"></i>
          </div>
          <h1 class="brand-title">数字存证 DApp</h1>
          <p class="brand-desc">基于 FISCO-BCOS 区块链的数字存证平台<br>安全 · 不可篡改 · 可追溯</p>
          <div class="brand-features">
            <div class="feature-item"><i class="el-icon-lock"></i><span>链上存证</span></div>
            <div class="feature-item"><i class="el-icon-view"></i><span>实时验证</span></div>
            <div class="feature-item"><i class="el-icon-document-checked"></i><span>法律效力</span></div>
          </div>
        </div>
        <div class="decoration-circles">
          <div class="circle c1"></div>
          <div class="circle c2"></div>
          <div class="circle c3"></div>
        </div>
      </div>
      <div class="login-form-area">
        <div class="form-card">
          <h2 class="form-title">欢迎登录</h2>
          <p class="form-subtitle">请输入您的账号信息</p>
          <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-position="top">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="loginForm.username" placeholder="请输入用户名/手机号/邮箱" prefix-icon="el-icon-user" size="large"></el-input>
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="el-icon-lock" size="large" @keyup.enter.native="submit" show-password></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" class="login-btn" @click="submit" :loading="loading">
                {{ loading ? '登录中...' : '登 录' }}
              </el-button>
            </el-form-item>
          </el-form>
          <div class="form-footer">
            <span class="footer-text">还没有账号？</span>
            <router-link to="/register" class="footer-link">立即注册 →</router-link>
          </div>
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
      loginForm: { username: "", password: "" },
      loginRules: {
        username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
        password: [{ required: true, message: "请输入密码", trigger: "blur" }]
      },
      loading: false
    };
  },
  methods: {
    submit() {
      this.$refs.loginFormRef.validate((valid) => {
        if (!valid) return;
        this.loading = true;
        this.axios.post("/api/auth/login", this.loginForm).then((response) => {
          if (response.data.code == 200) {
            let data = response.data.data;
            this.$accessToken.set(data.token);
            this.$cookies.set("userId", data.user.id, "7d");
            this.$cookies.set("username", data.user.username, "7d");
            this.$cookies.set("userType", data.user.userType, "7d");
            this.$cookies.set("realName", data.user.realName || "", "7d");
            this.$message.success("登录成功");
            this.$router.push("/home");
          } else {
            this.$message.error(response.data.message || "登录失败");
          }
        }).catch(() => {
          this.$message.error("网络错误");
        }).finally(() => {
          this.loading = false;
        });
      });
    }
  },
  mounted() {
    if (this.$cookies.get("userId")) {
      this.$router.push("/home");
    }
  }
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: var(--bg-body);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeInUp 0.6s ease;
}
.login-container {
  display: flex;
  width: 900px;
  min-height: 540px;
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
}
.login-brand {
  width: 380px;
  background: linear-gradient(135deg, var(--dark) 0%, #1e2a5e 40%, var(--primary-dark) 100%);
  padding: 48px 36px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
.brand-content { position: relative; z-index: 2; }
.brand-icon-wrap {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--primary), var(--accent-cyan));
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(43, 90, 237, 0.35);
}
.brand-icon-wrap i { font-size: 32px; color: #fff; }
.brand-title { color: #fff; font-size: 28px; font-weight: 800; margin-bottom: 12px; letter-spacing: 1px; }
.brand-desc { color: rgba(255,255,255,0.65); font-size: 14px; line-height: 1.8; margin-bottom: 32px; }
.brand-features { display: flex; gap: 16px; }
.feature-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: rgba(255,255,255,0.7);
  font-size: 13px;
  padding: 8px 12px;
  background: rgba(255,255,255,0.08);
  border-radius: var(--radius-sm);
}
.feature-item i { font-size: 14px; color: var(--accent-cyan); }
.decoration-circles .circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.06;
  background: #fff;
}
.decoration-circles .c1 { width: 300px; height: 300px; right: -80px; top: -60px; }
.decoration-circles .c2 { width: 180px; height: 180px; left: -40px; bottom: -30px; }
.decoration-circles .c3 { width: 100px; height: 100px; right: 20px; bottom: 60px; }

.login-form-area {
  flex: 1;
  background: #fff;
  padding: 48px 44px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.form-card { width: 100%; max-width: 360px; }
.form-title { font-size: 24px; font-weight: 700; color: var(--text-primary); margin-bottom: 4px; }
.form-subtitle { color: var(--text-secondary); font-size: 14px; margin-bottom: 28px; }
.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-md);
  letter-spacing: 2px;
}
.form-footer {
  text-align: center;
  margin-top: 20px;
}
.footer-text { color: var(--text-secondary); font-size: 14px; }
.footer-link {
  color: var(--primary);
  font-weight: 600;
  font-size: 14px;
  margin-left: 4px;
}
.footer-link:hover { color: var(--primary-dark); }

@media (max-width: 768px) {
  .login-brand { display: none; }
  .login-container { width: 100%; min-height: auto; border-radius: 0; }
  .login-form-area { padding: 32px 24px; }
}
</style>
