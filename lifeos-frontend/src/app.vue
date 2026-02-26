<template>
  <div class="min-h-screen" :class="`theme-${settingsStore.currentTheme}`">
    <!-- Three.js 情绪背景 -->
    <EmotionBackground 
      v-if="settingsStore.appSettings.showEmotionBackground"
      :emotion-score="currentEmotionScore" 
      :intensity="settingsStore.appSettings.particleIntensity"
    />
    
    <!-- 渐变遮罩 -->
    <div class="fixed inset-0 -z-5 theme-gradient pointer-events-none"></div>
    
    <!-- 头部导航 -->
    <header class="sticky top-0 z-10 transition-colors duration-300"
            :style="{ 
              backgroundColor: settingsStore.theme.colors.bgPrimary
            }">
      <div class="max-w-6xl mx-auto px-4 py-3">
        <div class="flex items-center justify-between">
          <!-- Logo -->
          <div class="flex items-center space-x-2">
            <svg class="w-6 h-6" :style="{ color: settingsStore.theme.colors.accentColor }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
            <div>
              <h1 class="text-lg font-semibold transition-colors duration-300"
                  :style="{ color: settingsStore.theme.colors.textPrimary }">LifeOS</h1>
            </div>
            <span class="text-xs transition-colors duration-300 ml-1"
                  :style="{ color: settingsStore.theme.colors.textMuted }">AI 生活管家</span>
          </div>
          
          <!-- 导航菜单 -->
          <nav class="hidden md:flex items-center space-x-6">
            <button 
              @click="currentView = 'timeline'"
              class="flex items-center space-x-1 text-sm font-medium transition-all duration-200"
              :style="{ color: currentView === 'timeline' ? settingsStore.theme.colors.accentColor : settingsStore.theme.colors.textSecondary }"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>时间轴</span>
            </button>
            
            <button 
              @click="currentView = 'analytics'"
              class="flex items-center space-x-1 text-sm font-medium transition-all duration-200"
              :style="{ color: currentView === 'analytics' ? settingsStore.theme.colors.accentColor : settingsStore.theme.colors.textSecondary }"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
              <span>数据分析</span>
            </button>
            
            <button 
              @click="currentView = 'query'"
              class="flex items-center space-x-1 text-sm font-medium transition-all duration-200"
              :style="{ color: currentView === 'query' ? settingsStore.theme.colors.accentColor : settingsStore.theme.colors.textSecondary }"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              <span>高级查询</span>
            </button>
          </nav>
          
          <!-- 右侧操作区 -->
          <div class="flex items-center space-x-3">
            <!-- 用户账号显示 -->
            <div class="flex items-center space-x-2 mr-2">
              <div class="w-7 h-7 rounded-full flex items-center justify-center"
                   :style="{ backgroundColor: userStore.isLoggedIn ? 'rgba(34, 197, 94, 0.2)' : 'rgba(156, 163, 175, 0.2)' }">
                <svg class="w-4 h-4" :style="{ color: userStore.isLoggedIn ? '#22c55e' : '#9ca3af' }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              <span class="text-sm font-medium hidden sm:block" 
                    :style="{ color: userStore.isLoggedIn ? settingsStore.theme.colors.textPrimary : settingsStore.theme.colors.textMuted }">
                {{ userStore.displayName }}
              </span>
            </div>

            <!-- 登录/退出按钮 -->
            <button 
              @click="userStore.isLoggedIn ? userStore.logout() : userStore.openLoginModal()"
              class="flex items-center space-x-1 text-sm font-medium transition-all duration-200 px-3 py-1.5 rounded-lg"
              :style="{ 
                color: userStore.isLoggedIn ? '#ef4444' : settingsStore.theme.colors.accentColor,
                backgroundColor: userStore.isLoggedIn ? 'rgba(239, 68, 68, 0.1)' : 'transparent'
              }"
            >
              <svg v-if="!userStore.isLoggedIn" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1" />
              </svg>
              <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
              <span>{{ userStore.isLoggedIn ? '退出' : '登录' }}</span>
            </button>

            <!-- 报告按钮 -->
            <button 
              @click="showReportModal = true"
              class="flex items-center space-x-1 text-sm font-medium transition-all duration-200"
              :style="{ color: settingsStore.theme.colors.textSecondary }"
              title="生活报告"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              <span>报告</span>
            </button>
            
            <!-- BGM开关按钮 -->
            <button 
              @click="toggleBGM"
              class="flex items-center space-x-1 text-sm font-medium transition-all duration-200"
              :style="{ color: bgmEnabled ? settingsStore.theme.colors.accentColor : settingsStore.theme.colors.textSecondary }"
              :title="bgmEnabled ? '关闭背景音乐' : '开启背景音乐'"
            >
              <svg v-if="bgmEnabled" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19V6l12-3v13M9 19c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zm12-3c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zM9 10l12-3" />
              </svg>
              <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z" stroke-dasharray="2 2" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2" />
              </svg>
              <span>{{ bgmEnabled ? 'BGM开' : 'BGM关' }}</span>
            </button>

            <!-- 设置按钮 -->
            <button 
              @click="showSettings = true"
              class="flex items-center space-x-1 text-sm font-medium transition-all duration-200"
              :style="{ color: settingsStore.theme.colors.textSecondary }"
              title="设置"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              <span>设置</span>
            </button>
            
            <!-- 刷新按钮 -->
            <button 
              @click="refreshRecords" 
              :disabled="isLoading"
              class="flex items-center space-x-1 text-sm font-medium transition-all duration-200"
              :class="isLoading ? 'opacity-50 cursor-not-allowed' : ''"
              :style="{ color: settingsStore.theme.colors.textSecondary }"
              title="刷新数据"
            >
              <svg class="w-4 h-4" :class="{ 'animate-spin': isLoading }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
              <span>刷新</span>
            </button>
            
            <!-- 连接状态 -->
            <div class="hidden sm:flex items-center space-x-1.5 px-2 py-1 rounded text-xs"
                 :style="{ 
                   backgroundColor: connectionStatus === 'connected' ? 'rgba(34, 197, 94, 0.1)' : 'rgba(239, 68, 68, 0.1)',
                   color: connectionStatus === 'connected' ? '#22c55e' : '#ef4444'
                 }">
              <div :class="['w-1.5 h-1.5 rounded-full', connectionStatus === 'connected' ? 'bg-green-500' : 'bg-red-500']"></div>
              <span>{{ connectionStatus === 'connected' ? '已连接' : '未连接' }}</span>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="max-w-6xl mx-auto px-4 py-6 pb-40">
      <!-- 统计卡片 -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
        <div class="card text-center backdrop-blur-sm">
          <div class="text-2xl md:text-3xl font-bold" :style="{ color: settingsStore.theme.colors.accentColor }">{{ records.length }}</div>
          <div class="text-sm mt-1 transition-colors duration-300"
               :style="{ color: settingsStore.theme.colors.textMuted }">总记录</div>
        </div>
        <div class="card text-center backdrop-blur-sm">
          <div class="text-2xl md:text-3xl font-bold text-green-500">{{ totalExpenses.toFixed(0) }}</div>
          <div class="text-sm mt-1 transition-colors duration-300"
               :style="{ color: settingsStore.theme.colors.textMuted }">总消费(元)</div>
        </div>
        <div class="card text-center backdrop-blur-sm">
          <div class="flex items-center justify-center gap-2">
            <svg class="w-6 h-6" style="color: white; fill: white;" viewBox="0 0 24 24">
              <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
            </svg>
            <div class="text-2xl md:text-3xl font-bold" :class="averageEmotion >= 0 ? 'text-green-500' : 'text-red-500'">
              {{ averageEmotion >= 0 ? '+' : '' }}{{ averageEmotion.toFixed(1) }}
            </div>
          </div>
          <div class="text-sm mt-1 transition-colors duration-300"
               :style="{ color: settingsStore.theme.colors.textMuted }">平均情绪</div>
        </div>
        <div class="card text-center backdrop-blur-sm">
          <div class="text-2xl md:text-3xl font-bold text-purple-500">{{ uniqueTags.length }}</div>
          <div class="text-sm mt-1 transition-colors duration-300"
               :style="{ color: settingsStore.theme.colors.textMuted }">标签数</div>
        </div>
      </div>

      <!-- 时间轴视图 -->
      <div v-if="currentView === 'timeline'">
        <div v-if="records.length > 0" class="space-y-4">
          <RecordTimeline 
            v-for="record in records" 
            :key="record.id" 
            :record="record"
            @delete="handleDelete"
            @edit="openEditModal"
            @click="selectRecord(record)"
          />
        </div>
        
        <div v-else class="text-center py-20">
          <!-- 文档图标 -->
          <div class="w-16 h-16 mx-auto mb-6 rounded-2xl flex items-center justify-center border-2 border-dashed"
               :style="{ borderColor: settingsStore.theme.colors.borderColor }">
            <svg class="w-8 h-8" :style="{ color: settingsStore.theme.colors.textMuted }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          </div>
          
          <!-- 标题 -->
          <h3 class="text-lg font-medium mb-2 transition-colors duration-300"
              :style="{ color: settingsStore.theme.colors.textPrimary }">还没有记录</h3>
          
          <!-- 说明文字 -->
          <p class="text-sm mb-6 transition-colors duration-300 max-w-md mx-auto"
             :style="{ color: settingsStore.theme.colors.textSecondary }">
            开始你的生活追踪之旅吧！在下方输入框告诉 AI 你今天做了什么，它会自动帮你整理。
          </p>
          
          <!-- 示例列表 -->
          <div class="inline-block text-left">
            <p class="text-xs mb-3 transition-colors duration-300"
               :style="{ color: settingsStore.theme.colors.textMuted }">试试这样说：</p>
            <ul class="space-y-2 text-sm">
              <li class="flex items-start space-x-2 transition-colors duration-300"
                  :style="{ color: settingsStore.theme.colors.textSecondary }">
                <span :style="{ color: settingsStore.theme.colors.textMuted }">•</span>
                <span>"今天中午打车花了30元，然后去吃了一顿150元的烤肉"</span>
              </li>
              <li class="flex items-start space-x-2 transition-colors duration-300"
                  :style="{ color: settingsStore.theme.colors.textSecondary }">
                <span :style="{ color: settingsStore.theme.colors.textMuted }">•</span>
                <span>"项目顺利上线，团队庆祝了一下，心情很好"</span>
              </li>
              <li class="flex items-start space-x-2 transition-colors duration-300"
                  :style="{ color: settingsStore.theme.colors.textSecondary }">
                <span :style="{ color: settingsStore.theme.colors.textMuted }">•</span>
                <span>"晚上一个人看了场电影，有点孤独"</span>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <!-- 数据分析视图 -->
      <div v-else-if="currentView === 'analytics'" class="space-y-8">
        <!-- 页面标题 -->
        <div class="text-center">
          <h2 class="text-2xl font-semibold mb-2" :style="{ color: settingsStore.theme.colors.textPrimary }">数据分析与统计</h2>
          <p class="text-sm" :style="{ color: settingsStore.theme.colors.textSecondary }">深入了解您的生活模式和习惯趋势</p>
        </div>
        
        <!-- 时间范围选择 -->
        <div class="flex flex-wrap items-center gap-4 mb-6">
          <div class="flex items-center space-x-2">
            <button
              v-for="range in timeRanges"
              :key="range.value"
              @click="selectedTimeRange = range.value"
              class="px-4 py-1.5 rounded-full text-sm font-medium transition-all duration-200"
              :class="selectedTimeRange === range.value ? 'text-white' : ''"
              :style="{ 
                backgroundColor: selectedTimeRange === range.value 
                  ? settingsStore.theme.colors.accentColor 
                  : 'transparent',
                color: selectedTimeRange === range.value 
                  ? 'white' 
                  : settingsStore.theme.colors.textSecondary
              }"
            >
              {{ range.label }}
            </button>
          </div>
          
          <div class="flex items-center space-x-2 text-sm" :style="{ color: settingsStore.theme.colors.textSecondary }">
            <span>自定义时间范围：</span>
            <input 
              type="date" 
              v-model="customStartDate"
              class="px-3 py-1.5 rounded-lg border text-sm"
              :style="{ 
                backgroundColor: settingsStore.theme.colors.inputBg,
                borderColor: settingsStore.theme.colors.inputBorder,
                color: settingsStore.theme.colors.textPrimary
              }"
            />
            <span>至</span>
            <input 
              type="date" 
              v-model="customEndDate"
              class="px-3 py-1.5 rounded-lg border text-sm"
              :style="{ 
                backgroundColor: settingsStore.theme.colors.inputBg,
                borderColor: settingsStore.theme.colors.inputBorder,
                color: settingsStore.theme.colors.textPrimary
              }"
            />
          </div>
        </div>
        
        <!-- 统计面板 - 核心数据概览 -->
        <StatsPanel :records="filteredRecords" />
        
        <!-- ECharts 图表面板 -->
        <ChartsPanel :records="filteredRecords" />
        
        <!-- 数据导出 -->
        <div class="space-y-3">
          <h3 class="text-sm font-medium" :style="{ color: settingsStore.theme.colors.textPrimary }">数据导出</h3>
          <button
            @click="exportData"
            class="px-6 py-2 rounded-lg text-sm font-medium text-white transition-all duration-200 hover:opacity-90"
            :style="{ backgroundColor: settingsStore.theme.colors.accentColor }"
          >
            导出 JSON
          </button>
          
          <div class="space-y-2 mt-4">
            <label class="flex items-center space-x-2 text-sm cursor-pointer" :style="{ color: settingsStore.theme.colors.textSecondary }">
              <input type="checkbox" v-model="exportOptions.stats" class="rounded" />
              <span>包含统计信息</span>
            </label>
            <label class="flex items-center space-x-2 text-sm cursor-pointer" :style="{ color: settingsStore.theme.colors.textSecondary }">
              <input type="checkbox" v-model="exportOptions.charts" class="rounded" />
              <span>情绪图表</span>
            </label>
            <label class="flex items-center space-x-2 text-sm cursor-pointer" :style="{ color: settingsStore.theme.colors.textSecondary }">
              <input type="checkbox" v-model="exportOptions.tags" class="rounded" />
              <span>标签图表</span>
            </label>
            <label class="flex items-center space-x-2 text-sm cursor-pointer" :style="{ color: settingsStore.theme.colors.textSecondary }">
              <input type="checkbox" v-model="exportOptions.timeline" class="rounded" />
              <span>时间图表</span>
            </label>
          </div>
        </div>
        
        <!-- 消费分析卡片 -->
        <div class="card">
          <h3 class="text-lg font-semibold mb-4" :style="{ color: settingsStore.theme.colors.textPrimary }">📊 消费分析</h3>
          <div class="grid grid-cols-2 gap-4">
            <div class="p-4 rounded-xl" :style="{ backgroundColor: settingsStore.theme.colors.bgSecondary }">
              <div class="text-sm mb-1" :style="{ color: settingsStore.theme.colors.textMuted }">今日消费</div>
              <div class="text-2xl font-bold" :style="{ color: settingsStore.theme.colors.accentColor }">¥{{ todayExpenses.toFixed(2) }}</div>
            </div>
            <div class="p-4 rounded-xl" :style="{ backgroundColor: settingsStore.theme.colors.bgSecondary }">
              <div class="text-sm mb-1" :style="{ color: settingsStore.theme.colors.textMuted }">本周消费</div>
              <div class="text-2xl font-bold" :style="{ color: settingsStore.theme.colors.accentColor }">¥{{ weekExpenses.toFixed(2) }}</div>
            </div>
          </div>
        </div>
        
        <!-- 热门标签 -->
        <div class="card">
          <h3 class="text-lg font-semibold mb-4" :style="{ color: settingsStore.theme.colors.textPrimary }">🏷️ 热门标签</h3>
          <div class="flex flex-wrap gap-2">
            <span 
              v-for="tag in uniqueTags.slice(0, 20)" 
              :key="tag"
              class="px-3 py-1.5 rounded-full text-sm transition-colors duration-200"
              :style="{ 
                backgroundColor: settingsStore.theme.colors.bgSecondary,
                color: settingsStore.theme.colors.textSecondary
              }"
            >
              #{{ tag }}
            </span>
            <span v-if="uniqueTags.length === 0" :style="{ color: settingsStore.theme.colors.textMuted }">暂无标签</span>
          </div>
        </div>
      </div>

      <!-- 高级查询视图 -->
      <div v-else-if="currentView === 'query'" class="space-y-6">
        <div class="card">
          <h3 class="text-lg font-semibold mb-4" :style="{ color: settingsStore.theme.colors.textPrimary }">🔍 自然语言查询</h3>
          <div class="space-y-4">
            <div class="relative">
              <input
                v-model="queryText"
                type="text"
                placeholder="输入您的问题，例如：这周花了多少钱？"
                class="w-full px-4 py-3 pr-24 rounded-xl border transition-all duration-200 focus:outline-none focus:ring-2"
                :style="{ 
                  backgroundColor: settingsStore.theme.colors.inputBg,
                  borderColor: settingsStore.theme.colors.inputBorder,
                  color: settingsStore.theme.colors.textPrimary
                }"
                @keydown.enter="executeQuery"
              />
              <button
                @click="executeQuery"
                :disabled="!queryText.trim() || isQuerying"
                class="absolute right-2 top-1/2 -translate-y-1/2 px-4 py-1.5 rounded-lg text-sm font-medium text-white transition-all duration-200 disabled:opacity-50"
                :style="{ backgroundColor: settingsStore.theme.colors.accentColor }"
              >
                {{ isQuerying ? '查询中...' : '查询' }}
              </button>
            </div>
            
            <div class="flex flex-wrap gap-2">
              <button
                v-for="example in queryExamples"
                :key="example"
                @click="queryText = example"
                class="px-3 py-1.5 rounded-lg text-sm transition-all duration-200 hover:opacity-80"
                :style="{ 
                  backgroundColor: settingsStore.theme.colors.bgSecondary,
                  color: settingsStore.theme.colors.textSecondary
                }"
              >
                {{ example }}
              </button>
            </div>
            
            <!-- 查询结果 -->
            <div v-if="queryResult" class="mt-4 p-4 rounded-xl border"
                 :style="{ 
                   backgroundColor: settingsStore.theme.id === 'dark' || settingsStore.theme.id === 'midnight' || settingsStore.theme.id === 'forest' ? 'rgba(59, 130, 246, 0.1)' : 'rgba(59, 130, 246, 0.05)',
                   borderColor: settingsStore.theme.colors.borderColor
                 }">
              <div class="flex items-start justify-between">
                <div class="text-sm flex-1"
                     :style="{ color: settingsStore.theme.colors.textPrimary }">
                  <MarkdownRenderer :content="queryResult" />
                </div>
                <button 
                  @click="queryResult = ''"
                  class="ml-2 p-1 rounded transition-colors duration-200 hover:bg-black/10"
                  :style="{ color: settingsStore.theme.colors.textMuted }"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 底部输入区 -->
    <footer class="fixed bottom-0 left-0 right-0 border-t shadow-lg z-20 backdrop-blur-md transition-colors duration-300"
            :style="{ 
              backgroundColor: settingsStore.theme.colors.bgPrimary + 'f2',
              borderColor: settingsStore.theme.colors.borderColor
            }">
      <div class="max-w-6xl mx-auto px-4 py-4">
        <ChatInput @success="handleSuccess" />
      </div>
    </footer>

    <!-- 设置面板 -->
    <SettingsPanel 
      :is-open="showSettings" 
      @close="showSettings = false"
      @save="handleSettingsSave"
    />

    <!-- 报告弹窗 -->
    <div v-if="showReportModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/50" @click="showReportModal = false"></div>
      <div class="relative w-full max-w-2xl rounded-2xl p-6 shadow-2xl max-h-[80vh] overflow-hidden flex flex-col"
           :style="{ backgroundColor: settingsStore.theme.colors.bgPrimary }">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold" :style="{ color: settingsStore.theme.colors.textPrimary }">
            生活报告
          </h3>
          <button @click="showReportModal = false" class="p-1 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        
        <div class="flex space-x-2 mb-4">
          <button 
            @click="generateReport('weekly')"
            class="px-4 py-2 rounded-lg transition-colors duration-200"
            :style="{ 
              backgroundColor: settingsStore.theme.colors.bgSecondary,
              color: settingsStore.theme.colors.textPrimary
            }"
          >
            本周报告
          </button>
          <button 
            @click="generateReport('monthly')"
            class="px-4 py-2 rounded-lg transition-colors duration-200"
            :style="{ 
              backgroundColor: settingsStore.theme.colors.bgSecondary,
              color: settingsStore.theme.colors.textPrimary
            }"
          >
            本月报告
          </button>
        </div>
        
        <div v-if="isGeneratingReport" class="flex-1 flex items-center justify-center py-10">
          <div class="animate-spin rounded-full h-10 w-10 border-b-2"
               :style="{ borderColor: settingsStore.theme.colors.primaryColor }"></div>
        </div>
        
        <div v-else-if="currentReport" class="flex-1 overflow-y-auto">
          <pre class="whitespace-pre-wrap text-sm" 
               :style="{ color: settingsStore.theme.colors.textPrimary }">{{ currentReport }}</pre>
        </div>
        
        <div v-else class="flex-1 flex items-center justify-center py-10 text-center"
             :style="{ color: settingsStore.theme.colors.textMuted }">
          点击上方按钮生成本周或本月的生活报告
        </div>
      </div>
    </div>

    <!-- 编辑记录弹窗 -->
    <div v-if="showEditModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/50" @click="closeEditModal"></div>
      <div class="relative w-full max-w-lg rounded-2xl p-6 shadow-2xl"
           :style="{ backgroundColor: settingsStore.theme.colors.bgPrimary }">
        <h3 class="text-lg font-semibold mb-4" :style="{ color: settingsStore.theme.colors.textPrimary }">
          编辑记录
        </h3>
        
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium mb-1" :style="{ color: settingsStore.theme.colors.textSecondary }">
              原文内容
            </label>
            <textarea 
              v-model="editingRecord.originalText"
              class="w-full px-3 py-2 rounded-lg border transition-colors duration-200"
              :style="{ 
                backgroundColor: settingsStore.theme.colors.bgSecondary,
                borderColor: settingsStore.theme.colors.borderColor,
                color: settingsStore.theme.colors.textPrimary
              }"
              rows="3"
            ></textarea>
          </div>
          
          <div>
            <label class="block text-sm font-medium mb-1" :style="{ color: settingsStore.theme.colors.textSecondary }">
              记录类型
            </label>
            <select 
              v-model="editingRecord.recordType"
              class="w-full px-3 py-2 rounded-lg border transition-colors duration-200"
              :style="{ 
                backgroundColor: settingsStore.theme.colors.bgSecondary,
                borderColor: settingsStore.theme.colors.borderColor,
                color: settingsStore.theme.colors.textPrimary
              }"
            >
              <option value="expense">消费</option>
              <option value="diary">日记</option>
              <option value="event">事件</option>
              <option value="mood">情绪</option>
            </select>
          </div>
          
          <div>
            <label class="block text-sm font-medium mb-1" :style="{ color: settingsStore.theme.colors.textSecondary }">
              金额（消费记录）
            </label>
            <input 
              type="number"
              v-model="editingRecord.amount"
              class="w-full px-3 py-2 rounded-lg border transition-colors duration-200"
              :style="{ 
                backgroundColor: settingsStore.theme.colors.bgSecondary,
                borderColor: settingsStore.theme.colors.borderColor,
                color: settingsStore.theme.colors.textPrimary
              }"
              step="0.01"
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium mb-1" :style="{ color: settingsStore.theme.colors.textSecondary }">
              情绪分数 (-10 到 10)
            </label>
            <input 
              type="number"
              v-model="editingRecord.emotionScore"
              class="w-full px-3 py-2 rounded-lg border transition-colors duration-200"
              :style="{ 
                backgroundColor: settingsStore.theme.colors.bgSecondary,
                borderColor: settingsStore.theme.colors.borderColor,
                color: settingsStore.theme.colors.textPrimary
              }"
              min="-10"
              max="10"
            />
          </div>
        </div>
        
        <div class="flex justify-end space-x-3 mt-6">
          <button 
            @click="closeEditModal"
            class="px-4 py-2 rounded-lg transition-colors duration-200"
            :style="{ 
              backgroundColor: settingsStore.theme.colors.bgSecondary,
              color: settingsStore.theme.colors.textSecondary
            }"
          >
            取消
          </button>
          <button 
            @click="saveEdit"
            class="px-4 py-2 rounded-lg transition-colors duration-200 text-white"
            style="background-color: #3b82f6"
          >
            保存
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- 音乐播放器 -->
  <MusicPlayer ref="musicPlayer" />

  <!-- 页脚统计 -->
  <footer class="fixed bottom-0 left-0 right-0 py-2 px-4 text-center text-xs z-10"
          :style="{ backgroundColor: settingsStore.theme.colors.bgPrimary, color: settingsStore.theme.colors.textMuted }">
    <div class="flex items-center justify-center gap-4">
      <!-- 不蒜子统计 -->
      <span id="busuanzi_container_site_pv">
        访问量: <span id="busuanzi_value_site_pv">0</span>
      </span>
      <span id="busuanzi_container_site_uv">
        访客数: <span id="busuanzi_value_site_uv">0</span>
      </span>
      <span>© 2024 LifeOS</span>
    </div>
  </footer>

  <!-- 登录模态框 -->
  <LoginModal />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useTrackStore } from './store/useTrackStore.js'
