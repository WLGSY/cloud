<template>
  <div class="merchants-page">
    <h2 class="page-title">🏪 商家审核</h2>

    <el-table :data="merchantList" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="商家账号" width="120" />
      <el-table-column prop="shopName" label="店铺名称" width="140" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="email" label="邮箱" width="160" />
      <el-table-column prop="createTime" label="注册时间" width="160" />
      <el-table-column label="状态" width="100">
        <template #default>
          <el-tag type="success" size="small" effect="plain">已通过</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{row}">
          <el-button link type="primary" size="small" @click="viewDetail(row)">查看详情</el-button>
          <el-button link type="danger" size="small" @click="reject(row)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar"><el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="total, prev, pager, next" @current-change="loadMerchants" /></div>

    <el-dialog v-model="detailVisible" title="商家详情" width="500px">
      <template v-if="detailUser">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="ID">{{ detailUser.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ detailUser.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ detailUser.nickname }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detailUser.phone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ detailUser.email }}</el-descriptions-item>
          <el-descriptions-item label="角色"><el-tag type="warning" size="small" effect="plain">商家</el-tag></el-descriptions-item>
          <el-descriptions-item label="注册时间" :span="2">{{ detailUser.createTime }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { shopApi } from '@/api/shop'

const merchantList = ref([]); const pageNum = ref(1); const pageSize = ref(10)
const total = ref(0); const loading = ref(false)
const detailVisible = ref(false); const detailUser = ref(null)

const loadMerchants = async () => {
  loading.value = true
  try {
    const [userRes, shopRes] = await Promise.all([
      request.get('/api/user/list', { params: { pageNum: pageNum.value, pageSize: pageSize.value, role: 'merchant' } }),
      shopApi.getList({ pageNum: 1, pageSize: 100 })
    ])
    if (userRes.code === 200) {
      const users = userRes.data.list || []
      const shops = shopRes.code === 200 ? (shopRes.data.list || []) : []
      merchantList.value = users.map(u => {
        const shop = shops.find(s => s.userId === u.id)
        return { ...u, shopName: shop?.name || '未创建店铺', shopStatus: shop?.status }
      })
      total.value = userRes.data.total || 0
    }
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

const viewDetail = (row) => { detailUser.value = row; detailVisible.value = true }

const reject = async (row) => {
  try { await ElMessageBox.confirm(`确认驳回商家 "${row.username}"？`, '警告', { type: 'warning' }); ElMessage.info('驳回功能需后端支持') } catch {}
}

onMounted(() => loadMerchants())
</script>

<style scoped>
.merchants-page { max-width: 1100px; }
.page-title { font-size: 22px; font-weight: 700; color: var(--color-text); margin-bottom: var(--space-md); }
.pagination-bar { margin-top: var(--space-md); text-align: center; }
</style>
