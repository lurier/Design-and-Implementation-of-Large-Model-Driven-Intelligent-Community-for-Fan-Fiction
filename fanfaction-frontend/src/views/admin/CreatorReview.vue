<template>
  <div class="admin-review-page">
    <Layout>
      <div class="page-container">
        <div class="page-header">
          <h1 class="page-title">创作者申请审核</h1>
          <el-button type="primary" @click="loadApplications">
            <el-icon><Refresh /></el-icon>
            刷新列表
          </el-button>
        </div>

        <!-- 筛选栏 -->
        <el-card class="filter-card">
          <el-form :inline="true">
            <el-form-item label="审核状态">
              <el-select v-model="filterStatus" placeholder="全部" clearable @change="loadApplications">
                <el-option label="审核中" :value="0" />
                <el-option label="已通过" :value="1" />
                <el-option label="已拒绝" :value="2" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 申请列表 -->
        <el-card class="table-card">
          <el-table
            :data="applications"
            v-loading="loading"
            style="width: 100%"
            empty-text="暂无申请记录"
          >
            <el-table-column prop="userId" label="用户 ID" width="80" />
            <el-table-column prop="penName" label="笔名" width="120" />
            <el-table-column prop="expertise" label="擅长领域" width="150" />
            <el-table-column prop="introduction" label="个人简介" min-width="200" show-overflow-tooltip />
            <el-table-column prop="contact" label="联系方式" width="150" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="申请时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 0"
                  type="primary"
                  link
                  @click="handleReview(row)"
                >
                  审核
                </el-button>
                <el-button
                  type="primary"
                  link
                  @click="viewDetail(row)"
                >
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 审核对话框 -->
        <el-dialog
          v-model="reviewDialogVisible"
          title="审核申请"
          width="500px"
        >
          <el-form
            ref="reviewFormRef"
            :model="reviewForm"
            :rules="reviewRules"
            label-width="80px"
          >
            <el-form-item label="笔名">
              <span>{{ currentApplication?.penName }}</span>
            </el-form-item>
            <el-form-item label="个人简介">
              <span>{{ currentApplication?.introduction }}</span>
            </el-form-item>
            <el-form-item label="审核结果" prop="status">
              <el-radio-group v-model="reviewForm.status">
                <el-radio :label="1">通过</el-radio>
                <el-radio :label="2">拒绝</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item
              label="审核意见"
              prop="comment"
              v-if="reviewForm.status === 2"
            >
              <el-input
                v-model="reviewForm.comment"
                type="textarea"
                :rows="3"
                placeholder="请输入拒绝原因（必填）"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="reviewDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitReview" :loading="submitting">
              提交
            </el-button>
          </template>
        </el-dialog>

        <!-- 详情对话框 -->
        <el-dialog
          v-model="detailDialogVisible"
          title="申请详情"
          width="600px"
        >
          <el-descriptions :column="1" border v-if="currentApplication">
            <el-descriptions-item label="用户 ID">{{ currentApplication.userId }}</el-descriptions-item>
            <el-descriptions-item label="笔名">{{ currentApplication.penName }}</el-descriptions-item>
            <el-descriptions-item label="擅长领域">{{ currentApplication.expertise }}</el-descriptions-item>
            <el-descriptions-item label="个人简介">{{ currentApplication.introduction }}</el-descriptions-item>
            <el-descriptions-item label="代表作品">{{ currentApplication.representativeWork || '无' }}</el-descriptions-item>
            <el-descriptions-item label="联系方式">{{ currentApplication.contact }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(currentApplication.status)">
                {{ getStatusText(currentApplication.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="审核意见" v-if="currentApplication.reviewComment">
              {{ currentApplication.reviewComment }}
            </el-descriptions-item>
            <el-descriptions-item label="申请时间">
              {{ formatDate(currentApplication.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="审核时间" v-if="currentApplication.reviewTime">
              {{ formatDate(currentApplication.reviewTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-dialog>
      </div>
    </Layout>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import axios from 'axios'
import dayjs from 'dayjs'

const loading = ref(false)
const submitting = ref(false)
const applications = ref([])
const filterStatus = ref(null)
const reviewDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const reviewFormRef = ref(null)
const currentApplication = ref(null)

const reviewForm = reactive({
  status: 1,
  comment: ''
})

const reviewRules = {
  status: [
    { required: true, message: '请选择审核结果', trigger: 'change' }
  ],
  comment: [
    { required: true, message: '请输入拒绝原因', trigger: 'blur' }
  ]
}

// 加载申请列表
const loadApplications = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const params = filterStatus.value !== null ? { status: filterStatus.value } : {}
    
    const res = await axios.get('/api/creator-application/list', {
      headers: {
        'Authorization': `Bearer ${token}`
      },
      params
    })
    
    applications.value = res.data.data || []
  } catch (error) {
    console.error('加载申请列表失败:', error)
    ElMessage.error('加载申请列表失败')
  } finally {
    loading.value = false
  }
}

// 处理审核
const handleReview = (row) => {
  currentApplication.value = row
  reviewForm.status = 1
  reviewForm.comment = ''
  reviewDialogVisible.value = true
}

// 查看详情
const viewDetail = (row) => {
  currentApplication.value = row
  detailDialogVisible.value = true
}

// 提交审核
const submitReview = async () => {
  if (!reviewFormRef.value) return
  
  await reviewFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    
    try {
      const token = localStorage.getItem('token')
      await axios.post(
        '/api/creator-application/review',
        null,
        {
          params: {
            id: currentApplication.value.id,
            status: reviewForm.status,
            comment: reviewForm.comment
          },
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      )
      
      ElMessage.success('审核成功')
      reviewDialogVisible.value = false
      loadApplications()
    } catch (error) {
      console.error('审核失败:', error)
      ElMessage.error(error.response?.data?.message || '审核失败')
    } finally {
      submitting.value = false
    }
  })
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    0: '审核中',
    1: '已通过',
    2: '已拒绝'
  }
  return textMap[status] || '未知'
}

// 格式化日期
const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  loadApplications()
})
</script>

<style lang="scss" scoped>
.admin-review-page {
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

.filter-card,
.table-card {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}
</style>
