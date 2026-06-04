<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="logo">🍔 云外卖</h1>
      <p class="subtitle">欢迎回来！请登录您的账号</p>

      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名/手机号"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            @click="handleLogin"
            :loading="loading"
            style="width: 100%"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-link">
        还没有账号？
        <router-link to="/register">立即注册</router-link>
      </div>

      <!-- 【修复】增强演示提示 -->
      <div class="demo-tip">
        <p>📢 演示账号（模拟模式）：</p>
        <p>用户名: <strong>test</strong> / 密码: <strong>123456</strong></p>
        <p>用户名: <strong>admin</strong> / 密码: <strong>admin123</strong></p>
        <p style="color: #ff6b35; margin-top: 8px;">
          💡 当前使用模拟登录，如需对接真实后端，请修改 src/api/user.js 中的 USE_MOCK = false
        </p>
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

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 【修复】增强登录逻辑
const handleLogin = async () => {
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
  } catch (error) {
    console.error('表单验证失败', error)
    return
  }

  loading.value = true
  try {
    console.log('开始登录:', form.username)
    const res = await userApi.login(form)
    console.log('登录响应:', res)

    if (res.code === 200) {
      const token = res.data?.token || res.token
      const userInfo = res.data?.userInfo || res.userInfo || { username: form.username }

      if (token) {
        userStore.setUser({
          token: token,
          userInfo: userInfo
        })
        ElMessage.success('登录成功！')
        router.push('/')
      } else {
        ElMessage.error('登录响应中没有token')
      }
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    const msg = error.response?.data?.message || error.message || '登录失败'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 420px;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
  text-align: center;
}
.logo {
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
}
.subtitle {
  color: #666;
  margin-bottom: 30px;
}
.register-link {
  margin-top: 20px;
  color: #666;
}
.register-link a {
  color: #667eea;
  text-decoration: none;
}
.demo-tip {
  margin-top: 24px;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 8px;
  font-size: 12px;
  color: #999;
}
.demo-tip p {
  margin: 4px 0;
}
.demo-tip strong {
  color: #ff6b35;
}
</style>
