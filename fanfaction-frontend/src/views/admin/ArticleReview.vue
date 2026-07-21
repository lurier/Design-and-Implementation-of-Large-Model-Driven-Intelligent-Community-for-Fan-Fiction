<template>
  <div class="admin-article-review-page">
    <Layout>
      <div class="page-container">
        <div class="page-header">
          <h1 class="page-title">文章审核</h1>
          <el-button type="primary" @click="loadArticles">
            <el-icon><Refresh /></el-icon>
            刷新列表
          </el-button>
        </div>

        <el-card class="table-card">
          <!-- 状态筛选 -->
          <div class="filter-bar">
            <el-select
              v-model="filterStatus"
              placeholder="筛选状态"
              clearable
              style="width: 150px"
              @change="loadArticles"
            >
              <el-option label="待审核" value="PENDING" />
              <el-option label="已发布" value="APPROVED" />
              <el-option label="已驳回" value="REJECTED" />
            </el-select>
          </div>

          <!-- 文章列表 -->
          <el-table
            :data="articles"
            v-loading="loading"
            style="width: 100%"
            empty-text="暂无待审核的文章"
          >
            <el-table-column prop="title" label="文章标题" min-width="200" />
            <el-table-column prop="authorId" label="作者ID" width="100" />
            <el-table-column prop="viewCount" label="阅读量" width="100" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="提交时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="280" fixed="right">
              <template #default="{ row }">
                <el-button
                  link
                  type="primary"
                  @click="viewDetail(row)"
                >
                  查看详情
                </el-button>
                <el-button
                  v-if="row.status === 'PENDING' || row.status === 'REJECTED'"
                  type="success"
                  @click="handleApprove(row)"
                  :loading="row._approving"
                >
                  通过
                </el-button>
                <el-button
                  v-if="row.status === 'PENDING' || row.status === 'APPROVED'"
                  type="danger"
                  @click="handleReject(row)"
                >
                  驳回
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination">
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

        <!-- 详情弹窗 -->
        <el-dialog
          v-model="detailDialogVisible"
          title="文章详情"
          width="800px"
          :close-on-click-modal="false"
        >
          <div v-if="currentArticle" class="article-detail">
            <h3>{{ currentArticle.title }}</h3>
            <div class="detail-meta">
              <span>作者ID: {{ currentArticle.authorId }}</span>
              <span>状态: {{ getStatusText(currentArticle.status) }}</span>
              <span>阅读量: {{ currentArticle.viewCount }}</span>
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

        <!-- 驳回原因弹窗 -->
        <el-dialog
          v-model="rejectDialogVisible"
          title="驳回文章"
          width="460px"
          :close-on-click-modal="false"
        >
          <el-form
            ref="rejectFormRef"
            :model="rejectForm"
            :rules="rejectRules"
            label-width="80px"
          >
            <el-form-item label="文章标题">
              <span>{{ currentArticle?.title }}</span>
            </el-form-item>
            <el-form-item label="驳回原因" prop="comment">
              <el-input
                v-model="rejectForm.comment"
                type="textarea"
                :rows="4"
                placeholder="请输入驳回原因"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="rejectDialogVisible = false">取消</el-button>
            <el-button type="danger" @click="submitReject" :loading="rejecting">
              确认驳回
            </el-button>
          </template>
        </el-dialog>
      </div>
    </Layout>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import axios from 'axios'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const rejecting = ref(false)
const articles = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterStatus = ref('')

const detailDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const rejectFormRef = ref(null)
const currentArticle = ref(null)

const rejectForm = reactive({
  comment: ''
})

const rejectRules = {
  comment: [
    { required: true, message: '请输入驳回原因', trigger: 'blur' },
    { min: 2, message: '驳回原因至少2个字符', trigger: 'blur' }
  ]
}

// 加载文章列表
const loadArticles = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    let url = '/api/articles/review/pending'
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    // 根据筛选条件选择不同的API
    if (filterStatus.value) {
      url = '/api/articles/admin/list'
      params.status = filterStatus.value
    }
    
    const res = await axios.get(url, {
      headers: { 'Authorization': `Bearer ${token}` },
      params
    })
    
    // 处理状态转换（数字转字符串）
    articles.value = (res.data.data?.records || res.data.data || []).map(item => ({
      ...item,
      status: getStatusString(item.status),
      _approving: false
    }))
    
    total.value = res.data.data?.total || 0
  } catch (error) {
    console.error('加载文章列表失败:', error)
    ElMessage.error('加载文章列表失败')
  } finally {
    loading.value = false
  }
}

// 状态映射：数字 -> 字符串
const getStatusString = (status) => {
  const map = {
    0: 'DRAFT',
    1: 'APPROVED',
    2: 'PENDING',
    3: 'REJECTED'
  }
  return map[status] || 'UNKNOWN'
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    DRAFT: 'info',
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    DRAFT: '草稿',
    PENDING: '待审核',
    APPROVED: '已发布',
    REJECTED: '已驳回'
  }
  return textMap[status] || status
}

// 查看详情
const viewDetail = (row) => {
  currentArticle.value = row
  detailDialogVisible.value = true
}

// 审核通过
const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要通过文章「${row.title}」吗？`,
      '确认通过',
      {
        confirmButtonText: '确定通过',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
  } catch {
    return
  }

  row._approving = true
  try {
    const token = localStorage.getItem('token')
    await axios.post(
      `/api/articles/${row.id}/review`,
      null,
      {
        params: { status: 'APPROVED' },
        headers: { 'Authorization': `Bearer ${token}` }
      }
    )

    ElMessage.success('审核通过')
    row.status = 'APPROVED'
    loadArticles()
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error(error.response?.data?.message || '审核失败')
  } finally {
    row._approving = false
  }
}

// 驳回
const handleReject = (row) => {
  currentArticle.value = row
  rejectForm.comment = ''
  rejectDialogVisible.value = true
}

// 提交驳回
const submitReject = async () => {
  if (!rejectFormRef.value) return

  await rejectFormRef.value.validate(async (valid) => {
    if (!valid) return

    rejecting.value = true
    try {
      const token = localStorage.getItem('token')
      await axios.post(
        `/api/articles/${currentArticle.value.id}/review`,
        null,
        {
          params: {
            status: 'REJECTED',
            comment: rejectForm.comment
          },
          headers: { 'Authorization': `Bearer ${token}` }
        }
      )

      ElMessage.success('已驳回该文章')
      rejectDialogVisible.value = false
      loadArticles()
    } catch (error) {
      console.error('驳回失败:', error)
      ElMessage.error(error.response?.data?.message || '驳回失败')
    } finally {
      rejecting.value = false
    }
  })
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  loadArticles()
})
</script>

<style lang="scss" scoped>
.admin-article-review-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.page-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin: 0;
  }
}

.table-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

  .filter-bar {
    margin-bottom: 16px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}

.article-detail {
  .detail-meta {
    display: flex;
    gap: 20px;
    margin: 16px 0;
    color: #666;
  }

  .review-comment {
    background-color: #fff7f0;
    padding: 12px;
    border-radius: 4px;
    margin: 16px 0;
    
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
    margin-top: 16px;
    line-height: 1.8;
  }
}
</style>