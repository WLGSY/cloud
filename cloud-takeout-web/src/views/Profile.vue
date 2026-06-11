<template>
  <div class="profile-page">
    <h2 class="page-title">👤 个人资料</h2>

    <el-row :gutter="20">
      <el-col :span="8">
        <div class="profile-sidebar">
          <div class="avatar-card">
            <div class="avatar-wrapper" @click="showAvatarPicker = true">
              <el-avatar :size="100" :src="avatarSrc" :style="{ background: avatarBg, fontSize: '44px' }">
                {{ avatarFallback }}
              </el-avatar>
              <div class="avatar-overlay">📷</div>
            </div>
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

    <!-- 头像选择弹窗 -->
    <el-dialog v-model="showAvatarPicker" title="选择头像" width="440px">
      <el-tabs v-model="avatarTab">
        <el-tab-pane label="预设头像" name="preset">
          <div class="preset-avatars">
            <div v-for="(av, i) in presetAvatars" :key="i" class="preset-item"
              :class="{ selected: selectedPreset === av }" @click="selectedPreset = av">
              <span class="preset-emoji">{{ av }}</span>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="图片链接" name="url">
          <el-input v-model="customAvatarUrl" placeholder="输入头像图片URL..." />
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="showAvatarPicker = false">取消</el-button>
        <el-button type="primary" @click="confirmAvatar" :loading="avatarUpdating">确认</el-button>
      </template>
    </el-dialog>
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

// 头像相关
const showAvatarPicker = ref(false)
const avatarTab = ref('preset')
const avatarUpdating = ref(false)
const customAvatarUrl = ref('')
const selectedPreset = ref(null)
const presetAvatars = ['🐱', '🐶', '🐼', '🐨', '🦊', '🐰', '🐸', '🐵', '🦁', '🐯', '🐮', '🐷', '🐭', '🐹', '🐔', '🐧', '🐦', '🦄', '🐙', '🦀', '🌞', '⭐', '🌈', '🎈', '🎯', '🍕', '🍔', '🌮', '🍣', '🎸', '🚀', '💎']

// emoji预设映射到颜色
const emojiColors = ['#fef3c7','#dbeafe','#d1fae5','#fce7f3','#e0e7ff','#fef2f2','#f0fdf4','#faf5ff','#fff7ed','#ecfeff']

const confirmAvatar = async () => {
  avatarUpdating.value = true
  try {
    let url
    if (avatarTab.value === 'preset' && selectedPreset.value) {
      // 存简单标识字符串，由 el-avatar 渲染 emoji
      url = selectedPreset.value
    } else if (avatarTab.value === 'url' && customAvatarUrl.value) {
      url = customAvatarUrl.value
    }
    if (!url) { ElMessage.warning('请选择或输入头像'); avatarUpdating.value = false; return }
    const uid = userInfo.value?.id || JSON.parse(localStorage.getItem('userInfo') || '{}').id
    if (!uid) { ElMessage.error('登录信息丢失，请重新登录'); avatarUpdating.value = false; return }
    const res = await userApi.uploadAvatar(url, uid)
    if (res.code === 200) {
      ElMessage.success('头像更新成功')
      userStore.setUser({ token: userStore.token, userInfo: { ...userStore.userInfo, avatar: url } })
      showAvatarPicker.value = false
    } else { ElMessage.error(res.message || '更新失败') }
  } catch (e) { ElMessage.error('更新失败: ' + (e.message || '网络错误')) } finally { avatarUpdating.value = false }
}

const userInfo = computed(() => userStore.userInfo)
// 头像处理：如果是emoji则用背景色显示，如果是URL则直接显示
const isEmoji = (s) => s && /^\p{Emoji}/u.test(s) && s.length <= 4
const avatarSrc = computed(() => isEmoji(userInfo.value?.avatar) ? '' : (userInfo.value?.avatar || ''))
const avatarFallback = computed(() => {
  const av = userInfo.value?.avatar
  if (isEmoji(av)) return av
  return (userInfo.value?.nickname || userInfo.value?.username || 'U')[0]?.toUpperCase()
})
const avatarBg = computed(() => {
  if (isEmoji(userInfo.value?.avatar)) return emojiColors[0]
  return undefined
})
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
.profile-page { max-width: 960px; margin: 0 auto; animation: fadeInUp .4s var(--transition); }
.page-title { font-size: 24px; font-weight: 800; color: var(--color-text); margin-bottom: var(--space-md); letter-spacing: -.02em; }
.profile-sidebar { display: flex; flex-direction: column; gap: var(--space-md); }
.avatar-card {
  text-align: center; padding: var(--space-xl) var(--space-lg);
  background: linear-gradient(135deg, var(--color-primary-bg), #f8f7ff);
  border-radius: var(--radius-lg); border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
}
.avatar-card h3 { margin: var(--space-sm) 0 4px; font-size: 18px; font-weight: 700; color: var(--color-text); }
.join-date { font-size: 12px; color: var(--color-text-placeholder); margin-top: var(--space-sm); }
.quick-actions { display: flex; flex-direction: column; gap: 8px; }
.action-btn {
  width: 100%; padding: 14px 16px; border-radius: var(--radius-sm);
  border: 1px solid var(--color-border-light); background: var(--color-surface);
  font-size: 14px; font-weight: 500; cursor: pointer; text-align: left;
  transition: all var(--transition); color: var(--color-text);
}
.action-btn:hover {
  border-color: var(--color-primary); background: var(--color-primary-bg);
  color: var(--color-primary); transform: translateX(4px);
}
.info-card {
  background: var(--color-surface); border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light); padding: var(--space-lg);
  box-shadow: var(--shadow-sm);
}
.card-title { font-size: 18px; font-weight: 700; color: var(--color-text); margin-bottom: var(--space-md); }
.form-actions { display: flex; gap: var(--space-sm); padding-top: var(--space-sm); }
.logout-bar { text-align: center; margin-top: var(--space-lg); padding: var(--space-md) 0; }
.avatar-wrapper { position: relative; display: inline-block; cursor: pointer; border-radius: 50%; }
.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}
.avatar-overlay {
  position: absolute; inset: 0; border-radius: 50%;
  background: rgba(0,0,0,.35); display: flex; align-items: center; justify-content: center;
  font-size: 28px; color: #fff; opacity: 0; transition: opacity .25s;
}
.preset-avatars { display: flex; flex-wrap: wrap; gap: 8px; }
.preset-item {
  width: 56px; height: 56px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; cursor: pointer;
  background: var(--color-bg); border: 2px solid transparent;
  transition: all .2s; font-size: 28px;
}
.preset-item:hover { border-color: var(--color-primary); background: var(--color-primary-bg); }
.preset-item.selected { border-color: var(--color-primary); background: var(--color-primary-bg); }
</style>
