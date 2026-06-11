<template>
  <div class="admin-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="230px" class="sidebar">
        <div class="logo" @click="$router.push('/admin')">
          <span class="logo-mark">⚙️</span>
          <span class="logo-text">管理后台</span>
        </div>
        <el-menu :default-active="$route.path" router>
          <el-menu-item index="/admin">
            <el-icon><Odometer /></el-icon><span>数据总览</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon><span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/merchants">
            <el-icon><OfficeBuilding /></el-icon><span>商家审核</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <!-- 顶栏 -->
        <el-header class="topbar">
          <span class="greeting">
            <img v-if="isImg(userStore.userInfo?.avatar)" :src="userStore.userInfo.avatar" class="top-avatar" />
            <span v-else-if="userStore.userInfo?.avatar" class="top-avatar-emoji">{{ userStore.userInfo.avatar }}</span>
            <span v-else class="top-avatar-dot">{{ (userStore.userInfo?.username||'A')[0].toUpperCase() }}</span>
            管理员：{{ userStore.userInfo?.nickname || userStore.userInfo?.username || 'Admin' }}
          </span>
          <div class="topbar-actions">
            <button class="btn-text" @click="$router.push('/')">预览用户端</button>
            <button class="btn-text danger" @click="handleLogout">退出登录</button>
          </div>
        </el-header>
        <!-- 内容 -->
        <el-main><router-view /></el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Odometer, User, OfficeBuilding } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const isImg = (url) => url && (url.startsWith('http') || url.startsWith('/') || url.startsWith('data:'))

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/admin/login')
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background: var(--color-bg);
}

/* 侧边栏 */
.sidebar {
  background: var(--color-surface) !important;
  border-right: 1px solid var(--color-border-light);
  min-height: 100vh;
}
.logo {
  height: 56px; line-height: 56px; text-align: center;
  display: flex; align-items: center; justify-content: center; gap: 6px;
  color: var(--color-text); font-size: 18px; font-weight: 700;
  letter-spacing: -.02em; cursor: pointer;
  border-bottom: 1px solid var(--color-border-light);
}
.logo-mark { font-size: 22px; }
.logo-text {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 菜单 */
.el-menu {
  border-right: none !important;
  background: transparent !important;
  padding: var(--space-sm);
}
.el-menu-item {
  border-radius: var(--radius-sm) !important;
  margin-bottom: 4px;
  color: var(--color-text-secondary) !important;
  height: 44px !important;
  line-height: 44px !important;
  font-size: 14px;
  transition: all var(--transition);
}
.el-menu-item:hover {
  background: var(--color-bg-alt) !important;
  color: var(--color-text) !important;
}
.el-menu-item.is-active {
  background: var(--color-primary-bg) !important;
  color: var(--color-primary) !important;
  font-weight: 600;
}

/* 顶栏 */
.topbar {
  background: var(--color-surface) !important;
  border-bottom: 1px solid var(--color-border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 var(--space-md) !important;
  height: 56px !important;
}
.greeting {
  color: var(--color-text);
  font-weight: 500; font-size: 15px;
  display: flex; align-items: center; gap: 8px;
}
.top-avatar { width: 32px; height: 32px; border-radius: 50%; object-fit: cover; }
.top-avatar-emoji { width: 32px; height: 32px; border-radius: 50%; background: var(--color-bg-alt); display: inline-flex; align-items: center; justify-content: center; font-size: 16px; flex-shrink: 0; }
.top-avatar-dot { width: 32px; height: 32px; border-radius: 50%; background: var(--color-primary-bg); color: var(--color-primary); display: inline-flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 700; flex-shrink: 0; }
.topbar-actions {
  display: flex;
  gap: 8px;
}
.btn-text {
  padding: 6px 14px;
  border-radius: var(--radius-full);
  border: 1px solid var(--color-border);
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition);
}
.btn-text:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: var(--color-primary-bg);
}
.btn-text.danger:hover {
  border-color: var(--color-danger);
  color: var(--color-danger);
  background: #fef2f2;
}

/* 主内容 */
.el-main {
  padding: var(--space-md) !important;
  background: var(--color-bg);
}
</style>
