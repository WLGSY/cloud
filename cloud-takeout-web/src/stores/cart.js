import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  // 购物车数据（从localStorage读取）
  const items = ref(JSON.parse(localStorage.getItem('cart') || '[]'))

  // 购物车商品总数
  const totalCount = computed(() => {
    return items.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  // 购物车总金额
  const totalAmount = computed(() => {
    return items.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
  })

  // 当前购物车所属店铺ID
  const shopId = ref(null)

  // 添加商品
  const addItem = (dish) => {
    // 检查店铺归属：不同店铺的商品不能混在同一个购物车
    if (shopId.value && dish.shopId && shopId.value !== dish.shopId) {
      // 切换店铺，清空购物车
      items.value = []
    }
    if (dish.shopId) shopId.value = dish.shopId

    const existing = items.value.find(item => item.dishId === dish.id)
    if (existing) {
      existing.quantity++
    } else {
      items.value.push({
        dishId: dish.id,
        name: dish.name,
        price: dish.price,
        quantity: 1,
        image: dish.image,
        shopId: dish.shopId
      })
    }
    saveToLocal()
  }

  // 删除商品
  const removeItem = (dishId) => {
    const index = items.value.findIndex(item => item.dishId === dishId)
    if (index > -1) {
      items.value.splice(index, 1)
    }
    saveToLocal()
  }

  // 更新数量
  const updateQuantity = (dishId, quantity) => {
    const item = items.value.find(item => item.dishId === dishId)
    if (item) {
      if (quantity <= 0) {
        removeItem(dishId)
      } else {
        item.quantity = quantity
      }
    }
    saveToLocal()
  }

  // 清空购物车
  const clearCart = () => {
    items.value = []
    shopId.value = null
    saveToLocal()
  }

  // 保存到localStorage
  const saveToLocal = () => {
    localStorage.setItem('cart', JSON.stringify(items.value))
  }

  return {
    items,
    shopId,
    totalCount,
    totalAmount,
    addItem,
    removeItem,
    updateQuantity,
    clearCart
  }
})
