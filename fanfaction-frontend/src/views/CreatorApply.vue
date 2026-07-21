<template>
  <Layout>
    <div class="creator-apply-page">
      <div class="apply-container">
        <!-- 头部 -->
        <div class="page-header">
          <h1 class="page-title">申请成为创作者</h1>
          <p class="page-subtitle">用文字创造无限可能，与同好共筑想象世界</p>
        </div>

        <!-- 申请表单 -->
        <el-card class="apply-card" v-if="!hasApplied">
          <div class="benefits">
            <h3>创作者权益</h3>
            <ul>
              <li><el-icon><Check /></el-icon> 发布同人作品，分享创作灵感</li>
              <li><el-icon><Check /></el-icon> 获得阅读量和粉丝关注</li>
              <li><el-icon><Check /></el-icon> 建立个人品牌影响力</li>
              <li><el-icon><Check /></el-icon> 与其他创作者交流学习</li>
            </ul>
          </div>

          <el-form
            ref="formRef"
            :model="formData"
            :rules="formRules"
            label-width="100px"
            class="apply-form"
          >
            <el-form-item label="笔名" prop="penName">
              <el-input
                v-model="formData.penName"
                placeholder="请输入您的创作笔名"
                maxlength="20"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="formData.email"
                placeholder="请输入您的邮箱地址"
                maxlength="100"
              />
            </el-form-item>

            <el-form-item label="创作简介" prop="introduction">
              <el-input
                v-model="formData.introduction"
                type="textarea"
                :rows="4"
                placeholder="请简要介绍您的创作经历和擅长风格（100-500 字）"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="代表作品" prop="representativeWork">
              <el-input
                v-model="formData.representativeWork"
                type="textarea"
                :rows="3"
                placeholder="请提供您的代表作品链接或简介（可选）"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="联系方式" prop="contact">
              <el-input
                v-model="formData.contact"
                placeholder="请输入您的微信号或其他联系方式"
                maxlength="50"
              />
            </el-form-item>

            <el-form-item>
              <el-alert
                title="温馨提示"
                type="info"
                :closable="false"
                show-icon
              >
                <template #default>
                  <p>提交申请后，我们将在 3 个工作日内完成审核。</p>
                  <p>审核结果将通过站内消息通知您，请注意查看。</p>
                </template>
              </el-alert>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSubmit" :loading="submitting" size="large">
                提交申请
              </el-button>
              <el-button @click="$router.back()" size="large">取消</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 已申请状态 -->
        <el-card class="status-card" v-else>
          <div class="status-content">
            <el-result
              icon="info"
              title="申请已提交"
              sub-title="我们正在审核您的申请，请耐心等待"
            >
              <template #extra>
                <div class="status-info">
                  <div class="status-item">
                    <span class="label">申请状态：</span>
                    <el-tag type="warning" size="large">审核中</el-tag>
                  </div>
                  <div class="status-item">
                    <span class="label">申请时间：</span>
                    <span>{{ applyData?.createTime }}</span>
                  </div>
                  <div class="status-item">
                    <span class="label">笔名：</span>
                    <span>{{ applyData?.penName }}</span>
                  </div>
                </div>
                <div class="status-actions">
                  <el-button type="primary" @click="$router.push('/profile')">
                    返回个人中心
                  </el-button>
                  <el-button @click="$router.push('/')">
                    返回首页
                  </el-button>
                </div>
              </template>
            </el-result>
          </div>
        </el-card>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import { useUserStore } from '@/stores/user'
import { getCreatorApplication, submitCreatorApplication } from '@/api/creator'

const userStore = useUserStore()
const formRef = ref(null)
const submitting = ref(false)
const hasApplied = ref(false)
const applyData = ref(null)

const formData = reactive({
  penName: '',
  email: '',
  expertise: '',
  introduction: '',
  representativeWork: '',
  contact: ''
})

const formRules = {
  penName: [
    { required: true, message: '请输入创作笔名', trigger: 'blur' },
    { min: 2, max: 20, message: '笔名长度在 2-20 个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  introduction: [
    { required: true, message: '请输入创作简介', trigger: 'blur' },
    { min: 20, max: 500, message: '创作简介长度在 20-500 个字符之间', trigger: 'blur' }
  ],
  contact: [
    { required: true, message: '请输入联系方式', trigger: 'blur' }
  ]
}

// 检查是否已申请
const checkApplication = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    const res = await getCreatorApplication()
    if (res.data) {
      hasApplied.value = true
      applyData.value = res.data
    }
  } catch (error) {
    if (error.response?.status !== 404) {
      console.error('查询申请状态失败:', error)
    }
  }
}

// 提交申请
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    
    try {
    await submitCreatorApplication(formData)
    
    ElMessage.success('申请提交成功，请耐心等待审核')
      hasApplied.value = true
      await checkApplication()
    } catch (error) {
      console.error('提交申请失败:', error)
      ElMessage.error(error.response?.data?.message || '提交失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  checkApplication()
})
</script>

<style lang="scss" scoped>
.creator-apply-page {
  min-height: calc(100vh - 60px);
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 40px 20px;
}

.apply-container {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
  
  .page-title {
    font-size: 32px;
    color: #333;
    margin-bottom: 12px;
    font-weight: 600;
  }
  
  .page-subtitle {
    font-size: 16px;
    color: #666;
  }
}

.apply-card,
.status-card {
  border-radius: 16px;
  
  :deep(.el-card__body) {
    padding: 32px;
  }
}

.benefits {
  margin-bottom: 32px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-radius: 12px;
  
  h3 {
    font-size: 18px;
    color: #333;
    margin-bottom: 16px;
  }
  
  ul {
    list-style: none;
    padding: 0;
    margin: 0;
    
    li {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 0;
      color: #555;
      font-size: 14px;
      
      .el-icon {
        color: var(--color-primary);
        font-size: 18px;
      }
    }
  }
}

.apply-form {
  :deep(.el-form-item__label) {
    font-weight: 500;
    color: #333;
  }
}

.status-content {
  text-align: center;
  
  .status-info {
    margin: 24px 0;
    text-align: left;
    max-width: 400px;
    margin-left: auto;
    margin-right: auto;
    
    .status-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;
      
      .label {
        color: #666;
        font-size: 14px;
      }
      
      span {
        color: #333;
        font-size: 14px;
      }
    }
  }
  
  .status-actions {
    margin-top: 24px;
    display: flex;
    gap: 12px;
    justify-content: center;
  }
}
</style>
