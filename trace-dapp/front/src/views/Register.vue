<template>
  <div class="register-page">
    <!-- 左侧品牌区域 -->
    <div class="reg-brand">
      <div class="brand-content">
        <div class="brand-icon">&#x1F33E;</div>
        <h1>加入溯源平台</h1>
        <p>注册成为供应链中的一员，共建可信农业生态</p>
        <div class="brand-steps">
          <div class="step"><span class="step-num">1</span><span>填写信息</span></div>
          <div class="step-line"></div>
          <div class="step"><span class="step-num">2</span><span>选择角色</span></div>
          <div class="step-line"></div>
          <div class="step"><span class="step-num">3</span><span>开始使用</span></div>
        </div>
      </div>
    </div>

    <!-- 右侧注册表单 -->
    <div class="reg-form-wrapper">
      <div class="reg-form-card">
        <div class="form-header">
          <h2>创建新账户</h2>
          <p>请完善以下信息完成注册</p>
        </div>

        <el-form :model="form" label-position="top" size="medium">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="用户名">
                <el-input v-model="form.username" placeholder="3-20个字符" prefix-icon="el-icon-user"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="真实姓名">
                <el-input v-model="form.realName" placeholder="请输入真实姓名" prefix-icon="el-icon-postcard"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="密码">
                <el-input v-model="form.password" type="password" show-password placeholder="6-50个字符" prefix-icon="el-icon-lock"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="确认密码">
                <el-input v-model="form.confirmPassword" type="password" show-password placeholder="再次输入密码" prefix-icon="el-icon-lock"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="手机号">
                <el-input v-model="form.phone" placeholder="请输入手机号" prefix-icon="el-icon-mobile-phone"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="所属组织">
                <el-input v-model="form.organization" placeholder="公司/合作社名称" prefix-icon="el-icon-office-building"></el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="选择角色">
            <div class="role-cards">
              <div
                class="role-card"
                :class="{ active: form.role === 1 }"
                @click="form.role = 1"
              >
                <i class="el-icon-s-promotion"></i>
                <span>农户</span>
              </div>
              <div
                class="role-card"
                :class="{ active: form.role === 2 }"
                @click="form.role = 2"
              >
                <i class="el-icon-s-operation"></i>
                <span>加工商</span>
              </div>
              <div
                class="role-card"
                :class="{ active: form.role === 3 }"
                @click="form.role = 3"
              >
                <i class="el-icon-success"></i>
                <span>检测机构</span>
              </div>
              <div
                class="role-card"
                :class="{ active: form.role === 4 }"
                @click="form.role = 4"
              >
                <i class="el-icon-truck"></i>
                <span>物流商</span>
              </div>
            </div>
          </el-form-item>

          <el-form-item style="margin-top: 8px;">
            <el-button type="primary" size="large" style="width: 100%; height: 44px; font-size: 15px;" @click="submit" :loading="loading">注 册</el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <span class="footer-text">已有账号？</span>
          <router-link to="/login" class="login-link">返回登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Register",
  data() {
    return {
      form: { username: "", password: "", confirmPassword: "", realName: "", phone: "", role: 1, organization: "" },
      loading: false
    };
  },
  methods: {
    submit() {
      if (this.form.username.length < 3 || this.form.username.length > 20) {
        this.$message.warning("用户名长度应在3-20个字符之间"); return;
      }
      if (this.form.password.length < 6 || this.form.password.length > 50) {
        this.$message.warning("密码长度应在6-50个字符之间"); return;
      }
      if (this.form.password != this.form.confirmPassword) {
        this.$message.warning("两次输入的密码不一致"); return;
      }
      if (this.form.realName == "") { this.$message.warning("真实姓名不能为空"); return; }
      if (!/^1[3-9]\d{9}$/.test(this.form.phone)) { this.$message.warning("手机号格式不正确"); return; }
      this.loading = true;
      this.axios.post('/api/auth/register', {
        username: this.form.username,
        password: this.form.password,
        confirmPassword: this.form.confirmPassword,
        realName: this.form.realName,
        phone: this.form.phone,
        role: this.form.role,
        organization: this.form.organization
      }).then((response) => {
        if (response.data.code == 200) {
          this.$message.success(response.data.msg || '注册成功');
          setTimeout(() => { this.$router.push('/login'); }, 1500);
        } else {
          this.$message.error(response.data.msg || '注册失败');
        }
      }).finally(() => { this.loading = false; });
    }
  }
};
</script>

<style scoped>
.register-page {
  display: flex;
  height: 100vh;
  width: 100%;
}
.reg-brand {
  flex: 0 0 42%;
  background: linear-gradient(135deg, #2d6cb5 0%, #409EFF 40%, #53a3f5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
.brand-content { text-align: center; color: #fff; z-index: 2; position: relative; }
.brand-content .brand-icon { font-size: 56px; margin-bottom: 16px; }
.brand-content h1 { font-size: 26px; font-weight: 700; letter-spacing: 2px; margin: 0 0 10px; }
.brand-content p { font-size: 13px; opacity: 0.75; margin: 0 0 36px; }
.brand-steps { display: flex; align-items: center; justify-content: center; gap: 0; }
.step { display: flex; align-items: center; gap: 6px; font-size: 13px; opacity: 0.85; }
.step-num {
  width: 24px; height: 24px; line-height: 24px; text-align: center;
  background: rgba(255,255,255,0.25); border-radius: 50%; font-size: 12px; font-weight: 600;
}
.step-line { width: 40px; height: 2px; background: rgba(255,255,255,0.2); }

.reg-form-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  padding: 40px;
  overflow-y: auto;
}
.reg-form-card {
  width: 520px;
  background: #fff;
  border-radius: 16px;
  padding: 36px 36px 28px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}
.form-header { text-align: center; margin-bottom: 24px; }
.form-header h2 { font-size: 22px; color: #303133; margin: 0 0 8px; font-weight: 600; }
.form-header p { font-size: 13px; color: #909399; margin: 0; }

.role-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; }
.role-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px 8px;
  border: 2px solid #e4e7ed;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
  background: #fafbfc;
}
.role-card:hover { border-color: #c0e0ff; background: #ecf5ff; }
.role-card.active {
  border-color: #409EFF;
  background: linear-gradient(135deg, #ecf5ff, #d9ecff);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}
.role-card i { font-size: 22px; color: #606266; transition: color 0.25s; }
.role-card.active i { color: #409EFF; }
.role-card span { font-size: 12px; color: #606266; font-weight: 500; }
.role-card.active span { color: #409EFF; font-weight: 600; }

.form-footer {
  text-align: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
.footer-text { font-size: 13px; color: #909399; }
.login-link { color: #409EFF; font-size: 13px; font-weight: 500; text-decoration: none; margin-left: 4px; }
.login-link:hover { color: #3a8ee6; text-decoration: underline; }
</style>
