<template>
  <div id="app">
    <el-container>
      <el-header class="header"><Header /></el-header>
      <el-container>
        <el-aside width="220px"><navigator></navigator></el-aside>
        <el-main class="page-main" style="padding:20px!important">
          <div class="page-title-bar">
            <h2><i class="el-icon-s-check"></i>上链审批管理</h2>
            <p class="title-desc">审核用户提交的上链申请，通过或拒绝后触发实际上链</p>
          </div>

          <!-- 状态筛选标签 -->
          <div class="filter-bar fade-in-up">
            <el-radio-group v-model="statusFilter" size="medium" @change="queryList">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button :label="0">待审批</el-radio-button>
            <el-radio-button :label="1">已通过</el-radio-button>
            <el-radio-button :label="2">已拒绝</el-radio-button>
            </el-radio-group>
          </div>

          <el-table :data="list" border stripe v-loading="loading" style="width: 100%">
            <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
            <el-table-column prop="productNo" label="产品编号" width="180">
              <template slot-scope="scope">
                <span style="font-family: monospace;">{{ scope.row.productNo }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="userName" label="申请人" width="120"></el-table-column>
            <el-table-column prop="userRole" label="角色" width="100" align="center">
              <template slot-scope="scope">
                <el-tag size="small" :type="getRoleTagType(scope.row.userRole)">{{ getRoleName(scope.row.userRole) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90" align="center">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.status === 0" type="warning" size="small">待审批</el-tag>
                <el-tag v-else-if="scope.row.status === 1" type="success" size="small">已通过</el-tag>
                <el-tag v-else type="danger" size="small">已拒绝</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="validationMsg" label="校验信息" min-width="220" show-overflow-tooltip></el-table-column>
            <el-table-column prop="createTime" label="申请时间" width="170">
              <template slot-scope="scope">{{ formatDate(scope.row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template slot-scope="scope">
                <el-button v-if="scope.row.status === 0" type="text" style="color: #67c23a;" @click="handleApprove(scope.row, true)">通过</el-button>
                <el-button v-if="scope.row.status === 0" type="text" style="color: #f56c6c;" @click="handleApprove(scope.row, false)">拒绝</el-button>
                <el-button type="text" @click="showDetail(scope.row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 审批详情弹窗 -->
          <el-dialog title="审批详情" :visible.sync="detailVisible" width="500px">
            <div v-if="detailItem">
              <el-descriptions :column="1" border size="medium">
                <el-descriptions-item label="产品编号">{{ detailItem.productNo }}</el-descriptions-item>
                <el-descriptions-item label="申请人">{{ detailItem.userName }}</el-descriptions-item>
                <el-descriptions-item label="角色">{{ getRoleName(detailItem.userRole) }}</el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag v-if="detailItem.status === 0" type="warning" size="small">待审批</el-tag>
                  <el-tag v-else-if="detailItem.status === 1" type="success" size="small">已通过</el-tag>
                  <el-tag v-else type="danger" size="small">已拒绝</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="校验结果" v-if="detailItem.validationMsg">
                  <div style="white-space: pre-wrap;">{{ detailItem.validationMsg }}</div>
                </el-descriptions-item>
                <el-descriptions-item label="拒绝原因" v-if="detailItem.rejectReason">{{ detailItem.rejectReason }}</el-descriptions-item>
                <el-descriptions-item label="申请时间">{{ formatDate(detailItem.createTime) }}</el-descriptions-item>
                <el-descriptions-item label="审批时间" v-if="detailItem.approveTime">{{ formatDate(detailItem.approveTime) }}</el-descriptions-item>
              </el-descriptions>
            </div>
            <span slot="footer">
              <el-button @click="detailVisible = false">关闭</el-button>
            </span>
          </el-dialog>

          <!-- 拒绝原因弹窗 -->
          <el-dialog title="拒绝上链申请" :visible.sync="rejectDialogVisible" width="420px" :close-on-click-modal="false">
            <el-input type="textarea" v-model="rejectReason" :rows="4" placeholder="请输入拒绝原因（可选）" maxlength="500" show-word-limit></el-input>
            <span slot="footer">
              <el-button @click="rejectDialogVisible = false">取消</el-button>
              <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
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
  name: 'ChainApproval',
  components: { Navigator, Header },
  data() {
    return {
      list: [],
      loading: false,
      statusFilter: '',
      detailVisible: false,
      detailItem: null,
      rejectDialogVisible: false,
      rejectReason: '',
      currentRow: null
    };
  },
  methods: {
    formatDate(val) {
      if (!val) return '-';
      return val.replace('T', ' ').substring(0, 16);
    },
    getRoleName(role) {
      const map = { 0: '监管者', 1: '农户', 2: '加工商', 3: '检测机构', 4: '物流商' };
      return map[role] || '未知';
    },
    getRoleTagType(role) {
      const map = { 0: '', 1: 'success', 2: 'warning', 3: 'danger', 4: 'info' };
      return map[role] || '';
    },
    queryList() {
      this.loading = true;
      let url = '/api/chain/pending';
      if (this.statusFilter !== '' && this.statusFilter !== null) {
        url = '/api/chain/pending?status=' + this.statusFilter;
      }
      this.axios.get(url, {
        headers: { Authorization: 'Bearer ' + this.$cookies.get('token') }
      }).then((response) => {
        if (response.data.code == 200) {
          let data = response.data.data;
          // 后端返回的是列表，前端按状态过滤
          if (Array.isArray(data)) {
            if (this.statusFilter !== '' && this.statusFilter !== null) {
              this.list = data.filter(item => item.status === this.statusFilter);
            } else {
              this.list = data;
            }
          } else {
            this.list = [];
          }
        }
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    showDetail(row) {
      this.detailItem = row;
      this.detailVisible = true;
    },
    handleApprove(row, approved) {
      if (approved) {
        this.$confirm('确认通过该上链申请？通过后产品将进入上链流程。', '审批确认', {
          confirmButtonText: '确认通过',
          cancelButtonText: '取消',
          type: 'success'
        }).then(() => {
          this.doApprove(row.id, true);
        }).catch(() => {});
      } else {
        this.currentRow = row;
        this.rejectReason = '';
        this.rejectDialogVisible = true;
      }
    },
    confirmReject() {
      if (!this.currentRow) return;
      this.doApprove(this.currentRow.id, false);
      this.rejectDialogVisible = false;
    },
    doApprove(id, approved) {
      let body = { approved: approved };
      if (!approved && this.rejectReason) {
        body.reason = this.rejectReason;
      }
      this.axios.put('/api/chain/approve/' + id, body, {
        headers: { Authorization: 'Bearer ' + this.$cookies.get('token') }
      }).then((response) => {
        if (response.data.code == 200) {
          this.$message.success(response.data.data.message || (approved ? '已通过' : '已拒绝'));
          this.queryList();
        } else {
          this.$message.error(response.data.msg || '操作失败');
        }
      });
    }
  },
  mounted() {
    this.queryList();
  }
};
</script>

<style scoped>
.header {
  background-color: #409EFF;
  color: #fff;
  line-height: 20px;
}
</style>
