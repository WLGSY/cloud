import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  // ========== 公共路由（无需登录） ==========
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/merchant/login',
    name: 'MerchantLogin',
    component: () => import('@/views/merchant/MerchantLogin.vue')
  },
  {
    path: '/merchant/register',
    name: 'MerchantRegister',
    component: () => import('@/views/merchant/Register.vue')
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/AdminLogin.vue')
  },

  // ========== 用户端路由 ==========
  {
    path: '/',
    component: () => import('@/components/Layout/UserLayout.vue'),
    meta: { requiresAuth: true, role: 'user' },
    children: [
      { path: '', name: 'Home', component: () => import('@/views/Home.vue') },
      { path: 'cart', name: 'Cart', component: () => import('@/views/Cart.vue') },
      { path: 'orders', name: 'Orders', component: () => import('@/views/Orders.vue') },
      { path: 'points', name: 'Points', component: () => import('@/views/Points.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue') }
    ]
  },

  // ========== 商家端路由 ==========
  {
    path: '/merchant',
    component: () => import('@/components/Layout/MerchantLayout.vue'),
    meta: { requiresAuth: true, role: 'merchant' },
    children: [
      { path: '', name: 'MerchantDashboard', component: () => import('@/views/merchant/Dashboard.vue') },
      { path: 'shop', name: 'MerchantShop', component: () => import('@/views/merchant/Shop.vue') },
      { path: 'dishes', name: 'MerchantDishes', component: () => import('@/views/merchant/Dishes.vue') },
      { path: 'orders', name: 'MerchantOrders', component: () => import('@/views/merchant/Orders.vue') }
    ]
  },

  // ========== 管理后台路由 ==========
  {
    path: '/admin',
    component: () => import('@/components/Layout/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'admin' },
    children: [
      { path: '', name: 'AdminDashboard', component: () => import('@/views/admin/Dashboard.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/Users.vue') },
      { path: 'merchants', name: 'AdminMerchants', component: () => import('@/views/admin/Merchants.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：检查登录状态和角色权限
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const userRole = userInfo.role || 'user'

  // 需要登录的页面
  if (to.meta.requiresAuth) {
    if (!token) {
      // 未登录，跳转到对应角色的登录页
      const role = to.meta.role
      if (role === 'merchant') {
        next('/merchant/login')
      } else if (role === 'admin') {
        next('/admin/login')
      } else {
        next('/login')
      }
      return
    }

    // 检查角色权限
    const requiredRole = to.meta.role
    if (requiredRole && userRole !== requiredRole) {
      // 角色不匹配，跳转到该角色对应的首页
      if (userRole === 'merchant') {
        next('/merchant')
      } else if (userRole === 'admin') {
        next('/admin')
      } else {
        next('/')
      }
      return
    }
  }

  // 已登录用户访问登录页，自动跳转
  if (token && (to.path === '/login' || to.path === '/merchant/login' || to.path === '/admin/login')) {
    if (userRole === 'merchant') {
      next('/merchant')
    } else if (userRole === 'admin') {
      next('/admin')
    } else {
      next('/')
    }
    return
  }

  next()
})

export default router
