import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/discover',
    name: 'Discover',
    component: () => import('@/views/Home.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/article/:id',
    name: 'ArticleDetail',
    component: () => import('@/views/ArticleDetail.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/profile',
    name: 'UserProfile',
    component: () => import('@/views/UserProfile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/history',
    name: 'ReadingHistory',
    component: () => import('@/views/ReadingHistory.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/reader-center',
    name: 'ReaderCenter',
    component: () => import('@/views/ReaderCenter.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/creator-apply',
    name: 'CreatorApply',
    component: () => import('@/views/CreatorApply.vue'),
    meta: { requiresAuth: true }
  },
  // 创作者中心路由
  {
    path: '/creator',
    name: 'CreatorCenter',
    component: () => import('@/views/creator/CreatorCenter.vue'),
    meta: { requiresAuth: true, requiresCreator: true },
    redirect: '/creator/publish',
    children: [
      {
        path: 'publish',
        name: 'CreatorPublish',
        component: () => import('@/views/creator/PublishWork.vue'),
        meta: { requiresAuth: true, requiresCreator: true }
      },
      {
        path: 'works',
        name: 'CreatorWorks',
        component: () => import('@/views/creator/MyWorks.vue'),
        meta: { requiresAuth: true, requiresCreator: true }
      },
      {
        path: 'stats',
        name: 'CreatorStats',
        component: () => import('@/views/creator/CreatorStats.vue'),
        meta: { requiresAuth: true, requiresCreator: true }
      }
    ]
  },
  // 管理员后台路由组
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'content-review',
        name: 'AdminContentReview',
        component: () => import('@/views/admin/ContentReview.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'user-management',
        name: 'AdminUserManagement',
        component: () => import('@/views/admin/UserManagement.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'system-settings',
        name: 'AdminSystemSettings',
        component: () => import('@/views/admin/SystemSettings.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      // 原有管理员页面保留
      {
        path: 'creator-review',
        name: 'AdminCreatorReview',
        component: () => import('@/views/admin/CreatorReview.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'applications',
        name: 'AdminApplications',
        component: () => import('@/views/admin/Applications.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'article-review',
        name: 'AdminArticleReview',
        component: () => import('@/views/admin/ArticleReview.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'audit-workbench',
        name: 'AdminAuditWorkbench',
        component: () => import('@/views/admin/AuditList.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'user-manage',
        name: 'AdminUserManage',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'content-manage',
        name: 'AdminContentManage',
        component: () => import('@/views/admin/ContentManage.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'comment-manage',
        name: 'AdminCommentManage',
        component: () => import('@/views/admin/CommentManage.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'sensitive-words',
        name: 'AdminSensitiveWords',
        component: () => import('@/views/admin/SensitiveWords.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'tag-manage',
        name: 'AdminTagManage',
        component: () => import('@/views/admin/TagManage.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'faq-manage',
        name: 'AdminFaqManage',
        component: () => import('@/views/admin/FaqManage.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      }
    ]
  },
  // 403 页面
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/Forbidden.vue'),
    meta: { requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userStore = useUserStore()
  
  // 如果有 token，允许访问所有页面
  if (token) {
    // 检查是否需要管理员权限
    if (to.meta.requiresAdmin) {
      // 确保用户信息已加载
      if (!userStore.userInfo && token) {
        userStore.fetchUserInfo().then(() => {
          checkAdminPermission(to, from, next, userStore)
        }).catch(() => {
          ElMessage.error('获取用户信息失败')
          next('/login')
        })
        return
      }
      
      checkAdminPermission(to, from, next, userStore)
      return
    }
    
    // 检查是否需要创作者权限
    if (to.meta.requiresCreator) {
      // 确保用户信息已加载
      if (!userStore.userInfo && token) {
        userStore.fetchUserInfo().then(() => {
          checkCreatorPermission(to, from, next, userStore)
        }).catch(() => {
          ElMessage.error('获取用户信息失败')
          next('/login')
        })
        return
      }
      
      checkCreatorPermission(to, from, next, userStore)
      return
    }
    
    next()
    return
  }
  
  // 如果没有 token，只能访问登录页和首页
  if (to.meta.requiresAuth === false || to.path === '/login' || to.path === '/') {
    next()
  } else {
    // 需要登录的页面，重定向到登录页
    next('/login')
  }
})

// 检查管理员权限的辅助函数
function checkAdminPermission(to, from, next, userStore) {
  if (userStore.isAdmin()) {
    next()
  } else {
    ElMessage.error('您没有管理员权限，无法访问此页面')
    next('/403')
  }
}

// 检查创作者权限的辅助函数
function checkCreatorPermission(to, from, next, userStore) {
  // 检查是否正在审核中
  if (userStore.isCreatorPending()) {
    ElMessage.warning({
      message: '您的创作者申请正在审核中，请耐心等待审核结果',
      duration: 3000
    })
    next('/creator-apply')
    return
  }
  
  // 检查是否有创作者权限
  if (userStore.hasCreatorPermission()) {
    next()
  } else {
    ElMessage.warning('您暂无创作者权限，请先申请成为创作者')
    next('/creator-apply')
  }
}

export default router
