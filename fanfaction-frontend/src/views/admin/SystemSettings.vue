<template>
  <div class="system-settings-page">
    <div class="page-header">
      <h2 class="page-title">系统设置</h2>
    </div>
    
    <div class="settings-container">
      <div class="settings-card">
        <div class="card-header">
          <h3 class="card-title">基本设置</h3>
        </div>
        <div class="card-body">
          <el-form :model="basicSettings" label-width="120px">
            <el-form-item label="网站名称">
              <el-input v-model="basicSettings.siteName" class="input-medium" />
            </el-form-item>
            <el-form-item label="网站描述">
              <el-input v-model="basicSettings.siteDesc" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="首页展示文章数">
              <el-input-number v-model="basicSettings.articleCount" :min="10" :max="100" />
            </el-form-item>
            <el-form-item label="是否开启审核">
              <el-switch v-model="basicSettings.reviewEnabled" />
            </el-form-item>
          </el-form>
        </div>
        <div class="card-footer">
          <el-button type="primary" @click="saveBasicSettings">保存设置</el-button>
        </div>
      </div>
      
      <div class="settings-card">
        <div class="card-header">
          <h3 class="card-title">安全设置</h3>
        </div>
        <div class="card-body">
          <el-form :model="securitySettings" label-width="120px">
            <el-form-item label="登录超时时间（分钟）">
              <el-input-number v-model="securitySettings.loginTimeout" :min="15" :max="1440" />
            </el-form-item>
            <el-form-item label="密码最小长度">
              <el-input-number v-model="securitySettings.minPasswordLength" :min="6" :max="32" />
            </el-form-item>
            <el-form-item label="启用验证码">
              <el-switch v-model="securitySettings.captchaEnabled" />
            </el-form-item>
            <el-form-item label="限制登录尝试次数">
              <el-switch v-model="securitySettings.loginAttemptLimit" />
              <span v-if="securitySettings.loginAttemptLimit" class="limit-text">（最多5次）</span>
            </el-form-item>
          </el-form>
        </div>
        <div class="card-footer">
          <el-button type="primary" @click="saveSecuritySettings">保存设置</el-button>
        </div>
      </div>
      
      <div class="settings-card">
        <div class="card-header">
          <h3 class="card-title">数据管理</h3>
        </div>
        <div class="card-body">
          <div class="data-actions">
            <div class="action-item">
              <div class="action-icon backup">
                <el-icon :size="24"><Download /></el-icon>
              </div>
              <div class="action-info">
                <div class="action-title">备份数据库</div>
                <div class="action-desc">导出所有数据到本地文件</div>
              </div>
              <el-button type="primary" size="small" @click="backupDatabase">备份</el-button>
            </div>
            
            <div class="action-item">
              <div class="action-icon clean">
                <el-icon :size="24"><Delete /></el-icon>
              </div>
              <div class="action-info">
                <div class="action-title">清理缓存</div>
                <div class="action-desc">清除系统缓存和临时文件</div>
              </div>
              <el-button type="warning" size="small" @click="cleanCache">清理</el-button>
            </div>
            
            <div class="action-item">
              <div class="action-icon logs">
                <el-icon :size="24"><Document /></el-icon>
              </div>
              <div class="action-info">
                <div class="action-title">查看日志</div>
                <div class="action-desc">查看系统操作日志</div>
              </div>
              <el-button size="small" @click="viewLogs">查看</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { Download, Delete, Document } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const basicSettings = reactive({
  siteName: 'Fan Faction',
  siteDesc: '发现更多精彩，分享你的热爱',
  articleCount: 20,
  reviewEnabled: true
})

const securitySettings = reactive({
  loginTimeout: 60,
  minPasswordLength: 8,
  captchaEnabled: false,
  loginAttemptLimit: true
})

const saveBasicSettings = () => {
  ElMessage.success('基本设置已保存')
}

const saveSecuritySettings = () => {
  ElMessage.success('安全设置已保存')
}

const backupDatabase = () => {
  ElMessage.info('备份功能开发中...')
}

const cleanCache = () => {
  ElMessage.success('缓存清理成功')
}

const viewLogs = () => {
  ElMessage.info('日志功能开发中...')
}

onMounted(() => {
  // 加载设置
})
</script>

<style lang="scss" scoped>
.system-settings-page {
  .page-header {
    margin-bottom: 20px;
    
    .page-title {
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }
  }
  
  .settings-container {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    
    .settings-card {
      background: #fff;
      border-radius: 12px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
      overflow: hidden;
      
      .card-header {
        padding: 16px 20px;
        border-bottom: 1px solid #f0f0f0;
        
        .card-title {
          font-size: 15px;
          font-weight: 600;
          color: #303133;
          margin: 0;
        }
      }
      
      .card-body {
        padding: 20px;
        
        .input-medium {
          width: 300px;
        }
        
        .limit-text {
          margin-left: 8px;
          color: #909399;
          font-size: 13px;
        }
        
        .data-actions {
          .action-item {
            display: flex;
            align-items: center;
            gap: 16px;
            padding: 16px;
            background: #fafafa;
            border-radius: 8px;
            margin-bottom: 12px;
            
            &:last-child {
              margin-bottom: 0;
            }
            
            .action-icon {
              width: 48px;
              height: 48px;
              border-radius: 12px;
              display: flex;
              align-items: center;
              justify-content: center;
              
              &.backup {
                background: linear-gradient(135deg, #284139 0%, #1a2d26 100%);
                color: #fff;
              }
              
              &.clean {
                background: linear-gradient(135deg, #fc4a1a 0%, #f7b733 100%);
                color: #fff;
              }
              
              &.logs {
                background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
                color: #fff;
              }
            }
            
            .action-info {
              flex: 1;
              
              .action-title {
                font-size: 14px;
                font-weight: 500;
                color: #303133;
              }
              
              .action-desc {
                font-size: 12px;
                color: #909399;
                margin-top: 4px;
              }
            }
          }
        }
      }
      
      .card-footer {
        padding: 16px 20px;
        border-top: 1px solid #f0f0f0;
        display: flex;
        justify-content: flex-end;
      }
    }
  }
}
</style>
