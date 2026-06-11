<template>
  <div class="user-layout">
    <!-- 顶部导航 — 毛玻璃效果 -->
    <header class="topbar">
      <div class="topbar-inner">
        <router-link to="/" class="logo">
          <span class="logo-mark">🍃</span>
          <span class="logo-text">云外卖</span>
        </router-link>
        <nav class="nav-links">
          <router-link to="/" class="nav-item" :class="{ active: $route.path === '/' }">首页</router-link>
          <router-link to="/orders" class="nav-item" :class="{ active: $route.path === '/orders' }">订单</router-link>
          <router-link to="/points" class="nav-item" :class="{ active: $route.path === '/points' }">积分</router-link>
        </nav>
        <div class="user-area">
          <router-link to="/cart" class="cart-btn">
            🛒
            <span v-if="cartStore.totalCount > 0" class="cart-badge">{{ cartStore.totalCount }}</span>
          </router-link>
          <template v-if="userStore.isLoggedIn()">
            <router-link to="/profile" class="user-link">
              <img v-if="isImageUrl(userStore.userInfo?.avatar)" :src="userStore.userInfo.avatar" class="avatar-img" />
              <span v-else-if="userStore.userInfo?.avatar" class="avatar-emoji">{{ userStore.userInfo.avatar }}</span>
              <span v-else class="avatar-dot">{{ (userStore.userInfo?.username || 'U')[0].toUpperCase() }}</span>
              <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
            </router-link>
            <button class="btn-text" @click="handleLogout">退出</button>
          </template>
          <template v-else>
            <router-link to="/login" class="btn-primary-sm">登录</router-link>
          </template>
        </div>
      </div>
    </header>

    <!-- 内容区 -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const isImageUrl = (url) => url && (url.startsWith('http') || url.startsWith('/') || url.startsWith('data:'))

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  background: var(--color-bg);
}

/* 顶部导航 — 毛玻璃 */
.topbar {
  position: sticky; top: 0; z-index: 100;
  background: rgba(255,255,255,.78);
  backdrop-filter: blur(24px) saturate(1.2);
  -webkit-backdrop-filter: blur(24px) saturate(1.2);
  border-bottom: 1px solid var(--color-border-light);
  box-shadow: 0 1px 4px rgba(0,0,0,.02);
}
.topbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--space-md);
  height: 56px;
  display: flex;
  align-items: center;
  gap: var(--space-md);
}
.logo {
  font-size: 20px; font-weight: 700;
  color: var(--color-text); text-decoration: none;
  letter-spacing: -.02em; flex-shrink: 0;
  display: flex; align-items: center; gap: 6px;
}
.logo-mark { font-size: 24px; }
.logo-text {
  background: linear-gradient(135deg, var(--color-primary), #a78bfa);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  background-clip: text;
}
.nav-links {
  display: flex;
  gap: 4px;
}
.nav-item {
  padding: 6px 16px;
  border-radius: var(--radius-full);
  color: var(--color-text-secondary);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all var(--transition);
}
.nav-item:hover,
.nav-item.active {
  color: var(--color-primary);
  background: var(--color-primary-bg);
}
.user-area {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}
.cart-btn {
  position: relative;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  text-decoration: none;
  transition: all var(--transition);
}
.cart-btn:hover {
  background: var(--color-bg-alt);
}
.cart-badge {
  position: absolute;
  top: -2px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  border-radius: var(--radius-full);
  background: var(--color-primary);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}
.user-link {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: var(--color-text);
  font-size: 14px;
  padding: 4px 8px;
  border-radius: var(--radius-full);
  transition: all var(--transition);
}
.user-link:hover {
  background: var(--color-bg-alt);
}
.avatar-img {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--color-border-light);
}
.avatar-emoji {
  width: 32px; height: 32px; border-radius: 50%;
  background: var(--color-bg-alt); display: inline-flex;
  align-items: center; justify-content: center; font-size: 18px;
  flex-shrink: 0;
}
.avatar-dot {
  width: 30px;
  height: 30px;
  border-radius: var(--radius-full);
  background: var(--color-primary-bg);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 13px;
}
.username {
  font-weight: 500;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.btn-text {
  padding: 6px 12px;
  border-radius: var(--radius-full);
  border: none;
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition);
}
.btn-text:hover {
  color: var(--color-danger);
  background: #fef2f2;
}
.btn-primary-sm {
  padding: 8px 18px;
  border-radius: var(--radius-full);
  background: var(--color-primary);
  color: #fff;
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  transition: all var(--transition);
}
.btn-primary-sm:hover {
  background: var(--color-primary-light);
  box-shadow: var(--shadow-sm);
}

/* 内容区 */
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-md);
  min-height: calc(100vh - 56px);
}

@media (max-width: 768px) {
  .nav-links { display: none; }
  .username { display: none; }
}
</style>
