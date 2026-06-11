<template>
  <div class="home">
    <!-- Hero 区域 -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">
          <span class="hero-icon">🍃</span>
          美味，即刻送达
        </h1>
        <p class="hero-sub">精选周边优质商家，你想吃的都在这里</p>
      </div>
      <div class="search-box" @click="showSearch = true">
        <span class="search-icon">🔍</span>
        <span class="search-text">搜索商家或菜品...</span>
      </div>
    </div>

    <!-- 商家列表 -->
    <div class="shop-list">
      <div class="list-header">
        <span class="list-title">
          <span class="title-dot"></span>
          附近商家
        </span>
        <span class="list-count">{{ shops.length }} 家</span>
      </div>

      <div v-if="loading" class="loading">
        <el-skeleton :rows="3" animated />
      </div>

      <div v-else class="shop-grid">
        <div
          v-for="shop in shops" :key="shop.id"
          class="shop-card"
          @click="goShop(shop.id)"
        >
          <!-- 商家头像 -->
          <div class="shop-avatar" :style="{ background: avatarColor(shop.id) }">
            <img v-if="shop.logo" :src="shop.logo" :alt="shop.name" class="shop-logo-img" />
            <span v-else class="shop-logo-text">{{ shop.name.charAt(0) }}</span>
          </div>

          <!-- 商家信息 -->
          <div class="shop-body">
            <div class="shop-name-row">
              <span class="shop-name">{{ shop.name }}</span>
              <span class="shop-rating">⭐ {{ getRating(shop.id) }}</span>
            </div>
            <div class="shop-meta">
              <span>月售 {{ getSales(shop.id) }}</span>
              <span class="dot">·</span>
              <span>{{ getDeliveryTime(shop.id) }}</span>
              <span class="dot">·</span>
              <span>¥{{ getAvgPrice(shop.id) }} 人均</span>
            </div>
            <div class="shop-tags">
              <span class="tag">{{ getTag(shop.id) }}</span>
              <span class="tag discount" v-if="shop.id % 3 === 0">满减优惠</span>
              <span class="tag new" v-if="shop.id > 4">新店</span>
            </div>
            <div class="shop-desc">{{ shop.description }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { shopApi } from '@/api/shop'
import { dishApi } from '@/api/dish'

const router = useRouter()
const shops = ref([])
const shopStats = ref({})
const loading = ref(true)

// 加载所有商家
onMounted(async () => {
  try {
    // 1. 加载商家列表
    const shopRes = await shopApi.getAll()
    if (shopRes.code === 200) {
      shops.value = shopRes.data || []
    }

    // 2. 加载所有带 shopId 的菜品，计算每个店的统计
    const dishRes = await dishApi.getList({ pageNum: 1, pageSize: 200 })
    if (dishRes.code === 200) {
      const dishes = dishRes.data.list || []
      const stats = {}
      dishes.forEach(d => {
        if (!d.shopId) return
        if (!stats[d.shopId]) {
          stats[d.shopId] = { count: 0, sales: 0, totalPrice: 0 }
        }
        stats[d.shopId].count++
        stats[d.shopId].sales += (d.sales || 0)
        stats[d.shopId].totalPrice += parseFloat(d.price || 0)
      })
      shopStats.value = stats
    }
  } catch (e) {
    console.error('加载失败', e)
  } finally {
    loading.value = false
  }
})

// 辅助函数
const avatarColor = (id) => {
  const colors = ['#FF6B35', '#E84A5F', '#3B82F6', '#10B981', '#8B5CF6', '#F59E0B']
  return colors[(id - 1) % colors.length]
}

const getRating = (id) => (4.3 + (id * 0.13) % 0.7).toFixed(1)
const getSales = (id) => (shopStats.value[id]?.sales || 500 + id * 100)
const getDeliveryTime = (id) => (25 + id * 3) + 'min'
const getAvgPrice = (id) => {
  const s = shopStats.value[id]
  if (!s || s.count === 0) return '—'
  return Math.round(s.totalPrice / s.count)
}
const getTag = (id) => {
  const tags = { 1: '湘菜', 2: '湘菜', 4: '粤菜', 5: '川菜', 6: '东北菜', 7: '日料' }
  return tags[id] || '美食'
}

const goShop = (id) => router.push(`/shop/${id}`)
</script>

<style scoped>
.home { max-width: 960px; margin: 0 auto; padding-bottom: var(--space-xl); }

/* Hero 区域 */
.hero-section {
  padding: var(--space-lg) var(--space-md) var(--space-md);
  margin-bottom: var(--space-md);
  background: var(--gradient-hero);
  border-radius: var(--radius-xl);
  text-align: center;
}
.hero-content { margin-bottom: var(--space-md); }
.hero-icon { font-size: 36px; display: block; margin-bottom: 8px; }
.hero-title {
  font-size: 28px; font-weight: 800; color: var(--color-text);
  letter-spacing: -.03em; margin-bottom: 6px;
}
.hero-sub { font-size: 14px; color: var(--color-text-secondary); }

.search-box {
  max-width: 500px; margin: 0 auto;
  padding: 12px 20px; background: var(--color-surface);
  border-radius: var(--radius-full); border: 1.5px solid var(--color-border);
  cursor: pointer; display: flex; align-items: center; gap: 8px;
  transition: all var(--transition); box-shadow: var(--shadow-sm);
}
.search-box:hover { border-color: var(--color-primary); box-shadow: var(--shadow-md); }
.search-icon { font-size: 18px; }
.search-text { font-size: 14px; color: var(--color-text-placeholder); }

/* 列表头 */
.list-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: var(--space-md); padding: 0 2px;
}
.list-title { font-size: 18px; font-weight: 700; color: var(--color-text); display: flex; align-items: center; gap: 8px; }
.title-dot { width: 8px; height: 8px; border-radius: 50%; background: var(--color-primary); display: inline-block; }
.list-count { font-size: 13px; color: var(--color-text-placeholder); background: var(--color-bg-alt); padding: 4px 12px; border-radius: var(--radius-full); }

