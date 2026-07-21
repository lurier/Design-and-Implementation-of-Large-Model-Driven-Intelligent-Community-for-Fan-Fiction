<template>
  <div class="user-management-page">
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
      <div class="filter-bar">
        <el-select v-model="roleFilter" class="role-select" @change="handleFilterChange">
          <el-option label="全部角色" value="all" />
          <el-option label="普通用户" value="user" />
          <el-option label="创作者" value="creator" />
          <el-option label="管理员" value="admin" />
        </el-select>
        <el-input
          v-model="keyword"
          placeholder="搜索用户名..."
          :prefix-icon="Search"
          class="search-input"
          clearable
        />
      </div>
    </div>
    
    <div class="user-table">
      <el-table :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag :type="getRoleType(scope.row.role)">
              {{ getRoleText(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-switch 
              :value="scope.row.status === 'active'" 
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="registerTime" label="注册时间" width="160" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">详情</el-button>
            <el-button size="small" type="primary" @click="editUser(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteUser(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const roleFilter = ref('all')
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const tableData = ref([])

const getRoleType = (role) => {
  const types = {
    admin: 'danger',
    creator: 'primary',
    user: 'success'
  }
  return types[role] || 'info'
}

const getRoleText = (role) => {
  const texts = {
    admin: '管理员',
    creator: '创作者',
    user: '普通用户'
  }
  return texts[role] || role
}

const fetchData = async () => {
  // 模拟数据
  tableData.value = [
    { id: 1, username: 'admin', nickname: '系统管理员', email: 'admin@fanfaction.com', role: 'admin', status: 'active', registerTime: '2024-01-01 00:00' },
    { id: 2, username: 'jiangnan', nickname: '江南', email: 'jiangnan@example.com', role: 'creator', status: 'active', registerTime: '2024-01-10 14:30' },
    { id: 3, username: 'jiuge', nickname: '九歌', email: 'jiuge@example.com', role: 'creator', status: 'active', registerTime: '2024-01-12 09:15' },
    { id: 4, username: 'user001', nickname: '读者小王', email: 'user001@example.com', role: 'user', status: 'active', registerTime: '2024-01-13 16:45' },
    { id: 5, username: 'user002', nickname: '书迷小李', email: 'user002@example.com', role: 'user', status: 'inactive', registerTime: '2024-01-14 11:20' }
  ]
  total.value = 5
}

const handleFilterChange = () => {
  currentPage.value = 1
  fetchData()
}

const handleStatusChange = (row) => {
  row.status = row.status === 'active' ? 'inactive' : 'active'
  ElMessage.success('状态已更新')
}

const viewDetail = (row) => {
  ElMessage.info(`查看用户 ${row.username} 的详情`)
}

const editUser = (row) => {
  ElMessage.info(`编辑用户 ${row.username}`)
}

const deleteUser = (row) => {
  ElMessageBox.confirm(
    `确定要删除用户 ${row.username} 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    tableData.value = tableData.value.filter(item => item.id !== row.id)
    total.value--
    ElMessage.success('删除成功')
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.user-management-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .page-title {
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }
    
    .filter-bar {
      display: flex;
      gap: 12px;
      align-items: center;
      
      .role-select {
        width: 140px;
      }
      
      .search-input {
        width: 220px;
      }
    }
  }
  
  .user-table {
    background: #fff;
    border-radius: 8px;
    padding: 16px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    
    :deep(.el-table) {
      --el-table-header-text-color: #606266;
      --el-table-row-hover-bg-color: #f5f7fa;
    }
  }
  
  .pagination-wrapper {
    display: flex;
    justify-content: center;
    padding: 16px 0;
  }
}
</style>
