<template>
  <div class="login-page">
    <div class="login-card">
      <div class="card-header">
        <div class="logo-icon">🍃</div>
        <h1>云外卖</h1>
        <p>欢迎回来，请登录您的账号</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock"
            size="large" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" size="large" @click="handleLogin" :loading="loading" class="submit-btn">
          登录
        </el-button>
      </el-form>

      <div class="card-footer">
        <router-link to="/register">还没有账号？立即注册</router-link>
        <div class="role-links">
          <router-link to="/merchant/login">商家登录</router-link>
          <span>·</span>
          <router-link to="/admin/login">管理员登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  try { await formRef.value.validate() } catch { return }
  loading.value = true
  try {
    const res = await userApi.login(form)
    if (res.code === 200) {
      const token = res.data?.token
      const userId = res.data?.userId
      const username = res.data?.username
      const role = res.data?.role || 'user'
      if (token) {
        userStore.setUser({ token, userInfo: { id: userId, username: username || form.username, role } })
        ElMessage.success('登录成功！')
        if (role === 'merchant') router.push('/merchant')
        else if (role === 'admin') router.push('/admin')
        else router.push('/')
      } else {
        ElMessage.error('登录响应中没有token')
      }
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '登录失败')
  } finally { loading.value = false }
}
</script>

<style scoped>
.login-page {
  display: flex; justify-content: center; align-items: center;
  min-height: 100vh; background: var(--color-bg);
}
.login-card {
  width: 400px;
  padding: var(--space-xl);
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-md);
}
.card-header {
  text-align: center; margin-bottom: var(--space-lg);
}
.logo-icon { font-size: 40px; margin-bottom: var(--space-sm); }
.card-header h1 {
  font-size: 24px; font-weight: 700; color: var(--color-text);
  letter-spacing: -0.03em; margin-bottom: 4px;
}
.card-header p { font-size: 14px; color: var(--color-text-secondary); }
.submit-btn { width: 100%; margin-top: var(--space-xs); }
.card-footer {
  margin-top: var(--space-md); text-align: center;
  display: flex; flex-direction: column; gap: var(--space-sm);
}
.card-footer a { color: var(--color-primary); text-decoration: none; font-size: 14px; }
.card-footer a:hover { color: var(--color-primary-light); }
.role-links { display: flex; justify-content: center; gap: var(--space-sm); }
.role-links a { color: var(--color-text-secondary); font-size: 13px; }
.role-links span { color: var(--color-border); }
</style>
