<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4" @click.self="close">
        <!-- 半透明遮罩 - 根据主题切换颜色 -->
        <div class="absolute inset-0 backdrop-blur-md transition-colors duration-300"
             :style="{ 
               backgroundColor: isDarkTheme ? 'rgba(15, 23, 42, 0.75)' : 'rgba(255, 255, 255, 0.75)'
             }">
        </div>
        
        <!-- 设置面板 - 毛玻璃效果 -->
        <div class="relative w-full max-w-2xl max-h-[85vh] overflow-hidden rounded-2xl shadow-2xl transition-all duration-300 border"
             :style="{ 
               backgroundColor: isDarkTheme ? 'rgba(30, 41, 59, 0.85)' : 'rgba(255, 255, 255, 0.85)',
               borderColor: isDarkTheme ? 'rgba(71, 85, 105, 0.5)' : 'rgba(229, 231, 235, 0.8)',
               backdropFilter: 'blur(20px)'
             }">
          <!-- 头部 -->
          <div class="flex items-center justify-between px-6 py-4 border-b transition-colors duration-300"
               :style="{ borderColor: isDarkTheme ? 'rgba(71, 85, 105, 0.5)' : 'rgba(229, 231, 235, 0.8)' }">
            <div class="flex items-center space-x-3">
              <div class="w-10 h-10 rounded-xl flex items-center justify-center transition-colors duration-300"
                   :style="{ backgroundColor: isDarkTheme ? 'rgba(51, 65, 85, 0.5)' : 'rgba(243, 244, 246, 0.8)' }">
                <svg class="w-5 h-5 transition-colors duration-300"
                     :style="{ color: isDarkTheme ? '#94a3b8' : '#4b5563' }"
                     fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
              </div>
              <h2 class="text-xl font-bold transition-colors duration-300"
                  :style="{ color: isDarkTheme ? '#f1f5f9' : '#111827' }">设置</h2>
            </div>
            <button @click="close" 
                    class="p-2 rounded-lg transition-colors duration-200"
                    :style="{ 
                      color: isDarkTheme ? '#94a3b8' : '#6b7280',
                      '--hover-bg': isDarkTheme ? 'rgba(51, 65, 85, 0.5)' : 'rgba(243, 244, 246, 0.8)'
                    }"
                    @mouseenter="$event.target.style.backgroundColor = isDarkTheme ? 'rgba(51, 65, 85, 0.5)' : 'rgba(243, 244, 246, 0.8)'"
                    @mouseleave="$event.target.style.backgroundColor = 'transparent'">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
          
          <!-- 内容区 -->
          <div class="overflow-y-auto max-h-[calc(85vh-140px)]">
            <!-- 主题设置 -->
            <div class="p-6 border-b transition-colors duration-300"
                 :style="{ borderColor: isDarkTheme ? 'rgba(71, 85, 105, 0.5)' : 'rgba(229, 231, 235, 0.8)' }">
              <h3 class="text-lg font-semibold mb-4 transition-colors duration-300"
                  :style="{ color: isDarkTheme ? '#f1f5f9' : '#111827' }">
                🎨 主题外观
              </h3>
              <div class="grid grid-cols-4 sm:grid-cols-6 gap-3">
                <button
                  v-for="t in settingsStore.themes"
                  :key="t.id"
                  @click="settingsStore.setTheme(t.id)"
                  class="flex flex-col items-center p-3 rounded-xl border-2 transition-all duration-200"
                  :class="[
                    settingsStore.currentTheme === t.id 
                      ? 'border-primary-500' 
                      : isDarkTheme ? 'border-slate-600 hover:border-slate-500' : 'border-gray-200 hover:border-gray-300'
                  ]"
                  :style="{
                    backgroundColor: settingsStore.currentTheme === t.id 
                      ? (isDarkTheme ? 'rgba(59, 130, 246, 0.2)' : 'rgba(59, 130, 246, 0.1)')
                      : (isDarkTheme ? 'rgba(51, 65, 85, 0.3)' : 'rgba(255, 255, 255, 0.5)')
                  }"
                >
                  <span class="text-2xl mb-1">{{ t.icon }}</span>
                  <span class="text-xs transition-colors duration-300"
                        :style="{ color: isDarkTheme ? '#cbd5e1' : '#4b5563' }">{{ t.name }}</span>
                </button>
              </div>
            </div>

            <!-- 音乐播放器设置 -->
            <div class="p-6 border-b transition-colors duration-300"
                 :style="{ borderColor: isDarkTheme ? 'rgba(71, 85, 105, 0.5)' : 'rgba(229, 231, 235, 0.8)' }">
              <h3 class="text-lg font-semibold mb-4 transition-colors duration-300"
                  :style="{ color: isDarkTheme ? '#f1f5f9' : '#111827' }">
                🎵 音乐播放器设置
              </h3>

              <!-- 专辑封面形状 -->
              <div class="mb-4">
                <label class="block text-sm font-medium mb-2 transition-colors duration-300"
                       :style="{ color: isDarkTheme ? '#cbd5e1' : '#374151' }">专辑封面形状</label>
                <div class="flex gap-3">
                  <button
                    v-for="shape in ['square', 'rounded', 'circle']"
                    :key="shape"
                    @click="setAlbumCoverShape(shape)"
                    class="px-4 py-2 rounded-lg border-2 transition-all duration-200"
                    :class="(localAppSettings.musicPlayer?.albumCoverShape || 'rounded') === shape
                      ? 'border-primary-500'
                      : isDarkTheme ? 'border-slate-600 hover:border-slate-500' : 'border-gray-200 hover:border-gray-300'"
                    :style="{
                      backgroundColor: (localAppSettings.musicPlayer?.albumCoverShape || 'rounded') === shape
                        ? (isDarkTheme ? 'rgba(59, 130, 246, 0.2)' : 'rgba(59, 130, 246, 0.1)')
                        : (isDarkTheme ? 'rgba(51, 65, 85, 0.3)' : 'rgba(255, 255, 255, 0.5)'),
                      color: isDarkTheme ? '#cbd5e1' : '#4b5563'
                    }"
                  >
                    {{ shape === 'square' ? '⬜ 方形' : shape === 'rounded' ? '🔲 圆角' : '⭕ 圆形' }}
                  </button>
                </div>
              </div>

              <!-- 专辑封面旋转 -->
              <div class="mb-4 flex items-center justify-between">
                <label class="text-sm font-medium transition-colors duration-300"
                       :style="{ color: isDarkTheme ? '#cbd5e1' : '#374151' }">播放时旋转专辑封面</label>
                <button
                  @click="toggleAlbumCoverRotate"
                  class="relative inline-flex h-6 w-11 items-center rounded-full transition-colors duration-200"
                  :style="{ backgroundColor: (localAppSettings.musicPlayer?.albumCoverRotate !== false) ? settingsStore.theme.colors.accentColor : (isDarkTheme ? '#475569' : '#d1d5db') }"
                >
                  <span
                    class="inline-block h-4 w-4 transform rounded-full bg-white transition-transform duration-200"
                    :class="(localAppSettings.musicPlayer?.albumCoverRotate !== false) ? 'translate-x-6' : 'translate-x-1'"
                  ></span>
                </button>
              </div>

              <!-- 强制播放自定义歌单 -->
              <div class="mb-4 pt-4 border-t"
                   :style="{ borderColor: isDarkTheme ? 'rgba(71, 85, 105, 0.3)' : 'rgba(229, 231, 235, 0.5)' }">
                <div class="flex items-center justify-between mb-3">
                  <label class="text-sm font-medium transition-colors duration-300"
                         :style="{ color: isDarkTheme ? '#cbd5e1' : '#374151' }">强制播放自定义歌单</label>
                  <button
                    @click="toggleForceCustomPlaylist"
                    class="relative inline-flex h-6 w-11 items-center rounded-full transition-colors duration-200"
                    :style="{ backgroundColor: localAppSettings.musicPlayer?.forceCustomPlaylist ? settingsStore.theme.colors.accentColor : (isDarkTheme ? '#475569' : '#d1d5db') }"
                  >
                    <span
                      class="inline-block h-4 w-4 transform rounded-full bg-white transition-transform duration-200"
                      :class="localAppSettings.musicPlayer?.forceCustomPlaylist ? 'translate-x-6' : 'translate-x-1'"
                    ></span>
                  </button>
                </div>

                <!-- 自定义歌单ID输入 -->
                <div v-if="localAppSettings.musicPlayer?.forceCustomPlaylist" class="space-y-3">
                  <div>
                    <label class="block text-sm font-medium mb-2 transition-colors duration-300"
                           :style="{ color: isDarkTheme ? '#cbd5e1' : '#374151' }">网易云歌单ID</label>
                    <input
                      :value="localAppSettings.musicPlayer?.customPlaylistId || ''"
                      @input="setCustomPlaylistId($event.target.value)"
                      type="text"
                      placeholder="输入网易云歌单ID"
                      class="w-full px-4 py-2 rounded-lg border transition-colors duration-200"
                      :style="{
                        backgroundColor: isDarkTheme ? 'rgba(15, 23, 42, 0.6)' : 'rgba(255, 255, 255, 0.8)',
                        borderColor: isDarkTheme ? 'rgba(71, 85, 105, 0.5)' : 'rgba(209, 213, 219, 0.8)',
                        color: isDarkTheme ? '#f1f5f9' : '#111827'
                      }"
                    />
                    <p class="mt-1 text-xs transition-colors duration-300"
                       :style="{ color: isDarkTheme ? '#64748b' : '#9ca3af' }">输入有效的网易云歌单ID，播放器将始终播放此歌单</p>
                  </div>

                  <div>
                    <label class="block text-sm font-medium mb-2 transition-colors duration-300"
                           :style="{ color: isDarkTheme ? '#cbd5e1' : '#374151' }">歌单显示名称</label>
                    <input
                      :value="localAppSettings.musicPlayer?.customPlaylistName || ''"
                      @input="setCustomPlaylistName($event.target.value)"
                      type="text"
                      placeholder="输入歌单显示名称（可选）"
                      class="w-full px-4 py-2 rounded-lg border transition-colors duration-200"
                      :style="{
                        backgroundColor: isDarkTheme ? 'rgba(15, 23, 42, 0.6)' : 'rgba(255, 255, 255, 0.8)',
                        borderColor: isDarkTheme ? 'rgba(71, 85, 105, 0.5)' : 'rgba(209, 213, 219, 0.8)',
                        color: isDarkTheme ? '#f1f5f9' : '#111827'
                      }"
                    />
                  </div>
                </div>
              </div>
            </div>

            <!-- AI 配置状态提示 -->
            <div class="p-6 border-b transition-colors duration-300"
                 :style="{ borderColor: isDarkTheme ? 'rgba(71, 85, 105, 0.5)' : 'rgba(229, 231, 235, 0.8)' }">
              <div class="flex items-center justify-between mb-4">
                <h3 class="text-lg font-semibold transition-colors duration-300"
                    :style="{ color: isDarkTheme ? '#f1f5f9' : '#111827' }">
                  🤖 AI 大模型配置
                </h3>
                <!-- API KEY 状态标签 -->
                <div class="flex items-center space-x-2">
                  <span class="text-sm transition-colors duration-300"
                        :style="{ color: isDarkTheme ? '#94a3b8' : '#6b7280' }">状态:</span>
                  <span v-if="backendConfigStatus === 'loading'" 
                        class="px-2 py-1 rounded text-xs bg-gray-200 text-gray-600">检测中...</span>
                  <span v-else-if="backendConfigStatus === 'configured'" 
                        class="px-2 py-1 rounded text-xs bg-green-100 text-green-700">✓ 后端已配置</span>
                  <span v-else-if="hasApiKey" 
                        class="px-2 py-1 rounded text-xs bg-blue-100 text-blue-700">✓ 已配置</span>
                  <span v-else-if="localLlmConfig.useLocalRules" 
                        class="px-2 py-1 rounded text-xs bg-yellow-100 text-yellow-700">⚡ 本地规则</span>
                  <span v-else 
                        class="px-2 py-1 rounded text-xs bg-red-100 text-red-700">✗ 未配置</span>
                </div>
              </div>

              <!-- 未配置警告 -->
              <div v-if="!hasApiKey && !localLlmConfig.useLocalRules && backendConfigStatus !== 'configured'" 
                   class="mb-4 p-3 rounded-lg border"
                   :style="{
                     backgroundColor: isDarkTheme ? 'rgba(239, 68, 68, 0.1)' : 'rgba(239, 68, 68, 0.05)',
                     borderColor: isDarkTheme ? 'rgba(239, 68, 68, 0.3)' : 'rgba(239, 68, 68, 0.2)'
                   }">
                <div class="flex items-center space-x-2">
                  <svg class="w-5 h-5 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                  </svg>
                  <span class="text-sm font-medium text-red-500">未接入 API KEY，AI 功能不可使用</span>
                </div>
                <p class="text-xs mt-1 ml-7" :style="{ color: isDarkTheme ? '#94a3b8' : '#6b7280' }">
                  请在下方配置 API Key，或开启本地规则模式
                </p>
              </div>

              <!-- 后端已配置提示 -->
              <div v-if="backendConfigStatus === 'configured'" 
                   class="mb-4 p-3 rounded-lg border"
                   :style="{
                     backgroundColor: isDarkTheme ? 'rgba(34, 197, 94, 0.1)' : 'rgba(34, 197, 94, 0.05)',
                     borderColor: isDarkTheme ? 'rgba(34, 197, 94, 0.3)' : 'rgba(34, 197, 94, 0.2)'
                   }">
                <div class="flex items-center justify-between">
                  <div class="flex items-center space-x-2">
                    <svg class="w-5 h-5 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                    </svg>
                    <span class="text-sm font-medium text-green-600">后端已配置 API KEY</span>
                  </div>
                  <!-- 使用后台配置开关 -->
                  <div class="flex items-center space-x-2">
                    <span class="text-xs" :style="{ color: isDarkTheme ? '#94a3b8' : '#6b7280' }">
                      {{ localLlmConfig.useBackendConfig ? '使用后台配置' : '使用自定义配置' }}
                    </span>
                    <button
                      @click="toggleBackendConfig"
                      class="relative inline-flex h-5 w-9 items-center rounded-full transition-colors duration-200"
                      :style="{ backgroundColor: localLlmConfig.useBackendConfig ? settingsStore.theme.colors.accentColor : (isDarkTheme ? 'rgba(71, 85, 105, 0.5)' : 'rgba(209, 213, 219, 0.8)') }"
                    >
                      <span
                        class="inline-block h-3 w-3 transform rounded-full bg-white transition-transform duration-200"
                        :class="localLlmConfig.useBackendConfig ? 'translate-x-5' : 'translate-x-1'"
                      />
                    </button>
                  </div>
                </div>
              </div>
              
              <div class="space-y-4">
                <!-- 本地规则开关 -->
                <div class="flex items-center justify-between p-3 rounded-lg border"
                     :style="{
                       backgroundColor: isDarkTheme ? 'rgba(51, 65, 85, 0.3)' : 'rgba(255, 255, 255, 0.5)',
                       borderColor: isDarkTheme ? 'rgba(71, 85, 105, 0.5)' : 'rgba(209, 213, 219, 0.8)'
                     }">
                  <div>
                    <div class="font-medium transition-colors duration-300"
                         :style="{ color: isDarkTheme ? '#e2e8f0' : '#374151' }">使用本地规则</div>
                    <div class="text-sm transition-colors duration-300"
                         :style="{ color: isDarkTheme ? '#64748b' : '#9ca3af' }">
                      开启后将无视 API Key，直接使用本地规则解析（无需联网）
                    </div>
                  </div>
                  <button
                    @click="toggleLocalRules"
                    class="relative inline-flex h-6 w-11 items-center rounded-full transition-colors duration-200"
                    :style="{ backgroundColor: localLlmConfig.useLocalRules ? settingsStore.theme.colors.accentColor : (isDarkTheme ? 'rgba(71, 85, 105, 0.5)' : 'rgba(209, 213, 219, 0.8)') }"
                  >
                    <span
                      class="inline-block h-4 w-4 transform rounded-full bg-white transition-transform duration-200"
                      :class="localLlmConfig.useLocalRules ? 'translate-x-6' : 'translate-x-1'"
                    />
                  </button>
                </div>

                <!-- API Key 配置区域 - 只在未使用本地规则时显示 -->
                <template v-if="!localLlmConfig.useLocalRules">
                  <!-- 使用后台配置时的只读显示 -->
                  <template v-if="backendConfigStatus === 'configured' && localLlmConfig.useBackendConfig">
                    <!-- 提供商 -->
                    <div>
                      <label class="block text-sm font-medium mb-2 transition-colors duration-300"
                             :style="{ color: isDarkTheme ? '#cbd5e1' : '#374151' }">提供商</label>
                      <div class="w-full px-4 py-2 rounded-lg border transition-colors duration-200"
                           :style="{
                             backgroundColor: isDarkTheme ? 'rgba(15, 23, 42, 0.3)' : 'rgba(243, 244, 246, 0.8)',
                             borderColor: isDarkTheme ? 'rgba(71, 85, 105, 0.3)' : 'rgba(209, 213, 219, 0.5)',
                             color: isDarkTheme ? '#94a3b8' : '#6b7280'
                           }">
                        {{ localLlmConfig.provider || '未配置' }}
                      </div>
                    </div>
                    
                    <!-- API Key -->
                    <div>
                      <label class="block text-sm font-medium mb-2 transition-colors duration-300"
                             :style="{ color: isDarkTheme ? '#cbd5e1' : '#374151' }">API Key</label>
                      <div class="w-full px-4 py-2 rounded-lg border transition-colors duration-200"
                           :style="{
                             backgroundColor: isDarkTheme ? 'rgba(15, 23, 42, 0.3)' : 'rgba(243, 244, 246, 0.8)',
                             borderColor: isDarkTheme ? 'rgba(71, 85, 105, 0.3)' : 'rgba(209, 213, 219, 0.5)',
                             color: isDarkTheme ? '#94a3b8' : '#6b7280'
                           }">
                        {{ localLlmConfig.apiKey ? '••••••••' + localLlmConfig.apiKey.slice(-4) : '未配置' }}
                      </div>
                    </div>
                    
                    <!-- API URL -->
                    <div>
                      <label class="block text-sm font-medium mb-2 transition-colors duration-300"
                             :style="{ color: isDarkTheme ? '#cbd5e1' : '#374151' }">API URL</label>
                      <div class="w-full px-4 py-2 rounded-lg border transition-colors duration-200"
                           :style="{
                             backgroundColor: isDarkTheme ? 'rgba(15, 23, 42, 0.3)' : 'rgba(243, 244, 246, 0.8)',
                             borderColor: isDarkTheme ? 'rgba(71, 85, 105, 0.3)' : 'rgba(209, 213, 219, 0.5)',
                             color: isDarkTheme ? '#94a3b8' : '#6b7280'
                           }">
                        {{ localLlmConfig.apiUrl || '默认' }}
                      </div>
                    </div>
                    
                    <!-- 模型 -->
                    <div>
                      <label class="block text-sm font-medium mb-2 transition-colors duration-300"
                             :style="{ color: isDarkTheme ? '#cbd5e1' : '#374151' }">模型</label>
                      <div class="w-full px-4 py-2 rounded-lg border transition-colors duration-200"
                           :style="{
                             backgroundColor: isDarkTheme ? 'rgba(15, 23, 42, 0.3)' : 'rgba(243, 244, 246, 0.8)',
                             borderColor: isDarkTheme ? 'rgba(71, 85, 105, 0.3)' : 'rgba(209, 213, 219, 0.5)',
                             color: isDarkTheme ? '#94a3b8' : '#6b7280'
                           }">
                        {{ localLlmConfig.model || '未配置' }}
                      </div>
                    </div>
                  </template>
                  
                  <!-- 自定义配置时的编辑表单 -->
                  <template v-else>
                    <!-- 提供商输入 -->
                    <div>
                      <label class="block text-sm font-medium mb-2 transition-colors duration-300"
                             :style="{ color: isDarkTheme ? '#cbd5e1' : '#374151' }">提供商</label>
                      <input
                        v-model="localLlmConfig.provider"
                        type="text"
                        placeholder="例如：OpenAI、Azure、Claude、Gemini 等"
                        class="w-full px-4 py-2 rounded-lg border transition-colors duration-200"
                        :style="{
                          backgroundColor: isDarkTheme ? 'rgba(15, 23, 42, 0.6)' : 'rgba(255, 255, 255, 0.8)',
                          borderColor: isDarkTheme ? 'rgba(71, 85, 105, 0.5)' : 'rgba(209, 213, 219, 0.8)',
                          color: isDarkTheme ? '#f1f5f9' : '#111827'
                        }"
                      />
                    </div>
                    
                    <!-- API Key -->
                    <div>
                      <label class="block text-sm font-medium mb-2 transition-colors duration-300"
                             :style="{ color: isDarkTheme ? '#cbd5e1' : '#374151' }">API Key</label>
                      <div class="relative">
                        <input
                          v-model="localLlmConfig.apiKey"
                          :type="showApiKey ? 'text' : 'password'"
                          placeholder="输入您的 API Key"
                          class="w-full px-4 py-2 pr-10 rounded-lg border transition-colors duration-200"
                          :style="{
                            backgroundColor: isDarkTheme ? 'rgba(15, 23, 42, 0.6)' : 'rgba(255, 255, 255, 0.8)',
                            borderColor: isDarkTheme ? 'rgba(71, 85, 105, 0.5)' : 'rgba(209, 213, 219, 0.8)',
                            color: isDarkTheme ? '#f1f5f9' : '#111827'
                          }"
                        />
                        <button
                          @click="showApiKey = !showApiKey"
                          class="absolute right-3 top-1/2 -translate-y-1/2 transition-colors duration-200"
                          :style="{ color: isDarkTheme ? '#64748b' : '#9ca3af' }"
                        >
                          <svg v-if="showApiKey" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                          </svg>
                          <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                          </svg>
                        </button>
                      </div>
                      <p class="mt-1 text-xs transition-colors duration-300"
                         :style="{ color: isDarkTheme ? '#64748b' : '#9ca3af' }">您的 API Key 仅保存在本地浏览器中</p>
                    </div>
                    
                    <!-- API URL -->
                    <div>
                      <label class="block text-sm font-medium mb-2 transition-colors duration-300"
                             :style="{ color: isDarkTheme ? '#cbd5e1' : '#374151' }">API URL (可选)</label>
                      <input
                        v-model="localLlmConfig.apiUrl"
                        type="text"
                        placeholder="https://api.openai.com/v1"
                        class="w-full px-4 py-2 rounded-lg border transition-colors duration-200"
                        :style="{
                          backgroundColor: isDarkTheme ? 'rgba(15, 23, 42, 0.6)' : 'rgba(255, 255, 255, 0.8)',
                          borderColor: isDarkTheme ? 'rgba(71, 85, 105, 0.5)' : 'rgba(209, 213, 219, 0.8)',
                          color: isDarkTheme ? '#f1f5f9' : '#111827'
                        }"
                      />
                    </div>
                    
                    <!-- 模型输入 -->
                  <div>
                    <label class="block text-sm font-medium mb-2 transition-colors duration-300"
                           :style="{ color: isDarkTheme ? '#cbd5e1' : '#374151' }">模型</label>
                    <input
                      v-model="localLlmConfig.model"
                      type="text"
                      placeholder="例如：gpt-3.5-turbo、gpt-4、claude-3-opus 等"
                      class="w-full px-4 py-2 rounded-lg border transition-colors duration-200"
                      :style="{
                        backgroundColor: isDarkTheme ? 'rgba(15, 23, 42, 0.6)' : 'rgba(255, 255, 255, 0.8)',
                        borderColor: isDarkTheme ? 'rgba(71, 85, 105, 0.5)' : 'rgba(209, 213, 219, 0.8)',
                        color: isDarkTheme ? '#f1f5f9' : '#111827'
                      }"
                    />
                  </div>
                  
                  <!-- Temperature -->
                  <div>
                    <label class="block text-sm font-medium mb-2 transition-colors duration-300"
                           :style="{ color: isDarkTheme ? '#cbd5e1' : '#374151' }">
                      Temperature: {{ localLlmConfig.temperature }}
                    </label>
                    <input
                      v-model.number="localLlmConfig.temperature"
                      type="range"
                      min="0"
                      max="2"
                      step="0.1"
                      class="w-full"
                    />
                    <div class="flex justify-between text-xs mt-1 transition-colors duration-300"
                         :style="{ color: isDarkTheme ? '#64748b' : '#9ca3af' }">
                      <span>精确</span>
                      <span>平衡</span>
                      <span>创意</span>
                    </div>
                  </div>
                  </template>
                </template>
              </div>
            </div>
            
            <!-- 应用设置 -->
            <div class="p-6 border-b transition-colors duration-300"
                 :style="{ borderColor: isDarkTheme ? 'rgba(71, 85, 105, 0.5)' : 'rgba(229, 231, 235, 0.8)' }">
              <h3 class="text-lg font-semibold mb-4 transition-colors duration-300"
                  :style="{ color: isDarkTheme ? '#f1f5f9' : '#111827' }">
                ⚙️ 应用设置
              </h3>
              
              <div class="space-y-4">
                <!-- 情绪背景 -->
                <div class="flex items-center justify-between">
                  <div>
                    <div class="font-medium transition-colors duration-300"
                         :style="{ color: isDarkTheme ? '#e2e8f0' : '#374151' }">情绪粒子背景</div>
                    <div class="text-sm transition-colors duration-300"
                         :style="{ color: isDarkTheme ? '#64748b' : '#9ca3af' }">根据情绪分数显示动态背景</div>
                  </div>
                  <button
                    @click="localAppSettings.showEmotionBackground = !localAppSettings.showEmotionBackground"
                    class="relative inline-flex h-6 w-11 items-center rounded-full transition-colors duration-200"
                    :style="{ backgroundColor: localAppSettings.showEmotionBackground ? settingsStore.theme.colors.accentColor : (isDarkTheme ? 'rgba(71, 85, 105, 0.5)' : 'rgba(209, 213, 219, 0.8)') }"
                  >
                    <span
                      class="inline-block h-4 w-4 transform rounded-full bg-white transition-transform duration-200"
                      :class="localAppSettings.showEmotionBackground ? 'translate-x-6' : 'translate-x-1'"
                    />
                  </button>
                </div>
                
                <!-- 粒子强度 -->
                <div>
                  <label class="block text-sm font-medium mb-2 transition-colors duration-300"
                         :style="{ color: isDarkTheme ? '#cbd5e1' : '#374151' }">粒子效果强度</label>
                  <div class="flex space-x-2">
                    <button
                      v-for="level in ['low', 'medium', 'high']"
                      :key="level"
                      @click="localAppSettings.particleIntensity = level"
                      class="flex-1 py-2 px-3 rounded-lg text-sm font-medium border transition-all duration-200"
                      :class="[
                        localAppSettings.particleIntensity === level
                          ? 'text-white'
                          : isDarkTheme ? 'text-slate-300 border-slate-600' : 'text-gray-700 border-gray-300'
                      ]"
                      :style="{
                        backgroundColor: localAppSettings.particleIntensity === level 
                          ? settingsStore.theme.colors.accentColor 
                          : (isDarkTheme ? 'rgba(51, 65, 85, 0.3)' : 'rgba(255, 255, 255, 0.5)'),
                        borderColor: localAppSettings.particleIntensity === level 
                          ? settingsStore.theme.colors.accentColor 
                          : (isDarkTheme ? 'rgba(71, 85, 105, 0.5)' : 'rgba(209, 213, 219, 0.8)')
                      }"
                    >
                      {{ level === 'low' ? '低' : level === 'medium' ? '中' : '高' }}
                    </button>
                  </div>
                </div>
                
                <!-- 日期格式 -->
                <div>
                  <label class="block text-sm font-medium mb-2 transition-colors duration-300"
                         :style="{ color: isDarkTheme ? '#cbd5e1' : '#374151' }">日期格式</label>
                  <select v-model="localAppSettings.dateFormat"
                          class="w-full px-4 py-2 rounded-lg border transition-colors duration-200"
                          :style="{
                            backgroundColor: isDarkTheme ? 'rgba(15, 23, 42, 0.6)' : 'rgba(255, 255, 255, 0.8)',
                            borderColor: isDarkTheme ? 'rgba(71, 85, 105, 0.5)' : 'rgba(209, 213, 219, 0.8)',
                            color: isDarkTheme ? '#f1f5f9' : '#111827'
                          }">
                    <option value="YYYY-MM-DD">2024-01-15</option>
                    <option value="DD/MM/YYYY">15/01/2024</option>
                    <option value="MM/DD/YYYY">01/15/2024</option>
                    <option value="YYYY年MM月DD日">2024年01月15日</option>
                  </select>
                </div>
              </div>
            </div>
            
            <!-- 数据管理 -->
            <div class="p-6">
              <h3 class="text-lg font-semibold mb-4 transition-colors duration-300"
                  :style="{ color: isDarkTheme ? '#f1f5f9' : '#111827' }">
                💾 数据管理
              </h3>
              
              <div class="space-y-3">
                <button
                  @click="exportData"
                  class="w-full flex items-center justify-center space-x-2 py-3 px-4 rounded-lg border transition-all duration-200"
                  :style="{
                    borderColor: isDarkTheme ? 'rgba(71, 85, 105, 0.5)' : 'rgba(209, 213, 219, 0.8)',
                    color: isDarkTheme ? '#cbd5e1' : '#374151',
                    backgroundColor: isDarkTheme ? 'rgba(51, 65, 85, 0.3)' : 'rgba(255, 255, 255, 0.5)'
                  }"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                  </svg>
                  <span>导出数据 (JSON)</span>
                </button>
                
                <button
                  @click="resetAllSettings"
                  class="w-full flex items-center justify-center space-x-2 py-3 px-4 rounded-lg border transition-all duration-200"
                  :style="{
                    borderColor: isDarkTheme ? 'rgba(239, 68, 68, 0.5)' : 'rgba(239, 68, 68, 0.3)',
                    color: isDarkTheme ? '#f87171' : '#dc2626',
                    backgroundColor: isDarkTheme ? 'rgba(239, 68, 68, 0.1)' : 'rgba(239, 68, 68, 0.05)'
                  }"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                  <span>重置所有设置</span>
                </button>
              </div>
            </div>
          </div>
          
          <!-- 底部按钮 -->
          <div class="flex items-center justify-between px-6 py-4 border-t transition-colors duration-300"
               :style="{ 
                 borderColor: isDarkTheme ? 'rgba(71, 85, 105, 0.5)' : 'rgba(229, 231, 235, 0.8)',
                 backgroundColor: isDarkTheme ? 'rgba(15, 23, 42, 0.5)' : 'rgba(249, 250, 251, 0.5)'
               }">
            <!-- 保存状态消息 -->
            <div v-if="saveMessage" class="text-sm"
                 :style="{ color: saveMessage.includes('success') || saveMessage.includes('成功') ? '#22c55e' : '#ef4444' }">
              {{ saveMessage }}
            </div>
            <div v-else></div>
            
            <div class="flex items-center space-x-3">
              <button
                @click="close"
                class="px-4 py-2 rounded-lg text-sm font-medium transition-colors duration-200"
                :style="{ color: isDarkTheme ? '#cbd5e1' : '#4b5563' }"
              >
                取消
              </button>
              <button
                @click="saveSettings"
                :disabled="isSaving"
                class="px-6 py-2 rounded-lg text-sm font-medium text-white transition-colors duration-200 disabled:opacity-50 flex items-center space-x-2"
                :style="{ backgroundColor: settingsStore.theme.colors.accentColor }"
              >
                <svg v-if="isSaving" class="w-4 h-4 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                </svg>
                <span>{{ isSaving ? '保存中...' : '保存设置' }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, reactive, watch, computed, onMounted } from 'vue'
