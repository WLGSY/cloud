<template>
  <div class="points-page">
    <h2 class="page-title">⭐ 我的积分</h2>

    <el-row :gutter="20" class="points-cards">
      <el-col :span="12">
        <div class="stat-card primary">
          <div class="stat-number">{{ pointsInfo.totalPoints || 0 }}</div>
          <div class="stat-label">当前积分</div>
          <div class="stat-desc">消费1元 ≈ {{ pointsInfo.rate || 10 }} 积分</div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="stat-card accent">
          <div class="stat-number">{{ stats.totalEarned || 0 }}</div>
          <div class="stat-label">累计获得</div>
          <div class="stat-desc">单笔上限 {{ pointsInfo.maxPerOrder || 1000 }}</div>
        </div>
      </el-col>
    </el-row>

    <h3 class="section-title">积分明细</h3>
    <el-table :data="pointsLog" v-loading="loading" class="log-table">
      <el-table-column prop="createTime" label="时间" width="170" />
      <el-table-column prop="type" label="类型" width="120">
        <template #default="{ row }">
          <el-tag :type="row.points > 0 ? 'success' : 'danger'" size="small" effect="plain">
            {{ row.type === 'ORDER_PAID' ? '下单获得' : row.type === 'ORDER_CANCELLED' ? '订单取消' : '其他' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="points" label="变动" width="100">
        <template #default="{ row }">
          <span :style="{ color: row.points > 0 ? 'var(--color-success)' : 'var(--color-danger)', fontWeight: 600 }">
            {{ row.points > 0 ? '+' : '' }}{{ row.points }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="说明" />
    </el-table>

    <div class="pagination-bar">
      <el-pagination v-model:current-page="pageNum" :page-size="pageSize"
        :total="total" layout="total, prev, pager, next" @current-change="loadPointsLog" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { pointsApi } from '@/api/points'

const pointsInfo = ref({})
const pointsLog = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

const stats = computed(() => ({
  totalEarned: pointsLog.value.filter(l => l.points > 0).reduce((s, l) => s + l.points, 0)
}))

const loadPointsInfo = async () => {
  try { const res = await pointsApi.getMyPoints(); if (res.code === 200) pointsInfo.value = res.data } catch {}
}
const loadPointsLog = async () => {
  loading.value = true
  try {
    const res = await pointsApi.getLog({ pageNum: pageNum.value, pageSize: pageSize.value })
    if (res.code === 200) { pointsLog.value = res.data.list || []; total.value = res.data.total || 0 }
  } catch {} finally { loading.value = false }
}

onMounted(() => { loadPointsInfo(); loadPointsLog() })
</script>

<style scoped>
.points-page { max-width: 800px; margin: 0 auto; }
.page-title { font-size: 22px; font-weight: 700; color: var(--color-text); margin-bottom: var(--space-md); }
.points-cards { margin-bottom: var(--space-lg); }
.stat-card {
  padding: var(--space-md); border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light); text-align: center;
}
.stat-card.primary { background: var(--color-primary-bg); }
.stat-card.accent { background: #fdf2f8; }
.stat-number { font-size: 40px; font-weight: 700; color: var(--color-text); letter-spacing: -0.02em; }
.stat-label { font-size: 15px; color: var(--color-text); margin: 4px 0; font-weight: 500; }
.stat-desc { font-size: 12px; color: var(--color-text-secondary); }
.section-title { font-size: 17px; font-weight: 600; color: var(--color-text); margin-bottom: var(--space-sm); }
.pagination-bar { margin-top: var(--space-md); text-align: center; }
</style>
