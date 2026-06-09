<template>
  <div class="home">
    <!-- 搜索栏 -->
    <div class="search-bar">
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

      <!-- 分页信息 -->
      <div class="pagination-info" v-if="dishList.length > 0">
        <span>已展示 {{ dishList.length }} / {{ total }} 个菜品</span>
      </div>

      <!-- 加载更多 -->
      <div class="load-more" v-if="checkHasMore">
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
import { Search, Plus } from '@element-plus/icons-vue'
import { dishApi } from '@/api/dish'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cartStore = useCartStore()

// 数据定义
const keyword = ref('')
const activeCategory = ref(null)
const dishList = ref([])
const pageNum = ref(1)
const pageSize = ref(8)
const total = ref(0)
const loading = ref(false)
const loadingMore = ref(false)

const defaultImage = 'https://picsum.photos/200/150?random=1'

// 分类列表（value 改为数字ID，与数据库 category_id 对应）
const categories = [
  { label: '全部', value: null },
  { label: '热销推荐', value: 1 },
  { label: '主食', value: 2 },
  { label: '小炒', value: 3 },
  { label: '汤类', value: 4 }
]

// 计算属性
const currentCategoryName = computed(() => {
  const cat = categories.find(c => c.value === activeCategory.value)
  return cat ? cat.label : '全部'
})

const hasMore = computed(() => {
  return dishList.value.length < total.value
})

const checkHasMore = computed(() => {
  if (total.value === 0) return false
  return dishList.value.length < total.value
})

// 加载菜品列表
const loadDishes = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }

    // 只在有值时才添加参数
    if (keyword.value && keyword.value.trim()) {
      params.keyword = keyword.value.trim()
    }

    if (activeCategory.value !== null && activeCategory.value !== undefined) {
      params.categoryId = activeCategory.value
    }

    console.log('========== 请求参数 ==========')
    console.log('pageNum:', pageNum.value)
    console.log('pageSize:', pageSize.value)
    console.log('完整参数:', params)

    const res = await dishApi.getList(params)

    console.log('========== 响应数据 ==========')
    console.log('响应:', res)
    console.log('data.list:', res.data?.list)
    console.log('data.total:', res.data?.total)
    console.log('list长度:', res.data?.list?.length)

    if (res.code === 200) {
      if (pageNum.value === 1) {
        dishList.value = res.data.list || []
      } else {
        dishList.value = [...dishList.value, ...(res.data.list || [])]
      }
      total.value = res.data.total || 0
      console.log('========== 更新后的状态 ==========')
      console.log('dishList.length:', dishList.value.length)
      console.log('total:', total.value)
      console.log('当前显示的菜品数:', dishList.value.length)
    }
  } catch (error) {
    console.error('加载菜品失败', error)
    ElMessage.error('菜品列表加载失败')
  } finally {
    loading.value = false
  }
}

// 加载更多
const loadMore = () => {
  if (checkHasMore.value && !loadingMore.value) {
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
const goCart = () => router.push('/cart')

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

/* 搜索栏 */
.search-bar {
  max-width: 600px;
  margin: 20px auto;
  padding: 0 20px;
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
.pagination-info {
  text-align: center;
  margin-top: 24px;
  color: #999;
  font-size: 14px;
}
.load-more {
  text-align: center;
  margin-top: 24px;
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
