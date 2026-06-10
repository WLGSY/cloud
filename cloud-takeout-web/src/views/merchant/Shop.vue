<template>
  <div class="shop-page">
    <h2 class="page-title">🏪 店铺管理</h2>
    <div class="form-card" v-loading="loading">
      <el-form :model="form" label-position="top" class="shop-form">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="店铺名称"><el-input v-model="form.name" placeholder="请输入店铺名称" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Logo URL"><el-input v-model="form.logo" placeholder="图片链接" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="联系电话"><el-input v-model="form.phone" placeholder="联系电话" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="店铺地址"><el-input v-model="form.address" placeholder="店铺地址" /></el-form-item>
          </el-col>
        </el-row>
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
        <el-button type="primary" @click="saveShop" :loading="saving">保存店铺信息</el-button>
        <el-tag v-if="shopId" effect="plain" style="margin-left:12px">ID: {{ shopId }}</el-tag>
      </el-form>
    </div>

    <!-- 预览 -->
    <div v-if="shopId" class="preview-card" style="margin-top:16px">
      <h3 class="card-title">店铺预览</h3>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="名称">{{ form.name }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag type="success" effect="plain" size="small">营业中</el-tag></el-descriptions-item>
        <el-descriptions-item label="电话">{{ form.phone }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ form.address }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ form.openTime }} - {{ form.closeTime }}</el-descriptions-item>
        <el-descriptions-item label="简介">{{ form.description }}</el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { shopApi } from '@/api/shop'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const saving = ref(false)
const loading = ref(true)
const shopId = ref(null)
const form = reactive({ name: '', logo: '', phone: '', address: '', openTime: '09:00', closeTime: '22:00', description: '' })

onMounted(async () => {
  try {
    const res = await shopApi.getMyShop()
    if (res.code === 200 && res.data) {
      const s = res.data; shopId.value = s.id
      form.name = s.name || ''; form.logo = s.logo || ''; form.phone = s.phone || ''
      form.address = s.address || ''; form.openTime = s.openTime || '09:00'
      form.closeTime = s.closeTime || '22:00'; form.description = s.description || ''
      userStore.userInfo.shopId = s.id
    }
  } catch {} finally { loading.value = false }
})

const saveShop = async () => {
  saving.value = true
  try {
    const res = await shopApi.save({ ...form })
    if (res.code === 200) { ElMessage.success('保存成功'); if (res.data) { shopId.value = res.data.id; userStore.userInfo.shopId = res.data.id } }
  } catch { ElMessage.error('保存失败') } finally { saving.value = false }
}
</script>

<style scoped>
.shop-page { max-width: 700px; }
.page-title { font-size: 22px; font-weight: 700; color: var(--color-text); margin-bottom: var(--space-md); }
.form-card { background: var(--color-surface); border-radius: var(--radius-lg); border: 1px solid var(--color-border-light); padding: var(--space-md); }
.time-range { display: flex; align-items: center; gap: 10px; }
.time-range span { color: var(--color-text-secondary); }
.preview-card { background: var(--color-surface); border-radius: var(--radius-lg); border: 1px solid var(--color-border-light); padding: var(--space-md); }
.card-title { font-size: 16px; font-weight: 600; color: var(--color-text); margin-bottom: var(--space-sm); }
</style>
