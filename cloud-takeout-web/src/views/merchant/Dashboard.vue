<template>
  <div class="dashboard">
    <h2 class="page-title">📊 数据看板</h2>

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
import { useUserStore } from '@/stores/user'
import request from '@/api/request'

const userStore = useUserStore()
const stats = ref({ totalDishes: 0, onSaleDishes: 0, totalOrders: 0, todayIncome: 0 })
const recentOrders = ref([])

onMounted(async () => {
  const shopId = userStore.userInfo?.shopId
  try {
    const dishRes = await dishApi.getList({ pageNum: 1, pageSize: 100, shopId })
    if (dishRes.code === 200) {
      stats.value.totalDishes = dishRes.data.total
      stats.value.onSaleDishes = (dishRes.data.list || []).filter(d => d.status === 1).length
    }
  } catch {}
  try {
    if (!shopId) return
    const orderRes = await request.get('/api/order/shop', { params: { shopId, pageNum: 1, pageSize: 5 } })
    if (orderRes.code === 200) {
      recentOrders.value = orderRes.data.list || []
      stats.value.totalOrders = orderRes.data.total
      stats.value.todayIncome = (orderRes.data.list || []).reduce((s, o) => s + (o.totalAmount || 0), 0).toFixed(2)
    }
  } catch {}
})
</script>

<style scoped>
.dashboard { max-width: 1100px; }
.page-title { font-size: 22px; font-weight: 700; color: var(--color-text); margin-bottom: var(--space-md); }
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