/* 商家网格 */
.shop-grid { display: flex; flex-direction: column; gap: var(--space-sm); }

/* 商家卡片 */
.shop-card {
  display: flex; gap: var(--space-md);
  padding: var(--space-md);
  background: var(--color-surface); border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
  cursor: pointer; transition: all var(--transition);
  position: relative; overflow: hidden;
}
.shop-card::before {
  content: ''; position: absolute; inset: 0; opacity: 0;
  background: linear-gradient(135deg, var(--color-primary-bg), transparent 60%);
  transition: opacity var(--transition);
}
.shop-card:hover::before { opacity: 1; }
.shop-card:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); border-color: var(--color-primary-bg-strong); }

.shop-avatar {
  width: 68px; height: 68px; border-radius: var(--radius-md);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 26px; font-weight: 700;
  flex-shrink: 0; position: relative; z-index: 1;
  box-shadow: 0 4px 12px rgba(0,0,0,.1);
  overflow: hidden;
}
.shop-logo-img { width: 100%; height: 100%; object-fit: cover; }
.shop-logo-text { font-size: 26px; font-weight: 700; }

.shop-body { flex: 1; min-width: 0; position: relative; z-index: 1; }
.shop-name-row { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.shop-name { font-size: 17px; font-weight: 700; color: var(--color-text); }
.shop-rating {
  font-size: 12px; color: var(--color-warning); font-weight: 600;
  background: var(--color-warning-bg); padding: 2px 8px; border-radius: var(--radius-full);
}

.shop-meta { font-size: 12px; color: var(--color-text-secondary); margin-bottom: 6px; }
.dot { margin: 0 4px; color: var(--color-border); }

.shop-tags { display: flex; gap: 6px; margin-bottom: 6px; }
.tag {
  padding: 2px 10px; border-radius: var(--radius-full);
  font-size: 11px; font-weight: 500;
  background: var(--color-bg-alt); color: var(--color-text-secondary);
}
.tag.discount { background: #fef3c7; color: #b45309; }
.tag.new { background: #dbeafe; color: #2563eb; }

.shop-desc {
  font-size: 12px; color: var(--color-text-placeholder);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
</style>
