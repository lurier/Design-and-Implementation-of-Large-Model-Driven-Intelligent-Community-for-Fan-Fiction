<template>
  <div class="creator-center">
    <Layout>
      <div class="creator-container">
        <!-- 侧边栏导航 -->
        <el-card class="sidebar-card">
          <div class="creator-info">
            <el-avatar 
              :size="64" 
              :src="userStore.userInfo?.avatar || ''"
              class="creator-avatar"
            >
              {{ userStore.userInfo?.nickname?.charAt(0) || userStore.userInfo?.username?.charAt(0) || 'C' }}
            </el-avatar>
            <div class="creator-name">
              <h3>{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</h3>
              <el-tag type="success" size="small">创作者</el-tag>
            </div>
          </div>
          
          <el-menu
            :default-active="activeMenu"
            class="creator-menu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="/creator/publish">
              <el-icon><Edit /></el-icon>
              <span>发布作品</span>
            </el-menu-item>
            <el-menu-item index="/creator/works">
              <el-icon><Document /></el-icon>
              <span>作品管理</span>
            </el-menu-item>
            <el-menu-item index="/creator/stats">
              <el-icon><DataAnalysis /></el-icon>
              <span>数据统计</span>
            </el-menu-item>
          </el-menu>
        </el-card>

        <!-- 主内容区 -->
        <div class="main-content">
          <router-view />
        </div>
      </div>
    </Layout>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Edit, Document, DataAnalysis } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => {
  return route.path
})

const handleMenuSelect = (index) => {
  router.push(index)
}
</script>

<style lang="scss" scoped>
.creator-center {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.creator-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  display: flex;
  gap: 20px;
}

.sidebar-card {
  width: 240px;
  flex-shrink: 0;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

  .creator-info {
    padding: 20px;
    text-align: center;
    border-bottom: 1px solid #f0f0f0;

    .creator-avatar {
      margin-bottom: 12px;
      border: 3px solid #e0e0e0;
    }

    .creator-name {
      h3 {
        margin: 0 0 8px 0;
        font-size: 16px;
        color: #333;
      }

      .el-tag {
        font-size: 12px;
      }
    }
  }

  .creator-menu {
    border-right: none;
    
    .el-menu-item {
      height: 50px;
      line-height: 50px;
      margin: 4px 8px;
      border-radius: var(--radius-md);
      color: var(--color-text-secondary);
      font-family: var(--font-sans);
      border-left: 3px solid transparent;
      
      &:hover {
        background-color: rgba(128, 144, 118, 0.08);
        color: var(--color-primary);
      }
      
      &.is-active {
        background-color: var(--color-primary);
        color: #fff;
        
        .el-icon {
          color: #fff;
        }
      }
    }
  }
}

.main-content {
  flex: 1;
  min-width: 0;
}
</style>
