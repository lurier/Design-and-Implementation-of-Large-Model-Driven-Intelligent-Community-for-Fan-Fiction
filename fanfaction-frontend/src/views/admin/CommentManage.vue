<template>
  <div class="comment-manage-page">
    <div class="page-header">
      <h1 class="page-title">评论管理</h1>
      <div class="filter-bar">
        <el-select
          v-model="statusFilter"
          placeholder="状态筛选"
          clearable
          style="width: 140px"
        >
          <el-option label="全部" :value="''" />
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已驳回" :value="2" />
          <el-option label="已删除" :value="3" />
        </el-select>
        <el-input
          v-model="keyword"
          placeholder="搜索评论内容..."
          :prefix-icon="Search"
          class="search-input"
          clearable
          style="width: 250px"
        />
      </div>
    </div>

    <el-card class="table-card">
      <el-table
        :data="commentList"
        v-loading="loading"
        style="width: 100%"
        empty-text="暂无评论数据"
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="articleTitle" label="所属文章" min-width="150" show-overflow-tooltip />
        <el-table-column prop="content" label="评论内容" min-width="250" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="comment-content">{{ row.content }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="评论者" width="120" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评论时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status !== 1"
              type="success"
              size="small"
              @click="handleApprove(row)"
              :loading="row._processing"
            >
              通过
            </el-button>
            <el-button
              v-if="row.status === 1 || row.status === 0"
              type="warning"
              size="small"
              @click="handleHide(row)"
              :loading="row._processing"
            >
              隐藏
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
              :loading="row._processing"
            >
              删除
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
          @size-change="loadComments"
          @current-change="loadComments"
        />
      </div>
    </el-card>

    <!-- 操作确认弹窗 -->
    <el-dialog
      v-model="confirmDialogVisible"
      :title="confirmDialogTitle"
      width="400px"
      :close-on-click-modal="false"
    >
      <p>{{ confirmDialogMessage }}</p>
      <div class="comment-preview" v-if="currentComment">
        <p class="preview-label">评论内容：</p>
        <p class="preview-content">{{ currentComment.content }}</p>
      </div>
      <template #footer>
        <el-button @click="confirmDialogVisible = false">取消</el-button>
        <el-button :type="confirmDialogType" @click="handleConfirm" :loading="processing">
          {{ confirmDialogButtonText }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getAdminCommentList, updateCommentStatus, deleteComment } from '@/api/admin'

const loading = ref(false)
const processing = ref(false)
const commentList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref('')
const keyword = ref('')

const confirmDialogVisible = ref(false)
const confirmDialogTitle = ref('')
const confirmDialogMessage = ref('')
const confirmDialogType = ref('primary')
const confirmDialogButtonText = ref('确定')
const currentComment = ref(null)
const currentAction = ref('')

const loadComments = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    if (statusFilter.value !== '') {
      params.status = parseInt(statusFilter.value)
    }
    
    const res = await getAdminCommentList(params)
    commentList.value = (res.data?.records || []).map(item => ({
      ...item,
      _processing: false
    }))
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('加载评论列表失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleApprove = (comment) => {
  currentComment.value = comment
  currentAction.value = 'approve'
  confirmDialogTitle.value = '确认通过'
  confirmDialogMessage.value = `确定要通过这条评论吗？`
  confirmDialogType.value = 'success'
  confirmDialogButtonText.value = '确认通过'
  confirmDialogVisible.value = true
}

const handleHide = (comment) => {
  currentComment.value = comment
  currentAction.value = 'hide'
  confirmDialogTitle.value = '确认隐藏'
  confirmDialogMessage.value = `确定要隐藏这条评论吗？隐藏后用户将无法看到。`
  confirmDialogType.value = 'warning'
  confirmDialogButtonText.value = '确认隐藏'
  confirmDialogVisible.value = true
}

const handleDelete = (comment) => {
  currentComment.value = comment
  currentAction.value = 'delete'
  confirmDialogTitle.value = '确认删除'
  confirmDialogMessage.value = `确定要删除这条评论吗？删除后无法恢复。`
  confirmDialogType.value = 'danger'
  confirmDialogButtonText.value = '确认删除'
  confirmDialogVisible.value = true
}

const handleConfirm = async () => {
  processing.value = true
  const comment = currentComment.value
  comment._processing = true
  
  try {
    if (currentAction.value === 'delete') {
      await deleteComment(comment.id)
      ElMessage.success('删除成功')
    } else {
      const status = currentAction.value === 'approve' ? 1 : 2
      await updateCommentStatus({
        commentId: comment.id,
        status: status
      })
      ElMessage.success(currentAction.value === 'approve' ? '通过成功' : '隐藏成功')
      comment.status = status
    }
    confirmDialogVisible.value = false
    
    if (currentAction.value === 'delete') {
      loadComments()
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    processing.value = false
    comment._processing = false
  }
}

const getStatusText = (status) => {
  const map = {
    0: '待审核',
    1: '已通过',
    2: '已驳回',
    3: '已删除'
  }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = {
    0: 'warning',
    1: 'success',
    2: 'info',
    3: 'danger'
  }
  return map[status] || 'info'
}

const formatDate = (dateStr) => {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm:ss')
}

onMounted(() => {
  loadComments()
})
</script>

<style scoped>
.comment-manage-page {
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

.comment-content {
  color: #606266;
}

.comment-preview {
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.preview-label {
  font-weight: 500;
  margin: 0 0 8px 0;
  color: #606266;
}

.preview-content {
  margin: 0;
  color: #303133;
  line-height: 1.6;
}
</style>
