<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="logo" @click="$router.push('/admin/dashboard')">
          <el-icon :size="28"><Setting /></el-icon>
          <span v-if="!sidebarCollapsed" class="logo-text">管理后台</span>
        </div>
      </div>
      
      <nav class="sidebar-nav">
        <el-menu 
          :default-active="currentRoute" 
          class="sidebar-menu"
          mode="vertical"
        >
          <el-menu-item index="/admin/dashboard" @click="navigateTo('/admin/dashboard')">
            <el-icon :size="20"><DataAnalysis /></el-icon>
            <span v-if="!sidebarCollapsed">工作台</span>
          </el-menu-item>
          <el-menu-item index="/admin/audit-workbench" @click="navigateTo('/admin/audit-workbench')">
            <el-icon :size="20"><Grid /></el-icon>
            <span v-if="!sidebarCollapsed">智能审核</span>
          </el-menu-item>
          <el-sub-menu index="management">
            <template #title>
              <el-icon :size="20"><User /></el-icon>
              <span v-if="!sidebarCollapsed">系统管理</span>
            </template>
            <el-menu-item index="/admin/user-manage" @click="navigateTo('/admin/user-manage')">
              <el-icon :size="16"><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/content-manage" @click="navigateTo('/admin/content-manage')">
              <el-icon :size="16"><Document /></el-icon>
              <span>文章管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/comment-manage" @click="navigateTo('/admin/comment-manage')">
              <el-icon :size="16"><List /></el-icon>
              <span>评论管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/sensitive-words" @click="navigateTo('/admin/sensitive-words')">
              <el-icon :size="16"><List /></el-icon>
              <span>敏感词管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/tag-manage" @click="navigateTo('/admin/tag-manage')">
              <el-icon :size="16"><List /></el-icon>
              <span>标签管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/faq-manage" @click="navigateTo('/admin/faq-manage')">
              <el-icon :size="16"><List /></el-icon>
              <span>FAQ管理</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/admin/system-settings" @click="navigateTo('/admin/system-settings')">
            <el-icon :size="20"><Setting /></el-icon>
            <span v-if="!sidebarCollapsed">系统设置</span>
          </el-menu-item>
        </el-menu>
      </nav>
      
      <div class="sidebar-footer">
        <button 
          class="collapse-btn" 
          @click="sidebarCollapsed = !sidebarCollapsed"
          title="收起侧边栏"
        >
          <el-icon :size="18"><Fold v-if="!sidebarCollapsed" /></el-icon>
          <el-icon :size="18"><Expand v-if="sidebarCollapsed" /></el-icon>
        </button>
      </div>
    </aside>
    
    <!-- 主内容区域 -->
    <main class="main-content">
      <!-- 顶部状态栏 -->
      <header class="top-bar">
        <div class="top-left">
          <button 
            class="sidebar-toggle" 
            @click="sidebarCollapsed = !sidebarCollapsed"
          >
            <el-icon :size="20"><Menu /></el-icon>
          </button>
          <span class="page-title">{{ pageTitle }}</span>
        </div>
        
        <div class="top-right">
          <div class="user-info">
            <el-dropdown>
              <span class="dropdown-trigger">
                <el-icon :size="18"><User /></el-icon>
                <span>{{ userStore.userInfo?.nickname || '管理员' }}</span>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item @click.native="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </div>
      </header>
      
      <!-- 页面内容 -->
      <div class="content-wrapper">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { 
  DataAnalysis, 
  Document, 
  User, 
  Setting, 
  Menu, 
  Fold,
  Expand,
  SwitchButton,
  Grid,
  List
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const sidebarCollapsed = ref(false)

const navigateTo = (path) => {
  router.push(path)
}

const currentRoute = computed(() => route.fullPath)

const pageTitleMap = {
  '/admin/dashboard': '工作台',
  '/admin/audit-workbench': '智能审核工作台',
  '/admin/user-manage': '用户管理',
  '/admin/content-manage': '文章管理',
  '/admin/comment-manage': '评论管理',
  '/admin/sensitive-words': '敏感词库管理',
  '/admin/tag-manage': '分类标签管理',
  '/admin/faq-manage': 'FAQ知识库维护',
  '/admin/system-settings': '系统设置',
  '/admin/creator-review': '创作者审核',
  '/admin/applications': '申请管理',
  '/admin/article-review': '文章审核'
}

const pageTitle = computed(() => pageTitleMap[route.fullPath] || '管理后台')

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
  ElMessage.success('退出登录成功')
}
</script>

<style lang="scss" scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  background: #f5f7fa;
  overflow: hidden;
}

