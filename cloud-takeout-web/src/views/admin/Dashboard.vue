<template>
  <div class="dashboard">
    <h2 class="page-title">📊 数据总览</h2>
    <el-row :gutter="16" class="stats-row">
      <el-col :span="8">
        <div class="stat-card"><div class="stat-number">{{ stats.totalUsers }}</div><div class="stat-label">用户总数</div></div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card"><div class="stat-number">{{ stats.totalMerchants }}</div><div class="stat-label">商家数</div></div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card"><div class="stat-number">{{ stats.totalAdmins }}</div><div class="stat-label">管理员数</div></div>
      </el-col>
    </el-row>

    <div class="quick-panel">
      <h3 class="panel-title">⚡ 快捷管理</h3>
      <div class="quick-actions">
        <button class="action-btn primary" @click="$router.push('/admin/users')">👥 用户管理</button>
        <button class="action-btn warning" @click="$router.push('/admin/merchants')">🏪 商家审核</button>
        <button class="action-btn accent" @click="$router.push('/')">🔍 预览用户端</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const stats = ref({ totalUsers: 0, totalMerchants: 0, totalAdmins: 0 })

onMounted(async () => {
  try { const res = await request.get('/api/user/stats'); if (res.code === 200) stats.value = res.data } catch {}
})
</script>

<style scoped>
.dashboard { max-width: 900px; }
.page-title { font-size: 22px; font-weight: 700; color: var(--color-text); margin-bottom: var(--space-md); }
.stats-row { margin-bottom: var(--space-md); }
.stat-card {
  padding: var(--space-lg); text-align: center;
  background: var(--color-surface); border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
}
.stat-number { font-size: 38px; font-weight: 700; color: var(--color-primary); letter-spacing: -0.03em; }
.stat-label { font-size: 14px; color: var(--color-text-secondary); margin-top: 8px; }
.quick-panel {
  background: var(--color-surface); border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light); padding: var(--space-md);
}
.panel-title { font-size: 16px; font-weight: 600; color: var(--color-text); margin-bottom: var(--space-sm); }
.quick-actions { display: flex; gap: 10px; }
.action-btn {
  flex: 1; padding: 16px; border-radius: var(--radius-sm); border: none;
  font-size: 15px; font-weight: 500; cursor: pointer; transition: all var(--transition);
}
.action-btn.primary { background: var(--color-primary-bg); color: var(--color-primary); }
.action-btn.warning { background: #fffbeb; color: var(--color-warning); }
.action-btn.accent { background: #ecfdf5; color: var(--color-success); }
.action-btn:hover { filter: brightness(0.95); }
</style>
