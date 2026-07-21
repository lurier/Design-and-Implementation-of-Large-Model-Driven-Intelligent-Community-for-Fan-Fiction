<template>
  <div class="admin-applications-page">
    <Layout>
      <div class="page-container">
        <div class="page-header">
          <h1 class="page-title">创作者申请审核</h1>
          <el-button type="primary" @click="loadApplications">
            <el-icon><Refresh /></el-icon>
            刷新列表
          </el-button>
        </div>

        <el-card class="table-card">
          <el-table
            :data="applications"
            v-loading="loading"
            style="width: 100%"
            empty-text="暂无待审核的申请"
          >
            <el-table-column prop="userId" label="申请人ID" width="100" />
            <el-table-column prop="penName" label="笔名" width="150" />
            <el-table-column prop="email" label="邮箱" width="220" />
            <el-table-column prop="createTime" label="申请时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="200" fixed="right">
              <template #default="{ row }">
                <el-button
                  type="success"
                  @click="handleApprove(row)"
                  :loading="row._approving"
                >
                  通过
                </el-button>
                <el-button
                  type="danger"
                  @click="handleReject(row)"
                >
                  驳回
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 驳回原因弹窗 -->
        <el-dialog
          v-model="rejectDialogVisible"
          title="驳回申请"
          width="460px"
          :close-on-click-modal="false"
        >
          <el-form
            ref="rejectFormRef"
            :model="rejectForm"
            :rules="rejectRules"
            label-width="80px"
          >
            <el-form-item label="申请人">
              <span>{{ currentApplication?.penName }}</span>
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
import { useUserStore } from '@/stores/user'
import axios from 'axios'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const rejecting = ref(false)
const applications = ref([])
const rejectDialogVisible = ref(false)
const rejectFormRef = ref(null)
const currentApplication = ref(null)

const rejectForm = reactive({
  comment: ''
})

const rejectRules = {
  comment: [
    { required: true, message: '请输入驳回原因', trigger: 'blur' },
    { min: 2, message: '驳回原因至少2个字符', trigger: 'blur' }
  ]
}

const loadApplications = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const res = await axios.get('/api/creator-application/list', {
      headers: { 'Authorization': `Bearer ${token}` },
      params: { status: 0 }
    })
    applications.value = (res.data.data || []).map(item => ({
      ...item,
      _approving: false
    }))
  } catch (error) {
    console.error('加载申请列表失败:', error)
    ElMessage.error('加载申请列表失败')
  } finally {
    loading.value = false
  }
}

// 审核通过
const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要通过申请人 "${row.penName}" 的创作者申请吗？`,
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
      '/api/creator-application/review',
      null,
      {
        params: { id: row.id, status: 1 },
        headers: { 'Authorization': `Bearer ${token}` }
      }
    )

    ElMessage.success('审核通过，该用户已升级为创作者')

    // 无感升级：刷新当前用户信息（如果管理员审核的是自己的申请）
    await userStore.refreshUserInfo()

    // 如果审核的是自己的申请，自动跳转到创作者中心
    const currentUserId = userStore.userInfo?.id
    if (currentUserId && currentUserId === row.userId) {
      ElMessage.success('您已升级为创作者，即将跳转到创作者中心')
      setTimeout(() => {
        router.push('/creator')
      }, 1500)
    }

    loadApplications()
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error(error.response?.data?.message || '审核失败')
  } finally {
    row._approving = false
  }
}

// 驳回
const handleReject = (row) => {
  currentApplication.value = row
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
        '/api/creator-application/review',
        null,
        {
          params: {
            id: currentApplication.value.id,
            status: 2,
            comment: rejectForm.comment
          },
          headers: { 'Authorization': `Bearer ${token}` }
        }
      )

      ElMessage.success('已驳回该申请')
      rejectDialogVisible.value = false
      loadApplications()
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
  loadApplications()
})
</script>

<style lang="scss" scoped>
.admin-applications-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.page-container {
  max-width: 1200px;
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
}
</style>
