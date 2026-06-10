<template>
  <div class="register-page">
    <div class="register-card">
      <div class="card-header">
        <div class="logo-icon">🍃</div>
        <h1>创建账号</h1>
        <p>注册新账号，享受美食</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="register-form">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="至少6位密码" size="large" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" @click="handleRegister" :loading="loading" class="submit-btn">
          注册
        </el-button>
      </el-form>

      <div class="card-footer">
        <router-link to="/login">已有账号？立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api/user'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', phone: '', password: '', confirmPassword: '' })

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) callback(new Error('两次输入密码不一致'))
  else callback()
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  try { await formRef.value.validate() } catch { return }
  loading.value = true
  try {
    const res = await userApi.register(form)
    if (res.code === 200) { ElMessage.success('注册成功，请登录'); router.push('/login') }
  } catch (error) { console.error('注册失败', error) } finally { loading.value = false }
}
</script>

<style scoped>
.register-page {
  display: flex; justify-content: center; align-items: center;
  min-height: 100vh; background: var(--color-bg); padding: var(--space-md);
}
.register-card {
  width: 440px; padding: var(--space-xl);
  background: var(--color-surface); border-radius: var(--radius-xl);
  border: 1px solid var(--color-border-light); box-shadow: var(--shadow-md);
}
.card-header { text-align: center; margin-bottom: var(--space-lg); }
.logo-icon { font-size: 40px; margin-bottom: var(--space-sm); }
.card-header h1 { font-size: 24px; font-weight: 700; color: var(--color-text); letter-spacing: -0.03em; margin-bottom: 4px; }
.card-header p { font-size: 14px; color: var(--color-text-secondary); }
.submit-btn { width: 100%; margin-top: var(--space-sm); }
.card-footer { margin-top: var(--space-md); text-align: center; }
.card-footer a { color: var(--color-primary); text-decoration: none; font-size: 14px; }
</style>
