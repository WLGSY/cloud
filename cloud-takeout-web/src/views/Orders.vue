<template>
  <div class="orders-page">
    <h2 class="page-title">📋 我的订单</h2>

    <el-tabs v-model="activeStatus" @tab-change="handleTabChange" class="order-tabs">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="待支付" name="0" />
      <el-tab-pane label="已支付" name="1" />
      <el-tab-pane label="已完成" name="3" />
      <el-tab-pane label="已取消" name="2" />
    </el-tabs>

    <div v-if="loading" class="loading"><el-skeleton :rows="3" animated /></div>
    <div v-else-if="orderList.length === 0" class="empty">
      <el-empty description="暂无订单"><el-button type="primary" @click="$router.push('/')">去下单</el-button></el-empty>
    </div>
    <div v-else class="order-list">
      <div v-for="order in orderList" :key="order.id" class="order-card">
        <div class="order-top">
          <span class="order-no">{{ order.orderNo }}</span>
          <span class="order-time">{{ order.createTime }}</span>
          <el-tag :type="statusTag(order.status)" size="small" effect="plain">{{ order.statusText || statusText(order.status) }}</el-tag>
        </div>

        <div class="order-body">
          <div class="order-items">
            <div v-for="item in (order.items || [])" :key="item.dishId" class="order-item">
              <span>{{ item.name }}</span><span class="item-qty">x{{ item.quantity }}</span><span class="item-price">¥{{ item.price }}</span>
            </div>
            <div v-if="!order.items?.length" class="order-item-placeholder">共 {{ order.totalCount || 0 }} 件商品</div>
          </div>
          <div class="order-amount">¥{{ order.totalAmount }}</div>
        </div>

        <div class="order-meta" v-if="order.receiver">
          <span>📦 {{ order.receiver }}</span>
          <span v-if="order.address">📍 {{ order.address }}</span>
        </div>

        <div class="order-actions">
          <el-button link type="primary" size="small" @click="showDetail(order)">查看详情</el-button>
          <div class="actions-right">
            <el-button v-if="order.status === 0" type="primary" size="small" @click="handlePay(order)">立即支付</el-button>
            <el-button v-if="order.status === 0" size="small" @click="handleCancel(order.id)">取消</el-button>
            <el-button v-if="order.status === 1" type="success" size="small" @click="handleComplete(order.id)">确认收货</el-button>
          </div>
        </div>
      </div>

      <div class="pagination-bar">
        <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total"
          layout="total, prev, pager, next" @current-change="loadOrders" />
      </div>
    </div>

    <!-- 支付弹窗 -->
    <el-dialog v-model="payVisible" title="确认支付" width="380px">
      <div class="pay-info">
        <p>订单号：{{ currentOrder?.orderNo }}</p>
        <p>金额：<strong>¥{{ currentOrder?.totalAmount }}</strong></p>
      </div>
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPay" :loading="payLoading">确认支付</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="520px">
      <template v-if="detailOrder">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="订单号">{{ detailOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTag(detailOrder.status)" size="small" effect="plain">{{ detailOrder.statusText || statusText(detailOrder.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ detailOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ detailOrder.payTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ detailOrder.receiver || '-' }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ detailOrder.userPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ detailOrder.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="金额"><strong style="color:var(--color-primary)">¥{{ detailOrder.totalAmount }}</strong></el-descriptions-item>
          <el-descriptions-item label="数量">{{ detailOrder.totalCount || 0 }} 件</el-descriptions-item>
        </el-descriptions>
        <div v-if="detailOrder.items?.length" class="detail-items">
          <h4>菜品明细</h4>
          <div v-for="item in detailOrder.items" :key="item.dishId" class="detail-item">
            <span>{{ item.name }}</span><span>x{{ item.quantity }}</span><span>¥{{ item.price }}</span>
            <span class="subtotal">小计 ¥{{ (item.price * item.quantity).toFixed(2) }}</span>
          </div>
        </div>
      </template>
      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '@/api/order'

const activeStatus = ref('all'); const orderList = ref([])
const pageNum = ref(1); const pageSize = ref(10); const total = ref(0)
const loading = ref(false); const payVisible = ref(false); const payLoading = ref(false)
const currentOrder = ref(null); const detailVisible = ref(false); const detailOrder = ref(null)

const statusText = (s) => ({ 0: '待支付', 1: '已支付', 2: '已取消', 3: '已完成' }[s] || '未知')
const statusTag = (s) => ({ 0: 'warning', 1: 'success', 2: 'info', 3: '' }[s] || 'info')

const handleTabChange = () => { pageNum.value = 1; loadOrders() }

const loadOrders = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value, status: activeStatus.value === 'all' ? null : parseInt(activeStatus.value) }
    const res = await orderApi.getList(params)
    if (res.code === 200) { orderList.value = res.data.list || []; total.value = res.data.total || 0 }
  } catch {} finally { loading.value = false }
}

