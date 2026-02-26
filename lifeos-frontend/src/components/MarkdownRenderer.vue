<template>
  <div class="markdown-content" v-html="renderedContent"></div>
</template>

<script setup>
import { computed } from 'vue'
import { marked } from 'marked'

const props = defineProps({
  content: {
    type: String,
    default: ''
  }
})

// 配置 marked 选项
marked.setOptions({
  breaks: true,  // 支持换行
  gfm: true,     // 支持 GitHub Flavored Markdown
})

const renderedContent = computed(() => {
  if (!props.content) return ''
  return marked.parse(props.content)
})
</script>

<style scoped>
.markdown-content {
  line-height: 1.6;
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4) {
  margin-top: 16px;
  margin-bottom: 12px;
  font-weight: 600;
}

.markdown-content :deep(p) {
  margin-bottom: 12px;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin-bottom: 12px;
  padding-left: 24px;
}

.markdown-content :deep(li) {
  margin-bottom: 4px;
}

.markdown-content :deep(strong) {
  font-weight: 600;
  color: #10b981;
}

.markdown-content :deep(code) {
  background-color: #f3f4f6;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
}

.markdown-content :deep(pre) {
  background-color: #f3f4f6;
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
}
</style>