import { useSettingsStore } from '../store/useSettingsStore.js'
import { api } from '../api/http.js'

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'save'])

const settingsStore = useSettingsStore()
const showApiKey = ref(false)
const isSaving = ref(false)
const saveMessage = ref('')
const backendConfigStatus = ref('loading') // 'loading', 'configured', 'not_configured'

// 计算是否是深色主题
const isDarkTheme = computed(() => {
  return ['dark', 'midnight', 'forest'].includes(settingsStore.currentTheme)
})

// 计算是否有 API Key
const hasApiKey = computed(() => {
  return localLlmConfig.apiKey && localLlmConfig.apiKey.trim().length > 0 && !localLlmConfig.apiKey.includes('dummy')
})

// 本地副本用于编辑
const localLlmConfig = reactive({ 
  ...settingsStore.llmConfig,
  useLocalRules: settingsStore.llmConfig.useLocalRules || false,
  useBackendConfig: settingsStore.llmConfig.useBackendConfig !== undefined ? settingsStore.llmConfig.useBackendConfig : true
})
const localAppSettings = reactive({ ...settingsStore.appSettings })

// 同步外部变化
watch(() => settingsStore.llmConfig, (newVal) => {
  Object.assign(localLlmConfig, newVal)
}, { deep: true })

watch(() => settingsStore.appSettings, (newVal) => {
  Object.assign(localAppSettings, newVal)
}, { deep: true })

