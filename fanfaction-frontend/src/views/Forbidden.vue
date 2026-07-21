<template>
  <div class="forbidden-page">
    <div class="forbidden-container">
      <div class="error-icon">
        <el-icon :size="80"><CircleClose /></el-icon>
      </div>
      <h1 class="error-title">403</h1>
      <p class="error-message">您没有权限访问此页面</p>
      <p class="error-desc">请联系管理员获取相应权限，或返回首页继续浏览</p>
      <div class="actions">
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
        <el-button @click="$router.push('/login')">重新登录</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { CircleClose } from '@element-plus/icons-vue'
</script>

<style lang="scss" scoped>
.forbidden-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #284139 0%, #1a2d26 100%);
  
  .forbidden-container {
    text-align: center;
    padding: 60px 80px;
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
    
    .error-icon {
      color: #f56c6c;
      margin-bottom: 20px;
    }
    
    .error-title {
      font-size: 64px;
      font-weight: 700;
      color: #f56c6c;
      margin: 0 0 16px 0;
    }
    
    .error-message {
      font-size: 18px;
      color: #303133;
      margin: 0 0 8px 0;
    }
    
    .error-desc {
      font-size: 14px;
      color: #909399;
      margin: 0 0 32px 0;
    }
    
    .actions {
      display: flex;
      gap: 12px;
      justify-content: center;
    }
  }
}
</style>
