<template>
  <div class="dishes-page">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">🍽️ 菜品管理</h2>
        <!-- 店铺选择器 -->
        <el-select v-model="selectedShopId" placeholder="选择店铺" @change="onShopChange" class="shop-select">
          <el-option v-for="s in shops" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </div>
      <el-button type="primary" @click="showAddDialog" :disabled="!selectedShopId">添加菜品</el-button>
    </div>

    <!-- 空提示 -->
    <el-empty v-if="shops.length === 0" description="请先创建店铺" />

    <el-table v-else :data="dishList" v-loading="loading" class="dish-table">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="菜品名称" />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{row}">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="categoryId" label="分类" width="100">
        <template #default="{row}">{{ catMap[row.categoryId] || '其他' }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.status===1?'success':'info'" size="small" effect="plain">{{ row.status===1?'在售':'已下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sales" label="销量" width="80" />
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button link type="primary" size="small" @click="editDish(row)">编辑</el-button>
          <el-button link :type="row.status===1?'warning':'success'" size="small" @click="toggleStatus(row)">{{ row.status===1?'下架':'上架' }}</el-button>
          <el-button link type="danger" size="small" @click="deleteDish(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar" v-if="shops.length > 0">
      <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="loadDishes" />
    </div>

    <!-- 添加/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editing ? '编辑菜品' : '添加菜品'" width="480px">
      <el-form :model="dishForm" label-position="top">
        <el-form-item label="菜品名称"><el-input v-model="dishForm.name" placeholder="请输入名称" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="价格"><el-input-number v-model="dishForm.price" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="分类">
            <el-select v-model="dishForm.categoryId" placeholder="选择分类" style="width:100%">
              <el-option v-for="cat in categories" :key="cat.value" :label="cat.label" :value="cat.value" />
            </el-select>
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="描述"><el-input v-model="dishForm.description" type="textarea" placeholder="菜品描述" /></el-form-item>
        <el-form-item label="图片URL"><el-input v-model="dishForm.image" placeholder="图片地址" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDish" :loading="saving">{{ editing ? '更新' : '添加' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dishApi } from '@/api/dish'
import { shopApi } from '@/api/shop'

const dishList = ref([])
const pageNum = ref(1)
const pageSize = ref(8)  // 每页8个
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const editing = ref(false)
const saving = ref(false)
const dishForm = reactive({ name: '', price: 0, categoryId: null, description: '', image: '' })
let editId = null

// 多店铺支持
const shops = ref([])
const selectedShopId = ref(null)

const categories = [{ label: '热销推荐', value: 1 }, { label: '主食', value: 2 }, { label: '小炒', value: 3 }, { label: '汤类', value: 4 }]
const catMap = Object.fromEntries(categories.map(c => [c.value, c.label]))

const loadShops = async () => {
  try {
    const res = await shopApi.getMyShops()
    if (res.code === 200) {
      shops.value = Array.isArray(res.data) ? res.data : (res.data ? [res.data] : [])
      if (shops.value.length > 0 && !selectedShopId.value) {
        selectedShopId.value = shops.value[0].id
        loadDishes()
      }
    }
  } catch {}
}

const onShopChange = () => {
  pageNum.value = 1
  loadDishes()
}

const loadDishes = async () => {
  if (!selectedShopId.value) return
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value, shopId: selectedShopId.value }
    const res = await dishApi.getList(params)
    if (res.code === 200) { dishList.value = res.data.list || []; total.value = res.data.total || 0 }
  } catch {} finally { loading.value = false }
}

const showAddDialog = () => { editing.value = false; editId = null; Object.assign(dishForm, { name: '', price: 0, categoryId: null, description: '', image: '' }); dialogVisible.value = true }
const editDish = (row) => { editing.value = true; editId = row.id; Object.assign(dishForm, row); dialogVisible.value = true }

const saveDish = async () => {
  saving.value = true
  try {
    const data = { ...dishForm, shopId: selectedShopId.value }
    const res = editing.value ? await dishApi.update({ ...data, id: editId }) : await dishApi.add(data)
    if (res.code === 200) { ElMessage.success(editing.value ? '更新成功' : '添加成功'); dialogVisible.value = false; loadDishes() }
  } catch { ElMessage.error('操作失败') } finally { saving.value = false }
}

const toggleStatus = async (row) => {
  try {
    const res = row.status === 1 ? await dishApi.recall(row.id) : await dishApi.publish(row.id)
    if (res.code === 200) { ElMessage.success(row.status === 1 ? '已下架' : '已上架'); loadDishes() }
  } catch { ElMessage.error('操作失败') }
}

const deleteDish = async (id) => {
  try { await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' }); const res = await dishApi.delete(id); if (res.code === 200) { ElMessage.success('删除成功'); loadDishes() } } catch {}
}

onMounted(() => loadShops())
</script>

<style scoped>
.dishes-page { max-width: 1100px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-md); gap: var(--space-md); flex-wrap: wrap; }
.header-left { display: flex; align-items: center; gap: var(--space-md); }
.page-title { font-size: 22px; font-weight: 700; color: var(--color-text); }
.shop-select { width: 180px; }
.pagination-bar { margin-top: var(--space-md); text-align: center; }
</style>