const close = () => {
  emit('close')
}

// 初始化 musicPlayer 设置
const initMusicPlayerSettings = () => {
  if (!localAppSettings.musicPlayer) {
    localAppSettings.musicPlayer = {
      albumCoverShape: 'rounded',
      albumCoverRotate: true,
      forceCustomPlaylist: false,
      customPlaylistId: '',
      customPlaylistName: ''
    }
  }
}

// 设置专辑封面形状
const setAlbumCoverShape = (shape) => {
  initMusicPlayerSettings()
  localAppSettings.musicPlayer.albumCoverShape = shape
}

// 切换专辑封面旋转
const toggleAlbumCoverRotate = () => {
  initMusicPlayerSettings()
  localAppSettings.musicPlayer.albumCoverRotate = !localAppSettings.musicPlayer.albumCoverRotate
}

// 切换强制自定义歌单
const toggleForceCustomPlaylist = () => {
  initMusicPlayerSettings()
  localAppSettings.musicPlayer.forceCustomPlaylist = !localAppSettings.musicPlayer.forceCustomPlaylist
}

// 设置自定义歌单ID
const setCustomPlaylistId = (value) => {
  initMusicPlayerSettings()
  localAppSettings.musicPlayer.customPlaylistId = value
}

// 设置自定义歌单名称
const setCustomPlaylistName = (value) => {
  initMusicPlayerSettings()
  localAppSettings.musicPlayer.customPlaylistName = value
}

