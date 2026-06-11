<template>
  <div class="orders-page">
    <h2 class="page-title">📦 订单管理</h2>

    <!-- 搜索栏 + 店铺选择器 -->
    <div class="toolbar">
      <el-select v-model="selectedShopId" placeholder="选择店铺" @change="onShopChange" class="shop-select">
        <el-option v-for="s in shops" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
      <el-input v-model="keyword" placeholder="搜索订单号/客户名/收货人" clearable @clear="onSearch" @keyup.enter="onSearch" class="search-input">
        <template #prefix><span style="font-size:14px">🔍</span></template>
      </el-input>
      <el-button type="primary" @click="onSearch" :disabled="!selectedShopId">搜索</el-button>
    </div>

    <!-- 状态标签页 -->
    <el-tabs v-model="activeStatus" @tab-change="loadOrders" class="order-tabs">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="待支付" name="0" />
      <el-tab-pane label="已支付" name="1" />
      <el-tab-pane label="已完成" name="3" />
      <el-tab-pane label="已取消" name="2" />
    </el-tabs>

    <el-table :data="orderList" v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="170" />
      <el-table-column prop="userName" label="客户" width="100" />
      <el-table-column prop="receiver" label="收货人" width="100" />
      <el-table-column prop="totalAmount" label="金额" width="100">
        <template #default="{row}">¥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column prop="statusText" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="statusType(row.status)" size="small" effect="plain">{{ row.statusText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="菜品" min-width="150">
        <template #default="{row}">
          <span v-for="item in (row.items || [])" :key="item.dishId" style="margin-right:8px;font-size:13px;color:var(--color-text-secondary)">{{ item.name }}x{{ item.quantity }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="160" />
      <el-table-column label="操作" width="100">
        <template #default="{row}">
          <el-button v-if="row.status===1" link type="success" size="small" @click="handleComplete(row.id)">完成</el-button>
          <el-tag v-else-if="row.status===0" type="warning" size="small" effect="plain">等待支付</el-tag>
          <span v-else-if="row.status===3" class="text-done">已完结</span>
          <span v-else class="text-cancel">已取消</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar">
      <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="loadOrders" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '@/api/order'
import { shopApi } from '@/api/shop'
import request from '@/api/request'

const orderList = ref([])
const activeStatus = ref('all')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const keyword = ref('')

// 多店铺支持
const shops = ref([])
const selectedShopId = ref(null)

const statusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'info', 3: '' }[s] || 'info')

const loadShops = async () => {
  try {
    const res = await shopApi.getMyShops()
    if (res.code === 200) {
      shops.value = Array.isArray(res.data) ? res.data : (res.data ? [res.data] : [])
      if (shops.value.length > 0 && !selectedShopId.value) {
        selectedShopId.value = shops.value[0].id
        loadOrders()
      }
    }
  } catch {}
}

const onShopChange = () => {
  pageNum.value = 1
  loadOrders()
}

const onSearch = () => {
  pageNum.value = 1
  loadOrders()
}

const loadOrders = async () => {
  if (!selectedShopId.value) return
  loading.value = true
  try {
    const shopId = selectedShopId.value
    const status = activeStatus.value === 'all' ? null : parseInt(activeStatus.value)
    const params = { shopId, pageNum: pageNum.value, pageSize: pageSize.value, status }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    const res = await request.get('/api/order/shop', { params })
    if (res.code === 200) {
      orderList.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

const handleComplete = async (id) => {
  try {
    await ElMessageBox.confirm('确认订单已完成？', '提示', { type: 'success' })
    const res = await orderApi.complete(id)
    if (res.code === 200) { ElMessage.success('订单已完成'); loadOrders() }
  } catch {}
}

onMounted(() => loadShops())
</script>

<style scoped>
.orders-page { max-width: 1100px; }
.page-title { font-size: 22px; font-weight: 700; color: var(--color-text); margin-bottom: var(--space-md); }
.toolbar { display: flex; gap: var(--space-sm); margin-bottom: var(--space-sm); align-items: center; flex-wrap: wrap; }
.shop-select { width: 160px; flex-shrink: 0; }
.search-input { width: 280px; }
.order-tabs { margin-bottom: var(--space-sm); }
.pagination-bar { margin-top: var(--space-md); text-align: center; }
.text-done { color: var(--color-success); font-size: 13px; }
.text-cancel { color: var(--color-text-placeholder); font-size: 13px; }
</style>
