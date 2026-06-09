<template>
  <div class="points">
    <h2>⭐ 我的积分</h2>

    <!-- 积分卡片 -->
    <el-row :gutter="20" class="points-overview">
      <el-col :span="12">
        <el-card class="points-card">
          <div class="points-info">
            <div class="points-number">{{ pointsInfo.totalPoints || 0 }}</div>
            <div class="points-label">当前总积分</div>
            <div class="points-tip">消费1元 = {{ pointsInfo.rate || 10 }} 积分</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="stats-card">
          <div class="stats-info">
            <div class="stats-number">{{ stats.totalEarned || 0 }}</div>
            <div class="stats-label">累计获得积分</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 积分规则 -->
    <div class="points-rule">
      <el-collapse>
        <el-collapse-item title="📋 积分规则说明" name="1">
          <div class="rule-content">
            <p>✅ 每消费<strong>1元</strong>可获得<strong>{{ pointsInfo.rate || 10 }}积分</strong></p>
            <p>🔝 单笔订单最高可获得<strong>{{ pointsInfo.maxPerOrder || 1000 }}积分</strong></p>
            <p>🔄 订单取消后积分将<strong>自动扣除</strong></p>
            <p>💡 积分功能当前状态：<strong>{{ pointsInfo.enabled ? '已开启' : '已关闭' }}</strong></p>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>

    <!-- 积分明细（对接后端） -->
    <div class="points-log">
      <h3>积分明细</h3>
      <el-table :data="pointsLog" style="width: 100%" v-loading="loading">
        <el-table-column prop="createTime" label="时间" width="180" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.points > 0 ? 'success' : 'danger'" size="small">
              {{ row.type === 'ORDER_PAID' ? '订单获得' : row.type === 'ORDER_CANCELLED' ? '订单取消' : '其他' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分变动" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.points > 0 ? '#67c23a' : '#f56c6c' }">
              {{ row.points > 0 ? '+' : '' }}{{ row.points }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="说明" />
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadPointsLog"
        />
      </div>
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

// 统计数据
const stats = computed(() => {
  const earned = pointsLog.value
    .filter(log => log.points > 0)
    .reduce((sum, log) => sum + log.points, 0)
  return { totalEarned: earned }
})

// 加载积分信息（对接后端）
const loadPointsInfo = async () => {
  try {
    const res = await pointsApi.getMyPoints()
    if (res.code === 200) {
      pointsInfo.value = res.data
    }
  } catch (error) {
    console.error('加载积分信息失败', error)
  }
}

// 加载积分明细（对接后端）
const loadPointsLog = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    const res = await pointsApi.getLog(params)
    if (res.code === 200) {
      pointsLog.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载积分明细失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPointsInfo()
  loadPointsLog()
})
</script>

<style scoped>
.points {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}
.points h2 {
  margin-bottom: 20px;
}
.points-overview {
  margin-bottom: 20px;
}
.points-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-align: center;
}
.points-number {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 8px;
}
.points-label {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 8px;
}
.points-tip {
  font-size: 12px;
  opacity: 0.7;
}
.stats-card {
  text-align: center;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}
.stats-number {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 8px;
}
.stats-label {
  font-size: 14px;
  opacity: 0.9;
}
.points-rule {
  margin-bottom: 30px;
}
.rule-content {
  padding: 16px;
  color: #666;
  line-height: 1.8;
}
.points-log h3 {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 20px;
  text-align: center;
}
</style>