// 切换本地规则
const toggleLocalRules = () => {
  localLlmConfig.useLocalRules = !localLlmConfig.useLocalRules
}

// 切换使用后台配置
const toggleBackendConfig = () => {
  localLlmConfig.useBackendConfig = !localLlmConfig.useBackendConfig
}

const saveSettings = async () => {
  isSaving.value = true
  saveMessage.value = ''
  
  try {
    // Save to local store
    settingsStore.updateLlmConfig(localLlmConfig)
    settingsStore.updateAppSettings(localAppSettings)
    
    // Save LLM config to backend (only if not using local rules and backend not configured)
    if (!localLlmConfig.useLocalRules && backendConfigStatus.value !== 'configured') {
      const configToSave = {
        provider: localLlmConfig.provider,
        apiKey: localLlmConfig.apiKey,
        apiUrl: localLlmConfig.apiUrl,
        model: localLlmConfig.model,
        temperature: localLlmConfig.temperature,
        useLocalRules: localLlmConfig.useLocalRules
      }
      
      const response = await api.saveLlmConfig(configToSave)
      
      if (response.success) {
        saveMessage.value = 'Settings saved successfully!'
        emit('save')
        setTimeout(() => {
          close()
        }, 1000)
      } else {
        saveMessage.value = 'Failed to save: ' + response.message
      }
    } else {
      saveMessage.value = 'Settings saved successfully!'
      emit('save')
      setTimeout(() => {
        close()
      }, 1000)
    }
  } catch (error) {
    console.error('Error saving settings:', error)
    saveMessage.value = 'Error saving settings: ' + error.message
    // Still save to local store even if backend fails
    settingsStore.updateLlmConfig(localLlmConfig)
    settingsStore.updateAppSettings(localAppSettings)
    emit('save')
    close()
  } finally {
    isSaving.value = false
  }
}

