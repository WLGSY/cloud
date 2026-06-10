<template>
  <div class="users-page">
    <h2 class="page-title">👥 用户管理</h2>

    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索用户名/手机号" clearable @clear="loadUsers" @keyup.enter="loadUsers" style="width:220px" />
      <el-select v-model="roleFilter" placeholder="角色筛选" clearable @change="loadUsers" style="width:150px;margin-left:12px">
        <el-option label="全部" value="" />
        <el-option label="普通用户" value="user" />
        <el-option label="商家" value="merchant" />
        <el-option label="管理员" value="admin" />
      </el-select>
      <el-button type="primary" @click="loadUsers" style="margin-left:12px">搜索</el-button>
    </div>

    <el-table :data="userList" v-loading="loading" class="user-table">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="100" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="email" label="邮箱" width="150" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{row}">
          <el-tag :type="row.role==='admin'?'danger':row.role==='merchant'?'warning':'info'" size="small" effect="plain">
            {{ roleMap[row.role] || '用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="160" />
      <el-table-column label="操作" width="160">
        <template #default="{row}">
          <el-button link type="primary" size="small" @click="changeRole(row)">改角色</el-button>
          <el-button link type="danger" size="small" @click="deleteUser(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar"><el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="total, prev, pager, next" @current-change="loadUsers" /></div>

    <el-dialog v-model="roleDialogVisible" title="修改用户角色" width="400px">
      <p>用户：<strong>{{ currentUser?.username }}</strong></p>
      <p>当前角色：<el-tag :type="currentUser?.role==='admin'?'danger':'info'" size="small" effect="plain">{{ roleMap[currentUser?.role] }}</el-tag></p>
      <el-select v-model="newRole" placeholder="选择新角色" style="width:100%;margin-top:12px">
        <el-option label="普通用户" value="user" />
        <el-option label="商家" value="merchant" />
        <el-option label="管理员" value="admin" />
      </el-select>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmChangeRole" :loading="roleLoading">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const userList = ref([]); const pageNum = ref(1); const pageSize = ref(10)
const total = ref(0); const loading = ref(false)
const keyword = ref(''); const roleFilter = ref('')
const roleDialogVisible = ref(false); const roleLoading = ref(false)
const currentUser = ref(null); const newRole = ref('')
const roleMap = { user: '普通用户', merchant: '商家', admin: '管理员' }

const loadUsers = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (keyword.value) params.keyword = keyword.value
    if (roleFilter.value) params.role = roleFilter.value
    const res = await request.get('/api/user/list', { params })
    if (res.code === 200) { userList.value = res.data.list || []; total.value = res.data.total || 0 }
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

const changeRole = (row) => { currentUser.value = row; newRole.value = row.role; roleDialogVisible.value = true }

const confirmChangeRole = async () => {
  roleLoading.value = true
  try {
    const res = await request.put(`/api/user/${currentUser.value.id}/role`, { role: newRole.value })
    if (res.code === 200) { ElMessage.success('角色更新成功'); roleDialogVisible.value = false; loadUsers() }
  } catch { ElMessage.error('更新失败') } finally { roleLoading.value = false }
}

const deleteUser = async (id) => {
  try { await ElMessageBox.confirm('确认删除该用户？', '警告', { type: 'warning' }); ElMessage.info('删除功能需后端支持') } catch {}
}

onMounted(() => loadUsers())
</script>

<style scoped>
.users-page { max-width: 1100px; }
.page-title { font-size: 22px; font-weight: 700; color: var(--color-text); margin-bottom: var(--space-md); }
.toolbar { display: flex; align-items: center; margin-bottom: var(--space-sm); }
.user-table { margin-top: var(--space-sm); }
.pagination-bar { margin-top: var(--space-md); text-align: center; }
</style>
