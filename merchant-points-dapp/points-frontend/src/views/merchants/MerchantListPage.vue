<template>
  <div class="merchant-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商户管理</span>
          <div class="search-bar">
            <el-input v-model="query.keyword" placeholder="搜索商户名/编号/手机号" clearable style="width: 250px" @clear="loadData" />
            <el-select v-model="query.auditStatus" placeholder="审核状态" clearable style="width: 120px">
              <el-option label="待审核" :value="0" />
              <el-option label="已通过" :value="1" />
              <el-option label="已拒绝" :value="2" />
            </el-select>
            <el-button type="primary" @click="loadData">搜索</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="merchantNo" label="商户编号" width="150" />
        <el-table-column prop="merchantName" label="商户名称" width="150" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="businessType" label="经营类型" width="100">
          <template #default="{ row }">
            {{ getBusinessTypeName(row.businessType) }}
          </template>
        </el-table-column>
        <el-table-column prop="pointsBalance" label="积分余额" width="120" />
        <el-table-column prop="auditStatus" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getAuditColor(row.auditStatus)">{{ getAuditName(row.auditStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="营业状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '营业中' : '已停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="$router.push(`/merchants/${row.id}`)">详情</el-button>
            <el-button v-if="row.auditStatus === 0" type="success" link @click="handleAudit(row, 1)">通过</el-button>
            <el-button v-if="row.auditStatus === 0" type="danger" link @click="handleAudit(row, 2)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadData"
        style="margin-top: 15px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getMerchantList, auditMerchant } from '@/api/merchant'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  auditStatus: undefined as number | undefined
})

const getBusinessTypeName = (type: string) => {
  const map: any = { catering: '餐饮', retail: '零售', service: '服务', other: '其他' }
  return map[type] || type
}
const getAuditName = (status: number) => ['待审核', '已通过', '已拒绝'][status]
const getAuditColor = (status: number) => ['warning', 'success', 'danger'][status]

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getMerchantList(query)
    if (res.code === 200) {
      tableData.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleAudit = async (row: any, auditStatus: number) => {
  const action = auditStatus === 1 ? '通过' : '拒绝'
  await ElMessageBox.confirm(`确认${action}商户 "${row.merchantName}" 的审核？`, '确认操作')
  try {
    const res: any = await auditMerchant(row.id, { auditStatus, rejectReason: auditStatus === 2 ? '不符合要求' : '' })
    if (res.code === 200) {
      ElMessage.success(`${action}成功`)
      loadData()
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-bar { display: flex; gap: 10px; }
</style>
