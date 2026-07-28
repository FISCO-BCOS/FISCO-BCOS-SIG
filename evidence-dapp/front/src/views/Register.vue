<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-brand">
        <div class="brand-content">
          <div class="brand-icon-wrap">
            <i class="el-icon-connection"></i>
          </div>
          <h1 class="brand-title">加入我们</h1>
          <p class="brand-desc">创建账号，开启您的区块链存证之旅<br>安全可信 · 简单便捷</p>
          <div class="brand-features">
            <div class="feature-item"><i class="el-icon-user"></i><span>多类型支持</span></div>
            <div class="feature-item"><i class="el-icon-s-check"></i><span>实名认证</span></div>
            <div class="feature-item"><i class="el-icon-shield"></i><span>数据安全</span></div>
          </div>
        </div>
        <div class="decoration-circles">
          <div class="circle c1"></div>
          <div class="circle c2"></div>
          <div class="circle c3"></div>
        </div>
      </div>
      <div class="register-form-area">
        <div class="form-card">
          <h2 class="form-title">创建账号</h2>
          <p class="form-subtitle">填写以下信息完成注册</p>
          <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-position="top" size="normal">
            <div class="section-label"><i class="el-icon-user"></i> 账号信息</div>
            <el-form-item label="用户名" prop="username">
              <el-input v-model="registerForm.username" placeholder="3-50位字符" prefix-icon="el-icon-user"></el-input>
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="registerForm.password" type="password" placeholder="含字母和数字，6-20位" prefix-icon="el-icon-lock" show-password></el-input>
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="registerForm.confirmPassword" type="password" placeholder="再次输入密码" prefix-icon="el-icon-lock" show-password></el-input>
            </el-form-item>

            <el-divider></el-divider>

            <div class="section-label"><i class="el-icon-postcard"></i> 个人信息（选填）</div>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="registerForm.realName" placeholder="您的姓名"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="registerForm.phone" placeholder="11位手机号" prefix-icon="el-icon-mobile-phone"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="registerForm.email" placeholder="example@mail.com" prefix-icon="el-icon-message"></el-input>
            </el-form-item>
            <el-form-item label="用户类型" prop="userType">
              <el-radio-group v-model="registerForm.userType" class="type-radio-group">
                <el-radio-button :label="0"><i class="el-icon-user"></i> 个人</el-radio-button>
                <el-radio-button :label="1"><i class="el-icon-office-building"></i> 企业</el-radio-button>
                <el-radio-button :label="2"><i class="el-icon-school"></i> 机构</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" class="submit-btn" @click="submit" :loading="loading">
                {{ loading ? '注册中...' : '注 册' }}
              </el-button>
            </el-form-item>
          </el-form>
          <div class="form-footer">
            <span class="footer-text">已有账号？</span>
            <router-link to="/login" class="footer-link">← 返回登录</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Register",
  data() {
    let validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.registerForm.password) {
        callback(new Error("两次输入密码不一致"));
      } else {
        callback();
      }
    };
    return {
      registerForm: { username: "", password: "", confirmPassword: "", realName: "", phone: "", email: "", userType: 0 },
      registerRules: {
        username: [
          { required: true, message: "请输入用户名", trigger: "blur" },
          { min: 3, max: 50, message: "用户名长度3-50位", trigger: "blur" }
        ],
        password: [
          { required: true, message: "请输入密码", trigger: "blur" },
          { pattern: /^(?=.*[A-Za-z])(?=.*\d).{6,20}$/, message: "密码需含字母和数字，6-20位", trigger: "blur" }
        ],
        confirmPassword: [
          { required: true, message: "请确认密码", trigger: "blur" },
          { validator: validateConfirmPassword, trigger: "blur" }
        ],
        phone: [{ pattern: /^1[3-9]\d{9}$/, message: "手机号格式不正确", trigger: "blur" }],
        email: [{ type: "email", message: "邮箱格式不正确", trigger: "blur" }],
        userType: [{ required: true, message: "请选择用户类型", trigger: "change" }]
      },
      loading: false
    };
  },
  methods: {
    submit() {
      this.$refs.registerFormRef.validate((valid) => {
        if (!valid) return;
        this.loading = true;
        this.axios.post("/api/auth/register", this.registerForm).then((response) => {
          if (response.data.code == 200 || response.data.code == 201) {
            this.$message.success("注册成功，请登录");
            this.$router.push("/login");
          } else {
            this.$message.error(response.data.message || "注册失败");
          }
        }).catch(() => {
          this.$message.error("网络错误");
        }).finally(() => {
          this.loading = false;
        });
      });
    }
  }
};
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: var(--bg-body);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeInUp 0.6s ease;
}
.register-container {
  display: flex;
  width: 960px;
  min-height: 600px;
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
}
.register-brand {
  width: 360px;
  background: linear-gradient(135deg, #1a1f36 0%, #252d5a 40%, #1e3a8a 100%);
  padding: 48px 32px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
.brand-content { position: relative; z-index: 2; }
.brand-icon-wrap {
  width: 56px; height: 56px; border-radius: 14px;
  background: linear-gradient(135deg, var(--accent-cyan), var(--primary));
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 20px;
  box-shadow: 0 4px 16px rgba(0, 212, 255, 0.3);
}
.brand-icon-wrap i { font-size: 28px; color: #fff; }
.brand-title { color: #fff; font-size: 26px; font-weight: 800; margin-bottom: 10px; letter-spacing: 1px; }
.brand-desc { color: rgba(255,255,255,0.6); font-size: 13px; line-height: 1.8; margin-bottom: 28px; }
.brand-features { display: flex; flex-direction: column; gap: 10px; }
.feature-item {
  display: flex; align-items: center; gap: 8px;
  color: rgba(255,255,255,0.65); font-size: 13px;
  padding: 8px 12px; background: rgba(255,255,255,0.06);
  border-radius: var(--radius-sm);
}
.feature-item i { font-size: 14px; color: var(--accent-cyan); }
.decoration-circles .circle { position: absolute; border-radius: 50%; opacity: 0.05; background: #fff; }
.decoration-circles .c1 { width: 280px; height: 280px; right: -100px; top: -80px; }
.decoration-circles .c2 { width: 160px; height: 160px; left: -50px; bottom: -40px; }
.decoration-circles .c3 { width: 80px; height: 80px; right: 30px; bottom: 80px; }

.register-form-area {
  flex: 1; background: #fff; padding: 36px 40px;
  display: flex; align-items: center; justify-content: center;
  overflow-y: auto;
}
.form-card { width: 100%; max-width: 480px; }
.form-title { font-size: 24px; font-weight: 700; color: var(--text-primary); margin-bottom: 4px; }
.form-subtitle { color: var(--text-secondary); font-size: 14px; margin-bottom: 22px; }
.section-label {
  display: flex; align-items: center; gap: 6px;
  font-size: 13px; font-weight: 600; color: var(--primary);
  margin-bottom: 12px; padding-bottom: 8px;
  border-bottom: 2px solid var(--primary-bg);
}
.section-label i { font-size: 15px; }
.type-radio-group { display: flex; gap: 0; width: 100%; }
.type-radio-group .el-radio-button { flex: 1; }
.type-radio-group .el-radio-button__inner { width: 100%; text-align: center; }

.submit-btn {
  width: 100%; height: 44px; font-size: 16px;
  font-weight: 600; border-radius: var(--radius-md); letter-spacing: 4px;
}
.form-footer { text-align: center; margin-top: 18px; }
.footer-text { color: var(--text-secondary); font-size: 14px; }
.footer-link { color: var(--primary); font-weight: 600; font-size: 14px; margin-left: 4px; }
.footer-link:hover { color: var(--primary-dark); }

@media (max-width: 768px) {
  .register-brand { display: none; }
  .register-container { width: 100%; min-height: auto; border-radius: 0; }
  .register-form-area { padding: 28px 20px; }
}
</style>
