<template>
  <div class="orders">
    <h2>📋 我的订单</h2>

    <!-- 状态选项卡 -->
    <el-tabs v-model="activeStatus" @tab-click="loadOrders">
      <el-tab-pane label="全部订单" name="all"></el-tab-pane>
      <el-tab-pane label="待支付" name="0"></el-tab-pane>
      <el-tab-pane label="已支付" name="1"></el-tab-pane>
      <el-tab-pane label="已取消" name="2"></el-tab-pane>
    </el-tabs>

    <!-- 加载中 -->
    <div v-if="loading" class="loading">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- 空状态 -->
    <div v-else-if="orderList.length === 0" class="empty">
      <el-empty description="暂无订单">
        <el-button type="primary" @click="goHome">去下单</el-button>
      </el-empty>
    </div>

    <!-- 订单列表 -->
    <div v-else class="order-list">
      <el-card v-for="order in orderList" :key="order.id" class="order-card">
        <div class="order-header">
          <div class="order-info">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-time">{{ order.createTime }}</span>
          </div>
          <span class="order-status" :class="getStatusClass(order.status)">
            {{ getStatusText(order.status) }}
          </span>
        </div>

        <div class="order-content">
          <div class="order-items">
            <!-- 兼容后端不返回items的情况 -->
            <div v-for="item in (order.items || [])" :key="item.dishId" class="order-item">
              {{ item.name }} x {{ item.quantity }}
            </div>
            <!-- 如果订单项为空，显示总数 -->
            <div v-if="!order.items || order.items.length === 0" class="order-item-placeholder">
              共 {{ order.totalCount || 0 }} 件商品
            </div>
          </div>
          <div class="order-amount">¥{{ order.totalAmount }}</div>
        </div>

        <div class="order-footer">
          <div class="order-actions">
            <el-button
              v-if="order.status === 0"
              type="primary"
              size="small"
              @click="handlePay(order.id)"
            >
              立即支付
            </el-button>
            <el-button
              v-if="order.status === 0"
              type="danger"
              size="small"
              @click="handleCancel(order.id)"
            >
              取消订单
            </el-button>
            <el-button
              v-if="order.status === 1"
              type="info"
              size="small"
              @click="handleTrack(order.id)"
            >
              查看物流
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[5, 10, 20]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadOrders"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <!-- 支付对话框 -->
    <el-dialog v-model="payDialogVisible" title="确认支付" width="400px">
      <div class="pay-demo">
        <p>订单号：{{ currentOrder?.orderNo }}</p>
        <p>金额：<strong>¥{{ currentOrder?.totalAmount }}</strong></p>
        <p class="pay-tip">点击确认后，订单状态将变为已支付</p>
      </div>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPay" :loading="payLoading">
          确认支付
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '@/api/order'

const router = useRouter()
const activeStatus = ref('all')
const orderList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const payDialogVisible = ref(false)
const payLoading = ref(false)
const currentOrder = ref(null)

// 状态映射
const getStatusText = (status) => {
  const map = { 0: '待支付', 1: '已支付', 2: '已取消' }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  const map = { 0: 'status-pending', 1: 'status-paid', 2: 'status-cancelled' }
  return map[status] || ''
}

// 加载订单列表（对接后端）
const loadOrders = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: activeStatus.value === 'all' ? null : parseInt(activeStatus.value)
    }
    const res = await orderApi.getList(params)
    if (res.code === 200) {
      orderList.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载订单失败', error)
  } finally {
    loading.value = false
  }
}

// 分页大小改变
const handleSizeChange = (size) => {
  pageSize.value = size
  pageNum.value = 1
  loadOrders()
}

// 支付订单（对接后端）
const handlePay = (orderId) => {
  const order = orderList.value.find(o => o.id === orderId)
  if (order) {
    currentOrder.value = order
    payDialogVisible.value = true
  }
}

const confirmPay = async () => {
  payLoading.value = true
  try {
    const res = await orderApi.pay(currentOrder.value.id)
    if (res.code === 200) {
      ElMessage.success('支付成功')
      payDialogVisible.value = false
      loadOrders()
    }
  } catch (error) {
    console.error('支付失败', error)
    ElMessage.error(error.response?.data?.message || '支付失败')
  } finally {
    payLoading.value = false
  }
}

// 取消订单（对接后端）
const handleCancel = (orderId) => {
  ElMessageBox.confirm('确认取消该订单吗？取消后不可恢复', '提示', {
    confirmButtonText: '确认取消',
    cancelButtonText: '返回',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await orderApi.cancel(orderId, '用户主动取消')
      if (res.code === 200) {
        ElMessage.success('订单已取消')
        loadOrders()
      }
    } catch (error) {
      console.error('取消订单失败', error)
      ElMessage.error(error.response?.data?.message || '取消失败')
    }
  }).catch(() => {})
}

// 查看物流（对接物流服务）
const handleTrack = (orderId) => {
  // 对接物流服务，查询配送状态
  ElMessage.info('物流功能开发中，敬请期待')
}

const goHome = () => {
  router.push('/')
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.orders {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}
.orders h2 {
  margin-bottom: 20px;
}
.loading, .empty {
  padding: 60px 0;
  text-align: center;
}
.order-list {
  margin-top: 20px;
}
.order-card {
  margin-bottom: 16px;
}
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
  margin-bottom: 12px;
}
.order-info {
  display: flex;
  gap: 20px;
}
.order-no {
  color: #666;
  font-weight: 500;
}
.order-time {
  color: #999;
  font-size: 14px;
}
.order-status {
  font-weight: bold;
}
.status-pending {
  color: #e6a23c;
}
.status-paid {
  color: #67c23a;
}
.status-cancelled {
  color: #f56c6c;
}
.order-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.order-items {
  flex: 1;
}
.order-item {
  line-height: 1.8;
  color: #666;
}
.order-item-placeholder {
  color: #999;
  font-size: 14px;
}
.order-amount {
  font-size: 18px;
  font-weight: bold;
  color: #ff6b35;
}
.order-footer {
  padding-top: 12px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
}
.order-actions {
  display: flex;
  gap: 10px;
}
.pagination {
  margin-top: 20px;
  text-align: center;
}
.pay-demo {
  text-align: center;
  padding: 20px;
}
.pay-demo p {
  margin: 10px 0;
}
.pay-demo strong {
  font-size: 24px;
  color: #ff6b35;
}
.pay-tip {
  color: #999;
  font-size: 12px;
}
</style>
