<template>
  <div class="faq-manage-page">
    <div class="page-header">
      <h1 class="page-title">FAQ知识库维护</h1>
      <div class="header-actions">
        <el-input
          v-model="keyword"
          placeholder="搜索问题..."
          :prefix-icon="Search"
          class="search-input"
          clearable
          style="width: 250px"
        />
        <el-button type="primary" @click="addFaq">
          <el-icon><Plus /></el-icon>
          添加FAQ
        </el-button>
      </div>
    </div>

    <el-card class="faq-card">
      <div v-if="filteredFaqs.length === 0" class="empty-state">
        <el-icon :size="48" class="empty-icon"><QuestionFilled /></el-icon>
        <p>暂无FAQ数据</p>
      </div>

      <el-collapse v-model="activeNames" accordion class="faq-accordion">
        <el-collapse-item
          v-for="(faq, index) in filteredFaqs"
          :key="faq.id"
          :title="getAccordionTitle(faq, index)"
          :name="faq.id.toString()"
        >
          <div class="faq-content">
            <div v-if="editingId === faq.id" class="edit-mode">
              <el-form :model="editForm" label-width="80px">
                <el-form-item label="问题">
                  <el-input
                    v-model="editForm.question"
                    type="textarea"
                    :rows="2"
                    placeholder="请输入问题"
                  />
                </el-form-item>
                <el-form-item label="答案">
                  <el-input
                    v-model="editForm.answer"
                    type="textarea"
                    :rows="5"
                    placeholder="请输入答案"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveEdit(faq)">保存</el-button>
                  <el-button @click="cancelEdit">取消</el-button>
                </el-form-item>
              </el-form>
            </div>
            <div v-else class="view-mode">
              <div class="answer-content" v-html="faq.answer"></div>
              <div class="faq-meta">
                <span class="category-tag">{{ getCategoryText(faq.category) }}</span>
                <span class="create-time">创建于 {{ formatDate(faq.createTime) }}</span>
              </div>
              <div class="faq-actions">
                <el-button link type="primary" size="small" @click="startEdit(faq)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button link type="danger" size="small" @click="openDeleteDialog(faq)">
                  <el-icon><SwitchButton /></el-icon>
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <!-- 添加FAQ弹窗 -->
    <el-dialog
      v-model="addDialogVisible"
      title="添加FAQ"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="addForm" label-width="80px">
        <el-form-item label="分类" prop="category">
          <el-select v-model="addForm.category" placeholder="请选择分类">
            <el-option label="账号相关" value="account" />
            <el-option label="内容相关" value="content" />
            <el-option label="权限相关" value="permission" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题" prop="question">
          <el-input
            v-model="addForm.question"
            type="textarea"
            :rows="2"
            placeholder="请输入问题"
          />
        </el-form-item>
        <el-form-item label="答案" prop="answer">
          <el-input
            v-model="addForm.answer"
            type="textarea"
            :rows="5"
            placeholder="请输入答案"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd" :loading="processing">
          确认添加
        </el-button>
      </template>
    </el-dialog>

    <!-- 删除确认弹窗 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="400px"
      :close-on-click-modal="false"
    >
      <p>确定要删除FAQ <span class="highlight">{{ currentFaq?.question }}</span> 吗？</p>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleDelete" :loading="processing">
          确认删除
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, QuestionFilled, Edit, SwitchButton } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const faqList = ref([])
const keyword = ref('')
const activeNames = ref(['1'])
const editingId = ref(null)
const editForm = ref({ question: '', answer: '' })
const processing = ref(false)

// 弹窗状态
const addDialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const currentFaq = ref(null)

const addForm = ref({
  category: 'other',
  question: '',
  answer: ''
})

