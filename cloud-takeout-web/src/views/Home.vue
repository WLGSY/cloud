<template>
  <div class="home">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <div class="logo" @click="goHome">🍔 云外卖</div>

        <div class="search-box">
          <el-input
            v-model="keyword"
            placeholder="搜索菜品..."
            :prefix-icon="Search"
            size="large"
            clearable
            @clear="loadDishes"
            @keyup.enter="searchDish"
          >
            <template #append>
              <el-button @click="searchDish">搜索</el-button>
            </template>
          </el-input>
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
              <span>{{ userStore.userInfo?.username || '未登录' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                <el-dropdown-item command="points">我的积分</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 分类导航 -->
    <div class="category-nav">
      <div class="category-content">
        <div
          v-for="cat in categories"
          :key="cat.value"
          class="category-item"
          :class="{ active: activeCategory === cat.value }"
          @click="changeCategory(cat.value)"
        >
          {{ cat.label }}
        </div>
      </div>
    </div>

    <!-- 菜品列表 -->
    <div class="dish-section">
      <div class="section-header">
        <h2>{{ currentCategoryName }}</h2>
        <span class="section-desc">为您推荐</span>
      </div>

      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>

      <div v-else-if="dishList.length === 0" class="empty-container">
        <el-empty description="暂无菜品" />
      </div>

      <div v-else class="dish-grid">
        <div
          v-for="dish in dishList"
          :key="dish.id"
          class="dish-card"
        >
          <div class="dish-image">
            <img :src="dish.image || defaultImage" :alt="dish.name">
            <div class="dish-tag" v-if="dish.isHot">热销</div>
          </div>
          <div class="dish-info">
            <h3 class="dish-name">{{ dish.name }}</h3>
            <p class="dish-desc">{{ dish.description || '暂无描述' }}</p>
            <div class="dish-footer">
              <span class="dish-price">¥{{ dish.price }}</span>
              <span class="dish-sales">月销 {{ dish.sales || 0 }} 份</span>
            </div>
          </div>
          <div class="dish-actions">
            <el-button
              type="primary"
              circle
              size="small"
              @click="addToCart(dish)"
            >
              <el-icon><Plus /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 加载更多 -->
      <div class="load-more" v-if="hasMore">
        <el-button text @click="loadMore" :loading="loadingMore">加载更多</el-button>
      </div>
    </div>

    <!-- 购物车浮动按钮（移动端） -->
    <div class="cart-float" @click="goCart" v-if="cartStore.totalCount > 0">
      <el-badge :value="cartStore.totalCount" class="cart-badge">
        <div class="cart-icon">
          <el-icon><ShoppingCart /></el-icon>
        </div>
      </el-badge>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, ShoppingCart, ArrowDown, Plus } from '@element-plus/icons-vue'
import { dishApi } from '@/api/dish'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

// 数据定义
const keyword = ref('')
const activeCategory = ref('all')
const dishList = ref([])
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)
const loading = ref(false)
const loadingMore = ref(false)

const defaultImage = 'https://picsum.photos/200/150?random=1'
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 分类列表
const categories = [
  { label: '全部', value: 'all' },
  { label: '热销推荐', value: 'hot' },
  { label: '主食', value: 'main' },
  { label: '小炒', value: 'stir' },
  { label: '汤类', value: 'soup' }
]

// 计算属性
const currentCategoryName = computed(() => {
  const cat = categories.find(c => c.value === activeCategory.value)
  return cat ? cat.label : '全部'
})

const hasMore = computed(() => {
  return dishList.value.length < total.value
})

// 加载菜品列表
const loadDishes = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value,
      category: activeCategory.value === 'all' ? '' : activeCategory.value
    }
    const res = await dishApi.getList(params)
    if (res.code === 200) {
      if (pageNum.value === 1) {
        dishList.value = res.data.list || []
      } else {
        dishList.value = [...dishList.value, ...(res.data.list || [])]
      }
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载菜品失败', error)
  } finally {
    loading.value = false
  }
}

// 加载更多
const loadMore = () => {
  if (hasMore.value && !loadingMore.value) {
    pageNum.value++
    loadDishes()
  }
}

// 搜索
const searchDish = () => {
  pageNum.value = 1
  loadDishes()
}

// 切换分类
const changeCategory = (category) => {
  activeCategory.value = category
  pageNum.value = 1
  loadDishes()
}

// 加入购物车
const addToCart = (dish) => {
  cartStore.addItem(dish)
  ElMessage.success(`已添加 ${dish.name} 到购物车`)
}

// 跳转
const goHome = () => router.push('/')
const goCart = () => router.push('/cart')

// 下拉菜单命令
const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } else if (command === 'orders') {
    router.push('/orders')
  } else if (command === 'points') {
    router.push('/points')
  }
}

// 生命周期
onMounted(() => {
  loadDishes()
})
</script>

<style scoped>
.home {
  min-height: 100vh;
  background: #f5f5f5;
}

/* 头部导航 */
.header {
  background: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 12px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.logo {
  font-size: 24px;
  font-weight: bold;
  cursor: pointer;
  color: #ff6b35;
  white-space: nowrap;
}
.search-box {
  flex: 1;
  max-width: 500px;
  margin: 0 40px;
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
}

/* 分类导航 */
.category-nav {
  background: white;
  border-bottom: 1px solid #eee;
}
.category-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  gap: 32px;
}
.category-item {
  padding: 12px 0;
  cursor: pointer;
  font-size: 16px;
  color: #666;
  border-bottom: 2px solid transparent;
}
.category-item:hover {
  color: #ff6b35;
}
.category-item.active {
  color: #ff6b35;
  border-bottom-color: #ff6b35;
}

/* 菜品区域 */
.dish-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
}
.section-header {
  margin-bottom: 24px;
}
.section-header h2 {
  font-size: 28px;
  color: #333;
}

/* 菜品网格 */
.dish-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}
.dish-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  position: relative;
}
.dish-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
}
.dish-image {
  position: relative;
  height: 180px;
  overflow: hidden;
}
.dish-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.dish-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  background: #ff6b35;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}
.dish-info {
  padding: 16px;
}
.dish-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}
.dish-desc {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}
.dish-footer {
  display: flex;
  justify-content: space-between;
}
.dish-price {
  font-size: 18px;
  font-weight: bold;
  color: #ff6b35;
}
.dish-sales {
  font-size: 12px;
  color: #999;
}
.dish-actions {
  position: absolute;
  bottom: 16px;
  right: 16px;
}
.load-more {
  text-align: center;
  margin-top: 40px;
}

/* 响应式 */
@media (max-width: 768px) {
  .dish-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .search-box {
    margin: 0 16px;
  }
}
</style>
