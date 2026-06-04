<template>
  <div class="user-layout">
    <el-container>
      <el-header>
        <div class="header-content">
          <div class="logo" @click="goHome">
            🍔 云外卖
          </div>

          <div class="header-nav">
            <el-menu mode="horizontal" router :default-active="$route.path">
              <el-menu-item index="/">首页</el-menu-item>
              <el-menu-item index="/orders">我的订单</el-menu-item>
              <el-menu-item index="/points">我的积分</el-menu-item>
            </el-menu>
          </div>

          <div class="header-actions">
            <el-badge :value="cartStore.totalCount" :hidden="cartStore.totalCount === 0">
              <el-button circle @click="goCart">
                <el-icon><ShoppingCart /></el-icon>
              </el-button>
            </el-badge>

            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" :src="userStore.userInfo?.avatar || defaultAvatar" />
                <span>{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '未登录' }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item command="points">我的积分</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <el-main>
        <router-view />
      </el-main>

      <el-footer>
        <div class="footer-content">
          <p>&copy; 2026 云外卖平台 | 美食送到家</p>
        </div>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const goHome = () => router.push('/')
const goCart = () => router.push('/cart')

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'orders') {
    router.push('/orders')
  } else if (command === 'points') {
    router.push('/points')
  }
}
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
}
.el-header {
  background: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  padding: 0 20px;
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
}
.logo {
  font-size: 24px;
  font-weight: bold;
  cursor: pointer;
  color: #ff6b35;
  white-space: nowrap;
}
.header-nav {
  flex: 1;
  margin-left: 40px;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 24px;
  transition: background 0.3s;
}
.user-info:hover {
  background: #f5f5f5;
}
.el-footer {
  text-align: center;
  background: #f5f5f5;
  color: #999;
  padding: 20px;
}
</style>
