<template>
  <div class="profile-page">
    <h2 class="page-title">👤 个人资料</h2>

    <el-row :gutter="20">
      <el-col :span="8">
        <div class="profile-sidebar">
          <div class="avatar-card">
            <el-avatar :size="100" :src="userInfo.avatar || defaultAvatar">
              {{ (userInfo.nickname || userInfo.username || 'U')[0]?.toUpperCase() }}
            </el-avatar>
            <h3>{{ userInfo.nickname || userInfo.username }}</h3>
            <el-tag :type="roleTagType" size="small" effect="plain">{{ roleText }}</el-tag>
            <p class="join-date" v-if="userInfo.createTime">注册于 {{ formatDate(userInfo.createTime) }}</p>
          </div>

          <div class="quick-actions">
            <button class="action-btn" @click="$router.push('/orders')">📋 我的订单</button>
            <button class="action-btn" @click="$router.push('/points')">⭐ 我的积分</button>
            <button class="action-btn" @click="passwordDialogVisible = true">🔒 修改密码</button>
          </div>
        </div>
      </el-col>

      <el-col :span="16">
        <div class="info-card">
          <h3 class="card-title">基本信息</h3>
          <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="用户名">
                  <el-input v-model="form.username" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="昵称">
                  <el-input v-model="form.nickname" placeholder="请输入昵称" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="form.phone" placeholder="请输入手机号" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="form.email" placeholder="请输入邮箱" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio-button value="男">男</el-radio-button>
                <el-radio-button value="女">女</el-radio-button>
                <el-radio-button value="保密">保密</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="出生日期">
              <el-date-picker v-model="form.birthday" type="date" placeholder="选择日期"
                format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
            </el-form-item>
            <div class="form-actions">
              <el-button type="primary" @click="saveProfile" :loading="saving">保存修改</el-button>
              <el-button @click="resetForm">重置</el-button>
            </div>
          </el-form>
        </div>
      </el-col>
    </el-row>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-position="top">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="至少6位" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="再次输入" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updatePassword" :loading="passwordLoading">确认修改</el-button>
      </template>
    </el-dialog>

    <div class="logout-bar">
      <el-button @click="handleLogout" text type="danger">退出登录</el-button>
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

const userInfo = computed(() => userStore.userInfo)
const roleText = computed(() => ({ user: '普通用户', merchant: '商家', admin: '管理员', rider: '骑手' })[userInfo.value?.role] || '普通用户')
const roleTagType = computed(() => ({ user: 'info', merchant: 'warning', admin: 'danger', rider: 'success' })[userInfo.value?.role] || 'info')
const formatDate = (d) => d ? new Date(d).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' }) : ''

const form = reactive({ username: '', nickname: '', phone: '', email: '', gender: '保密', birthday: '' })
const rules = {
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误', trigger: 'blur' }],
  email: [{ pattern: /^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/, message: '邮箱格式错误', trigger: 'blur' }]
}

const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const validatePwd = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) callback(new Error('两次密码不一致'))
  else callback()
}
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '至少6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: validatePwd, trigger: 'blur' }]
}

const fetchUserInfo = async () => {
  try { const res = await userApi.getUserInfo(); if (res.code === 200) userStore.setUser({ token: userStore.token, userInfo: res.data }) } catch {}
}
const loadUserInfo = () => {
  form.username = userInfo.value?.username || ''; form.nickname = userInfo.value?.nickname || ''
  form.phone = userInfo.value?.phone || ''; form.email = userInfo.value?.email || ''
  form.gender = userInfo.value?.gender || '保密'; form.birthday = userInfo.value?.birthday || ''
}

const saveProfile = async () => {
  try { await formRef.value.validate() } catch { return }
  saving.value = true
  try {
    const res = await userApi.updateUserInfo(form)
    if (res.code === 200) { ElMessage.success('保存成功'); userStore.setUser({ token: userStore.token, userInfo: { ...userStore.userInfo, ...form, ...(res.data || {}) } }) }
  } catch { ElMessage.error('保存失败') } finally { saving.value = false }
}
const resetForm = () => loadUserInfo()

const updatePassword = async () => {
  try { await passwordFormRef.value.validate() } catch { return }
  passwordLoading.value = true
  try {
    const res = await userApi.updatePassword(passwordForm)
    if (res.code === 200) { ElMessage.success('密码修改成功，请重新登录'); passwordDialogVisible.value = false; setTimeout(() => { userStore.logout(); router.push('/login') }, 1500) }
  } catch { ElMessage.error('修改失败') } finally { passwordLoading.value = false }
}

const handleLogout = () => {
  ElMessageBox.confirm('确认退出登录吗？', '提示', { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' })
    .then(() => { userStore.logout(); ElMessage.success('已退出'); router.push('/login') }).catch(() => {})
}

onMounted(async () => { await fetchUserInfo(); loadUserInfo() })
</script>

<style scoped>
.profile-page { max-width: 960px; margin: 0 auto; }
.page-title { font-size: 22px; font-weight: 700; color: var(--color-text); margin-bottom: var(--space-md); }
.profile-sidebar { display: flex; flex-direction: column; gap: var(--space-md); }
.avatar-card {
  text-align: center; padding: var(--space-lg);
  background: var(--color-surface); border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
}
.avatar-card h3 { margin: var(--space-sm) 0 6px; font-size: 18px; color: var(--color-text); }
.join-date { font-size: 12px; color: var(--color-text-placeholder); margin-top: var(--space-sm); }
.quick-actions { display: flex; flex-direction: column; gap: 6px; }
.action-btn {
  width: 100%; padding: 12px; border-radius: var(--radius-sm);
  border: 1px solid var(--color-border-light); background: var(--color-surface);
  font-size: 14px; cursor: pointer; text-align: left; transition: all var(--transition);
}
.action-btn:hover { border-color: var(--color-primary); background: var(--color-primary-bg); }
.info-card {
  background: var(--color-surface); border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light); padding: var(--space-md);
}
.card-title { font-size: 17px; font-weight: 600; color: var(--color-text); margin-bottom: var(--space-md); }
.form-actions { display: flex; gap: var(--space-sm); padding-top: var(--space-sm); }
.logout-bar { text-align: center; margin-top: var(--space-md); }
</style>
