<template>
  <div class="admin-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="230px" class="sidebar">
        <div class="logo" @click="$router.push('/admin')">
          <span class="logo-icon">⚙️</span> 管理后台
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
          <span class="greeting">管理员：{{ userStore.userInfo?.username || 'Admin' }}</span>
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
  height: 56px;
  line-height: 56px;
  text-align: center;
  color: var(--color-text);
  font-size: 18px;
  font-weight: 700;
  letter-spacing: -0.02em;
  cursor: pointer;
  border-bottom: 1px solid var(--color-border-light);
}
.logo-icon {
  margin-right: 4px;
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
  font-weight: 500;
  font-size: 15px;
}
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