// 侧边栏样式
.sidebar {
  width: 220px;
  background: #1a2d26;
  color: var(--color-text-inverse);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;

  &.collapsed {
    width: 64px;
  }

  .sidebar-header {
    padding: 20px 16px;
    background: #111A19;
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);

    .logo {
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: pointer;

      .logo-text {
        font-family: var(--font-sans);
        font-size: 17px;
        font-weight: 600;
        color: var(--color-text-inverse);
        letter-spacing: 0.5px;
      }
    }
  }

  .sidebar-nav {
    flex: 1;
    padding: 16px 0;

    .sidebar-menu {
      border-right: none;
      background: transparent;

      :deep(.el-menu-item) {
        color: rgba(255, 255, 245, 0.75);
        margin: 4px 8px;
        border-radius: var(--radius-md);
        height: 44px;
        line-height: 44px;
        font-family: var(--font-sans);
        font-size: 14px;
        transition: all var(--transition-fast);
        border-left: 3px solid transparent;

        &:hover {
          background: rgba(255, 255, 255, 0.06);
          color: #fff;
        }

        &.is-active {
          background: rgba(255, 255, 255, 0.08);
          color: #fff;
          font-weight: 600;
          border-left: 3px solid var(--color-accent);
          border-radius: 0 var(--radius-md) var(--radius-md) 0;
        }
      }

      :deep(.el-sub-menu) {
        .el-sub-menu__title {
          color: rgba(255, 255, 245, 0.75);
          margin: 4px 8px;
          border-radius: var(--radius-md);
          height: 44px;
          line-height: 44px;
          font-family: var(--font-sans);
          font-size: 14px;
          transition: all var(--transition-fast);
          border-left: 3px solid transparent;

          &:hover {
            background: rgba(255, 255, 255, 0.06);
            color: #fff;
          }

          // 展开状态：加深背景 + 显示土褐色指示器
          &.is-opened {
            background: rgba(255, 255, 255, 0.05);
            color: #fff;
            border-left: 3px solid var(--color-accent);
            border-radius: 0 var(--radius-md) var(--radius-md) 0;
          }

          &.is-active {
            color: #fff;
          }
        }

        // 子菜单项
        .el-menu .el-menu-item {
          background: #355248;
          color: #E0E0E0;
          margin: 1px 16px;
          border-radius: var(--radius-sm);
          height: 40px;
          line-height: 40px;
          font-size: 13px;
          border-left: 3px solid transparent;

          &:hover {
            background: #809076;
            color: #fff;
          }

          &.is-active {
            background: #2a453b;
            color: #fff;
            font-weight: 600;
            border-left: 3px solid var(--color-accent);
            border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
          }
        }

        .el-menu--popup {
          background: #1a2d26 !important;
          border: none;

          .el-menu-item {
            background: #355248;
            color: #E0E0E0;

            &:hover {
              background: #809076;
              color: #fff;
            }

            &.is-active {
              background: #2a453b;
              color: #fff;
              border-left: 3px solid var(--color-accent);
            }
          }
        }
      }
    }
  }

  .sidebar-footer {
    padding: 12px;
    border-top: 1px solid rgba(255, 255, 255, 0.08);

    .collapse-btn {
      width: 100%;
      height: 36px;
      border: none;
      background: rgba(255, 255, 255, 0.08);
      color: #fff;
      border-radius: var(--radius-md);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: background var(--transition-fast);

      &:hover {
        background: rgba(255, 255, 255, 0.14);
      }
    }
  }
}

// 主内容区域
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-left: 220px;
  transition: margin-left 0.3s ease;
  
  .sidebar.collapsed & {
    margin-left: 64px;
  }
  
  // 顶部状态栏
  .top-bar {
    height: 64px;
    background: #fff;
    border-bottom: 1px solid var(--border-color);
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;
    box-shadow: var(--shadow-card);

    .top-left {
      display: flex;
      align-items: center;
      gap: 12px;

      .sidebar-toggle {
        width: 36px;
        height: 36px;
        border: none;
        background: rgba(128, 144, 118, 0.08);
        border-radius: var(--radius-md);
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all var(--transition-fast);
        color: var(--color-text-secondary);

        &:hover {
          background: rgba(128, 144, 118, 0.15);
          color: var(--color-primary);
        }
      }

      .page-title {
        font-family: var(--font-sans);
        font-size: 18px;
        font-weight: 600;
        color: var(--color-text-main);
      }
    }

    .top-right {
      display: flex;
      align-items: center;
      gap: 16px;

      .user-info {
        .dropdown-trigger {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 8px 12px;
          background: rgba(128, 144, 118, 0.06);
          border-radius: var(--radius-round);
          cursor: pointer;
          transition: all var(--transition-fast);
          color: var(--color-text-secondary);
          font-family: var(--font-sans);

          &:hover {
            background: rgba(128, 144, 118, 0.12);
            color: var(--color-primary);
          }
        }

        :deep(.el-dropdown-menu) {
          min-width: 140px;
        }
      }
    }
  }

  // 内容包装器
  .content-wrapper {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
    background: var(--color-bg-page);
  }
}
</style>
