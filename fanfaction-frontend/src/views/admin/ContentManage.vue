<template>
  <div class="content-manage-page">
    <div class="page-header">
      <h1 class="page-title">文章管理</h1>
      <div class="filter-bar">
        <el-select
          v-model="statusFilter"
          placeholder="状态筛选"
          clearable
          style="width: 140px"
        >
          <el-option label="全部" :value="''" />
          <el-option label="已发布" :value="1" />
          <el-option label="待审核" :value="0" />
          <el-option label="已驳回" :value="2" />
          <el-option label="已删除" :value="3" />
        </el-select>
        <el-input
          v-model="keyword"
          placeholder="搜索文章标题..."
          :prefix-icon="Search"
          class="search-input"
          clearable
          style="width: 250px"
          @keyup.enter="loadArticles"
        />
      </div>
    </div>

    <el-card class="table-card">
      <el-table
        :data="articleList"
        v-loading="loading"
        style="width: 100%"
        empty-text="暂无文章数据"
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="文章标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="authorNickname" label="作者昵称" width="120" />
        <el-table-column prop="authorUsername" label="用户名" width="120" />
        <el-table-column prop="viewCount" label="阅读量" width="100" />
        <el-table-column prop="commentCount" label="评论数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewComment" label="审核意见" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="280" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              @click="viewDetail(row)"
            >
              查看详情
            </el-button>
            <el-divider direction="vertical" />
            <el-button
              v-if="row.status === 0"
              type="success"
              size="small"
              @click="openAuditDialog(row, 1)"
              :loading="row._auditing"
            >
              通过
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="danger"
              size="small"
              @click="openAuditDialog(row, 2)"
              :loading="row._auditing"
            >
              驳回
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              size="small"
              @click="openAuditDialog(row, 3)"
              :loading="row._auditing"
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
          @size-change="loadArticles"
          @current-change="loadArticles"
        />
      </div>
    </el-card>

    <!-- 审核弹窗 -->
    <el-dialog
      v-model="auditDialogVisible"
      :title="auditDialogTitle"
      width="460px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="auditFormRef"
        :model="auditForm"
        :rules="auditRules"
        label-width="80px"
      >
        <el-form-item label="文章标题">
          <span class="article-title">{{ currentArticle?.title }}</span>
        </el-form-item>
        <el-form-item label="作者">
          <span>{{ currentArticle?.authorNickname }} ({{ currentArticle?.authorUsername }})</span>
        </el-form-item>
        <el-form-item v-if="auditAction === 'reject'" label="驳回原因" prop="reviewComment">
          <el-input
            v-model="auditForm.reviewComment"
            type="textarea"
            :rows="4"
            placeholder="请输入驳回原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item v-if="auditAction === 'delete'" label="删除原因" prop="reviewComment">
          <el-input
            v-model="auditForm.reviewComment"
            type="textarea"
            :rows="4"
            placeholder="请输入删除原因（选填）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button :type="auditButtonType" @click="submitAudit" :loading="processing">
          {{ auditButtonText }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 文章详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="文章详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="currentArticle" class="article-detail">
        <h3>{{ currentArticle.title }}</h3>
        <div class="detail-meta">
          <span>作者：{{ currentArticle.authorNickname }}</span>
          <span>阅读量：{{ currentArticle.viewCount }}</span>
          <span>评论数：{{ currentArticle.commentCount }}</span>
          <span>状态：{{ getStatusText(currentArticle.status) }}</span>
        </div>
        <div v-if="currentArticle.reviewComment" class="review-comment">
          <p class="comment-label">审核意见：</p>
          <p class="comment-content">{{ currentArticle.reviewComment }}</p>
        </div>
        <div class="article-content" v-html="currentArticle.content"></div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getAdminArticleList, auditArticle } from '@/api/admin'

const loading = ref(false)
const processing = ref(false)
const articleList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref('')
const keyword = ref('')

const auditDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const auditFormRef = ref(null)
const currentArticle = ref(null)
const auditAction = ref('')

const auditForm = reactive({
  reviewComment: ''
})

const auditRules = {
  reviewComment: [
    { required: true, message: '请输入驳回原因', trigger: 'blur' },
    { min: 2, message: '驳回原因至少2个字符', trigger: 'blur' }
  ]
}

const auditDialogTitle = ref('')
const auditButtonType = ref('primary')
const auditButtonText = ref('确定')

const loadArticles = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    if (statusFilter.value !== '') {
      params.status = parseInt(statusFilter.value)
    }
    if (keyword.value.trim()) {
      params.keyword = keyword.value.trim()
    }
    
    const res = await getAdminArticleList(params)
    articleList.value = (res.data?.records || []).map(item => ({
      ...item,
      _auditing: false
    }))
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('加载文章列表失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = (article) => {
  currentArticle.value = article
  detailDialogVisible.value = true
}

const openAuditDialog = (article, action) => {
  currentArticle.value = article
  auditForm.reviewComment = ''
  
  if (action === 1) {
    auditAction.value = 'approve'
    auditDialogTitle.value = '确认通过'
    auditButtonType.value = 'success'
    auditButtonText.value = '确认通过'
  } else if (action === 2) {
    auditAction.value = 'reject'
    auditDialogTitle.value = '确认驳回'
    auditButtonType.value = 'danger'
    auditButtonText.value = '确认驳回'
  } else {
    auditAction.value = 'delete'
    auditDialogTitle.value = '确认删除'
    auditButtonType.value = 'danger'
    auditButtonText.value = '确认删除'
  }
  
  auditDialogVisible.value = true
}

const submitAudit = async () => {
  if (auditAction.value === 'reject' && auditFormRef.value) {
    await auditFormRef.value.validate(async (valid) => {
      if (!valid) return
      await doAudit()
    })
  } else {
    await doAudit()
  }
}

const doAudit = async () => {
  processing.value = true
  const article = currentArticle.value
  article._auditing = true
  
  try {
    let status = 0
    if (auditAction.value === 'approve') {
      status = 1
    } else if (auditAction.value === 'reject') {
      status = 2
    } else {
      status = 3
    }
    
    await auditArticle({
      articleId: article.id,
      status: status,
      reviewComment: auditForm.reviewComment || ''
    })
    
    ElMessage.success(auditAction.value === 'approve' ? '审核通过' : (auditAction.value === 'reject' ? '已驳回' : '已删除'))
    
    article.status = status
    article.reviewComment = auditForm.reviewComment
    
    auditDialogVisible.value = false
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('操作失败')
  } finally {
    processing.value = false
    article._auditing = false
  }
}

const getStatusText = (status) => {
  const map = {
    0: '待审核',
    1: '已发布',
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
  loadArticles()
})
</script>

<style scoped>
.content-manage-page {
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

.article-title {
  color: #409eff;
  font-weight: 500;
}

.article-detail h3 {
  margin-top: 0;
  margin-bottom: 16px;
}

.detail-meta {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
  color: #606266;
  font-size: 14px;
}

.review-comment {
  background-color: #fff7f0;
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 16px;
  
  .comment-label {
    font-weight: 600;
    color: #e6a23c;
    margin-bottom: 8px;
  }
  
  .comment-content {
    color: #666;
    line-height: 1.6;
  }
}

.article-content {
  max-height: 400px;
  overflow-y: auto;
  padding: 16px;
  background-color: #fafafa;
  border-radius: 4px;
  line-height: 1.8;
  color: #303133;
}
</style>
