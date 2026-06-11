<template>
  <div class="dashboard">
    <div class="page-header">
      <h2 class="page-title">📊 数据看板</h2>
      <!-- 多店铺时显示选择器 -->
      <el-select v-if="shops.length > 1" v-model="selectedShopId" @change="onShopChange" placeholder="选择店铺" class="shop-select">
        <el-option v-for="s in shops" :key="s.id" :label="s.name" :value="s.id" />
        <el-option label="全部店铺" :value="0" />
      </el-select>
    </div>

    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-number">{{ stats.totalDishes }}</div>
          <div class="stat-label">菜品总数</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-number">{{ stats.onSaleDishes }}</div>
          <div class="stat-label">在售菜品</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-number">{{ stats.totalOrders }}</div>
          <div class="stat-label">今日订单</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card highlight">
          <div class="stat-number">¥{{ stats.todayIncome }}</div>
          <div class="stat-label">今日收入</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-row">
      <el-col :span="12">
        <div class="panel">
          <h3 class="panel-title">📋 最近订单</h3>
          <el-table :data="recentOrders" size="small">
            <el-table-column prop="orderNo" label="订单号" width="160" />
            <el-table-column prop="totalAmount" label="金额" width="80">
              <template #default="{ row }">¥{{ row.totalAmount }}</template>
            </el-table-column>
            <el-table-column prop="statusText" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'info' : 'warning'" size="small" effect="plain">{{ row.statusText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" />
          </el-table>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="panel">
          <h3 class="panel-title">⭐ 快捷入口</h3>
          <div class="quick-links">
            <button class="quick-btn primary" @click="$router.push('/merchant/dishes')">🍽️ 管理菜品</button>
            <button class="quick-btn success" @click="$router.push('/merchant/orders')">📦 处理订单</button>
            <button class="quick-btn warning" @click="$router.push('/merchant/shop')">🏪 编辑店铺</button>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { dishApi } from '@/api/dish'
import { shopApi } from '@/api/shop'
import request from '@/api/request'

const stats = ref({ totalDishes: 0, onSaleDishes: 0, totalOrders: 0, todayIncome: 0 })
const recentOrders = ref([])
const shops = ref([])
const selectedShopId = ref(null)
const allShopIds = ref([])

const loadShops = async () => {
  try {
    const res = await shopApi.getMyShops()
    if (res.code === 200) {
      shops.value = Array.isArray(res.data) ? res.data : (res.data ? [res.data] : [])
      allShopIds.value = shops.value.map(s => s.id)
      if (shops.value.length > 0) {
        selectedShopId.value = shops.value[0].id
      }
      loadStats()
    }
  } catch {}
}

const onShopChange = () => { loadStats() }

const loadStats = async () => {
  try {
    // 聚合或单店铺统计
    const targetShopIds = selectedShopId.value > 0 ? [selectedShopId.value] : allShopIds.value
    let totalDishes = 0, onSaleDishes = 0

    for (const shopId of targetShopIds) {
      const dishRes = await dishApi.getList({ pageNum: 1, pageSize: 100, shopId })
      if (dishRes.code === 200) {
        totalDishes += dishRes.data.total
        onSaleDishes += (dishRes.data.list || []).filter(d => d.status === 1).length
      }
    }

    // 加载所有相关店铺的订单
    let allOrders = []
    for (const shopId of targetShopIds) {
      const orderRes = await request.get('/api/order/shop', { params: { shopId, pageNum: 1, pageSize: 200 } })
      if (orderRes.code === 200) {
        allOrders = allOrders.concat(orderRes.data.list || [])
      }
    }

    const today = new Date().toISOString().slice(0, 10)
    const todayOrders = allOrders.filter(o => (o.createTime || '').startsWith(today))
    const income = todayOrders
      .filter(o => o.status === 1 || o.status === 3)
      .reduce((s, o) => s + (o.totalAmount || 0), 0).toFixed(2)

    stats.value = { totalDishes, onSaleDishes, totalOrders: todayOrders.length, todayIncome: income }
    recentOrders.value = allOrders.slice(0, 5)
  } catch {}
}

onMounted(() => loadShops())
</script>

<style scoped>
.dashboard { max-width: 1100px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-md); }
.page-title { font-size: 22px; font-weight: 700; color: var(--color-text); }
.shop-select { width: 160px; }
.stats-row { margin-bottom: var(--space-md); }
.stat-card {
  padding: var(--space-md); text-align: center;
  background: var(--color-surface); border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
}
.stat-card.highlight { background: var(--color-primary-bg); border-color: transparent; }
.stat-number { font-size: 30px; font-weight: 700; color: var(--color-text); letter-spacing: -0.02em; }
.stat-label { font-size: 14px; color: var(--color-text-secondary); margin-top: 4px; }
.content-row { margin-top: var(--space-md); }
.panel {
  background: var(--color-surface); border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light); padding: var(--space-md);
}
.panel-title { font-size: 16px; font-weight: 600; color: var(--color-text); margin-bottom: var(--space-sm); }
.quick-links { display: flex; flex-direction: column; gap: 10px; padding: 8px 0; }
.quick-btn {
  width: 100%; padding: 14px; border-radius: var(--radius-sm);
  border: none; font-size: 15px; font-weight: 500; cursor: pointer;
  text-align: left; transition: all var(--transition);
}
.quick-btn.primary { background: var(--color-primary-bg); color: var(--color-primary); }
.quick-btn.success { background: #ecfdf5; color: var(--color-success); }
.quick-btn.warning { background: #fffbeb; color: var(--color-warning); }
.quick-btn:hover { filter: brightness(0.95); }
</style>
