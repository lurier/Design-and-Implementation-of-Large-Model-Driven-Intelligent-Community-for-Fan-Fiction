<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <h1 class="logo" @click="$router.push('/')">Fan Faction</h1>
        <p class="subtitle">发现更多精彩，分享你的热爱</p>
      </div>
      
      <div class="login-tabs">
        <span 
          :class="['tab', { active: isLogin }]" 
          @click="isLogin = true"
        >登录</span>
        <span 
          :class="['tab', { active: !isLogin }]" 
          @click="isLogin = false"
        >注册</span>
      </div>

      <el-form 
        ref="formRef"
        :model="form" 
        :rules="rules" 
        class="login-form"
        @submit.prevent="handleSubmit"
      >
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <template v-if="!isLogin">
          <el-form-item prop="nickname">
            <el-input 
              v-model="form.nickname" 
              placeholder="昵称"
              :prefix-icon="Avatar"
              size="large"
            />
          </el-form-item>

          <el-form-item prop="email">
            <el-input 
              v-model="form.email" 
              placeholder="邮箱"
              :prefix-icon="Message"
              size="large"
            />
          </el-form-item>
        </template>

        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            class="submit-btn"
            :loading="loading"
            @click="handleSubmit"
          >
            {{ isLogin ? '登录' : '注册' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { User, Lock, Avatar, Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const isLogin = ref(true)

const form = reactive({
  username: '',
  password: '',
  nickname: '',
  email: ''
})

const rules = computed(() => ({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ]
}))

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    if (isLogin.value) {
      await userStore.login({
        username: form.username,
        password: form.password
      })
      ElMessage.success('登录成功')
      // 根据用户角色跳转
      if (userStore.isAdmin()) {
        router.push('/admin/dashboard')
      } else {
        router.push('/')
      }
    } else {
      await userStore.register({
        username: form.username,
        password: form.password,
        nickname: form.nickname,
        email: form.email
      })
      ElMessage.success('注册成功，请登录')
      isLogin.value = true
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #284139 0%, #1a2d26 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-container {
  width: 400px;
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 40px;
  box-shadow: var(--shadow-elevated);

  .login-header {
    text-align: center;
    margin-bottom: 30px;

    .logo {
      font-family: var(--font-serif);
      font-size: 28px;
      font-weight: 700;
      color: var(--color-primary);
      margin: 0 0 8px 0;
      cursor: pointer;
    }

    .subtitle {
      color: var(--color-text-muted);
      font-size: 14px;
      margin: 0;
    }
  }

  .login-tabs {
    display: flex;
    margin-bottom: 24px;
    border-bottom: 1px solid var(--border-color);

    .tab {
      flex: 1;
      text-align: center;
      padding: 12px 0;
      font-size: 16px;
      color: var(--color-text-secondary);
      cursor: pointer;
      transition: all var(--transition-normal);
      position: relative;

      &.active {
        color: var(--color-primary);
        font-weight: 500;

        &::after {
          content: '';
          position: absolute;
          bottom: -1px;
          left: 50%;
          transform: translateX(-50%);
          width: 40px;
          height: 2px;
          background: var(--color-primary);
        }
      }
    }
  }

  .login-form {
    .submit-btn {
      width: 100%;
      height: 44px;
      font-size: 16px;
      background: linear-gradient(135deg, #284139 0%, #1a2d26 100%);
      border: none;
    }
  }
}
</style>
