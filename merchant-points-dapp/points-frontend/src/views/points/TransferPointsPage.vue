<template>
  <div class="transfer-points">
    <el-card>
      <template #header><span>积分转账</span></template>
      <el-row :gutter="20" style="margin-bottom: 20px">
        <el-col :span="8">
          <el-card shadow="hover">
            <div class="balance-card">
              <div class="balance-value">{{ balance }}</div>
              <div class="balance-label">当前余额</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" style="max-width: 600px">
        <el-form-item label="收款商户" prop="toMerchantId">
          <el-select v-model="form.toMerchantId" filterable remote :remote-method="searchMerchant" placeholder="搜索收款商户" style="width: 100%">
            <el-option v-for="m in merchantOptions" :key="m.id" :label="m.merchantName" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="转账金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0.01" :max="balance || 999999999" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="可选" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">确认转账</el-button>
          <el-button @click="formRef?.resetFields()">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { transferPoints, getBalance } from '@/api/points'
import { getMerchantList } from '@/api/merchant'
import { useUserStore } from '@/stores/useUserStore'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'

const formRef = ref<FormInstance>()
const loading = ref(false)
const balance = ref(0)
const merchantOptions = ref<any[]>([])
const userStore = useUserStore()
let unmounted = false

onUnmounted(() => { unmounted = true })

const form = reactive({ toMerchantId: null as number | null, amount: 0, remark: '' })
const rules = {
  toMerchantId: [{ required: true, message: '请选择收款商户', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }]
}

const loadBalance = async () => {
  if (unmounted) return
  if (userStore.userInfo?.merchantId) {
    const res: any = await getBalance(userStore.userInfo.merchantId)
    if (!unmounted && res.code === 200) balance.value = res.data.balance
  }
}

const searchMerchant = async (query: string) => {
  if (unmounted || !query) return
  const res: any = await getMerchantList({ keyword: query, auditStatus: 1, pageNum: 1, pageSize: 20 })
  if (!unmounted && res.code === 200) merchantOptions.value = res.data.list.filter((m: any) => m.id !== userStore.userInfo?.merchantId)
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  await ElMessageBox.confirm(`确认转账 ${form.amount} 积分？`, '二次确认')
  loading.value = true
  try {
    const res: any = await transferPoints(form)
    if (res.code === 200) {
      ElMessage.success('转账成功')
      formRef.value?.resetFields()
      loadBalance()
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '转账失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadBalance)
</script>

<style scoped>
.balance-card { text-align: center; }
.balance-value { font-size: 32px; font-weight: bold; color: #409eff; }
.balance-label { color: #909399; margin-top: 8px; }
</style>
