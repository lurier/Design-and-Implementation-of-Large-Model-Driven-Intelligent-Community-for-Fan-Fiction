<template>
  <div class="publish-work-page">
    <div class="page-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <h1 class="page-title">发布作品</h1>
        <p class="page-subtitle">分享您的创作，与读者共筑想象世界</p>
      </div>

      <!-- 发布表单 -->
      <el-card class="form-card">
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="80px"
          class="publish-form"
        >
          <!-- 标题输入 -->
          <el-form-item label="作品标题" prop="title">
            <el-input
              v-model="formData.title"
              placeholder="请输入作品标题（不超过 50 字）"
              maxlength="50"
              show-word-limit
              size="large"
              clearable
            />
          </el-form-item>

          <!-- 摘要输入 + AI 生成 -->
          <el-form-item label="作品摘要" prop="summary">
            <div class="summary-row">
              <el-input
                v-model="formData.summary"
                type="textarea"
                :rows="3"
                placeholder="请手动输入摘要，或点击右侧按钮由 AI 自动生成"
                maxlength="200"
                show-word-limit
                class="summary-input"
              />
              <el-button
                type="primary"
                :loading="aiSummaryLoading"
                :disabled="aiSummaryLoading"
                @click="handleAiGenerateSummary"
                class="ai-summary-btn"
              >
                <el-icon v-if="!aiSummaryLoading"><MagicStick /></el-icon>
                AI 生成摘要
              </el-button>
            </div>
          </el-form-item>

          <!-- AI 推荐标签按钮 -->
          <el-form-item label="智能标签">
            <el-button
              type="warning"
              :loading="aiTagsLoading"
              :disabled="aiTagsLoading"
              @click="handleAiRecommendTags"
              style="margin-bottom: 12px"
            >
              <el-icon v-if="!aiTagsLoading"><MagicStick /></el-icon>
              AI 推荐标签
            </el-button>
          </el-form-item>

          <!-- 分类选择 -->
          <el-form-item label="作品分类" prop="categoryId">
            <el-select
              v-model="formData.categoryId"
              placeholder="请选择作品分类"
              size="large"
              style="width: 100%"
              filterable
              clearable
            >
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
          </el-form-item>

          <!-- CP 标签多选 -->
          <el-form-item label="CP 标签" prop="cpTags">
            <el-select
              v-model="formData.cpTags"
              placeholder="请选择或输入 CP 标签（可多选）"
              size="large"
              style="width: 100%"
              multiple
              filterable
              allow-create
              default-first-option
              :reserve-keyword="false"
            >
              <el-option
                v-for="tag in cpTags"
                :key="tag.id"
                :label="tag.name"
                :value="tag.name"
              />
            </el-select>
          </el-form-item>

          <!-- 富文本编辑器 -->
          <el-form-item label="正文内容" prop="content">
            <div class="editor-container">
              <Toolbar
                :editor="editorRef"
                :defaultConfig="toolbarConfig"
                :mode="mode"
                style="border-bottom: 1px solid #dcdfe6"
              />
              <Editor
                v-model="formData.content"
                :defaultConfig="editorConfig"
                :mode="mode"
                @onCreated="handleEditorCreated"
                @onChange="handleEditorChange"
                style="height: 400px; overflow-y: hidden"
              />
            </div>
          </el-form-item>

          <!-- 提交按钮 -->
          <el-form-item>
            <el-alert
              title="温馨提示"
              type="info"
              :closable="false"
              show-icon
              class="submit-tip"
            >
              <template #default>
                <p>作品提交后，将进入审核流程。</p>
                <p>审核通过后，您的作品将在平台上展示给所有读者。</p>
              </template>
            </el-alert>
          </el-form-item>

          <el-form-item>
            <el-button 
              type="primary" 
              @click="handleSubmit" 
              :loading="submitting" 
              size="large"
              class="submit-btn"
            >
              {{ isEdit ? '更新作品' : '提交审核' }}
            </el-button>
            <el-button 
              type="default" 
              @click="handleSaveDraft" 
              :loading="savingDraft" 
              size="large"
            >
              保存为草稿
            </el-button>
            <!-- 上传作品按钮：仅在编辑模式且当前为草稿状态时显示 -->
            <el-button
              v-if="isEdit && isDraftStatus"
              type="success"
              @click="handleSubmitForReview"
              :loading="submittingForReview"
              size="large"
            >
              上传作品
            </el-button>
            <el-button @click="handleReset" size="large">重置</el-button>
            <el-button @click="$router.push('/creator/works')" size="large">返回作品列表</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, shallowRef, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MagicStick } from '@element-plus/icons-vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { publishWork, getWorkDetail, updateWork, saveDraft, getCategories, getCpTags } from '@/api/creator'
