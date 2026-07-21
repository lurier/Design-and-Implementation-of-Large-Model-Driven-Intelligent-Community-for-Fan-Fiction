<template>
  <div class="content-review-page">
    <div class="page-header">
      <h2 class="page-title">内容审核</h2>
      <div class="filter-bar">
        <el-select v-model="activeTab" class="tab-select" @change="handleTabChange">
          <el-option label="全部" value="all" />
          <el-option label="待审核文章" value="article" />
          <el-option label="创作者申请" value="creator" />
        </el-select>
        <el-input
          v-model="keyword"
          placeholder="搜索..."
          :prefix-icon="Search"
          class="search-input"
          clearable
        />
      </div>
    </div>
    
    <div class="review-table">
      <el-table :data="tableData" border>
        <el-table-column prop="title" label="标题/名称" min-width="200" />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'article' ? 'primary' : 'warning'">
              {{ scope.row.type === 'article' ? '文章' : '申请' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="160" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleApprove(scope.row)">通过</el-button>
            <el-button size="small" type="danger" @click="handleReject(scope.row)">驳回</el-button>
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
import { ElMessage } from 'element-plus'

const activeTab = ref('all')
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const tableData = ref([])

const getStatusType = (status) => {
  const types = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已驳回'
  }
  return texts[status] || status
}

const fetchData = async () => {
  // 模拟数据
  tableData.value = [
    { id: 1, title: '《天官赐福》同人短篇：花灯夜', author: '墨香铜臭', type: 'article', status: 'pending', createTime: '2024-01-15 10:30' },
    { id: 2, title: '魔道祖师剧情分析', author: '忘羡CP粉', type: 'article', status: 'pending', createTime: '2024-01-15 09:20' },
    { id: 3, title: '用户「江南」创作者申请', author: '系统', type: 'creator', status: 'pending', createTime: '2024-01-14 16:45' },
    { id: 4, title: '渣反同人：冰妹的现代生活', author: '冰秋党', type: 'article', status: 'pending', createTime: '2024-01-14 14:30' },
    { id: 5, title: '用户「九歌」创作者申请', author: '系统', type: 'creator', status: 'pending', createTime: '2024-01-13 11:20' }
  ]
  total.value = 5
}

const handleTabChange = () => {
  currentPage.value = 1
  fetchData()
}

const handleApprove = (row) => {
  row.status = 'approved'
  ElMessage.success('操作成功')
}

const handleReject = (row) => {
  row.status = 'rejected'
  ElMessage.success('操作成功')
}

onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.content-review-page {
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
      
      .tab-select {
        width: 140px;
      }
      
      .search-input {
        width: 220px;
      }
    }
  }
  
  .review-table {
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
