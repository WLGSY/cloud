<template>
  <div class="shop-page">
    <div class="page-header">
      <h2 class="page-title">🏪 店铺管理</h2>
      <el-button type="primary" @click="showAddDialog">添加新店铺</el-button>
    </div>

    <!-- 店铺列表 -->
    <div v-loading="loading" class="shop-list">
      <div v-for="shop in shops" :key="shop.id" class="shop-card">
        <div class="shop-card-header">
          <div class="shop-avatar" :style="{ background: colorFor(shop.id) }">
            {{ shop.name ? shop.name.charAt(0) : '?' }}
          </div>
          <div class="shop-card-info">
            <h3>{{ shop.name }}</h3>
            <p>{{ shop.address || '未设置地址' }}</p>
            <el-tag :type="shop.status === 1 ? 'success' : 'info'" size="small" effect="plain">
              {{ shop.status === 1 ? '营业中' : '已关闭' }}
            </el-tag>
          </div>
        </div>
        <div class="shop-card-footer">
          <span class="shop-id">ID: {{ shop.id }}</span>
          <el-button size="small" @click="editShop(shop)">编辑</el-button>
        </div>
      </div>

      <el-empty v-if="!loading && shops.length === 0" description="还没有店铺">
        <el-button type="primary" @click="showAddDialog">创建第一个店铺</el-button>
      </el-empty>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editing ? '编辑店铺' : '添加新店铺'" width="520px">
      <el-form :model="form" label-position="top">
        <el-form-item label="店铺名称">
          <el-input v-model="form.name" placeholder="请输入店铺名称" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Logo URL"><el-input v-model="form.logo" placeholder="图片链接" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话"><el-input v-model="form.phone" placeholder="电话" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="店铺地址">
          <el-input v-model="form.address" placeholder="店铺地址" />
        </el-form-item>
        <el-form-item label="营业时间">
          <div class="time-range">
            <el-time-picker v-model="form.openTime" format="HH:mm" value-format="HH:mm" placeholder="开始" />
            <span>—</span>
            <el-time-picker v-model="form.closeTime" format="HH:mm" value-format="HH:mm" placeholder="结束" />
          </div>
        </el-form-item>
        <el-form-item label="店铺简介">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="店铺简介" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.statusBool" active-text="营业中" inactive-text="已关闭"
            inline-prompt :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveShop" :loading="saving">
          {{ editing ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { shopApi } from '@/api/shop'

const shops = ref([])
const loading = ref(true)
const dialogVisible = ref(false)
const editing = ref(false)
const saving = ref(false)
let editId = null

const form = reactive({ name: '', logo: '', phone: '', address: '', openTime: '09:00', closeTime: '22:00', description: '', statusBool: 1 })

const colors = ['#FF6B35', '#3B82F6', '#10B981', '#8B5CF6', '#F59E0B', '#E84A5F']
const colorFor = (id) => colors[(id - 1) % colors.length]

const loadShops = async () => {
  loading.value = true
  try {
    const res = await shopApi.getMyShop()
    if (res.code === 200) {
      // 返回数组（多店铺）
      shops.value = Array.isArray(res.data) ? res.data : (res.data ? [res.data] : [])
    }
  } catch {} finally { loading.value = false }
}

const showAddDialog = () => {
  editing.value = false
  editId = null
  Object.assign(form, { name: '', logo: '', phone: '', address: '', openTime: '09:00', closeTime: '22:00', description: '', statusBool: 1 })
  dialogVisible.value = true
}

const editShop = (shop) => {
  editing.value = true
  editId = shop.id
  form.name = shop.name || ''
  form.logo = shop.logo || ''
  form.phone = shop.phone || ''
  form.address = shop.address || ''
  form.openTime = shop.openTime || '09:00'
  form.closeTime = shop.closeTime || '22:00'
  form.description = shop.description || ''
  form.statusBool = shop.status || 1
  dialogVisible.value = true
}

const saveShop = async () => {
  saving.value = true
  try {
    const data = {
      name: form.name, logo: form.logo, phone: form.phone, address: form.address,
      openTime: form.openTime, closeTime: form.closeTime, description: form.description,
      status: form.statusBool
    }
    let res
    if (editing.value) {
      data.id = editId
      res = await shopApi.save(data)
    } else {
      res = await shopApi.create(data)
    }
    if (res.code === 200) {
      ElMessage.success(editing.value ? '保存成功' : '创建成功')
      dialogVisible.value = false
      loadShops()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch { ElMessage.error('操作失败') } finally { saving.value = false }
}

onMounted(() => loadShops())
</script>

<style scoped>
.shop-page { max-width: 900px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-md); }
.page-title { font-size: 22px; font-weight: 700; color: var(--color-text); }
.shop-list { display: flex; flex-direction: column; gap: var(--space-sm); }
.shop-card {
  background: var(--color-surface); border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light); padding: var(--space-md);
  display: flex; justify-content: space-between; align-items: center;
  transition: all var(--transition);
}
.shop-card:hover { box-shadow: var(--shadow-sm); border-color: var(--color-primary); }
.shop-card-header { display: flex; gap: var(--space-sm); align-items: center; }
.shop-avatar {
  width: 48px; height: 48px; border-radius: var(--radius-md);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 22px; font-weight: 700; flex-shrink: 0;
}
.shop-card-info h3 { font-size: 16px; font-weight: 600; color: var(--color-text); margin-bottom: 2px; }
.shop-card-info p { font-size: 13px; color: var(--color-text-secondary); margin-bottom: 4px; }
.shop-card-footer { display: flex; align-items: center; gap: var(--space-sm); }
.shop-id { font-size: 12px; color: var(--color-text-placeholder); }
.time-range { display: flex; align-items: center; gap: 10px; }
.time-range span { color: var(--color-text-secondary); }
</style>
