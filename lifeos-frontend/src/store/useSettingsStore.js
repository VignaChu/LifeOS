import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { useStorage, useDark } from '@vueuse/core'

// 定义所有可用主题
export const themes = [
  {
    id: 'light',
    name: '简约白',
    icon: '☀️',
    colors: {
      bgPrimary: '#ffffff',
      bgSecondary: '#f3f4f6',
      bgCard: 'rgba(255, 255, 255, 0.9)',
      textPrimary: '#111827',
      textSecondary: '#6b7280',
      textMuted: '#9ca3af',
      borderColor: 'rgba(229, 231, 235, 0.5)',
      inputBg: '#ffffff',
      inputBorder: '#d1d5db',
      shadowColor: 'rgba(0, 0, 0, 0.1)',
      accentColor: '#3b82f6',
      primaryColor: '#3b82f6',
      gradientFrom: 'rgba(59, 130, 246, 0.1)',
      gradientTo: 'rgba(147, 51, 234, 0.1)'
    }
  },
  {
    id: 'dark',
    name: '深邃黑',
    icon: '🌙',
    colors: {
      bgPrimary: '#0f172a',
      bgSecondary: '#1e293b',
      bgCard: 'rgba(30, 41, 59, 0.9)',
      textPrimary: '#f1f5f9',
      textSecondary: '#94a3b8',
      textMuted: '#64748b',
      borderColor: 'rgba(51, 65, 85, 0.5)',
      inputBg: '#1e293b',
      inputBorder: '#475569',
      shadowColor: 'rgba(0, 0, 0, 0.3)',
      accentColor: '#60a5fa',
      primaryColor: '#60a5fa',
      gradientFrom: 'rgba(30, 58, 138, 0.2)',
      gradientTo: 'rgba(88, 28, 135, 0.2)'
    }
  },
  {
    id: 'green',
    name: '清新绿',
    icon: '🌿',
    colors: {
      bgPrimary: '#f0fdf4',
      bgSecondary: '#dcfce7',
      bgCard: 'rgba(220, 252, 231, 0.9)',
      textPrimary: '#14532d',
      textSecondary: '#166534',
      textMuted: '#22c55e',
      borderColor: 'rgba(34, 197, 94, 0.3)',
      inputBg: '#ffffff',
      inputBorder: '#86efac',
      shadowColor: 'rgba(34, 197, 94, 0.1)',
      accentColor: '#16a34a',
      primaryColor: '#16a34a',
      gradientFrom: 'rgba(34, 197, 94, 0.15)',
      gradientTo: 'rgba(22, 163, 74, 0.15)'
    }
  },
  {
    id: 'pink',
    name: '梦幻粉',
    icon: '🌸',
    colors: {
      bgPrimary: '#fdf2f8',
      bgSecondary: '#fce7f3',
      bgCard: 'rgba(252, 231, 243, 0.9)',
      textPrimary: '#831843',
      textSecondary: '#be185d',
      textMuted: '#ec4899',
      borderColor: 'rgba(236, 72, 153, 0.3)',
      inputBg: '#ffffff',
      inputBorder: '#f9a8d4',
      shadowColor: 'rgba(236, 72, 153, 0.1)',
      accentColor: '#db2777',
      primaryColor: '#db2777',
      gradientFrom: 'rgba(236, 72, 153, 0.15)',
      gradientTo: 'rgba(219, 39, 119, 0.15)'
    }
  },
  {
    id: 'blue',
    name: '天空蓝',
    icon: '☁️',
    colors: {
      bgPrimary: '#eff6ff',
      bgSecondary: '#dbeafe',
      bgCard: 'rgba(219, 234, 254, 0.9)',
      textPrimary: '#1e3a8a',
      textSecondary: '#1d4ed8',
      textMuted: '#3b82f6',
      borderColor: 'rgba(59, 130, 246, 0.3)',
      inputBg: '#ffffff',
      inputBorder: '#93c5fd',
      shadowColor: 'rgba(59, 130, 246, 0.1)',
      accentColor: '#2563eb',
      primaryColor: '#2563eb',
      gradientFrom: 'rgba(59, 130, 246, 0.15)',
      gradientTo: 'rgba(37, 99, 235, 0.15)'
    }
  },
  {
    id: 'purple',
    name: '优雅紫',
    icon: '🔮',
    colors: {
      bgPrimary: '#faf5ff',
      bgSecondary: '#f3e8ff',
      bgCard: 'rgba(243, 232, 255, 0.9)',
      textPrimary: '#581c87',
      textSecondary: '#7c3aed',
      textMuted: '#a855f7',
      borderColor: 'rgba(168, 85, 247, 0.3)',
      inputBg: '#ffffff',
      inputBorder: '#d8b4fe',
      shadowColor: 'rgba(168, 85, 247, 0.1)',
      accentColor: '#7c3aed',
      primaryColor: '#7c3aed',
      gradientFrom: 'rgba(168, 85, 247, 0.15)',
      gradientTo: 'rgba(124, 58, 237, 0.15)'
    }
  },
  {
    id: 'orange',
    name: '活力橙',
    icon: '🍊',
    colors: {
      bgPrimary: '#fff7ed',
      bgSecondary: '#ffedd5',
      bgCard: 'rgba(255, 237, 213, 0.9)',
      textPrimary: '#7c2d12',
      textSecondary: '#c2410c',
      textMuted: '#f97316',
      borderColor: 'rgba(249, 115, 22, 0.3)',
      inputBg: '#ffffff',
      inputBorder: '#fdba74',
      shadowColor: 'rgba(249, 115, 22, 0.1)',
      accentColor: '#ea580c',
      primaryColor: '#ea580c',
      gradientFrom: 'rgba(249, 115, 22, 0.15)',
      gradientTo: 'rgba(234, 88, 12, 0.15)'
    }
  },
  {
    id: 'red',
    name: '热情红',
    icon: '❤️',
    colors: {
      bgPrimary: '#fef2f2',
      bgSecondary: '#fee2e2',
      bgCard: 'rgba(254, 226, 226, 0.9)',
      textPrimary: '#7f1d1d',
      textSecondary: '#b91c1c',
      textMuted: '#ef4444',
      borderColor: 'rgba(239, 68, 68, 0.3)',
      inputBg: '#ffffff',
      inputBorder: '#fca5a5',
      shadowColor: 'rgba(239, 68, 68, 0.1)',
      accentColor: '#dc2626',
      primaryColor: '#dc2626',
      gradientFrom: 'rgba(239, 68, 68, 0.15)',
      gradientTo: 'rgba(220, 38, 38, 0.15)'
    }
  },
  {
    id: 'yellow',
    name: '阳光黄',
    icon: '☀️',
    colors: {
      bgPrimary: '#fefce8',
      bgSecondary: '#fef9c3',
      bgCard: 'rgba(254, 249, 195, 0.9)',
      textPrimary: '#713f12',
      textSecondary: '#a16207',
      textMuted: '#eab308',
      borderColor: 'rgba(234, 179, 8, 0.3)',
      inputBg: '#ffffff',
      inputBorder: '#fde047',
      shadowColor: 'rgba(234, 179, 8, 0.1)',
      accentColor: '#ca8a04',
      primaryColor: '#ca8a04',
      gradientFrom: 'rgba(234, 179, 8, 0.15)',
      gradientTo: 'rgba(202, 138, 4, 0.15)'
    }
  },
  {
    id: 'teal',
    name: '青柠绿',
    icon: '🍋',
    colors: {
      bgPrimary: '#f0fdfa',
      bgSecondary: '#ccfbf1',
      bgCard: 'rgba(204, 251, 241, 0.9)',
      textPrimary: '#134e4a',
      textSecondary: '#0f766e',
      textMuted: '#14b8a6',
      borderColor: 'rgba(20, 184, 166, 0.3)',
      inputBg: '#ffffff',
      inputBorder: '#5eead4',
      shadowColor: 'rgba(20, 184, 166, 0.1)',
      accentColor: '#0d9488',
      primaryColor: '#0d9488',
      gradientFrom: 'rgba(20, 184, 166, 0.15)',
      gradientTo: 'rgba(13, 148, 136, 0.15)'
    }
  },
  {
    id: 'indigo',
    name: '靛青蓝',
    icon: '🎨',
    colors: {
      bgPrimary: '#eef2ff',
      bgSecondary: '#e0e7ff',
      bgCard: 'rgba(224, 231, 255, 0.9)',
      textPrimary: '#312e81',
      textSecondary: '#4338ca',
      textMuted: '#6366f1',
      borderColor: 'rgba(99, 102, 241, 0.3)',
      inputBg: '#ffffff',
      inputBorder: '#a5b4fc',
      shadowColor: 'rgba(99, 102, 241, 0.1)',
      accentColor: '#4f46e5',
      primaryColor: '#4f46e5',
      gradientFrom: 'rgba(99, 102, 241, 0.15)',
      gradientTo: 'rgba(79, 70, 229, 0.15)'
    }
  },
  {
    id: 'rose',
    name: '玫瑰红',
    icon: '🌹',
    colors: {
      bgPrimary: '#fff1f2',
      bgSecondary: '#ffe4e6',
      bgCard: 'rgba(255, 228, 230, 0.9)',
      textPrimary: '#881337',
      textSecondary: '#be123c',
      textMuted: '#fb7185',
      borderColor: 'rgba(251, 113, 133, 0.3)',
      inputBg: '#ffffff',
      inputBorder: '#fda4af',
      shadowColor: 'rgba(251, 113, 133, 0.1)',
      accentColor: '#e11d48',
      primaryColor: '#e11d48',
      gradientFrom: 'rgba(251, 113, 133, 0.15)',
      gradientTo: 'rgba(225, 29, 72, 0.15)'
    }
  },
  {
    id: 'midnight',
    name: '午夜蓝',
    icon: '🌃',
    colors: {
      bgPrimary: '#020617',
      bgSecondary: '#0f172a',
      bgCard: 'rgba(15, 23, 42, 0.95)',
      textPrimary: '#e2e8f0',
      textSecondary: '#94a3b8',
      textMuted: '#475569',
      borderColor: 'rgba(71, 85, 105, 0.5)',
      inputBg: '#1e293b',
      inputBorder: '#334155',
      shadowColor: 'rgba(0, 0, 0, 0.5)',
      accentColor: '#6366f1',
      primaryColor: '#6366f1',
      gradientFrom: 'rgba(99, 102, 241, 0.2)',
      gradientTo: 'rgba(79, 70, 229, 0.2)'
    }
  },
  {
    id: 'forest',
    name: '森林绿',
    icon: '🌲',
    colors: {
      bgPrimary: '#052e16',
      bgSecondary: '#064e3b',
      bgCard: 'rgba(6, 78, 59, 0.95)',
      textPrimary: '#d1fae5',
      textSecondary: '#6ee7b7',
      textMuted: '#34d399',
      borderColor: 'rgba(52, 211, 153, 0.3)',
      inputBg: '#065f46',
      inputBorder: '#059669',
      shadowColor: 'rgba(0, 0, 0, 0.4)',
      accentColor: '#10b981',
      primaryColor: '#10b981',
      gradientFrom: 'rgba(16, 185, 129, 0.2)',
      gradientTo: 'rgba(5, 150, 105, 0.2)'
    }
  },
  {
    id: 'ocean',
    name: '深海蓝',
    icon: '🌊',
    colors: {
      bgPrimary: '#082f49',
      bgSecondary: '#0c4a6e',
      bgCard: 'rgba(12, 74, 110, 0.95)',
      textPrimary: '#e0f2fe',
      textSecondary: '#7dd3fc',
      textMuted: '#38bdf8',
      borderColor: 'rgba(56, 189, 248, 0.3)',
      inputBg: '#075985',
      inputBorder: '#0284c7',
      shadowColor: 'rgba(0, 0, 0, 0.4)',
      accentColor: '#0ea5e9',
      primaryColor: '#0ea5e9',
      gradientFrom: 'rgba(14, 165, 233, 0.2)',
      gradientTo: 'rgba(2, 132, 199, 0.2)'
    }
  },
  {
    id: 'cyberpunk',
    name: '赛博朋克',
    icon: '🤖',
    colors: {
      bgPrimary: '#0a0a0a',
      bgSecondary: '#171717',
      bgCard: 'rgba(23, 23, 23, 0.95)',
      textPrimary: '#fafafa',
      textSecondary: '#a3a3a3',
      textMuted: '#737373',
      borderColor: 'rgba(236, 72, 153, 0.5)',
      inputBg: '#262626',
      inputBorder: '#ec4899',
      shadowColor: 'rgba(236, 72, 153, 0.3)',
      accentColor: '#ec4899',
      primaryColor: '#ec4899',
      gradientFrom: 'rgba(236, 72, 153, 0.25)',
      gradientTo: 'rgba(168, 85, 247, 0.25)'
    }
  }
]

