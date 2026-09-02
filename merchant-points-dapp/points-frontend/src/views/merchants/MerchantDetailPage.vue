<template>
  <div class="merchant-detail">
    <el-page-header @back="$router.back()" :content="merchant?.merchantName || '商户详情'" />

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="8">
        <el-card>
          <template #header><span>基本信息</span></template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="商户编号">{{ merchant?.merchantNo }}</el-descriptions-item>
            <el-descriptions-item label="商户名称">{{ merchant?.merchantName }}</el-descriptions-item>
            <el-descriptions-item label="联系人">{{ merchant?.contactPerson }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ merchant?.contactPhone }}</el-descriptions-item>
            <el-descriptions-item label="地址">{{ merchant?.address }}</el-descriptions-item>
            <el-descriptions-item label="经营类型">{{ getBusinessTypeName(merchant?.businessType) }}</el-descriptions-item>
            <el-descriptions-item label="链上地址">
              <el-text truncated>{{ merchant?.chainAddress }}</el-text>
            </el-descriptions-item>
            <el-descriptions-item label="审核状态">
              <el-tag :type="getAuditColor(merchant?.auditStatus)">{{ getAuditName(merchant?.auditStatus) }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="积分概览" name="overview">
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-card shadow="hover">
                    <div class="stat-item">
                      <div class="stat-value">{{ merchant?.pointsBalance || 0 }}</div>
                      <div class="stat-label">当前余额</div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card shadow="hover">
                    <div class="stat-item">
                      <div class="stat-value text-success">{{ merchant?.totalIssued || 0 }}</div>
                      <div class="stat-label">累计发行</div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card shadow="hover">
                    <div class="stat-item">
                      <div class="stat-value text-warning">{{ merchant?.totalConsumed || 0 }}</div>
                      <div class="stat-label">累计消费</div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </el-tab-pane>
            <el-tab-pane label="交易记录" name="transactions">
              <el-table :data="transactions" stripe>
                <el-table-column prop="txNo" label="编号" width="180" />
                <el-table-column prop="txType" label="类型" width="80">
                  <template #default="{ row }">
                    <el-tag :type="getTypeColor(row.txType)">{{ getTypeName(row.txType) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="amount" label="金额" width="120" />
                <el-table-column prop="balanceAfter" label="余额快照" width="120" />
                <el-table-column prop="remark" label="备注" />
                <el-table-column prop="createTime" label="时间" width="180" />
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getMerchantDetail } from '@/api/merchant'

const route = useRoute()
const merchant = ref<any>(null)
const transactions = ref<any[]>([])
const activeTab = ref('overview')

const getBusinessTypeName = (type: string) => {
  const map: any = { catering: '餐饮', retail: '零售', service: '服务', other: '其他' }
  return map[type] || type
}
const getAuditName = (status: number) => ['待审核', '已通过', '已拒绝'][status]
const getAuditColor = (status: number) => ['warning', 'success', 'danger'][status]
const getTypeName = (type: number) => ({ 1: '发行', 2: '转出', 3: '转入', 4: '消费' } as any)[type]
const getTypeColor = (type: number) => ({ 1: 'success', 2: 'danger', 3: 'success', 4: 'warning' } as any)[type]

onMounted(async () => {
  const id = Number(route.params.id)
  try {
    const res: any = await getMerchantDetail(id)
    if (res.code === 200) {
      merchant.value = res.data.merchant
      transactions.value = res.data.recentTransactions || []
    }
  } catch (e) { console.error(e) }
})
</script>

<style scoped>
.stat-item { text-align: center; padding: 10px 0; }
.stat-value { font-size: 28px; font-weight: bold; }
.stat-label { color: #909399; margin-top: 8px; }
.text-success { color: #67c23a; }
.text-warning { color: #e6a23c; }
</style>
