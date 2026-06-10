<template>
  <div class="orders-page">
    <h2 class="page-title">📦 订单管理</h2>

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
import { useUserStore } from '@/stores/user'
import request from '@/api/request'

const userStore = useUserStore()
const orderList = ref([]); const activeStatus = ref('all')
const pageNum = ref(1); const pageSize = ref(10)
const total = ref(0); const loading = ref(false)
const statusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'info', 3: '' }[s] || 'info')

const loadOrders = async () => {
  loading.value = true
  try {
    const shopId = userStore.userInfo?.shopId
    if (!shopId) { ElMessage.warning('请先完善店铺信息'); loading.value = false; return }
    const status = activeStatus.value === 'all' ? null : parseInt(activeStatus.value)
    const res = await request.get('/api/order/shop', { params: { shopId, pageNum: pageNum.value, pageSize: pageSize.value, status } })
    if (res.code === 200) { orderList.value = res.data.list || []; total.value = res.data.total || 0 }
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

const handleComplete = async (id) => {
  try {
    await ElMessageBox.confirm('确认订单已完成？', '提示', { type: 'success' })
    const res = await orderApi.complete(id)
    if (res.code === 200) { ElMessage.success('订单已完成'); loadOrders() }
  } catch {}
}

onMounted(() => loadOrders())
</script>

<style scoped>
.orders-page { max-width: 1100px; }
.page-title { font-size: 22px; font-weight: 700; color: var(--color-text); margin-bottom: var(--space-md); }
.order-tabs { margin-bottom: var(--space-sm); }
.pagination-bar { margin-top: var(--space-md); text-align: center; }
.text-done { color: var(--color-success); font-size: 13px; }
.text-cancel { color: var(--color-text-placeholder); font-size: 13px; }
</style>
