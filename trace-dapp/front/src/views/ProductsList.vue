<template>
  <div id="app">
    <el-container>
      <el-header><Header /></el-header>
      <el-container>
        <el-aside width="220px"><navigator></navigator></el-aside>
        <el-main class="page-main">
          <!-- 页面标题 -->
          <div class="page-title-bar">
            <h2><i class="el-icon-goods"></i> 产品列表</h2>
            <span class="title-desc">管理所有产品信息，支持上链申请</span>
          </div>

          <!-- 搜索栏 + Tab切换 -->
          <div class="search-card fade-in-up">
            <!-- 我的 / 全部 Tab -->
            <div class="scope-tabs">
              <div
                class="scope-tab"
                :class="{ active: scope === 'mine' }"
                @click="switchScope('mine')"
              ><i class="el-icon-user"></i> 我的产品</div>
              <div
                class="scope-tab"
                :class="{ active: scope === 'all' }"
                @click="switchScope('all')"
              ><i class="el-icon-menu"></i> 全部产品</div>
              <span v-if="scope === 'mine'" class="scope-hint">仅显示我创建的产品</span>
            </div>

            <el-row :gutter="12" type="flex" align="middle" style="margin-top:12px;">
              <el-col :span="5">
                <el-input v-model="keyword" clearable prefix-icon="el-icon-search" placeholder="搜索名称/编号" size="medium" @clear="handleSearch"></el-input>
              </el-col>
              <el-col :span="4">
                <el-select v-model="category" clearable placeholder="全部分类" size="medium" @change="handleSearch">
                  <el-option label="全部" value=""></el-option>
                  <el-option label="粮食" value="粮食"></el-option>
                  <el-option label="蔬菜" value="蔬菜"></el-option>
                  <el-option label="水果" value="水果"></el-option>
                  <el-option label="肉禽" value="肉禽"></el-option>
                  <el-option label="水产" value="水产"></el-option>
                  <el-option label="其他" value="其他"></el-option>
                </el-select>
              </el-col>
              <el-col :span="4">
                <el-select v-model="status" clearable placeholder="全部状态" size="medium" @change="handleSearch">
                  <el-option label="全部" value=""></el-option>
                  <el-option label="草稿" value="草稿"></el-option>
                  <el-option label="已上架" value="已上架"></el-option>
                  <el-option label="已下架" value="已下架"></el-option>
                </el-select>
              </el-col>
              <el-col :span="7" style="text-align: right;">
                <el-button icon="el-icon-search" size="medium" @click="handleSearch">搜索</el-button>
                <el-button icon="el-icon-refresh-right" size="medium" @click="resetSearch">重置</el-button>
                <el-button type="primary" icon="el-icon-plus" size="medium" @click="$router.push('/products/create')" v-if="currentUserRole === 1">新建产品</el-button>
              </el-col>
            </el-row>
          </div>

          <!-- 表格 -->
          <el-table :data="productList" border stripe v-loading="loading" style="width: 100%" size="medium">
            <el-table-column type="index" label="#" width="50" align="center"></el-table-column>
            <el-table-column prop="productNo" label="产品编号" width="180">
              <template slot-scope="scope">
                <code class="mono-text" @click="copyText(scope.row.productNo)" style="cursor:pointer;">{{ scope.row.productNo }}</code>
              </template>
            </el-table-column>
            <el-table-column prop="productName" label="产品名称" min-width="140"></el-table-column>
            <el-table-column prop="category" label="分类" width="90" align="center">
              <template slot-scope="scope">
                <el-tag :type="getCategoryTagType(scope.row.category)" size="small">{{ scope.row.category }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="price" label="价格" width="90" align="right">
              <template slot-scope="scope"><span class="price-text">¥{{ scope.row.price }}</span></template>
            </el-table-column>
            <el-table-column prop="originAddress" label="产地" min-width="140" show-overflow-tooltip></el-table-column>
            <el-table-column prop="status" label="状态" width="90" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.statusName === '已上架' ? 'success' : scope.row.statusName === '已下架' ? 'danger' : 'info'" size="small">{{ scope.row.statusName || '草稿' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="链上状态" width="110" align="center">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.txHash && scope.row.txHash.length > 0" type="success" size="small"><i class="el-icon-success"></i> 已上链</el-tag>
                <el-tag v-else-if="hasPendingApproval(scope.row.id)" type="warning" size="small"><i class="el-icon-loading"></i> 审批中</el-tag>
                <el-tag v-else type="info" size="small"><i class="el-icon-time"></i> 未上链</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="scanCount" label="扫码" width="70" align="center"></el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
            <el-table-column label="操作" width="280" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" @click="$router.push('/products/detail/' + scope.row.id)">详情</el-button>
                <template v-if="isOwner(scope.row)">
                  <el-button v-if="(scope.row.status === 0 || scope.row.statusName === '草稿') && !hasPendingApproval(scope.row.id)" type="text" style="color: #e6a23c;" @click="$router.push('/products/create?id=' + scope.row.id)">编辑</el-button>
                  <el-button v-if="(scope.row.status === 0 || scope.row.statusName === '草稿') && !hasPendingApproval(scope.row.id)" type="text" style="color: #409EFF;" @click="submitChain(scope.row)">提交上链</el-button>
                  <el-button v-else-if="(scope.row.status === 0 || scope.row.statusName === '草稿') && hasPendingApproval(scope.row.id)" type="text" style="color: #f56c6c;" @click="cancelChain(scope.row)">取消上链</el-button>
                  <el-button type="text" :style="{ color: scope.row.statusName === '已上架' ? '#f56c6c' : '#67c23a' }" @click="toggleStatus(scope.row)">{{ scope.row.statusName === '已上架' ? '下架' : '上架' }}</el-button>
                </template>
                <span v-else class="op-hint">仅创建者可操作</span>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-wrap">
            <el-pagination
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
              :current-page="pageNum"
              :page-sizes="[5, 10, 20, 50]"
              :page-size="pageSize"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              background
            >
            </el-pagination>
          </div>

          <!-- 上链提交结果弹窗 -->
          <el-dialog title="上链申请提交" :visible.sync="chainDialogVisible" width="520px" :close-on-click-modal="false" custom-class="result-dialog">
            <div v-if="chainResult">
              <el-alert :title="chainResult.status === 'pending' ? '申请已提交，等待管理员审批' : '提交失败'" :type="chainResult.status === 'pending' ? 'success' : 'error'" :closable="false" show-icon style="margin-bottom: 16px; border-radius: 8px;"></el-alert>
              <div class="result-info-box">
                <p><strong>产品编号：</strong>{{ chainProduct ? chainProduct.productNo : '-' }}</p>
                <p><strong>产品名称：</strong>{{ chainProduct ? chainProduct.productName : '-' }}</p>
                <p><strong>角色校验结果：</strong></p>
                <div class="validation-result">{{ chainResult.validationMsg || '无' }}</div>
              </div>
            </div>
            <span slot="footer"><el-button @click="chainDialogVisible = false">关闭</el-button></span>
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
  name: 'ProductsList',
  components: { Navigator, Header },
  data() {
    return {
      keyword: '', category: '', status: '',
      scope: 'mine',
      productList: [], pageNum: 1, pageSize: 10, total: 0,
      chainDialogVisible: false, chainProduct: null, chainResult: null,
      pendingProductIds: [], pendingApprovalMap: {}, loading: false,
      currentUserId: null,
      currentUserRole: null
    };
  },
  methods: {
    /** 判断当前用户是否为产品创建者（管理员/监管者也不得操作，仅创建者可编辑/上链/上架） */
    isOwner(row) {
      // 仅创建者可操作自己的产品
      var uid = Number(this.currentUserId);
      var fid = Number(row.farmerId);
      if (uid && fid && uid === fid) return true;
      return false;
    },
    getCategoryTagType(category) {
      const map = { '粮食': '', '蔬菜': 'success', '水果': 'warning', '肉禽': 'danger', '水产': 'info' }; return map[category] || '';
    },
    switchScope(val) {
      if (this.scope === val) return;
      this.scope = val;
      this.pageNum = 1;
      this.query();
    },
    query() {
      this.loading = true;
      this.axios.get('/api/products/list', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          keyword: this.keyword || undefined,
          category: this.category || undefined,
          status: this.status || undefined,
          scope: this.scope
        },
        headers: { Authorization: 'Bearer ' + this.$cookies.get('token') }
      }).then((response) => {
        if (response.data.code == 200) {
          let data = response.data.data;
          this.productList = data.rows || data.list || data || [];
          this.total = data.total || this.productList.length;
        }
      }).finally(() => { this.loading = false; });
      this.loadPendingApprovals();
    },
    loadPendingApprovals() {
      this.axios.get('/api/chain/pending', { headers: { Authorization: 'Bearer ' + this.$cookies.get('token') } }).then((response) => {
        if (response.data.code == 200 && Array.isArray(response.data.data)) {
          const pending = response.data.data.filter(item => item.status === 0);
          this.pendingProductIds = pending.map(item => item.productId);
          const map = {}; pending.forEach(item => { map[item.productId] = item.id; });
          this.pendingApprovalMap = map;
        }
      });
    },
    hasPendingApproval(productId) { return this.pendingProductIds.indexOf(productId) !== -1; },
    handleSearch() { this.pageNum = 1; this.query(); },
    resetSearch() { this.keyword = ''; this.category = ''; this.status = ''; this.pageNum = 1; this.query(); },
    handlePageChange(page) { this.pageNum = page; this.query(); },
    handleSizeChange(size) { this.pageSize = size; this.pageNum = 1; this.query(); },
    toggleStatus(row) {
      const newStatus = row.statusName === '已上架' ? '已下架' : '已上架';
      const actionText = newStatus === '已上架' ? '上架' : '下架';
      this.$confirm('确认' + actionText + '该产品？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }).then(() => {
        this.axios.put('/api/products/' + row.id + '/status', { status: newStatus }, { headers: { Authorization: 'Bearer ' + this.$cookies.get('token') } }).then((response) => {
          if (response.data.code == 200) { this.$message.success(actionText + '成功'); this.query(); }
          else { this.$message.error(response.data.msg || actionText + '失败'); }
        });
      }).catch(() => {});
    },
    cancelChain(row) {
      const requestId = this.pendingApprovalMap[row.id];
      if (!requestId) { this.$message.error('未找到对应的审批请求'); return; }
      this.$confirm('确认取消该产品的上链申请？取消后可重新提交。', '取消上链', { confirmButtonText: '确认取消', cancelButtonText: '返回', type: 'warning' }).then(() => {
        this.axios.put('/api/chain/cancel/' + requestId, {}, { headers: { Authorization: 'Bearer ' + this.$cookies.get('token') } }).then((response) => {
          if (response.data.code == 200) {
            this.$message.success('已取消上链请求');
            const idx = this.pendingProductIds.indexOf(row.id); if (idx !== -1) this.pendingProductIds.splice(idx, 1);
            this.$delete(this.pendingApprovalMap, row.id);
          } else { this.$message.error(response.data.msg || '取消失败'); }
        });
      }).catch(() => {});
    },
    copyText(text) {
      if (navigator.clipboard) { navigator.clipboard.writeText(text).then(() => { this.$message.success('已复制：' + text); }); }
      else { let input = document.createElement('input'); input.value = text; document.body.appendChild(input); input.select(); document.execCommand('copy'); document.body.removeChild(input); this.$message.success('已复制：' + text); }
    },
    submitChain(row) {
      this.chainProduct = row;
      this.$confirm('确认提交该产品的上链申请？提交后将等待管理员审批。', '提交上链', { confirmButtonText: '确认提交', cancelButtonText: '取消', type: 'info' }).then(() => {
        this.axios.post('/api/chain/submit', { productId: row.id }, { headers: { Authorization: 'Bearer ' + this.$cookies.get('token') } }).then((response) => {
          if (response.data.code == 200) {
            this.chainResult = response.data.data; this.chainDialogVisible = true;
            if (row.id && this.pendingProductIds.indexOf(row.id) === -1) this.pendingProductIds.push(row.id);
            if (row.id && response.data.data.requestId) this.$set(this.pendingApprovalMap, row.id, response.data.data.requestId);
          } else { this.$message.error(response.data.msg || '提交失败'); }
        }).catch(() => { this.$message.error('请求失败，请检查网络连接'); });
      }).catch(() => {});
    }
  },
  mounted() {
    // 从JWT解析当前用户身份
    var self = this;
    var jwtUsername = '';
    try {
      var token = this.$cookies.get('token');
      if (token) {
        var payload = JSON.parse(atob(token.split('.')[1]));
        this.currentUserId = payload.userId ? Number(payload.userId) : null;
        this.currentUserRole = payload.role !== undefined ? Number(payload.role) : null;
        jwtUsername = payload.sub || ''; // JWT中的用户名
      }
    } catch (e) { /* 解析失败时保持null */ }
    // 如果JWT中没有userId，通过API获取当前用户信息（兼容老JWT）
    if (!this.currentUserId && this.$cookies.get('token') && jwtUsername) {
      this.axios.get('/api/auth/user/info', {
        params: { username: jwtUsername },
        headers: { Authorization: 'Bearer ' + this.$cookies.get('token') }
      }).then(function(res) {
        if (res.data.code == 200 && res.data.data && res.data.data.id) {
          self.currentUserId = Number(res.data.data.id);
          if (res.data.data.role !== undefined) self.currentUserRole = Number(res.data.data.role);
          self.query();
        }
      }).catch(function() {});
    }
    this.query();
  }
};
</script>

