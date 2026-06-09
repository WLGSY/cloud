<template>
  <div class="profile">
    <h2>👤 个人资料</h2>

    <el-row :gutter="20">
      <!-- 左侧：头像和统计 -->
      <el-col :span="8">
        <el-card class="avatar-card">
          <div class="avatar-section">
            <el-avatar :size="120" :src="userInfo.avatar || defaultAvatar">
              {{ (userInfo.nickname || userInfo.username || 'U')[0]?.toUpperCase() }}
            </el-avatar>
            <h3>{{ userInfo.nickname || userInfo.username }}</h3>
            <p class="role">
              <el-tag :type="roleTagType" size="small">{{ roleText }}</el-tag>
            </p>
            <p class="join-date" v-if="userInfo.createTime">
              注册时间：{{ formatDate(userInfo.createTime) }}
            </p>
          </div>
        </el-card>

        <!-- 快捷操作 -->
        <el-card class="quick-actions-card">
          <template #header><span>快捷操作</span></template>
          <div class="quick-actions">
            <el-button type="primary" plain @click="router.push('/orders')" style="width:100%;margin-bottom:8px">
              📋 我的订单
            </el-button>
            <el-button type="success" plain @click="router.push('/points')" style="width:100%;margin-bottom:8px">
              ⭐ 我的积分
            </el-button>
            <el-button type="warning" plain @click="passwordDialogVisible = true" style="width:100%">
              🔒 修改密码
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：信息表单 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>基本信息</span>
          </template>

          <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" disabled />
            </el-form-item>

            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="form.nickname" placeholder="请输入昵称" />
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>

            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio value="男">男</el-radio>
                <el-radio value="女">女</el-radio>
                <el-radio value="保密">保密</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="出生日期" prop="birthday">
              <el-date-picker
                v-model="form.birthday"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveProfile" :loading="saving">
                保存修改
              </el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updatePassword" :loading="passwordLoading">
          确认修改
        </el-button>
      </template>
    </el-dialog>

    <!-- 底部按钮 -->
    <div class="profile-actions">
      <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
      <el-button type="primary" plain @click="passwordDialogVisible = true">修改密码</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const saving = ref(false)
const passwordDialogVisible = ref(false)
const passwordFormRef = ref()
const passwordLoading = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 用户信息
const userInfo = computed(() => userStore.userInfo)
const roleText = computed(() => {
  const map = { user: '普通用户', merchant: '商家', admin: '管理员', rider: '骑手' }
  return map[userInfo.value?.role] || '普通用户'
})
const roleTagType = computed(() => {
  const map = { user: 'info', merchant: 'warning', admin: 'danger', rider: 'success' }
  return map[userInfo.value?.role] || 'info'
})

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

// 表单数据
const form = reactive({
  username: '',
  nickname: '',
  phone: '',
  email: '',
  gender: '保密',
  birthday: ''
})

// 表单验证规则
const rules = {
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { pattern: /^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/, message: '请输入正确的邮箱', trigger: 'blur' }
  ]
}

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码验证规则
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 从后端加载用户信息
const fetchUserInfo = async () => {
  try {
    const res = await userApi.getUserInfo()
    if (res.code === 200) {
      // 更新本地 userStore
      userStore.setUser({
        token: userStore.token,
        userInfo: res.data
      })
    }
  } catch (error) {
    console.error('加载用户信息失败', error)
  }
}

// 加载用户信息到表单
const loadUserInfo = () => {
  form.username = userInfo.value?.username || ''
  form.nickname = userInfo.value?.nickname || ''
  form.phone = userInfo.value?.phone || ''
  form.email = userInfo.value?.email || ''
  form.gender = userInfo.value?.gender || '保密'
  form.birthday = userInfo.value?.birthday || ''
}

// 保存资料（对接后端）
const saveProfile = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return

  saving.value = true
  try {
    const res = await userApi.updateUserInfo(form)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      // 后端返回更新后的用户数据，用它更新
      const updatedData = res.data || {}
      userStore.setUser({
        token: userStore.token,
        userInfo: { ...userStore.userInfo, ...form, ...updatedData }
      })
    }
  } catch (error) {
    console.error('保存失败', error)
    ElMessage.error(error.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// 重置表单
const resetForm = () => {
  loadUserInfo()
}

// 修改密码（对接后端）
const updatePassword = async () => {
  const valid = await passwordFormRef.value.validate()
  if (!valid) return

  passwordLoading.value = true
  try {
    const res = await userApi.updatePassword(passwordForm)
    if (res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      passwordDialogVisible.value = false
      // 退出登录
      setTimeout(() => {
        userStore.logout()
        router.push('/login')
      }, 1500)
    }
  } catch (error) {
    console.error('修改密码失败', error)
    ElMessage.error(error.response?.data?.message || '修改密码失败')
  } finally {
    passwordLoading.value = false
  }
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确认退出登录吗？', '提示', {
    confirmButtonText: '确认退出',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}

onMounted(async () => {
  await fetchUserInfo()
  loadUserInfo()
})
</script>

<style scoped>
.profile {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}
.profile h2 {
  margin-bottom: 20px;
}
.avatar-card {
  text-align: center;
}
.avatar-section {
  padding: 20px;
}
.avatar-section h3 {
  margin: 16px 0 8px;
}
.role {
  margin-bottom: 12px;
}
.join-date {
  color: #999;
  font-size: 12px;
  margin-top: 8px;
}
.quick-actions-card {
  margin-top: 20px;
}
.quick-actions {
  padding: 8px 0;
}
.profile-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 8px;
}
</style>
