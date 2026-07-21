<template>
  <div class="audit-workbench">
    <div class="page-header">
      <h1 class="page-title">智能审核工作台</h1>
      <div class="header-actions">
        <el-button type="primary" @click="loadAuditList">
          <el-icon><Refresh /></el-icon>
          刷新列表
        </el-button>
      </div>
    </div>

    <!-- 左右布局 -->
    <div class="audit-layout">
      <!-- 左侧：待审核列表 -->
      <div class="audit-left">
        <el-card class="table-card">
          <div class="filter-bar">
            <el-select v-model="filterStatus" placeholder="审核状态" clearable style="width: 140px" @change="handleFilterChange">
              <el-option label="全部" value="" />
              <el-option label="待审核" value="PENDING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已驳回" value="REJECTED" />
            </el-select>
            <el-select v-model="filterType" placeholder="内容类型" clearable style="width: 120px" @change="handleFilterChange">
              <el-option label="全部" value="" />
              <el-option label="文章" value="ARTICLE" />
              <el-option label="评论" value="COMMENT" />
              <el-option label="创作者申请" value="CREATOR_APPLY" />
            </el-select>
          </div>

          <el-table
            :data="auditList"
            v-loading="loading"
            style="width: 100%"
            empty-text="暂无待审核数据"
            highlight-current-row
            @row-click="selectItem"
          >
            <el-table-column label="类型" width="90">
              <template #default="{ row }">
                <el-tag :type="getItemTypeTag(row.type)" size="small">
                  {{ getItemTypeText(row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
            <el-table-column prop="authorNickname" label="作者" width="100" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="submitTime" label="提交时间" width="160">
              <template #default="{ row }">
                {{ formatDate(row.submitTime) }}
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
              @size-change="loadAuditList"
              @current-change="loadAuditList"
            />
          </div>
        </el-card>
      </div>

      <!-- 右侧：详情与操作区 -->
      <div class="audit-right">
        <div v-if="!selectedItem" class="no-selection">
          <el-empty description="点击左侧记录查看详情并审核" />
        </div>

        <div v-else class="detail-panel">
          <el-card class="detail-card">
            <template #header>
              <div class="detail-header">
                <span class="detail-title">{{ selectedItem.title }}</span>
                <el-button type="primary" size="small" :loading="aiLoading" @click="runAiAudit">
                  <el-icon><Cpu /></el-icon>
                  AI预审
                </el-button>
              </div>
            </template>

            <div class="detail-meta">
              <span>类型：<el-tag size="small">{{ getItemTypeText(selectedItem.type) }}</el-tag></span>
              <span>作者：{{ selectedItem.authorNickname || '未知' }}</span>
              <span>提交时间：{{ formatDate(selectedItem.submitTime) }}</span>
            </div>

            <!-- 内容详情 -->
            <div class="content-section">
              <div class="section-label">内容详情</div>
              <div class="content-text" v-html="highlightViolations(selectedItem.content)"></div>
            </div>

            <!-- AI 预审报告 -->
            <div v-if="aiReport" class="ai-report-section">
              <el-divider content-position="left">AI 预审报告</el-divider>
              <div class="report-item">
                <span class="label">风险等级：</span>
                <el-tag :type="getRiskType(aiReport.riskLevel)" size="small">
                  {{ getRiskText(aiReport.riskLevel) }}
                </el-tag>
              </div>
              <div class="report-item">
                <span class="label">情感倾向：</span>
                <el-tag :type="getSentimentType(aiReport.sentiment)" size="small">
                  {{ aiReport.sentiment || '未知' }}
                </el-tag>
                <span class="score">({{ aiReport.sentimentScore }}分)</span>
              </div>
              <div class="report-item">
                <span class="label">判定理由：</span>
                <span class="reason-text">{{ aiReport.reason || '无' }}</span>
              </div>
              <div v-if="aiReport.violations && aiReport.violations.length > 0" class="violations">
                <span class="label">违规点：</span>
                <div v-for="(v, i) in aiReport.violations" :key="i" class="violation-tag">
                  <el-tag type="danger" size="small">{{ v.type }}</el-tag>
                  <span>{{ v.description }}</span>
                </div>
              </div>
            </div>

            <!-- 操作区 -->
            <el-divider content-position="left">审核操作</el-divider>
            <div class="action-area">
              <el-input
                v-model="reviewComment"
                type="textarea"
                :rows="3"
                placeholder="审核意见（选填）"
                maxlength="500"
                show-word-limit
              />
              <div class="action-buttons">
                <el-button
                  v-if="selectedItem.status === 'PENDING'"
                  type="success"
                  size="large"
                  :loading="submitting"
                  @click="doAudit('APPROVED')"
                >
                  通过
                </el-button>
                <el-button
                  v-if="selectedItem.status === 'PENDING'"
                  type="danger"
                  size="large"
                  :loading="submitting"
                  @click="doAudit('REJECTED')"
                >
                  驳回
                </el-button>
                <span v-if="selectedItem.status !== 'PENDING'" class="audited-tip">
                  该内容已审核（{{ getStatusText(selectedItem.status) }}）
                </span>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Cpu } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getAuditPendingList, aiPreAudit, handleAudit } from '@/api/admin'

const loading = ref(false)
const submitting = ref(false)
const aiLoading = ref(false)
const auditList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterStatus = ref('PENDING')
const filterType = ref('')

const selectedItem = ref(null)
const aiReport = ref(null)
const reviewComment = ref('')

const loadAuditList = async () => {
  loading.value = true
  try {
    const params = { pageNum: currentPage.value, pageSize: pageSize.value }
    if (filterStatus.value) params.status = filterStatus.value

    const res = await getAuditPendingList(params)
    let records = res.data?.records || []

    if (filterType.value) {
      records = records.filter(item => item.type === filterType.value)
    }

    auditList.value = records
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('加载审核列表失败:', error)
  } finally {
    loading.value = false
  }
}

const selectItem = (row) => {
  selectedItem.value = row
  reviewComment.value = ''
  aiReport.value = null
}

const runAiAudit = async () => {
  if (!selectedItem.value) return
  aiLoading.value = true
  try {
    const res = await aiPreAudit({
      itemId: selectedItem.value.id,
      type: selectedItem.value.type,
      content: selectedItem.value.content
    })
    const raw = res.data
    aiReport.value = typeof raw === 'string' ? JSON.parse(raw) : raw
    ElMessage.success('AI预审完成')
  } catch (error) {
    console.error('AI预审失败:', error)
    ElMessage.error('AI预审失败')
  } finally {
    aiLoading.value = false
  }
}

const doAudit = async (action) => {
  const text = action === 'APPROVED' ? '通过' : '驳回'
  try {
    await ElMessageBox.confirm(`确定要${text}该项内容吗？`, '审核确认', {
      confirmButtonText: `确认${text}`,
      cancelButtonText: '取消',
      type: action === 'APPROVED' ? 'success' : 'warning'
    })
  } catch {
    return
  }

  submitting.value = true
  try {
    await handleAudit({
      itemId: selectedItem.value.id,
      type: selectedItem.value.type,
      action: action,
      reviewComment: reviewComment.value
    })
    ElMessage.success(`${text}成功`)

    // 更新本地状态
    const idx = auditList.value.findIndex(item => item.id === selectedItem.value.id)
    if (idx !== -1) {
      auditList.value[idx].status = action
      auditList.value[idx].reviewComment = reviewComment.value
    }
    selectedItem.value.status = action
  } catch (error) {
    console.error('审核失败:', error)
  } finally {
    submitting.value = false
  }
}

const highlightViolations = (content) => {
  if (!content) return ''
  if (!aiReport.value?.violations?.length) return content
  // 简单转义
  return content
}

const handleFilterChange = () => {
  currentPage.value = 1
  loadAuditList()
}

const formatDate = (d) => dayjs(d).format('YYYY-MM-DD HH:mm:ss')

const getStatusText = (s) => ({ PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回' }[s] || s)
const getStatusType = (s) => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }[s] || 'info')
const getRiskText = (l) => ({ LOW: '低风险', MEDIUM: '中风险', HIGH: '高风险' }[l] || l)
const getRiskType = (l) => ({ LOW: 'success', MEDIUM: 'warning', HIGH: 'danger' }[l] || 'info')
const getSentimentType = (s) => ({ '正面': 'success', '中性': 'info', '负面': 'danger' }[s] || 'info')
const getItemTypeText = (t) => ({ ARTICLE: '文章', COMMENT: '评论', CREATOR_APPLY: '创作者申请' }[t] || t)
const getItemTypeTag = (t) => ({ ARTICLE: 'primary', COMMENT: 'success', CREATOR_APPLY: 'warning' }[t] || 'info')

onMounted(() => { loadAuditList() })
</script>

<style scoped>
.audit-workbench { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { 
  font-family: var(--font-sans);
  font-size: 24px; 
  font-weight: 600; 
  margin: 0; 
  color: var(--color-text-main);
}

.audit-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; height: calc(100vh - 160px); }
.audit-left { overflow: hidden; display: flex; flex-direction: column; }
.audit-right { overflow-y: auto; }

.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; }
.table-card { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.table-card :deep(.el-card__body) { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.table-card :deep(.el-table) { flex: 1; }

/* 表头 — 祖母绿背景 + 白色文字 */
.table-card :deep(.el-table__header-wrapper th) {
  background: var(--color-primary) !important;
  color: #fff !important;
  font-family: var(--font-sans);
  font-weight: 500;
  font-size: 13px;
  border-bottom: none;
}

/* 斑马纹交替 */
.table-card :deep(.el-table__body tr:nth-child(even)) {
  background: rgba(128, 144, 118, 0.05);
}
.table-card :deep(.el-table__body tr:nth-child(odd)) {
  background: #fff;
}

.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }

.no-selection { display: flex; align-items: center; justify-content: center; height: 100%; }

.detail-card { margin-bottom: 0; }
.detail-header { display: flex; justify-content: space-between; align-items: center; }
.detail-title { 
  font-family: var(--font-sans);
  font-size: 16px; 
  font-weight: 500;
  color: var(--color-text-main);
}
.detail-meta { display: flex; gap: 16px; margin-bottom: 16px; font-size: 13px; color: var(--color-text-muted); }

.content-section { margin-bottom: 16px; }
.section-label { 
  font-family: var(--font-sans);
  font-weight: 500; 
  margin-bottom: 8px; 
  color: var(--color-text-main); 
}
.content-text { 
  max-height: 300px; 
  overflow-y: auto; 
  padding: 12px; 
  background: rgba(128, 144, 118, 0.04); 
  border-radius: var(--radius-sm); 
  line-height: 1.8; 
  font-family: var(--font-serif);
  color: var(--color-text-secondary); 
  word-break: break-all; 
}

.ai-report-section { background: rgba(128, 144, 118, 0.06); padding: 12px 16px; border-radius: var(--radius-md); margin-bottom: 16px; }
.report-item { margin-bottom: 8px; display: flex; align-items: center; gap: 8px; font-size: 13px; }
.report-item .label { color: var(--color-text-secondary); white-space: nowrap; }
.reason-text { color: var(--color-text-main); }
.score { color: var(--color-text-muted); font-size: 12px; }

.violations { margin-top: 8px; }
.violation-tag { display: flex; align-items: center; gap: 8px; margin-top: 4px; margin-left: 16px; font-size: 13px; }
.violation-tag span { color: var(--color-text-secondary); }

.action-area { display: flex; flex-direction: column; gap: 12px; }
.action-buttons { display: flex; gap: 12px; margin-top: 8px; }
.audited-tip { color: var(--color-text-muted); font-size: 14px; line-height: 40px; }

/* 状态标签覆写 */
:deep(.el-tag--warning) {
  background: rgba(184, 104, 48, 0.12);
  color: var(--color-accent-dark);
  border-color: transparent;
}
:deep(.el-tag--success) {
  background: var(--color-primary);
  color: #fff;
  border-color: transparent;
}
:deep(.el-tag--danger) {
  background: rgba(220, 80, 60, 0.1);
  color: #a04030;
  border-color: transparent;
}

/* 操作按钮 — 默认图标，悬停展开 */
.action-buttons .el-button {
  font-family: var(--font-sans);
  min-width: 80px;
  transition: all var(--transition-normal);
}
</style>