import { useSettingsStore } from './store/useSettingsStore.js'
import { useUserStore } from './store/useUserStore.js'
import ChatInput from './components/ChatInput.vue'
import RecordTimeline from './components/RecordTimeline.vue'
import EmotionBackground from './components/EmotionBackground.vue'
import SettingsPanel from './components/SettingsPanel.vue'
import StatsPanel from './components/StatsPanel.vue'
import ChartsPanel from './components/ChartsPanel.vue'
import MarkdownRenderer from './components/MarkdownRenderer.vue'
import LoginModal from './components/LoginModal.vue'
import MusicPlayer from './components/MusicPlayer.vue'
import { api } from './api/http.js'

const trackStore = useTrackStore()
const settingsStore = useSettingsStore()
const userStore = useUserStore()

const currentEmotionScore = ref(0)
const musicPlayer = ref(null)
const currentView = ref('timeline')
const showSettings = ref(false)
const showEditModal = ref(false)
const editingRecord = ref(null)
const queryText = ref('')
const queryResult = ref('')
const isQuerying = ref(false)

// 时间范围选择
const selectedTimeRange = ref('30days')
const customStartDate = ref('')
const customEndDate = ref('')

const timeRanges = [
  { label: '最近7天', value: '7days' },
  { label: '最近30天', value: '30days' },
  { label: '最近90天', value: '90days' },
  { label: '最近半年', value: '6months' },
  { label: '最近一年', value: '1year' }
]

