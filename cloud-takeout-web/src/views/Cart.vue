<template>
  <div class="cart">
    <h2>🛒 我的购物车</h2>

    <!-- 空购物车 -->
    <div v-if="cartStore.items.length === 0" class="empty-cart">
      <el-empty description="购物车还是空的，快去逛逛吧">
        <el-button type="primary" @click="goHome">去逛逛</el-button>
      </el-empty>
    </div>

    <!-- 购物车列表 -->
    <div v-else>
      <el-table :data="cartStore.items" style="width: 100%">
        <el-table-column label="商品" min-width="200">
          <template #default="{ row }">
            <div class="cart-item">
              <img :src="row.image || defaultImage" class="cart-img" alt="商品图片">
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="单价" width="120">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>

        <el-table-column label="数量" width="150">
          <template #default="{ row }">
            <el-input-number
              v-model="row.quantity"
              :min="1"
              :max="99"
              size="small"
              @change="(val) => cartStore.updateQuantity(row.dishId, val)"
            />
          </template>
        </el-table-column>

        <el-table-column label="小计" width="120">
          <template #default="{ row }">¥{{ row.price * row.quantity }}</template>
        </el-table-column>

        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" link @click="cartStore.removeItem(row.dishId)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 购物车汇总 -->
      <div class="cart-summary">
        <div class="total">
          共 <strong>{{ cartStore.totalCount }}</strong> 件商品
          <span class="total-price">合计：¥{{ cartStore.totalAmount }}</span>
        </div>
        <el-button type="primary" size="large" @click="handleCheckout">
          去结算
        </el-button>
      </div>
    </div>

    <!-- 订单确认对话框 -->
    <el-dialog v-model="dialogVisible" title="确认订单" width="500px">
      <el-form :model="orderForm" :rules="orderRules" ref="orderFormRef" label-width="100px">
        <el-form-item label="收货人" prop="receiver">
          <el-input v-model="orderForm.receiver" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="orderForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="收货地址" prop="address">
          <el-input v-model="orderForm.address" type="textarea" placeholder="请输入详细收货地址" />
        </el-form-item>
        <el-form-item label="订单金额">
          <span class="order-amount">¥{{ cartStore.totalAmount }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrder" :loading="submitting">
          提交订单
        </el-button>
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

const orderForm = reactive({
  receiver: '',
  phone: '',
  address: ''
})

const orderRules = {
  receiver: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  address: [{ required: true, message: '请输入收货地址', trigger: 'blur' }]
}

const goHome = () => {
  router.push('/')
}

const handleCheckout = () => {
  if (cartStore.items.length === 0) {
    ElMessage.warning('购物车是空的')
    return
  }
  orderForm.receiver = userStore.userInfo?.nickname || userStore.userInfo?.username || ''
  orderForm.phone = userStore.userInfo?.phone || ''
  orderForm.address = ''
  dialogVisible.value = true
}

const submitOrder = async () => {
  const valid = await orderFormRef.value.validate()
  if (!valid) return

  submitting.value = true
  try {
    // 对接后端订单创建接口（实训6已实现）
    const orderData = {
      userId: userStore.userInfo?.id,
      shopId: cartStore.shopId,
      items: cartStore.items.map(item => ({
        dishId: item.dishId,
        quantity: item.quantity
      })),
      totalAmount: cartStore.totalAmount,
      receiver: orderForm.receiver,
      userPhone: orderForm.phone,
      address: orderForm.address
    }

    const res = await orderApi.create(orderData)
    if (res.code === 200) {
      ElMessage.success('订单创建成功')
      cartStore.clearCart()
      dialogVisible.value = false
      router.push('/orders')
    }
  } catch (error) {
    console.error('提交订单失败', error)
    ElMessage.error(error.response?.data?.message || '订单创建失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.cart {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}
.cart h2 {
  margin-bottom: 20px;
}
.empty-cart {
  text-align: center;
  padding: 60px 0;
}
.cart-item {
  display: flex;
  align-items: center;
  gap: 12px;
}
.cart-img {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  object-fit: cover;
}
.cart-summary {
  margin-top: 20px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.total {
  font-size: 16px;
}
.total-price {
  font-size: 24px;
  font-weight: bold;
  color: #ff6b35;
  margin-left: 20px;
}
.order-amount {
  font-size: 20px;
  font-weight: bold;
  color: #ff6b35;
}
</style>
