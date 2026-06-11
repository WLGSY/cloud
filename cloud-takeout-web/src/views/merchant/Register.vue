<template>
  <div class="register-page">
    <div class="register-card">
      <div class="card-header">
        <div class="logo-icon">🏪</div>
        <h1>商家入驻</h1>
        <p>开通您的店铺，开始接单吧</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="register-form">
        <el-form-item label="店铺名称" prop="shopName">
          <el-input v-model="form.shopName" placeholder="为您的美食店铺起个名字" size="large" />
        </el-form-item>
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="form.username" placeholder="用于登录商家后台" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="至少6位" size="large" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" size="large" show-password />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="店铺联系电话" size="large" />
        </el-form-item>
        <el-button type="primary" size="large" @click="handleRegister" :loading="loading" class="submit-btn">
          立即入驻
        </el-button>
      </el-form>

      <div class="card-footer">
        <router-link to="/merchant/login">已有账号？立即登录</router-link>
        <span class="sep">|</span>
        <router-link to="/login">返回用户登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { shopApi } from '@/api/shop'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({ shopName: '', username: '', password: '', confirmPassword: '', phone: '' })

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) callback(new Error('两次密码不一致'))
  else callback()
}

const rules = {
  shopName: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: validateConfirm, trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误', trigger: 'blur' }]
}

const handleRegister = async () => {
  try { await formRef.value.validate() } catch { return }
  loading.value = true
  try {
    const res = await shopApi.register({ shopName: form.shopName, username: form.username, password: form.password, phone: form.phone })
    if (res.code === 200) { ElMessage.success('入驻成功！请登录'); router.push('/merchant/login') }
    else { ElMessage.error(res.message || '注册失败') }
  } catch (error) { ElMessage.error(error.response?.data?.message || '注册失败') } finally { loading.value = false }
}
</script>

<style scoped>
.register-page {
  display: flex; justify-content: center; align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #fef9ef 0%, #fff7ed 30%, #fef2f2 60%, #faf5ff 100%);
  padding: var(--space-md);
}
.register-card {
  width: 460px; padding: var(--space-xl);
  background: var(--color-surface); border-radius: var(--radius-xl);
  border: 1px solid var(--color-border-light); box-shadow: var(--shadow-lg);
  animation: scaleIn .4s var(--transition);
}
.card-header { text-align: center; margin-bottom: var(--space-lg); }
.logo-icon {
  font-size: 48px; margin-bottom: var(--space-sm);
  display: inline-block; animation: fadeInUp .6s var(--transition);
}
.card-header h1 { font-size: 26px; font-weight: 800; color: var(--color-text); letter-spacing: -.03em; margin-bottom: 4px; }
.card-header p { font-size: 14px; color: var(--color-text-secondary); }
.submit-btn { width: 100%; margin-top: var(--space-sm); height: 44px; font-size: 15px; }
.card-footer { margin-top: var(--space-md); text-align: center; font-size: 13px; color: var(--color-text-secondary); }
.card-footer a { color: var(--color-primary); text-decoration: none; font-weight: 500; }
.card-footer a:hover { color: var(--color-primary-dark); }
.sep { margin: 0 8px; color: var(--color-border); }
</style>
