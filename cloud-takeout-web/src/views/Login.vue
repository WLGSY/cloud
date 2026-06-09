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

      <div class="role-links">
        <router-link to="/merchant/login">商家登录</router-link>
        <span>|</span>
        <router-link to="/admin/login">管理员登录</router-link>
      </div>

      <div class="demo-tip">
        <p>📢 提示：请先注册账号再登录</p>
        <p style="color: #999; margin-top: 8px;">
          💡 注册需要填写用户名、手机号和密码
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
      const token = res.data?.token
      const userId = res.data?.userId
      const username = res.data?.username
      const role = res.data?.role || 'user'

      if (token) {
        userStore.setUser({
          token: token,
          userInfo: {
            id: userId,
            username: username || form.username,
            role: role
          }
        })
        ElMessage.success('登录成功！')
        // 根据角色跳转不同首页
        if (role === 'merchant') {
          router.push('/merchant')
        } else if (role === 'admin') {
          router.push('/admin')
        } else {
          router.push('/')
        }
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
.role-links {
  margin-top: 16px;
  font-size: 12px;
  color: #ccc;
}
.role-links a {
  color: #999;
  text-decoration: none;
  margin: 0 8px;
}
.role-links a:hover {
  color: #667eea;
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
