<template>
  <div class="home">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索菜品..." :prefix-icon="Search"
        size="large" clearable @clear="loadDishes" @keyup.enter="searchDish"
        class="search-input" />
    </div>

    <!-- 分类导航 -->
    <div class="category-nav">
      <div
        v-for="cat in categories" :key="cat.value"
        class="category-item"
        :class="{ active: activeCategory === cat.value }"
        @click="changeCategory(cat.value)"
      >{{ cat.label }}</div>
    </div>

    <!-- 菜品列表 -->
    <div class="dish-section">
      <div v-if="loading" class="loading-container"><el-skeleton :rows="3" animated /></div>
      <div v-else-if="dishList.length === 0" class="empty-container"><el-empty description="暂无菜品" /></div>
      <div v-else class="dish-grid">
        <div v-for="dish in dishList" :key="dish.id" class="dish-card">
          <div class="dish-image">
            <img :src="dish.image || defaultImage" :alt="dish.name">
            <div class="dish-tag" v-if="dish.isHot">热销</div>
          </div>
          <div class="dish-info">
            <h3 class="dish-name">{{ dish.name }}</h3>
            <p class="dish-desc">{{ dish.description || '暂无描述' }}</p>
            <div class="dish-footer">
              <span class="dish-price">¥{{ dish.price }}</span>
              <span class="dish-sales">月销 {{ dish.sales || 0 }}</span>
            </div>
          </div>
          <button class="dish-add-btn" @click="addToCart(dish)">+</button>
        </div>
      </div>

      <div class="pagination-info" v-if="dishList.length > 0">
        <span>已展示 {{ dishList.length }} / {{ total }} 个菜品</span>
      </div>
      <div class="load-more" v-if="checkHasMore">
        <el-button text @click="loadMore" :loading="loadingMore">加载更多</el-button>
      </div>
    </div>

    <!-- 购物车浮动按钮 -->
    <div class="cart-float" @click="goCart" v-if="cartStore.totalCount > 0">
      🛒 <span class="cart-float-badge">{{ cartStore.totalCount }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { dishApi } from '@/api/dish'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cartStore = useCartStore()

const keyword = ref('')
const activeCategory = ref(null)
const dishList = ref([])
const pageNum = ref(1)
const pageSize = ref(8)
const total = ref(0)
const loading = ref(false)
const loadingMore = ref(false)
const defaultImage = 'https://picsum.photos/200/150?random=1'

const categories = [
  { label: '全部', value: null },
  { label: '热销推荐', value: 1 },
  { label: '主食', value: 2 },
  { label: '小炒', value: 3 },
  { label: '汤类', value: 4 }
]

const checkHasMore = computed(() => total.value > 0 && dishList.value.length < total.value)

const loadDishes = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (keyword.value?.trim()) params.keyword = keyword.value.trim()
    if (activeCategory.value !== null) params.categoryId = activeCategory.value
    const res = await dishApi.getList(params)
    if (res.code === 200) {
      if (pageNum.value === 1) dishList.value = res.data.list || []
      else dishList.value = [...dishList.value, ...(res.data.list || [])]
      total.value = res.data.total || 0
    }
  } catch (error) { console.error('加载菜品失败', error) } finally { loading.value = false }
}

const loadMore = () => { if (checkHasMore.value && !loadingMore.value) { pageNum.value++; loadDishes() } }
const searchDish = () => { pageNum.value = 1; loadDishes() }
const changeCategory = (cat) => { activeCategory.value = cat; pageNum.value = 1; loadDishes() }
const addToCart = (dish) => { cartStore.addItem(dish); ElMessage.success(`已添加 ${dish.name}`) }
const goCart = () => router.push('/cart')

onMounted(() => loadDishes())
</script>

<style scoped>
.home { padding-bottom: var(--space-xl); }

/* 搜索栏 */
.search-bar { max-width: 600px; margin: 0 auto var(--space-md); }
.search-input :deep(.el-input__wrapper) {
  border-radius: var(--radius-full) !important;
  padding: 4px 16px;
}

/* 分类导航 */
.category-nav {
  display: flex; justify-content: center; gap: 4px;
  margin-bottom: var(--space-lg);
  padding: 4px; background: var(--color-bg-alt);
  border-radius: var(--radius-full); max-width: 500px;
  margin-left: auto; margin-right: auto;
}
.category-item {
  padding: 8px 20px; border-radius: var(--radius-full);
  font-size: 14px; font-weight: 500; color: var(--color-text-secondary);
  cursor: pointer; transition: all var(--transition);
}
.category-item.active {
  background: var(--color-surface); color: var(--color-primary);
  box-shadow: var(--shadow-xs);
}

/* 菜品网格 */
.dish-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr); gap: var(--space-md);
}
.dish-card {
  background: var(--color-surface); border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
  overflow: hidden; position: relative;
  transition: all var(--transition);
}
.dish-card:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); }
.dish-image { position: relative; height: 170px; overflow: hidden; background: var(--color-bg-alt); }
.dish-image img { width: 100%; height: 100%; object-fit: cover; }
.dish-tag {
  position: absolute; top: 10px; left: 10px;
  background: var(--color-primary); color: #fff;
  padding: 2px 10px; border-radius: var(--radius-full);
  font-size: 11px; font-weight: 600;
}
.dish-info { padding: var(--space-sm) var(--space-sm) 0; }
.dish-name { font-size: 15px; font-weight: 600; color: var(--color-text); margin-bottom: 4px; }
.dish-desc { font-size: 12px; color: var(--color-text-secondary); margin-bottom: 10px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.dish-footer { display: flex; justify-content: space-between; align-items: center; padding-bottom: var(--space-sm); }
.dish-price { font-size: 18px; font-weight: 700; color: var(--color-primary); }
.dish-sales { font-size: 12px; color: var(--color-text-placeholder); }
.dish-add-btn {
  position: absolute; bottom: 12px; right: 12px;
  width: 34px; height: 34px; border-radius: var(--radius-full);
  border: 1px solid var(--color-primary); background: var(--color-surface);
  color: var(--color-primary); font-size: 18px; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all var(--transition);
}
.dish-add-btn:hover { background: var(--color-primary); color: #fff; }

/* 加载更多 */
.pagination-info { text-align: center; margin-top: var(--space-md); color: var(--color-text-secondary); font-size: 13px; }
.load-more { text-align: center; margin-top: var(--space-sm); }

/* 购物车浮动按钮 */
.cart-float {
  position: fixed; bottom: 32px; right: 32px;
  width: 56px; height: 56px; border-radius: var(--radius-full);
  background: var(--color-primary); color: #fff;
  font-size: 22px; display: flex; align-items: center; justify-content: center;
  cursor: pointer; box-shadow: var(--shadow-lg); transition: all var(--transition);
  z-index: 50;
}
.cart-float:hover { transform: scale(1.08); }
.cart-float-badge {
  position: absolute; top: -4px; right: -4px;
  min-width: 22px; height: 22px; border-radius: var(--radius-full);
  background: var(--color-danger); color: #fff;
  font-size: 12px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}

@media (max-width: 768px) {
  .dish-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
