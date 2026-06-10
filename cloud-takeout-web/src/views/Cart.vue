<template>
  <div class="cart-page">
    <div class="page-header">
      <h2>🛒 我的购物车</h2>
      <el-button v-if="cartStore.items.length > 0" @click="cartStore.clearCart()" text type="danger">清空购物车</el-button>
    </div>

    <div v-if="cartStore.items.length === 0" class="empty-state">
      <el-empty description="购物车还是空的">
        <el-button type="primary" @click="$router.push('/')">去逛逛</el-button>
      </el-empty>
    </div>

    <div v-else class="cart-content">
      <div class="cart-list">
        <div v-for="item in cartStore.items" :key="item.dishId" class="cart-item">
          <img :src="item.image || defaultImage" class="cart-item-img">
          <div class="cart-item-info">
            <span class="cart-item-name">{{ item.name }}</span>
            <span class="cart-item-price">¥{{ item.price }}</span>
          </div>
          <el-input-number v-model="item.quantity" :min="1" :max="99" size="small"
            @change="(v) => cartStore.updateQuantity(item.dishId, v)" />
          <span class="cart-item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
          <el-button link type="danger" @click="cartStore.removeItem(item.dishId)">删除</el-button>
        </div>
      </div>

      <div class="cart-summary">
        <div class="summary-info">
          <span>共 <strong>{{ cartStore.totalCount }}</strong> 件</span>
          <span class="summary-total">合计 <strong>¥{{ cartStore.totalAmount }}</strong></span>
        </div>
        <el-button type="primary" size="large" @click="handleCheckout">去结算</el-button>
      </div>
    </div>

    <!-- 订单确认弹窗 -->
    <el-dialog v-model="dialogVisible" title="确认订单" width="480px">
      <el-form :model="orderForm" :rules="orderRules" ref="orderFormRef" label-position="top">
        <el-form-item label="收货人" prop="receiver">
          <el-input v-model="orderForm.receiver" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="orderForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="收货地址" prop="address">
          <el-input v-model="orderForm.address" type="textarea" placeholder="请输入详细收货地址" />
        </el-form-item>
        <div class="order-amount-display">订单金额 <span>¥{{ cartStore.totalAmount }}</span></div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrder" :loading="submitting">提交订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { orderApi } from '@/api/order'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()
const dialogVisible = ref(false)
const submitting = ref(false)
const orderFormRef = ref()
const defaultImage = 'https://picsum.photos/60/60?random=1'

const orderForm = reactive({ receiver: '', phone: '', address: '' })
const orderRules = {
  receiver: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }]
}

const handleCheckout = () => {
  orderForm.receiver = userStore.userInfo?.nickname || userStore.userInfo?.username || ''
  orderForm.phone = userStore.userInfo?.phone || ''
  orderForm.address = ''
  dialogVisible.value = true
}

const submitOrder = async () => {
  try { await orderFormRef.value.validate() } catch { return }
  submitting.value = true
  try {
    const res = await orderApi.create({
      userId: userStore.userInfo?.id, shopId: cartStore.shopId,
      items: cartStore.items.map(i => ({ dishId: i.dishId, quantity: i.quantity })),
      totalAmount: cartStore.totalAmount,
      receiver: orderForm.receiver, userPhone: orderForm.phone, address: orderForm.address
    })
    if (res.code === 200) { ElMessage.success('订单创建成功'); cartStore.clearCart(); dialogVisible.value = false; router.push('/orders') }
  } catch (error) { ElMessage.error(error.response?.data?.message || '订单创建失败') } finally { submitting.value = false }
}
</script>

<style scoped>
.cart-page { max-width: 800px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-md); }
.page-header h2 { font-size: 22px; font-weight: 700; color: var(--color-text); }
.empty-state { padding: var(--space-xl) 0; text-align: center; }
.cart-list { display: flex; flex-direction: column; gap: var(--space-sm); }
.cart-item {
  display: flex; align-items: center; gap: var(--space-sm);
  padding: var(--space-sm) var(--space-md);
  background: var(--color-surface); border-radius: var(--radius-md);
  border: 1px solid var(--color-border-light);
}
.cart-item-img { width: 56px; height: 56px; border-radius: var(--radius-sm); object-fit: cover; }
.cart-item-info { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.cart-item-name { font-weight: 600; color: var(--color-text); }
.cart-item-price { font-size: 13px; color: var(--color-text-secondary); }
.cart-item-subtotal { font-weight: 600; color: var(--color-primary); min-width: 60px; text-align: right; }
.cart-summary {
  margin-top: var(--space-md); padding: var(--space-md);
  background: var(--color-surface); border-radius: var(--radius-md);
  border: 1px solid var(--color-border-light);
  display: flex; justify-content: space-between; align-items: center;
}
.summary-info { display: flex; flex-direction: column; gap: 4px; color: var(--color-text-secondary); }
.summary-total strong { font-size: 22px; color: var(--color-primary); }
.order-amount-display { text-align: center; padding: var(--space-sm) 0; font-size: 15px; color: var(--color-text-secondary); }
.order-amount-display span { font-size: 24px; font-weight: 700; color: var(--color-primary); margin-left: 8px; }
</style>
