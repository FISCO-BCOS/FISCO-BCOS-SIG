<template>
  <div id="app">
    <el-container>
      <el-header class="header"><Header /></el-header>
      <el-container>
        <el-aside width="220px"><navigator></navigator></el-aside>
        <el-main class="page-main" style="padding:20px!important">
          <div class="page-title-bar">
            <h2><i class="el-icon-user"></i>个人中心</h2>
            <p class="title-desc">管理您的个人信息、产品和操作记录</p>
          </div>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card class="profile-card fade-in-up">
                <div style="text-align: center">
                  <el-avatar :size="100" icon="el-icon-user-solid"></el-avatar>
                  <h3>{{ userInfo.realName || userInfo.username }}</h3>
                  <el-tag :type="roleTagType" size="medium">{{ roleName }}</el-tag>
                </div>
                <el-divider></el-divider>
                <p><strong>真实姓名：</strong>{{ userInfo.realName || '-' }}</p>
                <p><strong>手机号：</strong>{{ userInfo.phone || '-' }}</p>
                <p><strong>所属组织：</strong>{{ userInfo.organization || '-' }}</p>
                <p><strong>注册时间：</strong>{{ formatDate(userInfo.createTime) }}</p>
                <el-divider></el-divider>
                <el-button type="primary" @click="showEditDialog" style="width:100%; margin-bottom:10px">编辑资料</el-button>
                <el-button @click="showPwdDialog" style="width:100%">修改密码</el-button>
              </el-card>
            </el-col>
            <el-col :span="16">
              <div class="content-card fade-in-up">
              <el-tabs v-model="activeTab">
                <el-tab-pane label="我的产品" name="1">
                  <el-table :data="myProducts" v-loading="loadingProducts" empty-text="暂无产品" border stripe style="width:100%">
                    <el-table-column prop="productName" label="产品名称"></el-table-column>
                    <el-table-column prop="productNo" label="编号" width="180">
                      <template slot-scope="scope">
                        <span style="font-family:monospace">{{ scope.row.productNo }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="price" label="价格" width="100" align="right">
                      <template slot-scope="scope">¥{{ scope.row.price }}</template>
                    </el-table-column>
                    <el-table-column prop="statusName" label="状态" width="90" align="center">
                      <template slot-scope="scope">
                        <el-tag :type="scope.row.statusName==='已上架'?'success':scope.row.statusName==='已下架'?'danger':'info'" size="small">{{ scope.row.statusName || scope.row.status || '草稿' }}</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="80">
                      <template slot-scope="scope">
                        <el-button type="text" @click="$router.push('/products/detail/'+scope.row.id)">查看</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                  <el-pagination v-if="myProductTotal>0" small :total="myProductTotal" :page-size="10" layout="total,prev,pager,next" @current-change="handleProductPageChange" style="margin-top:15px;text-align:right"></el-pagination>
                </el-tab-pane>
                <el-tab-pane label="上链记录" name="2">
                  <el-table :data="chainRecords" v-loading="loadingChain" empty-text="暂无记录" border stripe style="width:100%">
                    <el-table-column prop="recordType" label="记录类型"></el-table-column>
                    <el-table-column prop="recordNo" label="编号" width="180">
                      <template slot-scope="s"><span style="font-family:monospace">{{ s.row.recordNo }}</span></template>
                    </el-table-column>
                    <el-table-column prop="txHash" label="交易哈希" width="120">
                      <template slot-scope="s">
                        <el-tag type="info" style="font-family:monospace;">{{ (s.row.txHash||'').substring(0,10) }}...</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="chainStatus" label="状态" width="90" align="center">
                      <template slot-scope="s">
                        <el-tag :type="s.row.chainStatus==1?'success':'warning'">{{ s.row.chainStatus==1?'已上链':'待上链' }}</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="createTime" label="时间" width="170"></el-table-column>
                  </el-table>
                </el-tab-pane>
                <el-tab-pane label="操作日志" name="3">
                  <el-timeline>
                    <el-timeline-item v-for="(log,idx) in operationLogs" :key="idx" :timestamp="log.time" placement="top">
                      <el-card>
                        <p>{{ log.action }} - {{ log.detail }}</p>
                      </el-card>
                    </el-timeline-item>
                  </el-timeline>
                  <el-empty v-if="operationLogs.length==0" description="暂无操作记录"></el-empty>
                </el-tab-pane>
                <el-tab-pane label="账户设置" name="4">
                  <el-form label-width="120px">
                    <el-form-item label="用户名"><el-tag>{{ userInfo.username }}</el-tag></el-form-item>
                    <el-form-item label="登录状态"><el-tag type="success">在线</el-tag></el-form-item>
                    <el-form-item label="Token有效期"><el-tag>24小时</el-tag></el-form-item>
                    <el-form-item><el-button type="danger" @click="logout">退出登录</el-button></el-form-item>
                  </el-form>
                </el-tab-pane>
              </el-tabs>
              </div>
            </el-col>
          </el-row>

          <el-dialog title="编辑资料" :visible.sync="editDialogVisible" width="40%">
            <el-form label-width="100px">
              <el-form-item label="真实姓名"><el-input v-model="editForm.realName"></el-input></el-form-item>
              <el-form-item label="手机号"><el-input v-model="editForm.phone"></el-input></el-form-item>
              <el-form-item label="所属组织"><el-input v-model="editForm.organization"></el-input></el-form-item>
            </el-form>
            <span slot="footer">
              <el-button @click="editDialogVisible=false">取消</el-button>
              <el-button type="primary" @click="updateProfile">保存</el-button>
            </span>
          </el-dialog>

          <el-dialog title="修改密码" :visible.sync="pwdDialogVisible" width="40%">
            <el-form label-width="100px">
              <el-form-item label="原密码"><el-input v-model="pwdForm.oldPassword" type="password"></el-input></el-form-item>
              <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password"></el-input></el-form-item>
              <el-form-item label="确认密码"><el-input v-model="pwdForm.confirmPassword" type="password"></el-input></el-form-item>
            </el-form>
            <span slot="footer">
              <el-button @click="pwdDialogVisible=false">取消</el-button>
              <el-button type="primary" @click="changePassword">修改</el-button>
            </span>
          </el-dialog>

        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import Navigator from '@/components/Navigator';
import Header from '@/components/Header';

export default {
  name: 'Individual',
  components: { Navigator, Header },
  data() {
    return {
      userInfo: {},
      editForm: { realName: '', phone: '', organization: '' },
      pwdForm: { oldPassword: '', newPassword: '', confirmPassword: '' },
      activeTab: '1',
      myProducts: [],
      myProductTotal: 0,
      myProductPage: 1,
      loadingProducts: false,
      chainRecords: [],
      loadingChain: false,
      operationLogs: [],
      editDialogVisible: false,
      pwdDialogVisible: false,
      roleMap: { 1: '农户', 2: '加工商', 3: '检测机构', 4: '物流商' },
      roleTagMap: { 1: 'success', 2: 'warning', 3: 'danger', 4: 'info' }
    };
  },
  computed: {
    roleName() {
      return this.roleMap[this.userInfo.role] || '未知角色';
    },
    roleTagType() {
      return this.roleTagMap[this.userInfo.role] || '';
    }
  },
  methods: {
    formatDate(val) { if (!val) return '-'; return String(val).replace('T', ' ').substring(0, 16); },
    queryMyProducts() {
      this.loadingProducts = true;
      this.axios.get('/api/products/list', {
        params: { pageNum: this.myProductPage, pageSize: 10 },
        headers: { Authorization: 'Bearer ' + this.$cookies.get('token') }
      }).then((response) => {
        if (response.data.code == 200) {
          let data = response.data.data;
          this.myProducts = data.rows || data.list || data || [];
          this.myProductTotal = data.total || this.myProducts.length;
        }
      }).finally(() => {
        this.loadingProducts = false;
      });
    },
    queryChainRecords() {
      this.loadingChain = true;
      this.axios.get('/api/chain/records', {
        params: { pageNum: 1, pageSize: 10 },
        headers: { Authorization: 'Bearer ' + this.$cookies.get('token') }
      }).then((response) => {
        if (response.data.code == 200) {
          let data = response.data.data;
          this.chainRecords = data.rows || data.list || data || [];
        }
      }).finally(() => {
        this.loadingChain = false;
      });
    },
    queryOperationLogs() {
      this.axios.get('/api/logs/operation', {
        params: { pageNum: 1, pageSize: 20 },
        headers: { Authorization: 'Bearer ' + this.$cookies.get('token') }
      }).then((response) => {
        if (response.data.code == 200) {
          let data = response.data.data;
          this.operationLogs = data.rows || data.list || data || [];
        }
      });
    },
    handleProductPageChange(page) {
      this.myProductPage = page;
      this.queryMyProducts();
    },
    showEditDialog() {
      this.editForm = {
        realName: this.userInfo.realName || '',
        phone: this.userInfo.phone || '',
        organization: this.userInfo.organization || ''
      };
      this.editDialogVisible = true;
    },
    showPwdDialog() {
      this.pwdForm = { oldPassword: '', newPassword: '', confirmPassword: '' };
      this.pwdDialogVisible = true;
    },
    updateProfile() {
      this.axios.put('/api/users/profile', this.editForm, {
        headers: { Authorization: 'Bearer ' + this.$cookies.get('token') }
      }).then((response) => {
        if (response.data.code == 200) {
          this.$message.success('资料更新成功');
          this.editDialogVisible = false;
          Object.assign(this.userInfo, this.editForm);
          this.$cookies.set('userInfo', JSON.stringify(this.userInfo));
        } else {
          this.$message.error(response.data.msg || '更新失败');
        }
      });
    },
    changePassword() {
      if (this.pwdForm.newPassword !== this.pwdForm.confirmPassword) {
        this.$message.error('两次密码输入不一致');
        return;
      }
      this.axios.put('/api/users/password', {
        oldPassword: this.pwdForm.oldPassword,
        newPassword: this.pwdForm.newPassword
      }, {
        headers: { Authorization: 'Bearer ' + this.$cookies.get('token') }
      }).then((response) => {
        if (response.data.code == 200) {
          this.$message.success('密码修改成功，请重新登录');
          this.pwdDialogVisible = false;
          this.$cookies.remove('token');
          this.$cookies.remove('userInfo');
          this.$router.push('/login');
        } else {
          this.$message.error(response.data.msg || '密码修改失败');
        }
      });
    },
    logout() {
      this.$confirm('确认退出登录？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$cookies.remove('token');
        this.$cookies.remove('userInfo');
        this.$router.push('/login');
      }).catch(() => {});
    }
  },
  mounted() {
    const info = this.$cookies.get('userInfo');
    if (info && typeof info === 'object') {
      this.userInfo = info;
    }
    this.queryMyProducts();
    this.queryChainRecords();
    this.queryOperationLogs();
  }
};
</script>

<style>
.page-main { padding: 20px !important; }
.page-title-bar { margin-bottom: 16px; }
.page-title-bar h2 { font-size: 20px; font-weight: 600; color: var(--text-primary); margin: 0 0 4px; display: flex; align-items: center; gap: 8px; }
.page-title-bar h2 i { color: var(--primary); }
.title-desc { font-size: 13px; color: var(--text-secondary); }
.profile-card { border-radius: var(--radius-md) !important; overflow: hidden; }
.content-card { background: #fff; border-radius: var(--radius-md); padding: 20px; box-shadow: var(--shadow-sm); }
.fade-in-up { animation: fadeInUp 0.5s ease both; }
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>