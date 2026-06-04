<template>
  <div class="points">
    <h2>⭐ 我的积分</h2>

    <!-- 积分卡片 -->
    <el-card class="points-card">
      <div class="points-info">
        <div class="points-number">{{ pointsInfo.totalPoints || 0 }}</div>
        <div class="points-label">当前总积分</div>
        <div class="points-tip">消费1元 = {{ pointsInfo.rate || 10 }} 积分</div>
      </div>
    </el-card>

    <!-- 积分规则 -->
    <div class="points-rule">
      <el-collapse>
        <el-collapse-item title="积分规则说明" name="1">
          <div class="rule-content">
            <p>1. 每消费1元可获得10积分</p>
            <p>2. 积分可用于兑换优惠券</p>
            <p>3. 积分有效期：1年</p>
            <p>4. 订单取消后积分将自动扣除</p>
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
import { ref, onMounted } from 'vue'
import { pointsApi } from '@/api/points'

const pointsInfo = ref({})
const pointsLog = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

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
.points-card {
  margin-bottom: 20px;
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
