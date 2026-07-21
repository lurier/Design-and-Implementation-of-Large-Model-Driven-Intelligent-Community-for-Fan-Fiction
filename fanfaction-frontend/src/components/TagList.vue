<template>
  <span class="tags">
    <el-tag 
      v-for="tag in tags" 
      :key="tag" 
      size="small" 
      effect="plain"
      class="tag-item"
      @click.stop="handleTagClick(tag)"
    >
      {{ tag }}
    </el-tag>
  </span>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const props = defineProps({
  tags: {
    type: Array,
    required: true
  },
  mode: {
    type: String,
    default: 'navigate', // 'navigate' - 跳转到首页带参数, 'emit' - 触发事件回调
    validator: (value) => ['navigate', 'emit'].includes(value)
  }
})

const emit = defineEmits(['tag-click'])

const handleTagClick = (tag) => {
  if (props.mode === 'navigate') {
    // 场景一：文章详情页点击，跳转到发现页带query参数
    router.push({
      path: '/discover',
      query: { activeTag: encodeURIComponent(tag) }
    })
  } else {
    // 场景三：首页文章卡片点击，触发事件回调
    emit('tag-click', tag)
  }
}
</script>

<style lang="scss" scoped>
.tags {
  display: flex;
  gap: 6px;

  .tag-item {
    cursor: pointer;
    transition: all var(--transition-fast);
    background: transparent;
    color: var(--color-primary);
    border: 1px solid var(--color-primary);
    border-radius: var(--radius-round);
    font-family: var(--font-sans);
    font-size: 11px;
    padding: 2px 10px;

    &:hover {
      background: var(--color-primary);
      color: #fff;
    }
  }
}
</style>
