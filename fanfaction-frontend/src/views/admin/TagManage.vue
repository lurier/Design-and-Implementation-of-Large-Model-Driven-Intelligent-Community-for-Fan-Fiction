<template>
  <div class="tag-manage-page">
    <div class="page-header">
      <h1 class="page-title">分类标签管理</h1>
      <div class="header-actions">
        <el-input
          v-model="keyword"
          placeholder="搜索标签..."
          :prefix-icon="Search"
          class="search-input"
          clearable
          style="width: 200px"
        />
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>
          添加标签
        </el-button>
      </div>
    </div>

    <el-card class="table-card">
      <el-table
        :data="tagList"
        v-loading="loading"
        style="width: 100%"
        empty-text="暂无标签数据"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="标签名称" min-width="150" />
        <el-table-column prop="category" label="所属分类" width="120">
          <template #default="{ row }">
            <el-tag :type="getCategoryType(row.category)">
              {{ getCategoryText(row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="color" label="标签颜色" width="100">
          <template #default="{ row }">
            <span
              class="color-preview"
              :style="{ backgroundColor: row.color }"
            ></span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="active"
              inactive-value="disabled"
              @change="toggleStatus(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="openDeleteDialog(row)">
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
          @size-change="loadTags"
          @current-change="loadTags"
        />
      </div>
    </el-card>

    <!-- 添加/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑标签' : '添加标签'"
      width="450px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="所属分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类">
            <el-option label="作品类型" value="work_type" />
            <el-option label="题材标签" value="theme" />
            <el-option label="人物标签" value="character" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签颜色" prop="color">
          <div class="color-picker-wrapper">
            <el-color-picker v-model="form.color" show-alpha />
            <span class="color-value">{{ form.color }}</span>
          </div>
        </el-form-item>
        <el-form-item label="排序值" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTag" :loading="processing">
          {{ isEdit ? '保存修改' : '确认添加' }}
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
      <p>确定要删除标签 <span class="highlight">{{ currentTag?.name }}</span> 吗？</p>
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
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getTagList, addTag, updateTag, deleteTag, toggleTagStatus } from '@/api/admin'
import dayjs from 'dayjs'

const loading = ref(false)
const processing = ref(false)
const tagList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')

// 弹窗状态
const dialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const isEdit = ref(false)
const currentTag = ref(null)

const form = ref({
  name: '',
  category: 'theme',
  color: '#409eff',
  sortOrder: 0
})

// 后端 status: 0=禁用 1=启用, 前端用字符串 'active'/'disabled'
const toFrontStatus = (status) => status === 1 ? 'active' : 'disabled'

const loadTags = async () => {
  loading.value = true
  try {
    const res = await getTagList({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined
    })
    if (res.code === 200 && res.data) {
      tagList.value = res.data.records.map(item => ({
        ...item,
        status: toFrontStatus(item.status)
      }))
      total.value = res.data.total
    }
  } catch (error) {
    console.error('加载标签列表失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 搜索防抖
let searchTimer = null
watch(keyword, () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 1
    loadTags()
  }, 300)
})

const openAddDialog = () => {
  isEdit.value = false
  form.value = {
    name: '',
    category: 'theme',
    color: '#409eff',
    sortOrder: 0
  }
  dialogVisible.value = true
}

const openEditDialog = (tag) => {
  isEdit.value = true
  currentTag.value = tag
  form.value = {
    name: tag.name,
    category: tag.category,
    color: tag.color,
    sortOrder: tag.sortOrder
  }
  dialogVisible.value = true
}

const openDeleteDialog = (tag) => {
  currentTag.value = tag
  deleteDialogVisible.value = true
}

const saveTag = async () => {
  if (!form.value.name.trim()) {
    ElMessage.warning('请输入标签名称')
    return
  }

  processing.value = true
  try {
    if (isEdit.value) {
      await updateTag(currentTag.value.id, form.value)
      ElMessage.success('修改成功')
    } else {
      await addTag(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadTags()
  } catch (error) {
    console.error('保存标签失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    processing.value = false
  }
}

const handleDelete = async () => {
  processing.value = true
  try {
    await deleteTag(currentTag.value.id)
    ElMessage.success('删除成功')
    deleteDialogVisible.value = false
    loadTags()
  } catch (error) {
    console.error('删除标签失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    processing.value = false
  }
}

const toggleStatus = async (row) => {
  processing.value = true
  try {
    await toggleTagStatus(row.id)
    ElMessage.success(row.status === 'active' ? '已启用' : '已禁用')
    loadTags()
  } catch (error) {
    console.error('切换状态失败:', error)
    ElMessage.error('操作失败')
    loadTags()
  } finally {
    processing.value = false
  }
}

const getCategoryText = (category) => {
  const map = {
    work_type: '作品类型',
    theme: '题材标签',
    character: '人物标签',
    other: '其他'
  }
  return map[category] || category
}

const getCategoryType = (category) => {
  const map = {
    work_type: 'primary',
    theme: 'success',
    character: 'warning',
    other: 'info'
  }
  return map[category] || 'info'
}

const formatDate = (dateStr) => {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm:ss')
}

onMounted(() => {
  loadTags()
})
</script>

<style scoped>
.tag-manage-page {
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

.table-card {
  margin-bottom: 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.color-preview {
  display: inline-block;
  width: 24px;
  height: 24px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.color-picker-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.color-value {
  font-size: 12px;
  color: #606266;
  font-family: monospace;
}

.highlight {
  color: #409eff;
  font-weight: 500;
}
</style>