// 导出选项
const exportOptions = ref({
  stats: true,
  charts: true,
  tags: true,
  timeline: false
})

const queryExamples = [
  '这周花了多少钱？',
  '最近心情怎么样？',
  '有多少条记录？',
  '消费最高的是哪一天？',
  '生成本周报告',
  '本月总结'
]

const showReportModal = ref(false)
const currentReport = ref('')
const isGeneratingReport = ref(false)

// BGM开关状态
const bgmEnabled = ref(localStorage.getItem('bgm_enabled') !== 'false')

// 切换BGM
const toggleBGM = () => {
  bgmEnabled.value = !bgmEnabled.value
  localStorage.setItem('bgm_enabled', bgmEnabled.value.toString())
  if (musicPlayer.value) {
    musicPlayer.value.toggleBGM()
  }
}

const generateReport = async (type) => {
  isGeneratingReport.value = true
  currentReport.value = ''
  showReportModal.value = true
  
  try {
    const response = type === 'weekly' 
      ? await api.getWeeklyReport()
      : await api.getMonthlyReport()
    if (response.success) {
      currentReport.value = response.data
    } else {
      currentReport.value = '生成报告失败: ' + response.message
    }
  } catch (error) {
    currentReport.value = '生成报告失败: ' + error.message
  } finally {
    isGeneratingReport.value = false
  }
}

