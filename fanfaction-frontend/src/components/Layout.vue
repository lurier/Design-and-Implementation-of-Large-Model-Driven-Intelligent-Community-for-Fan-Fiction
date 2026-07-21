<template>
  <div class="layout">
    <header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <span class="logo-text">Fan Faction</span>
        </div>
        <nav class="nav">
          <template v-if="userStore.token">
            <el-button link @click="$router.push('/')">发现</el-button>
            <el-button link @click="$router.push('/history')">阅读历史</el-button>
            <el-button link @click="$router.push('/reader-center')">数据中心</el-button>
            <!-- 根据权限显示不同的创作者入口 -->
            <el-button 
              v-if="userStore.hasCreatorPermission()" 
              link 
              @click="$router.push('/creator/publish')" 
              class="creator-btn"
            >
              <el-icon><Edit /></el-icon>
              创作中心
            </el-button>
            <el-button 
              v-else 
              link 
              @click="$router.push('/creator-apply')" 
              class="creator-btn"
            >
              <el-icon><Edit /></el-icon>
              成为创作者
            </el-button>
            <el-dropdown trigger="click">
              <span class="user-info">
                <el-avatar 
                  :size="32" 
                  :src="userStore.userInfo?.avatar || ''"
                  class="nav-avatar"
                >
                  {{ userStore.userInfo?.nickname?.charAt(0) || userStore.userInfo?.username?.charAt(0) || 'U' }}
                </el-avatar>
                <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/profile')">个人主页</el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin()" @click="$router.push('/admin/applications')">创作者审核</el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin()" @click="$router.push('/admin/article-review')">文章审核</el-dropdown-item>
                  <el-dropdown-item divided @click="clearToken()">清除 Token（测试用）</el-dropdown-item>
                  <el-dropdown-item divided @click="userStore.logout(); $router.push('/login')">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" @click="$router.push('/login')">登录</el-button>
          </template>
        </nav>
      </div>
    </header>
    <main class="main-content">
      <slot />
    </main>
    <!-- 智能客服浮窗 -->
    <CustomerService />
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import CustomerService from '@/components/CustomerService.vue'
import { Edit } from '@element-plus/icons-vue'

const userStore = useUserStore()

const clearToken = () => {
  localStorage.removeItem('token')
  ElMessage.success('Token 已清除，请重新登录')
  setTimeout(() => {
    window.location.reload()
  }, 500)
}
</script>

<style lang="scss" scoped>
.layout {
  min-height: 100vh;
  background-color: var(--color-bg-page);
}

.header {
  background: #FDFBF7;
  box-shadow: var(--shadow-card);
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid var(--color-primary);

  .header-content {
    max-width: 960px;
    margin: 0 auto;
    padding: 0 20px;
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .logo {
    cursor: pointer;
    .logo-text {
      font-family: var(--font-serif);
      font-size: 20px;
      font-weight: 700;
      color: var(--color-primary);
      letter-spacing: 0.5px;
    }
  }

  .nav {
    display: flex;
    align-items: center;
    gap: 12px;

    .el-button.is-link {
      color: var(--color-text-secondary);
      font-family: var(--font-sans);
      font-size: 14px;
      padding: 8px 12px;
      position: relative;
      text-decoration: none;

      // 土褐色下划线 hover 效果
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 12px;
        right: 12px;
        height: 2px;
        background: var(--color-accent);
        transform: scaleX(0);
        transition: transform var(--transition-normal);
        border-radius: 1px;
      }

      &:hover {
        color: var(--color-primary);

        &::after {
          transform: scaleX(1);
        }
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;

      .nav-avatar {
        border-radius: 50%;
        object-fit: cover;
        border: 2px solid var(--border-color);
        transition: border-color var(--transition-normal);

        &:hover {
          border-color: var(--color-primary);
        }
      }

      .username {
        font-family: var(--font-sans);
        font-size: 14px;
        color: var(--color-text-main);
      }
    }

    .creator-btn {
      color: var(--color-accent) !important;
      font-weight: 500;

      .el-icon {
        margin-right: 4px;
      }

      &:hover {
        color: var(--color-accent-light) !important;
      }
    }
  }
}

.main-content {
  max-width: 960px;
  margin: 20px auto;
  padding: 0 20px;
}
</style>