const handlePay = (order) => { currentOrder.value = order; payVisible.value = true }

const confirmPay = async () => {
  payLoading.value = true
  try { const res = await orderApi.pay(currentOrder.value.id); if (res.code === 200) { ElMessage.success('支付成功'); payVisible.value = false; loadOrders() } } catch {} finally { payLoading.value = false }
}

const handleCancel = (id) => {
  ElMessageBox.confirm('确认取消？', '提示', { type: 'warning' }).then(async () => {
    try { const res = await orderApi.cancel(id, '用户取消'); if (res.code === 200) { ElMessage.success('已取消'); loadOrders() } } catch {}
  }).catch(() => {})
}

const handleComplete = async (id) => {
  try { await ElMessageBox.confirm('确认收货？', '确认', { type: 'success' }); const res = await orderApi.complete(id); if (res.code === 200) { ElMessage.success('已收货'); loadOrders() } } catch {}
}

const showDetail = (order) => { detailOrder.value = order; detailVisible.value = true }

onMounted(() => loadOrders())
</script>

<style scoped>
.orders-page { max-width: 900px; margin: 0 auto; }
.page-title { font-size: 22px; font-weight: 700; color: var(--color-text); margin-bottom: var(--space-md); }
.empty { padding: var(--space-xl) 0; text-align: center; }
.order-list { display: flex; flex-direction: column; gap: var(--space-sm); }
.order-card {
  background: var(--color-surface); border-radius: var(--radius-md);
  border: 1px solid var(--color-border-light); padding: var(--space-sm) var(--space-md);
}
.order-top {
  display: flex; align-items: center; gap: var(--space-sm);
  padding-bottom: var(--space-sm); border-bottom: 1px solid var(--color-border-light);
}
.order-no { font-weight: 500; color: var(--color-text); font-size: 14px; }
.order-time { color: var(--color-text-placeholder); font-size: 13px; margin-left: auto; margin-right: var(--space-sm); }
.order-body { display: flex; justify-content: space-between; padding: var(--space-sm) 0; }
.order-items { flex: 1; }
.order-item { display: flex; gap: 8px; font-size: 14px; color: var(--color-text-secondary); line-height: 2; }
.item-qty { color: var(--color-text-placeholder); }
.item-price { color: var(--color-primary); font-weight: 500; }
.order-item-placeholder { color: var(--color-text-placeholder); font-size: 14px; }
.order-amount { font-size: 20px; font-weight: 700; color: var(--color-primary); white-space: nowrap; margin-left: var(--space-md); }
.order-meta { display: flex; gap: var(--space-sm); font-size: 12px; color: var(--color-text-placeholder); padding: var(--space-xs) 0; border-top: 1px dashed var(--color-border-light); }
.order-actions { display: flex; justify-content: space-between; align-items: center; padding-top: var(--space-sm); border-top: 1px solid var(--color-border-light); }
.actions-right { display: flex; gap: 8px; }
.pagination-bar { margin-top: var(--space-md); text-align: center; }
.pay-info { text-align: center; padding: var(--space-sm); }
.pay-info strong { font-size: 24px; color: var(--color-primary); }
.detail-items { margin-top: var(--space-sm); }
.detail-items h4 { margin-bottom: 8px; color: var(--color-text); }
.detail-item { display: flex; gap: 8px; padding: 6px 8px; background: var(--color-bg); border-radius: var(--radius-sm); margin-bottom: 4px; font-size: 13px; }
.subtotal { margin-left: auto; color: var(--color-primary); font-weight: 500; }
</style>