// 导出数据方法
const exportData = () => {
  const data = {
    records: records.value,
    stats: exportOptions.value.stats ? {
      totalRecords: records.value.length,
      totalExpenses: totalExpenses.value,
      averageEmotion: averageEmotion.value,
      uniqueTags: uniqueTags.value
    } : null,
    exportDate: new Date().toISOString()
  }
  
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `lifeos-export-${new Date().toISOString().split('T')[0]}.json`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

const records = computed(() => trackStore.sortedRecords)
const isLoading = computed(() => trackStore.isLoading)
const connectionStatus = computed(() => trackStore.connectionStatus)

// 根据时间范围过滤的记录
const filteredRecords = computed(() => {
  const now = new Date()
  let startDate = new Date()
  
  switch (selectedTimeRange.value) {
    case '7days':
      startDate = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
      break
    case '30days':
      startDate = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
      break
    case '90days':
      startDate = new Date(now.getTime() - 90 * 24 * 60 * 60 * 1000)
      break
    case '6months':
      startDate = new Date(now.getTime() - 180 * 24 * 60 * 60 * 1000)
      break
    case '1year':
      startDate = new Date(now.getTime() - 365 * 24 * 60 * 60 * 1000)
      break
    default:
      startDate = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
  }
  
  // 如果有自定义时间范围，使用自定义范围
  if (customStartDate.value && customEndDate.value) {
    startDate = new Date(customStartDate.value)
    const endDate = new Date(customEndDate.value)
    return records.value.filter(r => {
      const recordDate = new Date(r.recordTime)
      return recordDate >= startDate && recordDate <= endDate
    })
  }
  
  return records.value.filter(r => {
    const recordDate = new Date(r.recordTime)
    return recordDate >= startDate && recordDate <= now
  })
})

const totalExpenses = computed(() => {
  return records.value
    .filter(r => r.recordType === 'expense' && r.amount)
    .reduce((sum, r) => sum + parseFloat(r.amount), 0)
})

const todayExpenses = computed(() => {
  const today = new Date().toDateString()
  return records.value
    .filter(r => r.recordType === 'expense' && r.amount && new Date(r.recordTime).toDateString() === today)
    .reduce((sum, r) => sum + parseFloat(r.amount), 0)
})

const weekExpenses = computed(() => {
  const weekAgo = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000)
  return records.value
    .filter(r => r.recordType === 'expense' && r.amount && new Date(r.recordTime) >= weekAgo)
    .reduce((sum, r) => sum + parseFloat(r.amount), 0)
})

