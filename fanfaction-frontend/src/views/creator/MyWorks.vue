<template>
  <div class="my-works-page">
    <div class="page-header">
      <h1 class="page-title">我的作品</h1>
      <el-button type="primary" @click="$router.push('/creator/publish')">
        <el-icon><Plus /></el-icon>
        发布新作品
      </el-button>
    </div>

    <el-card class="works-card">
      <!-- 状态筛选 Tab -->
      <div class="status-tabs">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="全部" name="ALL" />
          <el-tab-pane label="草稿" name="DRAFT" />
          <el-tab-pane label="审核中" name="PENDING" />
          <el-tab-pane label="已发布" name="APPROVED" />
          <el-tab-pane label="已驳回" name="REJECTED" />
        </el-tabs>
      </div>

      <!-- 作品列表 -->
      <el-table
        :data="works"
        v-loading="loading"
        style="width: 100%"
        empty-text="暂无作品"
      >
        <el-table-column prop="title" label="作品标题" min-width="200">
          <template #default="{ row }">
            <span class="title-link">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="viewCount" label="阅读量" width="100" sortable />
        <el-table-column prop="likeCount" label="点赞数" width="100" sortable />
        <el-table-column prop="status" label="状态" width="140">
          <template #default="{ row }">
            <div class="status-cell">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
              <!-- 驳回意见悬浮提示 -->
              <el-popover
                v-if="row.status === 'REJECTED' && row.reviewComment"
                placement="top"
                width="300"
                trigger="hover"
              >
                <template #reference>
                  <el-button type="text" class="comment-btn">查看意见</el-button>
                </template>
                <div class="review-comment">
                  <p class="comment-label">审核意见：</p>
                  <p class="comment-content">{{ row.reviewComment }}</p>
                </div>
              </el-popover>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" sortable>
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'APPROVED'"
              link
              type="primary"
              @click="viewWork(row.id)"
            >
              查看
            </el-button>
            <el-button
              link
              type="primary"
              @click="editWork(row.id)"
            >
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              @click="deleteWork(row)"
            >
              删除
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
          @size-change="loadWorks"
          @current-change="loadWorks"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getMyWorks, deleteWork as deleteWorkApi } from '@/api/creator'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const works = ref([])
const activeTab = ref('ALL')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 状态映射
const statusMap = {
  ALL: '',
  DRAFT: 'DRAFT',
  PENDING: 'PENDING',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED'
}

// Tab切换处理
const handleTabChange = () => {
  currentPage.value = 1
  loadWorks()
}

// 加载作品列表
const loadWorks = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      status: statusMap[activeTab.value] || undefined
    }
    
    const res = await getMyWorks(params)
    // 将后端返回的数字状态转换为字符串状态
    works.value = (res.data.records || res.data || []).map(item => ({
      ...item,
      status: getStatusString(item.status)
    }))
    total.value = res.data.total || 0
  } catch (error) {
    console.error('加载作品列表失败:', error)
    ElMessage.error('加载作品列表失败')
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

// 查看作品
const viewWork = (id) => {
  router.push(`/article/${id}`)
}

// 编辑作品
const editWork = (id) => {
  router.push(`/creator/publish?id=${id}`)
}

// 删除作品
const deleteWork = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除作品「${row.title}」吗？删除后无法恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteWorkApi(row.id)
    ElMessage.success('删除成功')
    loadWorks()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除作品失败:', error)
      ElMessage.error('删除作品失败')
    }
  }
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
    PENDING: '审核中',
    APPROVED: '已发布',
    REJECTED: '已驳回'
  }
  return textMap[status] || status
}

// 格式化日期
const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  loadWorks()
})
</script>

<style lang="scss" scoped>
.my-works-page {
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

  .works-card {
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

    .status-tabs {
      margin-bottom: 16px;
      border-bottom: 1px solid #e8e8e8;
    }

    .title-link {
      color: #409eff;
      cursor: pointer;
      &:hover {
        text-decoration: underline;
      }
    }

    .status-cell {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .comment-btn {
      padding: 0;
      font-size: 12px;
      color: #909399;
    }

    .review-comment {
      .comment-label {
        font-weight: 600;
        margin-bottom: 8px;
        color: #606266;
      }
      .comment-content {
        color: #909399;
        line-height: 1.6;
      }
    }

    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