<style scoped>
.page-main { padding: 20px !important; }

.page-title-bar { margin-bottom: 16px; }
.page-title-bar h2 { font-size: 20px; font-weight: 600; color: var(--text-primary); margin: 0 0 4px; display: flex; align-items: center; gap: 8px; }
.page-title-bar h2 i { color: var(--primary); }
.title-desc { font-size: 13px; color: var(--text-secondary); }

.search-card {
  background: #fff; border-radius: var(--radius-md); padding: 16px 20px;
  box-shadow: var(--shadow-sm); margin-bottom: 18px;
}

/* 我的产品 / 全部产品 Tab */
.scope-tabs {
  display: flex; align-items: center; gap: 4px;
}
.scope-tab {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 6px 16px; border-radius: 20px; cursor: pointer;
  font-size: 14px; color: #606266; transition: all 0.25s;
  border: 1px solid #dcdfe6; background: #fafafa;
}
.scope-tab:hover { color: var(--primary); border-color: #b3d8ff; background: #ecf5ff; }
.scope-tab.active {
  color: #fff; background: var(--primary);
  border-color: var(--primary); font-weight: 600;
}
.scope-hint {
  margin-left: 12px; font-size: 12px; color: #909399;
}

.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 18px; }

.mono-text {
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, monospace;
  font-size: 12px; color: var(--primary); background: #ecf5ff; padding: 2px 8px; border-radius: 4px;
}
.mono-text:hover { background: #d9ecff; }
.price-text { font-weight: 600; color: #E6A23C; }

.result-info-box { background: #f5f7fa; padding: 16px; border-radius: 8px; }
.result-info-box p { margin: 4px 0; color: var(--text-regular); }
.validation-result {
  margin-top: 8px; padding: 12px; background: #fff; border-radius: 6px;
  border-left: 3px solid var(--primary); white-space: pre-wrap; font-size: 13px; line-height: 1.6;
}
.op-hint { color: #c0c4cc; font-size: 12px; }
</style>