// 检查后端配置状态并加载配置
const checkAndLoadBackendConfig = async () => {
  try {
    const response = await api.getLlmConfig()
    console.log('Backend config response:', response)
    
    // 如果 success 为 true 且 data 不为 null，说明后端有有效配置
    if (response.success && response.data != null) {
      console.log('Backend has valid config:', response.data)
      backendConfigStatus.value = 'configured'
      
      // 加载后端配置到表单
      const backendConfig = response.data
      
      // 逐个字段更新，确保响应式生效
      if (backendConfig.provider) localLlmConfig.provider = backendConfig.provider
      if (backendConfig.apiKey) localLlmConfig.apiKey = backendConfig.apiKey
      if (backendConfig.apiUrl) localLlmConfig.apiUrl = backendConfig.apiUrl
      if (backendConfig.model) localLlmConfig.model = backendConfig.model
      if (backendConfig.temperature !== undefined) localLlmConfig.temperature = backendConfig.temperature
      if (backendConfig.useLocalRules !== undefined) localLlmConfig.useLocalRules = backendConfig.useLocalRules
      
      console.log('Updated localLlmConfig:', localLlmConfig)
      
      // Update store as well
      settingsStore.updateLlmConfig({ ...localLlmConfig })
    } else {
      console.log('No valid backend config, response:', response)
      backendConfigStatus.value = 'not_configured'
    }
  } catch (error) {
    console.log('Error checking backend config:', error.message)
    backendConfigStatus.value = 'not_configured'
  }
}

