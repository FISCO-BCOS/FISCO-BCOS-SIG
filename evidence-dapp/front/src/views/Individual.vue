<template>
  <div class="individual-page">
    <el-card class="profile-card">
      <div slot="header" class="card-header">
        <span class="card-title"><i class="el-icon-user"></i> 个人中心</span>
      </div>
      <el-tabs v-model="activeTab" class="profile-tabs">
        <el-tab-pane name="info">
          <span slot="label"><i class="el-icon-user-solid"></i> 基本信息</span>
          <div class="tab-content">
            <el-form :model="profileForm" label-width="100px" size="medium" label-position="top" class="profile-form">
              <el-form-item label="用户名">
                <el-input :value="profileForm.username" disabled></el-input>
              </el-form-item>
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="真实姓名">
                    <el-input v-model="profileForm.realName" placeholder="请输入姓名"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="手机号">
                    <el-input v-model="profileForm.phone" placeholder="请输入手机号"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="邮箱">
                <el-input v-model="profileForm.email" placeholder="请输入邮箱"></el-input>
              </el-form-item>
              <el-form-item label="联系地址">
                <el-input v-model="profileForm.address" placeholder="请输入地址"></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveProfile" :loading="saving"><i class="el-icon-check"></i> 保存修改</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane name="password">
          <span slot="label"><i class="el-icon-lock"></i> 修改密码</span>
          <div class="tab-content">
            <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px" size="medium" label-position="top" class="profile-form">
              <el-form-item label="旧密码" prop="oldPassword">
                <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" show-password></el-input>
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" placeholder="含字母和数字，6-20位" show-password></el-input>
              </el-form-item>
              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="再次输入新密码" show-password></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="changePassword" :loading="changingPwd"><i class="el-icon-refresh"></i> 确认修改</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane name="evidences">
          <span slot="label"><i class="el-icon-document"></i> 我的存证</span>
          <div class="tab-content">
            <el-table :data="myEvidences" border stripe class="evidence-table">
              <el-table-column prop="evidenceNo" label="存证编号" width="220"></el-table-column>
              <el-table-column prop="workTitle" label="标题" show-overflow-tooltip></el-table-column>
              <el-table-column label="分类" width="90">
                <template slot-scope="scope">
                  <el-tag size="small">{{ categoryLabel(scope.row.workCategory) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="时间" width="170">
                <template slot-scope="scope">{{ fmtTime(scope.row.evidenceTime) }}</template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="90" align="center">
                <template slot-scope="scope">
                  <el-tag size="small" :type="scope.row.status === 1 ? 'success' : 'warning'" effect="dark">
                    {{ scope.row.status === 1 ? '已上链' : '待上链' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="90" align="center">
                <template slot-scope="scope">
                  <el-button v-if="scope.row.status !== 1" type="primary" plain size="mini" round icon="el-icon-connection"
                    @click="doChain(scope.row.id)" :loading="scope.row._chaining">上链</el-button>
                  <span v-else class="chain-done">-</span>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination class="evidence-pagination" layout="total, prev, pager, next"
              @current-change="handleMyPageChange"
              :current-page="myPageNum" :page-size="10" :total="myTotal" background small>
            </el-pagination>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "Individual",
  data() {
    let validateConfirm = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) callback(new Error("两次输入的密码不一致"));
      else callback();
    };
    return {
      activeTab: "info",
      profileForm: { username: "", realName: "", phone: "", email: "", address: "" },
      saving: false,
      passwordForm: { oldPassword: "", newPassword: "", confirmPassword: "" },
      passwordRules: {
        oldPassword: [{ required: true, message: "请输入旧密码", trigger: "blur" }],
        newPassword: [
          { required: true, message: "请输入新密码", trigger: "blur" },
          { pattern: /^(?=.*[A-Za-z])(?=.*\d).{6,20}$/, message: "需含字母和数字，6-20位", trigger: "blur" }
        ],
        confirmPassword: [
          { required: true, message: "请确认新密码", trigger: "blur" },
          { validator: validateConfirm, trigger: "blur" }
        ]
      },
      changingPwd: false,
      myEvidences: [],
      myPageNum: 1,
      myTotal: 0
    };
  },
  methods: {
    categoryLabel(c) { const m = { image: "图片", document: "文档", audio: "音频", video: "视频", code: "代码" }; return m[c] || c; },
    loadProfile() {
      this.axios.get("/api/user/profile").then((response) => {
        if (response.data.code == 200) {
          let d = response.data.data;
          this.profileForm = { username: d.username, realName: d.realName || "", phone: d.phone || "", email: d.email || "", address: d.address || "" };
        }
      });
    },
    saveProfile() {
      this.saving = true;
      this.axios.put("/api/user/profile", this.profileForm).then((response) => {
        if (response.data.code == 200) this.$message.success("保存成功");
        else this.$message.error(response.data.message || "保存失败");
      }).catch(() => { this.$message.error("网络错误"); }).finally(() => { this.saving = false; });
    },
    changePassword() {
      this.$refs.passwordFormRef.validate((valid) => {
        if (!valid) return;
        this.changingPwd = true;
        this.axios.put("/api/user/password", this.passwordForm).then((response) => {
          if (response.data.code == 200) {
            this.$message.success("密码修改成功，请重新登录");
            this.$accessToken.set(null);
            this.$cookies.remove('token'); this.$cookies.remove('userId'); this.$cookies.remove('username'); this.$cookies.remove('userType'); this.$cookies.remove('realName');
            this.$router.push("/login");
          } else { this.$message.error(response.data.message || "修改失败"); }
        }).catch(() => { this.$message.error("网络错误"); }).finally(() => { this.changingPwd = false; });
      });
    },
    loadMyEvidences() {
      this.axios.get("/api/evidence/my", {
        params: { pageNum: this.myPageNum, pageSize: 10 }
      }).then((response) => {
        if (response.data.code == 200) { this.myEvidences = response.data.data.list; this.myTotal = response.data.data.total; }
      });
    },
    handleMyPageChange(val) { this.myPageNum = val; this.loadMyEvidences(); },
    doChain(id) {
      let row = this.myEvidences.find(r => r.id === id);
      if (row) { this.$set(row, '_chaining', true); }
      this.$confirm("确认将该存证上链？数据将写入区块链，不可篡改。", "上链确认", {
        confirmButtonText: "确定上链", cancelButtonText: "取消", type: "warning"
      }).then(() => {
        this.axios.post("/api/evidence/" + id + "/chain").then((response) => {
          if (response.data.code == 200) { this.$message.success("上链成功"); this.loadMyEvidences(); }
          else { this.$message.error(response.data.message || "上链失败"); if (row) this.$set(row, '_chaining', false); }
        }).catch(() => { this.$message.error("网络错误"); if (row) this.$set(row, '_chaining', false); });
      }).catch(() => { if (row) this.$set(row, '_chaining', false); });
    },
    fmtTime(t) { if (!t) return "-"; return t.replace("T", " ").substring(0, 19); }
  },
  mounted() {
    this.loadProfile();
    this.loadMyEvidences();
  }
};
</script>

<style scoped>
.individual-page { background: var(--bg-body); padding: 24px; }
.profile-card { border-radius: var(--radius-lg); }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { display: flex; align-items: center; gap: 8px; font-weight: 700; font-size: 18px; color: var(--text-primary); }
.card-title i { color: var(--primary); font-size: 20px; }

.profile-tabs { margin-top: -8px; }
.tab-content { padding: 20px 0; }
.profile-form { max-width: 560px; margin: 0 auto; }

.evidence-table { border-radius: var(--radius-sm); overflow: hidden; }
.chain-done { color: #c0c4cc; font-size: 13px; }
.evidence-pagination { margin-top: 16px; padding-bottom: 4px; }
</style>
