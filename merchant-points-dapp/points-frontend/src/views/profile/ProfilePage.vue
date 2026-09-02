<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <div class="user-card">
            <el-avatar :size="80" icon="User" />
            <h3>{{ profile.username }}</h3>
            <p>{{ profile.role === 0 ? '管理员' : '商户' }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="个人信息" name="info">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="用户名">{{ profile.username }}</el-descriptions-item>
                <el-descriptions-item label="真实姓名">{{ profile.realName }}</el-descriptions-item>
                <el-descriptions-item label="手机号">{{ profile.phone }}</el-descriptions-item>
                <el-descriptions-item label="邮箱">{{ profile.email }}</el-descriptions-item>
                <el-descriptions-item label="角色">{{ profile.role === 0 ? '管理员' : '商户' }}</el-descriptions-item>
                <el-descriptions-item label="状态">{{ profile.status === 1 ? '正常' : '待审核' }}</el-descriptions-item>
              </el-descriptions>
            </el-tab-pane>
            <el-tab-pane label="修改密码" name="password">
              <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" style="max-width: 400px">
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input v-model="pwdForm.oldPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="pwdForm.newPassword" type="password" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getUserProfile, updatePassword } from '@/api/user'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'

const activeTab = ref('info')
const profile = ref<any>({})
const pwdFormRef = ref<FormInstance>()
const pwdForm = reactive({ oldPassword: '', newPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, max: 20, message: '长度6-20位', trigger: 'blur' }]
}

const handleChangePassword = async () => {
  await pwdFormRef.value?.validate()
  try {
    const res: any = await updatePassword(pwdForm)
    if (res.code === 200) {
      ElMessage.success('密码修改成功')
      pwdFormRef.value?.resetFields()
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '修改失败')
  }
}

onMounted(async () => {
  const res: any = await getUserProfile()
  if (res.code === 200) profile.value = res.data
})
</script>

<style scoped>
.user-card { text-align: center; padding: 20px 0; }
.user-card h3 { margin: 15px 0 5px; }
.user-card p { color: #909399; }
</style>