import { generateSummary, recommendTags } from '@/api/ai'
import '@wangeditor/editor/dist/css/style.css'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)

// 是否为编辑模式
const isEdit = computed(() => !!route.query.id)

// 编辑器实例
const editorRef = shallowRef(null)
const mode = ref('default')

// 工具栏配置
const toolbarConfig = {}

// 编辑器配置
const editorConfig = { 
  placeholder: '请输入作品内容（至少 100 字）...',
  MENU_CONF: {
    uploadImage: {
      // 图片上传配置（后续可扩展）
      server: '/api/upload/image'
    }
  }
}

// 表单数据
const formData = reactive({
  title: '',
  summary: '',
  categoryId: '',
  cpTags: [],
  content: ''
})

// 表单验证规则
const formRules = {
  title: [
    { required: true, message: '请输入作品标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度应在 2-50 个字符之间', trigger: 'blur' }
  ],
  summary: [
    { max: 200, message: '摘要不能超过 200 字', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择作品分类', trigger: 'change' }
  ],
  cpTags: [
    { required: false, message: '请选择 CP 标签', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入作品内容', trigger: 'blur' },
    { min: 100, message: '作品内容至少需要 100 字', trigger: 'blur' }
  ]
}

// 分类列表
const categories = ref([])

// CP 标签列表
const cpTags = ref([])

// 提交状态
const submitting = ref(false)
const savingDraft = ref(false)
const submittingForReview = ref(false)

// 文章状态
const articleStatus = ref('')

// 是否为草稿状态
const isDraftStatus = computed(() => articleStatus.value === 'DRAFT')

// AI 功能状态
const aiSummaryLoading = ref(false)
const aiTagsLoading = ref(false)

// 从富文本中提取纯文本内容
const getPlainTextContent = () => {
  if (editorRef.value) {
    return editorRef.value.getText() || ''
  }
  // 回退：从 HTML 中提取文本
  const div = document.createElement('div')
  div.innerHTML = formData.content
  return div.textContent || div.innerText || ''
}

// AI 生成摘要
const handleAiGenerateSummary = async () => {
  const plainText = getPlainTextContent()
  if (!plainText || plainText.trim().length < 20) {
    ElMessage.warning('请先输入正文内容（至少 20 字），再使用 AI 生成摘要')
    return
  }

  aiSummaryLoading.value = true
  try {
    const res = await generateSummary(plainText)
    if (res.data) {
      // 前端强制截断到 100 字以内
      let summary = res.data.trim()
      if (summary.length > 100) {
        summary = summary.substring(0, 100)
      }
      formData.summary = summary
      ElMessage.success('AI 摘要已生成（100字以内），您可以继续修改')
    } else {
      ElMessage.warning('AI 未能生成摘要，请手动输入')
    }
  } catch (error) {
    console.error('AI 生成摘要失败:', error)
    ElMessage.error('AI 生成摘要失败，请稍后重试')
  } finally {
    aiSummaryLoading.value = false
  }
}

// AI 推荐标签
const handleAiRecommendTags = async () => {
  const plainText = getPlainTextContent()
  if (!plainText || plainText.trim().length < 20) {
    ElMessage.warning('请先输入正文内容（至少 20 字），再使用 AI 推荐标签')
    return
  }

  aiTagsLoading.value = true
  try {
    const res = await recommendTags(plainText)
    const aiTags = res.data || []

    if (aiTags.length === 0) {
      ElMessage.warning('AI 未能识别出合适的标签，请手动选择')
      return
    }

    // 匹配分类
    const matchedCategory = matchTagsToOptions(aiTags, categories.value)
    if (matchedCategory) {
      formData.categoryId = matchedCategory
    }

    // 匹配 CP 标签（返回标签名称字符串数组）
    const matchedCpTags = matchTagsToOptions(aiTags, cpTags.value, true)
    if (matchedCpTags && matchedCpTags.length > 0) {
      // 合并去重：已有 + AI推荐
      const merged = [...new Set([...formData.cpTags, ...matchedCpTags])]
      formData.cpTags = merged
    }

    const parts = []
    if (matchedCategory) parts.push('分类')
    if (matchedCpTags.length > 0) parts.push(`${matchedCpTags.length} 个 CP 标签`)
    
    if (parts.length > 0) {
      ElMessage.success(`AI 已自动推荐：${parts.join('、')}，您可以手动调整`)
    } else {
      ElMessage.info('AI 推荐的标签与现有分类/标签不匹配，请手动选择')
    }
  } catch (error) {
    console.error('AI 推荐标签失败:', error)
    ElMessage.error('AI 推荐标签失败，请稍后重试')
  } finally {
    aiTagsLoading.value = false
  }
}

// 将 AI 标签字符串匹配到分类/CP标签选项
const matchTagsToOptions = (aiTags, options, isMultiple = false) => {
  if (!aiTags.length || !options.length) return isMultiple ? [] : null

  const matched = []
  
  for (const aiTag of aiTags) {
    const lower = aiTag.trim().toLowerCase()
    for (const opt of options) {
      const optName = (opt.name || '').trim()
      const optNameLower = optName.toLowerCase()
      if (
        lower === optNameLower ||
        optNameLower.includes(lower) ||
        lower.includes(optNameLower) ||
        isSemanticMatch(lower, optNameLower)
      ) {
        if (isMultiple) {
          if (!matched.includes(optName)) {
            matched.push(optName)
          }
        } else {
          return opt.id
        }
      }
    }
  }

  return isMultiple ? matched : null
}

// 语义关键词映射（处理 AI 标签与系统标签名称不一致的情况）
const isSemanticMatch = (aiTag, optName) => {
  const mappings = {
    '同人': ['同人小说'],
    '原创': ['原创文学'],
    '书评': ['书评影评'],
    '影评': ['书评影评'],
    '轻小说': ['轻小说'],
    '短篇': ['短篇故事'],
    '连载': ['连载系列'],
    '诗歌': ['诗歌散文'],
    '散文': ['诗歌散文'],
    'bg': ['bg'],
    'bl': ['bl'],
    'gl': ['gl'],
    '校园': ['校园'],
    '职场': ['职场'],
    '星际': ['星际'],
    '奇幻': ['奇幻'],
    '温馨': ['温馨'],
    '甜蜜': ['温馨'],
    '虐': ['虐向'],
    '虐文': ['虐向'],
    '古代': ['古代设定'],
    '现代': ['现代设定'],
    '原作': ['原作向'],
    'au': ['au'],
    '热门': ['热门cp'],
    '冷门': ['冷门cp'],
  }

  for (const [key, values] of Object.entries(mappings)) {
    if (aiTag.includes(key) || key.includes(aiTag)) {
      return values.some(v => v.toLowerCase() === optName)
    }
  }
  return false
}

// 编辑器创建成功回调
const handleEditorCreated = (editor) => {
  editorRef.value = editor
}

// 编辑器内容变化回调
const handleEditorChange = (editor) => {
  const content = editor.getHtml()
  formData.content = content
}

// 加载分类和标签
const loadCategoriesAndTags = async () => {
  try {
    const [categoriesRes, tagsRes] = await Promise.all([
      getCategories(),
      getCpTags()
    ])
    categories.value = categoriesRes.data || []
    cpTags.value = tagsRes.data || []

    console.log('[PublishWork] 分类列表已加载:', categories.value.length, '个分类')
    console.log('[PublishWork] CP标签列表已加载:', cpTags.value.length, '个标签')
    console.table(cpTags.value.map(t => ({ id: t.id, name: t.name })))
  } catch (error) {
    console.error('加载分类和标签失败:', error)
    ElMessage.warning('加载分类和标签失败，请稍后重试')
  }
}

// 提交表单
const handleSubmit = async () => {
  console.log('✅ 提交按钮已点击，开始校验...')
  
  // 检查 formRef 是否存在
  if (!formRef.value) {
    console.error('❌ formRef 未绑定，请检查 el-form 的 ref 属性')
    ElMessage.error('表单初始化异常，请刷新页面重试')
    return
  }
  
  console.log('📋 表单原始数据:', JSON.parse(JSON.stringify(formData)))
  
  try {
    // 执行表单验证
    console.log('🔍 开始表单字段验证...')
    const isValid = await formRef.value.validate().catch((err) => {
      console.error('❌ 表单验证失败:', err)
      return false
    })
    
    console.log('📊 表单验证结果:', isValid)
    
    if (!isValid) {
      console.warn('⚠️ 表单验证未通过，阻止提交')
      ElMessage.warning('请检查表单中的必填项是否已填写完整')
      return
    }
    
    // 获取纯文本内容验证长度
    const plainText = getPlainTextContent()
    console.log('📝 正文纯文本长度:', plainText.length, '字符')
    
    if (plainText.trim().length < 100) {
      console.warn('⚠️ 正文内容不足 100 字')
      ElMessage.warning('作品正文内容至少需要 100 字，当前仅 ' + plainText.trim().length + ' 字')
      return
    }

    submitting.value = true
    console.log('⏳ 已进入提交状态，按钮已禁用')
    
    // 准备提交数据
    const submitData = {
      title: formData.title,
      summary: formData.summary || '',
      categoryId: formData.categoryId,
      tags: Array.isArray(formData.cpTags) ? formData.cpTags.join(',') : '',
      content: formData.content
    }

    console.log('📤 准备提交的数据:', submitData)
    console.log('   - title:', submitData.title)
    console.log('   - summary:', submitData.summary)
    console.log('   - categoryId:', submitData.categoryId)
    console.log('   - tags:', submitData.tags)
    console.log('   - content 长度:', submitData.content ? submitData.content.length : 0)

    let res
    if (isEdit.value) {
      // 编辑模式：更新作品
      res = await updateWork(route.query.id, submitData)
      console.log('✅ 作品更新成功，后端返回:', res)
      ElMessage.success('作品更新成功')
    } else {
      // 新增模式：提交审核
      res = await publishWork(submitData)
      console.log('✅ 作品提交成功，后端返回:', res)
      ElMessage.success('作品提交成功，正在审核中')
    }
    
    // 跳转到作品列表
    setTimeout(() => {
      router.push('/creator/works')
    }, 1000)
  } catch (error) {
    console.error('❌ 提交作品失败:', error)
    
    // 尝试解析后端返回的错误信息
    const errorMsg = error.response?.data?.message || error.message || '提交失败，请稍后重试'
    console.error('   错误详情:', errorMsg)
    ElMessage.error(errorMsg)
  } finally {
    submitting.value = false
    console.log('🔓 提交状态已重置，按钮已恢复')
  }
}

// 保存为草稿
const handleSaveDraft = async () => {
  console.log('📋 保存为草稿')
  
  if (!formData.title && !formData.content) {
    ElMessage.warning('至少需要填写标题或内容才能保存草稿')
    return
  }

  savingDraft.value = true
  
  try {
    const draftData = {
      title: formData.title || '未命名作品',
      summary: formData.summary || '',
      categoryId: formData.categoryId || '',
      tags: Array.isArray(formData.cpTags) ? formData.cpTags.join(',') : '',
      content: formData.content || '',
      status: 'DRAFT'
    }

    console.log('📤 准备保存的草稿数据:', draftData)

    let res
    if (isEdit.value) {
      res = await updateWork(route.query.id, draftData)
      console.log('✅ 草稿更新成功，后端返回:', res)
      ElMessage.success('草稿已更新')
    } else {
      res = await saveDraft(draftData)
      console.log('✅ 草稿保存成功，后端返回:', res)
      ElMessage.success('草稿已保存')
      
      // 保存成功后跳转到作品列表
      setTimeout(() => {
        router.push('/creator/works')
      }, 1000)
    }
  } catch (error) {
    console.error('❌ 保存草稿失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '保存草稿失败，请稍后重试'
    ElMessage.error(errorMsg)
  } finally {
    savingDraft.value = false
  }
}

// 加载文章详情（编辑模式）
const loadWorkDetail = async () => {
  if (!isEdit.value) return
  
  try {
    const res = await getWorkDetail(route.query.id)
    const detail = res.data
    
    // 回填表单数据
    formData.title = detail.title || ''
    formData.summary = detail.summary || ''
    formData.categoryId = detail.categoryId || detail.category?.id || ''
    formData.cpTags = detail.tags ? detail.tags.split(',').filter(Boolean) : []
    formData.content = detail.content || ''
    
    // 保存文章状态
    articleStatus.value = detail.status || ''
    
    console.log('📥 已加载文章详情:', detail.title, '状态:', articleStatus.value)
  } catch (error) {
    console.error('❌ 加载文章详情失败:', error)
    ElMessage.error('加载文章详情失败，请刷新页面重试')
  }
}

// 上传作品到审核
const handleSubmitForReview = async () => {
  console.log('📤 上传作品到审核')
  
  // 表单验证
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
  } catch (error) {
    console.warn('⚠️ 表单验证失败')
    ElMessage.warning('请完善必填项后再提交')
    return
  }
  
  // 检查内容长度
  const plainText = formData.content.replace(/<[^>]*>/g, '')
  if (plainText.trim().length < 100) {
    console.warn('⚠️ 正文内容不足 100 字')
    ElMessage.warning('作品正文内容至少需要 100 字，当前仅 ' + plainText.trim().length + ' 字')
    return
  }

  submittingForReview.value = true
  console.log('⏳ 已进入提交审核状态，按钮已禁用')
  
  try {
    // 准备提交数据，状态设置为 PENDING
    const submitData = {
      title: formData.title,
      summary: formData.summary || '',
      categoryId: formData.categoryId,
      tags: Array.isArray(formData.cpTags) ? formData.cpTags.join(',') : '',
      content: formData.content,
      status: 'PENDING'
    }

    console.log('📤 准备提交审核的数据:', submitData)
    console.log('   - title:', submitData.title)
    console.log('   - status:', submitData.status)

    // 更新文章状态
    await updateWork(route.query.id, submitData)
    console.log('✅ 作品已提交审核')
    ElMessage.success('作品已提交审核，等待管理员审核')
    
    // 更新本地状态
    articleStatus.value = 'PENDING'
    
    // 跳转到作品列表
    setTimeout(() => {
      router.push('/creator/works')
    }, 1000)
  } catch (error) {
    console.error('❌ 提交审核失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '提交审核失败，请稍后重试'
    console.error('   错误详情:', errorMsg)
    ElMessage.error(errorMsg)
  } finally {
    submittingForReview.value = false
    console.log('🔓 提交审核状态已重置，按钮已恢复')
  }
}

// 重置表单
const handleReset = () => {
  formRef.value.resetFields()
  formData.summary = ''
  if (editorRef.value) {
    editorRef.value.clear()
  }
  formData.content = ''
}

onMounted(() => {
  loadCategoriesAndTags()
  loadWorkDetail()
})

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})
</script>

<style lang="scss" scoped>
.publish-work-page {
  min-height: 100vh;
  background-color: var(--color-bg-page);
  padding: 24px;
}

.page-container {
  max-width: 960px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 28px;
  text-align: center;

  .page-title {
    font-family: var(--font-sans);
    font-size: 26px;
    font-weight: 600;
    color: var(--color-text-main);
    margin-bottom: 6px;
  }

  .page-subtitle {
    font-family: var(--font-serif);
    font-size: 15px;
    color: var(--color-text-muted);
  }
}

.summary-row {
  display: flex;
  gap: 12px;
  width: 100%;

  .summary-input {
    flex: 1;
  }

  .ai-summary-btn {
    min-width: 140px;
    height: auto;
    align-self: flex-start;
  }
}

.form-card {
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-color-light);

  :deep(.el-card__body) {
    padding: 24px;
  }

  .publish-form {
    padding: 0;
    
    :deep(.el-form-item__label) {
      font-family: var(--font-sans);
      font-weight: 500;
      color: var(--color-text-secondary);
    }

    :deep(.el-input__wrapper) {
      box-shadow: 0 0 0 1px var(--border-color);
      border-radius: var(--radius-sm);
      
      &:hover {
        box-shadow: 0 0 0 1px var(--color-primary);
      }
    }
  }
}

.editor-container {
  width: 100%;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  
  :deep(.w-e-text-container) {
    height: 400px !important;
    overflow-y: auto;
  }

  :deep(.w-e-toolbar) {
    border-bottom: 1px solid var(--border-color-light);
  }
}

.submit-tip {
  margin-bottom: 16px;
}

.submit-btn {
  padding: 12px 32px;
  font-family: var(--font-sans);
}

// 按钮组
:deep(.el-form-item__content) {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;

  .el-button {
    flex-shrink: 0;
    font-family: var(--font-sans);
    
    &.el-button--primary {
      background: var(--color-primary);
      border-color: var(--color-primary);
      
      &:hover {
        background: var(--color-primary-light);
      }
    }
  }
}

// 移动端适配
@media (max-width: 768px) {
  .publish-work-page {
    padding: 16px;
  }

  .page-header .page-title {
    font-size: 22px;
  }

  :deep(.el-form-item__content) {
    flex-direction: column;

    .el-button {
      width: 100%;
    }
  }
}
</style>