const averageEmotion = computed(() => {
  const moodRecords = records.value.filter(r => r.emotionScore !== null && r.emotionScore !== undefined)
  if (moodRecords.length === 0) return 0
  return moodRecords.reduce((sum, r) => sum + (r.emotionScore || 0), 0) / moodRecords.length
})

const uniqueTags = computed(() => {
  const tags = new Set()
  records.value.forEach(record => {
    try {
      const recordTags = typeof record.tags === 'string' ? JSON.parse(record.tags) : record.tags
      if (Array.isArray(recordTags)) {
        recordTags.forEach(tag => tags.add(tag))
      }
    } catch (e) {
      // 忽略解析错误
    }
  })
  return Array.from(tags)
})

const handleSuccess = (record) => {
  console.log('记录添加成功:', record)
  if (record && record.emotionScore !== undefined) {
    currentEmotionScore.value = record.emotionScore
    
    // 根据情绪更新音乐歌单
    if (musicPlayer.value) {
      musicPlayer.value.updatePlaylistByEmotion(record.emotionScore)
    }
    
    if (record.emotionScore < -5) {
      showCareMessage()
    }
  }
}

const showCareMessage = async () => {
  try {
    const response = await api.getCareMessage()
    if (response.success && response.data) {
      setTimeout(() => {
        alert(response.data)
      }, 1500)
    }
  } catch (error) {
    console.error('获取关怀消息失败:', error)
  }
}

