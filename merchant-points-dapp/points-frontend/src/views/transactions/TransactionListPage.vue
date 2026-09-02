<template>
  <div class="transaction-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>交易流水</span>
          <div class="search-bar">
            <el-select v-model="query.txType" placeholder="交易类型" clearable style="width: 120px">
              <el-option label="发行" :value="1" />
              <el-option label="转出" :value="2" />
              <el-option label="转入" :value="3" />
              <el-option label="消费" :value="4" />
            </el-select>
            <el-date-picker v-model="dateRange" type="daterange" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 250px" />
            <el-button type="primary" @click="loadData">查询</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="txNo" label="交易编号" width="180" />
        <el-table-column prop="txType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.txType)">{{ getTypeName(row.txType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.txType === 2 || row.txType === 4 ? '#f56c6c' : '#67c23a' }">
              {{ row.txType === 2 || row.txType === 4 ? '-' : '+' }}{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balanceAfter" label="余额快照" width="120" />
        <el-table-column prop="remark" label="备注" />
        <el-table-column prop="txHash" label="交易Hash" width="200">
          <template #default="{ row }">
            <el-text truncated>{{ row.txHash }}</el-text>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="180" />
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
import { getTransactions } from '@/api/points'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const dateRange = ref<any>([])

const query = reactive({ pageNum: 1, pageSize: 10, txType: undefined as number | undefined })

const getTypeName = (type: number) => ({ 1: '发行', 2: '转出', 3: '转入', 4: '消费' } as any)[type]
const getTypeColor = (type: number) => ({ 1: 'success', 2: 'danger', 3: 'success', 4: 'warning' } as any)[type]

const loadData = async () => {
  loading.value = true
  try {
    const params: any = { ...query }
    if (dateRange.value?.length === 2) {
      params.dateFrom = dateRange.value[0].toISOString().split('T')[0]
      params.dateTo = dateRange.value[1].toISOString().split('T')[0]
    }
    const res: any = await getTransactions(params)
    if (res.code === 200) {
      tableData.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-bar { display: flex; gap: 10px; }
</style>
