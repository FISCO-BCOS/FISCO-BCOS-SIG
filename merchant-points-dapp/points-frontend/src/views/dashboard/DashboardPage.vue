<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ data.totalMerchants || 0 }}</div>
            <div class="stat-label">活跃商户</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ data.todayTransactions || 0 }}</div>
            <div class="stat-label">今日交易</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value text-success">{{ data.todayIssued || 0 }}</div>
            <div class="stat-label">今日发行</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value text-warning">{{ data.pendingAudit || 0 }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card>
          <template #header><span>最近交易</span></template>
          <el-table :data="recentTransactions" stripe>
            <el-table-column prop="txNo" label="交易编号" width="180" />
            <el-table-column prop="txType" label="类型" width="80">
              <template #default="{ row }">
                <el-tag :type="getTypeColor(row.txType)">{{ getTypeName(row.txType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" width="120" />
            <el-table-column prop="remark" label="备注" />
            <el-table-column prop="createTime" label="时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header><span>快捷操作</span></template>
          <div class="quick-actions">
            <el-button type="primary" size="large" @click="$router.push('/points/issue')">发行积分</el-button>
            <el-button type="success" size="large" @click="$router.push('/points/transfer')">发起转账</el-button>
            <el-button type="warning" size="large" @click="$router.push('/points/consume')">消费录入</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDashboardData } from '@/api/dashboard'
import { getTransactions } from '@/api/points'

const data = ref<any>({})
const recentTransactions = ref<any[]>([])

const getTypeName = (type: number) => {
  const map: any = { 1: '发行', 2: '转出', 3: '转入', 4: '消费' }
  return map[type] || '未知'
}

const getTypeColor = (type: number) => {
  const map: any = { 1: 'success', 2: 'danger', 3: 'success', 4: 'warning' }
  return map[type] || 'info'
}

onMounted(async () => {
  try {
    const res: any = await getDashboardData()
    if (res.code === 200) data.value = res.data
  } catch (e) { console.error(e) }

  try {
    const res: any = await getTransactions({ pageNum: 1, pageSize: 5 })
    if (res.code === 200) recentTransactions.value = res.data.list || []
  } catch (e) { console.error(e) }
})
</script>

<style scoped>
.stat-cards { margin-bottom: 10px; }
.stat-item { text-align: center; padding: 10px 0; }
.stat-value { font-size: 32px; font-weight: bold; color: #303133; }
.stat-label { color: #909399; margin-top: 8px; }
.text-success { color: #67c23a; }
.text-warning { color: #e6a23c; }
.quick-actions { display: flex; flex-direction: column; gap: 15px; }
.quick-actions .el-button { width: 100%; }
</style>