// Watch for panel open to load config
watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    backendConfigStatus.value = 'loading'
    checkAndLoadBackendConfig()
  }
})

// 组件挂载时也检查后端配置
onMounted(() => {
  backendConfigStatus.value = 'loading'
  checkAndLoadBackendConfig()
})

const exportData = () => {
  const data = {
    settings: {
      theme: settingsStore.currentTheme,
      llm: settingsStore.llmConfig,
      app: settingsStore.appSettings
    },
    exportTime: new Date().toISOString()
  }
  
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `lifeos-settings-${new Date().toISOString().split('T')[0]}.json`
  a.click()
  URL.revokeObjectURL(url)
}

const resetAllSettings = () => {
  if (confirm('确定要重置所有设置吗？这将清除您的主题选择、API Key等所有配置。')) {
    settingsStore.resetSettings()
    Object.assign(localLlmConfig, settingsStore.llmConfig)
    Object.assign(localAppSettings, settingsStore.appSettings)
  }
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 滑块样式 */
input[type="range"] {
  -webkit-appearance: none;
  appearance: none;
  height: 6px;
  border-radius: 3px;
  background: rgba(156, 163, 175, 0.3);
  outline: none;
}

input[type="range"]::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--accent-color, #3b82f6);
  cursor: pointer;
  transition: transform 0.2s ease;
}

input[type="range"]::-webkit-slider-thumb:hover {
  transform: scale(1.1);
}

input[type="range"]::-moz-range-thumb {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--accent-color, #3b82f6);
  cursor: pointer;
  border: none;
}
</style>