// 默认配置
const defaultLlmConfig = {
  provider: 'openai',
  apiKey: '',
  apiUrl: '',
  model: 'gpt-3.5-turbo',
  temperature: 0.7,
  maxTokens: 2000,
  useLocalRules: false,
  useBackendConfig: true
}

const defaultAppSettings = {
  autoSave: true,
  showEmotionBackground: true,
  particleIntensity: 'medium',
  language: 'zh-CN',
  dateFormat: 'YYYY-MM-DD',
  timeFormat: '24h',
  // 音乐播放器设置
  musicPlayer: {
    albumCoverShape: 'rounded', // 'square' | 'rounded' | 'circle'
    albumCoverRotate: true, // 是否旋转
    forceCustomPlaylist: false, // 是否强制播放自定义歌单
    customPlaylistId: '', // 自定义歌单ID
    customPlaylistName: '' // 自定义歌单名称
  }
}

export const useSettingsStore = defineStore('settings', () => {
  // 使用 VueUse 的 useStorage 替代手动 localStorage 操作
  const currentTheme = useStorage('lifeos-theme', 'green')
  const llmConfig = useStorage('lifeos-llm-config', defaultLlmConfig)
  const appSettings = useStorage('lifeos-app-settings', defaultAppSettings)
  
  // 使用 VueUse 的 useDark 处理深色模式
  const isDark = useDark()
  
  // 计算当前主题对象
  const theme = computed(() => {
    return themes.find(t => t.id === currentTheme.value) || themes[0]
  })
  
  // 应用主题
  const applyTheme = () => {
    const themeObj = theme.value
    const root = document.documentElement
    
    // 设置CSS变量
    Object.entries(themeObj.colors).forEach(([key, value]) => {
      root.style.setProperty(`--${key.replace(/([A-Z])/g, '-$1').toLowerCase()}`, value)
    })
    
    // 使用 useDark 自动处理 dark 类
    const darkThemes = ['dark', 'midnight', 'forest', 'ocean', 'cyberpunk']
    isDark.value = darkThemes.includes(currentTheme.value)
  }
  
  // 设置主题
  const setTheme = (themeId) => {
    currentTheme.value = themeId
    applyTheme()
  }
  
  // 更新LLM配置
  const updateLlmConfig = (config) => {
    llmConfig.value = { ...llmConfig.value, ...config }
  }
  
  // 更新应用设置
  const updateAppSettings = (settings) => {
    appSettings.value = { ...appSettings.value, ...settings }
  }
  
  // 重置所有设置
  const resetSettings = () => {
    currentTheme.value = 'green'
    llmConfig.value = { ...defaultLlmConfig }
    appSettings.value = { ...defaultAppSettings }
    applyTheme()
  }
  
  // 初始化
  const initSettings = () => {
    applyTheme()
  }
  
  // 监听主题变化自动应用
  watch(currentTheme, applyTheme)
  
  return {
    themes,
    currentTheme,
    theme,
    llmConfig,
    appSettings,
    isDark,
    initSettings,
    setTheme,
    updateLlmConfig,
    updateAppSettings,
    resetSettings
  }
})