const handleDelete = async (id) => {
  await trackStore.deleteRecord(id)
}

const openEditModal = (record) => {
  editingRecord.value = { ...record }
  showEditModal.value = true
}

const closeEditModal = () => {
  showEditModal.value = false
  editingRecord.value = null
}

const saveEdit = async () => {
  if (!editingRecord.value) return
  
  try {
    await api.updateRecord(editingRecord.value.id, editingRecord.value)
    await trackStore.fetchRecords()
    closeEditModal()
  } catch (error) {
    console.error('更新记录失败:', error)
    alert('更新失败: ' + error.message)
  }
}

const refreshRecords = async () => {
  await trackStore.fetchRecords()
}

const selectRecord = (record) => {
  if (record && record.emotionScore !== undefined) {
    currentEmotionScore.value = record.emotionScore
    
    // 根据情绪更新音乐歌单
    if (musicPlayer.value) {
      musicPlayer.value.updatePlaylistByEmotion(record.emotionScore)
    }
  }
}

const executeQuery = async () => {
  if (!queryText.value.trim()) return
  
  isQuerying.value = true
  queryResult.value = ''
  
  try {
    const result = await trackStore.sendQuery(queryText.value)
    if (result.success) {
      queryResult.value = result.data
    } else {
      queryResult.value = '查询失败：' + result.error
    }
  } catch (error) {
    queryResult.value = '查询出错：' + error.message
  } finally {
    isQuerying.value = false
  }
}

const handleSettingsSave = () => {
  // 设置已保存，可以显示提示
  console.log('设置已保存')
}

onMounted(async () => {
  settingsStore.initSettings()
  await trackStore.checkConnection()
  if (trackStore.connectionStatus === 'connected') {
    await trackStore.fetchRecords()
  }
})
</script>

<style scoped>
.-z-5 {
  z-index: -5;
}

/* 主题过渡动画 */
* {
  transition-property: background-color, border-color, color;
  transition-duration: 300ms;
  transition-timing-function: ease;
}
</style>
