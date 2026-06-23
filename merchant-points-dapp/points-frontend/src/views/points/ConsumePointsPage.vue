<template>
  <div class="consume-points">
    <el-card>
      <template #header><span>积分消费</span></template>
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
        <el-form-item label="消费者手机号" prop="consumerPhone">
          <el-input v-model="form.consumerPhone" placeholder="请输入消费者手机号" />
        </el-form-item>
        <el-form-item label="消费金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0.01" :max="balance || 999999999" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="消费说明">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">确认消费</el-button>
          <el-button @click="formRef?.resetFields()">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { consumePoints, getBalance } from '@/api/points'
import { useUserStore } from '@/stores/useUserStore'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'

const formRef = ref<FormInstance>()
const loading = ref(false)
const balance = ref(0)
const userStore = useUserStore()
let unmounted = false

onUnmounted(() => { unmounted = true })

const form = reactive({ consumerPhone: '', amount: 0, remark: '' })
const rules = {
  consumerPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }]
}

const loadBalance = async () => {
  if (unmounted) return
  if (userStore.userInfo?.merchantId) {
    const res: any = await getBalance(userStore.userInfo.merchantId)
    if (!unmounted && res.code === 200) balance.value = res.data.balance
  }
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  await ElMessageBox.confirm(`确认消费 ${form.amount} 积分？`, '二次确认')
  loading.value = true
  try {
    const res: any = await consumePoints(form)
    if (res.code === 200) {
      ElMessage.success(`消费成功，手机号已脱敏: ${res.data.consumerPhone}`)
      formRef.value?.resetFields()
      loadBalance()
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '消费失败')
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
