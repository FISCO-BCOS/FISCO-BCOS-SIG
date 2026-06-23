<template>
  <div class="issue-points">
    <el-card>
      <template #header><span>积分发行</span></template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" style="max-width: 600px">
        <el-form-item label="目标商户" prop="merchantId">
          <el-select v-model="form.merchantId" filterable remote :remote-method="searchMerchant" placeholder="搜索商户" style="width: 100%">
            <el-option v-for="m in merchantOptions" :key="m.id" :label="m.merchantName" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="发行金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="发行事由" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请输入发行事由" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">确认发行</el-button>
          <el-button @click="formRef?.resetFields()">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onUnmounted } from 'vue'
import { issuePoints } from '@/api/points'
import { getMerchantList } from '@/api/merchant'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'

const formRef = ref<FormInstance>()
const loading = ref(false)
const merchantOptions = ref<any[]>([])
let unmounted = false

onUnmounted(() => { unmounted = true })

const form = reactive({ merchantId: null as number | null, amount: 100, reason: '' })
const rules = {
  merchantId: [{ required: true, message: '请选择商户', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入事由', trigger: 'blur' }]
}

const searchMerchant = async (query: string) => {
  if (unmounted || !query) return
  const res: any = await getMerchantList({ keyword: query, auditStatus: 1, pageNum: 1, pageSize: 20 })
  if (!unmounted && res.code === 200) merchantOptions.value = res.data.list
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  await ElMessageBox.confirm(`确认为商户发行 ${form.amount} 积分？`, '二次确认')
  loading.value = true
  try {
    const res: any = await issuePoints(form)
    if (res.code === 200) {
      ElMessage.success('发行成功')
      formRef.value?.resetFields()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '发行失败')
  } finally {
    loading.value = false
  }
}
</script>