// 模拟数据
const mockFaqs = [
  { id: 1, category: 'account', question: '如何注册账号？', answer: '<p>点击首页右上角的"注册"按钮，填写手机号或邮箱，设置密码后即可完成注册。</p>', createTime: '2024-01-01 10:00:00' },
  { id: 2, category: 'account', question: '忘记密码怎么办？', answer: '<p>在登录页面点击"忘记密码"，通过注册时的手机号或邮箱进行验证后即可重置密码。</p>', createTime: '2024-01-02 11:00:00' },
  { id: 3, category: 'content', question: '如何发布文章？', answer: '<p>登录后进入个人中心，点击"发布文章"，填写标题和内容，选择分类后即可发布。</p>', createTime: '2024-01-03 12:00:00' },
  { id: 4, category: 'content', question: '文章审核需要多长时间？', answer: '<p>一般情况下，文章审核会在24小时内完成。审核结果会通过站内信通知您。</p>', createTime: '2024-01-04 13:00:00' },
  { id: 5, category: 'permission', question: '如何成为创作者？', answer: '<p>在个人中心点击"申请成为创作者"，提交相关材料后等待审核，审核通过后即可获得创作权限。</p>', createTime: '2024-01-05 14:00:00' },
  { id: 6, category: 'permission', question: '账号被封禁了怎么办？', answer: '<p>如果您的账号被封禁，可以在登录页面提交申诉，我们会在3个工作日内处理您的申诉请求。</p>', createTime: '2024-01-06 15:00:00' },
  { id: 7, category: 'other', question: '如何联系客服？', answer: '<p>您可以通过首页底部的"联系我们"链接，或者发送邮件至 support@example.com 与我们取得联系。</p>', createTime: '2024-01-07 16:00:00' },
  { id: 8, category: 'other', question: '平台有哪些社区规范？', answer: '<p>请查看平台首页的"社区规范"页面，了解详细的内容规范和行为准则。</p>', createTime: '2024-01-08 17:00:00' }
]

let nextId = 9

const filteredFaqs = computed(() => {
  if (!keyword.value) {
    return faqList.value
  }
  return faqList.value.filter(faq => 
    faq.question.includes(keyword.value) || faq.answer.includes(keyword.value)
  )
})

const getAccordionTitle = (faq, index) => {
  return `Q${index + 1}. ${faq.question}`
}

const getCategoryText = (category) => {
  const map = {
    account: '账号相关',
    content: '内容相关',
    permission: '权限相关',
    other: '其他'
  }
  return map[category] || category
}

const formatDate = (dateStr) => {
  return dayjs(dateStr).format('YYYY-MM-DD')
}

const loadFaqs = async () => {
  await new Promise(resolve => setTimeout(resolve, 200))
  faqList.value = [...mockFaqs]
}

const addFaq = () => {
  addForm.value = {
    category: 'other',
    question: '',
    answer: ''
  }
  addDialogVisible.value = true
}

const handleAdd = async () => {
  if (!addForm.value.question.trim() || !addForm.value.answer.trim()) {
    ElMessage.warning('请填写完整的问题和答案')
    return
  }

  processing.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 300))
    
    const newFaq = {
      id: nextId++,
      ...addForm.value,
      createTime: new Date().toLocaleString('zh-CN')
    }
    faqList.value.push(newFaq)
    
    ElMessage.success('添加成功')
    addDialogVisible.value = false
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    processing.value = false
  }
}

const startEdit = (faq) => {
  editingId.value = faq.id
  editForm.value = {
    question: faq.question,
    answer: faq.answer
  }
}

const saveEdit = async (faq) => {
  if (!editForm.value.question.trim() || !editForm.value.answer.trim()) {
    ElMessage.warning('请填写完整的问题和答案')
    return
  }

  processing.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 300))
    
    const index = faqList.value.findIndex(f => f.id === faq.id)
    if (index !== -1) {
      faqList.value[index].question = editForm.value.question
      faqList.value[index].answer = editForm.value.answer
    }
    
    ElMessage.success('保存成功')
    editingId.value = null
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    processing.value = false
  }
}

const cancelEdit = () => {
  editingId.value = null
}

const openDeleteDialog = (faq) => {
  currentFaq.value = faq
  deleteDialogVisible.value = true
}

const handleDelete = async () => {
  processing.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 300))
    
    faqList.value = faqList.value.filter(f => f.id !== currentFaq.value.id)
    
    ElMessage.success('删除成功')
    deleteDialogVisible.value = false
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    processing.value = false
  }
}

onMounted(() => {
  loadFaqs()
})
</script>

<style scoped>
.faq-manage-page {
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

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.faq-card {
  max-width: 800px;
  margin: 0 auto;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: #909399;
}

.empty-icon {
  margin-bottom: 12px;
}

.faq-accordion {
  border: none;
}

.faq-content {
  padding: 16px 0;
}

.view-mode .answer-content {
  line-height: 1.8;
  color: #606266;
  margin-bottom: 16px;
}

.faq-meta {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 16px;
}

.category-tag {
  padding: 4px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
}

.create-time {
  font-size: 12px;
  color: #909399;
}

.faq-actions {
  display: flex;
  gap: 16px;
}

.edit-mode {
  padding: 8px 0;
}

.highlight {
  color: #409eff;
  font-weight: 500;
}
</style>
