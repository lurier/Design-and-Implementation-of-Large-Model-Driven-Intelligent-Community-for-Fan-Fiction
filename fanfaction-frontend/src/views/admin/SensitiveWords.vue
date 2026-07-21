<template>
  <div class="sensitive-words-page">
    <div class="page-header">
      <h1 class="page-title">敏感词库管理</h1>
    </div>

    <div class="main-content">
      <div class="left-panel">
        <el-card title="敏感词标签云" class="tag-cloud-card">
          <div v-if="sensitiveWords.length === 0" class="empty-state">
            <el-icon :size="48" class="empty-icon"><Lock /></el-icon>
            <p>暂无敏感词</p>
          </div>
          <div v-else class="tag-cloud">
            <el-tag
              v-for="word in sensitiveWords"
              :key="word.id"
              :type="word.level === 'high' ? 'danger' : word.level === 'medium' ? 'warning' : 'info'"
              :closable="true"
              @close="removeWord(word.id)"
              class="tag-item"
            >
              {{ word.word }}
              <span class="tag-level">{{ getLevelText(word.level) }}</span>
            </el-tag>
          </div>
          
          <div class="stats-row">
            <el-statistic title="敏感词总数" :value="sensitiveWords.length" />
            <el-statistic title="高危词" :value="highCount" />
            <el-statistic title="中危词" :value="mediumCount" />
            <el-statistic title="低危词" :value="lowCount" />
          </div>
        </el-card>
      </div>

      <div class="right-panel">
        <el-card title="添加敏感词" class="add-card">
          <el-form :model="form" label-width="80px">
            <el-form-item label="敏感词" prop="word">
              <el-input
                v-model="form.word"
                placeholder="请输入敏感词"
                @keyup.enter="addWord"
              />
            </el-form-item>
            <el-form-item label="风险等级" prop="level">
              <el-radio-group v-model="form.level">
                <el-radio label="high">高危</el-radio>
                <el-radio label="medium">中危</el-radio>
                <el-radio label="low">低危</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="addWord" :disabled="!form.word">
                添加
              </el-button>
            </el-form-item>
          </el-form>

          <div class="quick-add">
            <h4>快速添加</h4>
            <div class="quick-tags">
              <el-button
                v-for="word in quickWords"
                :key="word.word"
                size="small"
                @click="quickAdd(word)"
              >
                {{ word.word }}
              </el-button>
            </div>
          </div>
        </el-card>

        <el-card title="操作日志" class="log-card">
          <el-timeline>
            <el-timeline-item
              v-for="log in logs"
              :key="log.id"
              :timestamp="log.time"
            >
              {{ log.message }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'

const sensitiveWords = ref([
  { id: 1, word: '暴力', level: 'high' },
  { id: 2, word: '色情', level: 'high' },
  { id: 3, word: '赌博', level: 'high' },
  { id: 4, word: '诈骗', level: 'high' },
  { id: 5, word: '广告', level: 'medium' },
  { id: 6, word: '推广', level: 'medium' },
  { id: 7, word: '微信', level: 'medium' },
  { id: 8, word: 'QQ', level: 'medium' },
  { id: 9, word: '链接', level: 'low' },
  { id: 10, word: '网址', level: 'low' }
])

const logs = ref([
  { id: 1, time: '2024-01-15 14:30:22', message: '添加敏感词：暴力' },
  { id: 2, time: '2024-01-15 14:28:15', message: '删除敏感词：测试' },
  { id: 3, time: '2024-01-15 14:25:30', message: '添加敏感词：赌博' }
])

const form = ref({
  word: '',
  level: 'medium'
})

const quickWords = ref([
  { word: '恶意', level: 'high' },
  { word: '辱骂', level: 'high' },
  { word: '刷屏', level: 'medium' },
  { word: '灌水', level: 'medium' },
  { word: '营销', level: 'medium' },
  { word: '引流', level: 'low' }
])

let nextId = 11

const highCount = computed(() => sensitiveWords.value.filter(w => w.level === 'high').length)
const mediumCount = computed(() => sensitiveWords.value.filter(w => w.level === 'medium').length)
const lowCount = computed(() => sensitiveWords.value.filter(w => w.level === 'low').length)

const getLevelText = (level) => {
  const map = { high: '高危', medium: '中危', low: '低危' }
  return map[level] || level
}

const addWord = () => {
  if (!form.value.word.trim()) {
    ElMessage.warning('请输入敏感词')
    return
  }

  const exists = sensitiveWords.value.find(w => w.word === form.value.word)
  if (exists) {
    ElMessage.warning('敏感词已存在')
    return
  }

  sensitiveWords.value.push({
    id: nextId++,
    word: form.value.word.trim(),
    level: form.value.level
  })

  addLog(`添加敏感词：${form.value.word} (${getLevelText(form.value.level)})`)
  ElMessage.success('添加成功')
  form.value.word = ''
}

const quickAdd = (wordObj) => {
  const exists = sensitiveWords.value.find(w => w.word === wordObj.word)
  if (exists) {
    ElMessage.warning('敏感词已存在')
    return
  }

  sensitiveWords.value.push({
    id: nextId++,
    word: wordObj.word,
    level: wordObj.level
  })

  addLog(`快速添加敏感词：${wordObj.word} (${getLevelText(wordObj.level)})`)
  ElMessage.success('添加成功')
}

const removeWord = (id) => {
  const word = sensitiveWords.value.find(w => w.id === id)
  if (word) {
    sensitiveWords.value = sensitiveWords.value.filter(w => w.id !== id)
    addLog(`删除敏感词：${word.word}`)
    ElMessage.success('删除成功')
  }
}

const addLog = (message) => {
  logs.value.unshift({
    id: Date.now(),
    time: new Date().toLocaleString('zh-CN'),
    message
  })
  if (logs.value.length > 10) {
    logs.value.pop()
  }
}
</script>

<style scoped>
.sensitive-words-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.main-content {
  display: flex;
  gap: 20px;
}

.left-panel {
  flex: 1;
}

.right-panel {
  width: 400px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.tag-cloud-card {
  height: 100%;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}

.tag-item {
  position: relative;
  padding: 8px 16px;
  font-size: 14px;
}

.tag-level {
  margin-left: 8px;
  font-size: 12px;
  opacity: 0.8;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #909399;
}

.empty-icon {
  margin-bottom: 12px;
}

.stats-row {
  display: flex;
  gap: 20px;
  justify-content: space-around;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.add-card {
  flex-shrink: 0;
}

.quick-add {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.quick-add h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 500;
}

.quick-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.log-card {
  flex: 1;
  min-height: 200px;
}
</style>
