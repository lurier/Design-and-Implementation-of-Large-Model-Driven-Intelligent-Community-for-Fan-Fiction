<template>
  <div class="user-manage-page">
    <div class="page-header">
      <h1 class="page-title">用户管理</h1>
      <div class="filter-bar">
        <el-select
          v-model="roleFilter"
          placeholder="筛选角色"
          clearable
          style="width: 150px"
        >
          <el-option label="全部" value="" />
          <el-option label="读者" value="READER" />
          <el-option label="创作者" value="CREATOR" />
          <el-option label="管理员" value="ADMIN" />
        </el-select>
        <el-select
          v-model="statusFilter"
          placeholder="状态筛选"
          clearable
          style="width: 120px"
        >
          <el-option label="全部" value="" />
          <el-option label="正常" value="ACTIVE" />
          <el-option label="封禁" value="BLOCKED" />
        </el-select>
        <el-input
          v-model="keyword"
          placeholder="搜索昵称..."
          :prefix-icon="Search"
          class="search-input"
          clearable
          style="width: 200px"
        />
      </div>
    </div>

    <el-card class="table-card">
      <el-table
        :data="userList"
        v-loading="loading"
        style="width: 100%"
        empty-text="暂无用户数据"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :src="row.avatar" :size="48">
              <User />
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registerTime" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.registerTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'ACTIVE'"
              type="danger"
              size="small"
              @click="openBlockDialog(row)"
            >
              封禁
            </el-button>
            <el-button
              v-else
              type="success"
              size="small"
              @click="openUnblockDialog(row)"
            >
              解封
            </el-button>
            <el-divider direction="vertical" />
            <el-button
              v-if="row.role !== 'ADMIN'"
              type="primary"
              size="small"
              @click="openRoleDialog(row)"
            >
              修改角色
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </el-card>

    <!-- 封禁确认弹窗 -->
    <el-dialog
      v-model="blockDialogVisible"
      title="确认封禁"
      width="400px"
      :close-on-click-modal="false"
    >
      <p>确定要封禁用户 <span class="highlight">{{ currentUser?.nickname }}</span> 吗？</p>
      <p class="warning-text">封禁后该用户将无法登录系统</p>
      <template #footer>
        <el-button @click="blockDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleBlock" :loading="processing">
          确认封禁
        </el-button>
      </template>
    </el-dialog>

    <!-- 解封确认弹窗 -->
    <el-dialog
      v-model="unblockDialogVisible"
      title="确认解封"
      width="400px"
      :close-on-click-modal="false"
    >
      <p>确定要解封用户 <span class="highlight">{{ currentUser?.nickname }}</span> 吗？</p>
      <template #footer>
        <el-button @click="unblockDialogVisible = false">取消</el-button>
        <el-button type="success" @click="handleUnblock" :loading="processing">
          确认解封
        </el-button>
      </template>
    </el-dialog>

    <!-- 修改角色弹窗 -->
    <el-dialog
      v-model="roleDialogVisible"
      title="修改角色"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="roleForm" label-width="80px">
        <el-form-item label="当前角色">
          <el-tag :type="getRoleType(currentUser?.role)">
            {{ getRoleText(currentUser?.role) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="新角色" prop="role">
          <el-select v-model="roleForm.role" placeholder="请选择角色">
            <el-option label="读者" value="READER" />
            <el-option label="创作者" value="CREATOR" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRoleChange" :loading="processing">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, User } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getAdminUserList, updateUserStatus, updateUserRole } from '@/api/admin'

const loading = ref(false)
const processing = ref(false)
const userList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const roleFilter = ref('')
const statusFilter = ref('')
const keyword = ref('')

// 弹窗状态
const blockDialogVisible = ref(false)
const unblockDialogVisible = ref(false)
const roleDialogVisible = ref(false)
const currentUser = ref(null)

const roleForm = reactive({
  role: ''
})

const loadUsers = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    if (roleFilter.value) {
      params.role = roleFilter.value
    }
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    if (keyword.value.trim()) {
      params.keyword = keyword.value.trim()
    }
    
    const res = await getAdminUserList(params)
    userList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const openBlockDialog = (user) => {
  currentUser.value = user
  blockDialogVisible.value = true
}

const openUnblockDialog = (user) => {
  currentUser.value = user
  unblockDialogVisible.value = true
}

const openRoleDialog = (user) => {
  currentUser.value = user
  roleForm.role = user.role === 'CREATOR' ? 'CREATOR' : 'READER'
  roleDialogVisible.value = true
}

const handleBlock = async () => {
  processing.value = true
  try {
    await updateUserStatus({
      userId: currentUser.value.id,
      status: 'BLOCKED'
    })
    
    currentUser.value.status = 'BLOCKED'
    ElMessage.success('封禁成功')
    blockDialogVisible.value = false
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    processing.value = false
  }
}

const handleUnblock = async () => {
  processing.value = true
  try {
    await updateUserStatus({
      userId: currentUser.value.id,
      status: 'ACTIVE'
    })
    
    currentUser.value.status = 'ACTIVE'
    ElMessage.success('解封成功')
    unblockDialogVisible.value = false
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    processing.value = false
  }
}

const handleRoleChange = async () => {
  processing.value = true
  try {
    await updateUserRole({
      userId: currentUser.value.id,
      role: roleForm.role
    })
    
    currentUser.value.role = roleForm.role
    ElMessage.success('角色修改成功')
    roleDialogVisible.value = false
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    processing.value = false
  }
}

const getRoleText = (role) => {
  const map = {
    ADMIN: '管理员',
    CREATOR: '创作者',
    READER: '读者'
  }
  return map[role] || role
}

const getRoleType = (role) => {
  const map = {
    ADMIN: 'danger',
    CREATOR: 'primary',
    READER: 'success'
  }
  return map[role] || 'info'
}

const getStatusText = (status) => {
  const map = {
    ACTIVE: '正常',
    BLOCKED: '封禁'
  }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = {
    ACTIVE: 'success',
    BLOCKED: 'danger'
  }
  return map[status] || 'info'
}

const formatDate = (dateStr) => {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm:ss')
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-manage-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.highlight {
  color: #409eff;
  font-weight: 500;
}

.warning-text {
  color: #f56c6c;
  font-size: 14px;
}
</style>
