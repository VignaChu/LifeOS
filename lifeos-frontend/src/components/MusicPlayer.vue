<template>
  <div class="music-player">
    <!-- 播放器抽屉 -->
    <div v-if="isEnabled" 
         class="player-drawer fixed bottom-6 left-0 z-50 transition-transform duration-300 ease-out"
         :class="{ '-translate-x-full': isCollapsed }">
      
      <!-- 播放器主体 -->
      <div class="rounded-r-xl shadow-2xl overflow-hidden"
           :style="{ 
             backgroundColor: settingsStore.theme.colors.bgPrimary,
             border: `1px solid ${settingsStore.theme.colors.borderColor}`,
             borderLeft: 'none',
             width: '380px'
           }">
        
        <!-- 播放器头部 -->
        <div class="player-header px-4 py-2 flex items-center justify-between"
             :style="{ backgroundColor: settingsStore.theme.colors.bgSecondary }">
          <div class="flex items-center gap-2">
            <i class="fas fa-music text-xs" :style="{ color: settingsStore.theme.colors.accentColor }"></i>
            <span class="text-xs font-medium truncate max-w-[120px]" 
                  :style="{ color: settingsStore.theme.colors.textMuted }">
              {{ currentPlaylist?.name || '音乐播放器' }}
            </span>
          </div>
          <div class="flex items-center gap-2">
            <!-- 歌单列表按钮 -->
            <button @click="toggleShowPlaylist" 
                    class="w-6 h-6 rounded-full flex items-center justify-center transition-all hover:scale-110"
                    :style="{ backgroundColor: settingsStore.theme.colors.bgPrimary }"
                    :title="showPlaylist ? '隐藏歌单' : '显示歌单'">
              <i class="fas text-xs" 
                 :class="showPlaylist ? 'fa-list-ul' : 'fa-list'"
                 :style="{ color: settingsStore.theme.colors.textSecondary }"></i>
            </button>
            <!-- 关闭按钮 -->
            <button @click="toggleBGM" 
                    class="w-6 h-6 rounded-full flex items-center justify-center transition-all hover:scale-110"
                    :style="{ backgroundColor: settingsStore.theme.colors.bgPrimary }">
              <i class="fas fa-times text-xs" :style="{ color: settingsStore.theme.colors.textSecondary }"></i>
            </button>
          </div>
        </div>

        <!-- 播放器主体内容 -->
        <div class="player-body p-4" v-if="currentPlaylist">
          <div class="flex gap-4">
            <!-- 专辑封面 -->
            <div class="album-cover relative w-16 h-16 overflow-hidden shadow-lg flex-shrink-0"
                 :class="[albumCoverShapeClass, { 'animate-spin-slow': shouldRotate }]">
              <img v-if="currentTrack?.pic || currentTrack?.cover"
                   :src="currentTrack?.pic || currentTrack?.cover"
                   class="w-full h-full object-cover"
                   alt="album cover">
              <div v-else
                   class="w-full h-full flex items-center justify-center"
                   :style="{ backgroundColor: settingsStore.theme.colors.bgSecondary }">
                <i class="fas fa-music text-2xl" :style="{ color: settingsStore.theme.colors.textMuted }"></i>
              </div>
              <!-- 播放状态指示器 -->
              <div v-if="isPlaying"
                   class="absolute inset-0 flex items-center justify-center bg-black/30">
                <div class="flex gap-0.5">
                  <div class="w-1 h-4 bg-white rounded-full animate-music-bar-1"></div>
                  <div class="w-1 h-6 bg-white rounded-full animate-music-bar-2"></div>
                  <div class="w-1 h-3 bg-white rounded-full animate-music-bar-3"></div>
                </div>
              </div>
            </div>

            <!-- 歌曲信息 -->
            <div class="flex-1 min-w-0 flex flex-col justify-center">
              <div class="text-sm font-medium truncate" 
                   :style="{ color: settingsStore.theme.colors.textPrimary }"
                   :title="currentTrack?.title || currentTrack?.name">
                {{ currentTrack?.title || currentTrack?.name || '未知歌曲' }}
              </div>
              <div class="text-xs truncate mt-1" 
                   :style="{ color: settingsStore.theme.colors.textMuted }"
                   :title="currentTrack?.author || currentTrack?.artist">
                {{ currentTrack?.author || currentTrack?.artist || '未知艺术家' }}
              </div>
              <!-- 情绪/自定义歌单标签和音量 -->
              <div class="flex items-center justify-between mt-2">
                <div class="flex items-center gap-1">
                  <i class="fas fa-heart text-[10px]" :style="{ color: settingsStore.theme.colors.accentColor }"></i>
                  <template v-if="isCustomPlaylist">
                    <span class="text-[10px]" :style="{ color: settingsStore.theme.colors.textMuted }">自定义歌单</span>
                    <span class="text-[10px] px-2 py-0.5 rounded-full"
                          :style="{
                            backgroundColor: settingsStore.theme.colors.accentColor + '20',
                            color: settingsStore.theme.colors.accentColor
                          }">
                      {{ customPlaylistDisplayName }}
                    </span>
                  </template>
                  <template v-else>
                    <span class="text-[10px]" :style="{ color: settingsStore.theme.colors.textMuted }">情绪</span>
                    <span class="text-[10px] px-2 py-0.5 rounded-full"
                          :style="{
                            backgroundColor: settingsStore.theme.colors.accentColor + '20',
                            color: settingsStore.theme.colors.accentColor
                          }">
                      {{ currentPlaylist.emotionMin }} ~ {{ currentPlaylist.emotionMax }}
                    </span>
                  </template>
                </div>

                <!-- 音量小短条和播放模式 -->
                <div class="flex items-center gap-2">
                  <!-- 播放模式 -->
                  <button @click="togglePlayMode"
                          class="w-4 h-4 flex items-center justify-center"
                          :title="playModeText">
                    <i class="fas text-[10px]"
                       :class="playMode === 'random' ? 'fa-random' : playMode === 'single' ? 'fa-redo' : 'fa-list'"
                       :style="{ color: playMode !== 'list' ? settingsStore.theme.colors.accentColor : settingsStore.theme.colors.textMuted }"></i>
                  </button>

                  <!-- 音量 -->
                  <div class="flex items-center gap-1">
                    <button @click="toggleMute"
                            class="w-3 h-4 flex items-center justify-center">
                      <i class="fas text-[10px]"
                         :class="isMuted ? 'fa-volume-mute' : volume > 0.5 ? 'fa-volume-up' : 'fa-volume-down'"
                         :style="{ color: settingsStore.theme.colors.textMuted }"></i>
                    </button>
                    <div class="w-10 h-1 rounded-full cursor-pointer"
                         :style="{ backgroundColor: settingsStore.theme.colors.bgSecondary }"
                         @click="setVolume">
                      <div class="h-full rounded-full"
                           :style="{
                             width: `${isMuted ? 0 : volume * 100}%`,
                             backgroundColor: settingsStore.theme.colors.accentColor
                           }"></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 进度条 -->
          <div class="mt-4 space-y-2">
            <div class="flex items-center gap-2">
              <span class="text-[10px] w-8 text-right" :style="{ color: settingsStore.theme.colors.textMuted }">
                {{ formatTime(currentTime) }}
              </span>
              <div class="flex-1 relative h-1 rounded-full cursor-pointer group"
                   :style="{ backgroundColor: settingsStore.theme.colors.bgSecondary }"
                   @click="seek">
                <div class="absolute h-full rounded-full transition-all"
                     :style="{ 
                       width: `${duration ? (currentTime / duration) * 100 : 0}%`,
                       backgroundColor: settingsStore.theme.colors.accentColor 
                     }"></div>
                <div class="absolute top-1/2 -translate-y-1/2 w-2 h-2 rounded-full opacity-0 group-hover:opacity-100 transition-opacity"
                     :style="{ 
                       left: `${duration ? (currentTime / duration) * 100 : 0}%`,
                       backgroundColor: settingsStore.theme.colors.accentColor 
                     }"></div>
              </div>
              <span class="text-[10px] w-8" :style="{ color: settingsStore.theme.colors.textMuted }">
                {{ formatTime(duration) }}
              </span>
            </div>
            
            <!-- 控制按钮 -->
            <div class="flex items-center justify-center gap-6">
              <!-- 上一首 -->
              <button @click="playPrev"
                      class="w-8 h-8 rounded-full flex items-center justify-center transition-all hover:scale-110"
                      :style="{ backgroundColor: settingsStore.theme.colors.bgSecondary }">
                <i class="fas fa-step-backward text-sm" :style="{ color: settingsStore.theme.colors.textSecondary }"></i>
              </button>

              <!-- 播放/暂停 -->
              <button @click="togglePlay"
                      class="w-12 h-12 rounded-full flex items-center justify-center transition-all hover:scale-105 shadow-lg"
                      :style="{ backgroundColor: settingsStore.theme.colors.accentColor }">
                <i class="fas text-lg text-white" :class="isPlaying ? 'fa-pause' : 'fa-play'"></i>
              </button>

              <!-- 下一首 -->
              <button @click="playNext"
                      class="w-8 h-8 rounded-full flex items-center justify-center transition-all hover:scale-110"
                      :style="{ backgroundColor: settingsStore.theme.colors.bgSecondary }">
                <i class="fas fa-step-forward text-sm" :style="{ color: settingsStore.theme.colors.textSecondary }"></i>
              </button>
            </div>
          </div>

          <!-- 播放列表 -->
          <div v-if="showPlaylist" 
               class="max-h-40 overflow-y-auto rounded-lg mt-3"
               :style="{ backgroundColor: settingsStore.theme.colors.bgSecondary }">
            <div v-for="(track, index) in playlistTracks" 
                 :key="index"
                 @click="playTrack(index)"
                 class="px-3 py-2 flex items-center gap-2 cursor-pointer transition-all hover:opacity-80"
                 :class="{ 'opacity-50': currentTrackIndex === index }"
                 :style="{ 
                   backgroundColor: currentTrackIndex === index ? settingsStore.theme.colors.accentColor + '20' : 'transparent'
                 }">
              <span class="text-[10px] w-4" :style="{ color: settingsStore.theme.colors.textMuted }">
                {{ index + 1 }}
              </span>
              <div class="flex-1 min-w-0">
                <div class="text-xs truncate" 
                     :style="{ color: currentTrackIndex === index ? settingsStore.theme.colors.accentColor : settingsStore.theme.colors.textPrimary }"
                     :title="track.title || track.name">
                  {{ track.title || track.name }}
                </div>
                <div class="text-[10px] truncate" :style="{ color: settingsStore.theme.colors.textMuted }">
                  {{ track.author || track.artist }}
                </div>
              </div>
              <i v-if="currentTrackIndex === index && isPlaying" 
                 class="fas fa-volume-up text-xs"
                 :style="{ color: settingsStore.theme.colors.accentColor }"></i>
            </div>
          </div>
        </div>

        <!-- 未加载歌单时的提示 -->
        <div v-else class="p-8 text-center">
          <i class="fas fa-music text-3xl mb-2" :style="{ color: settingsStore.theme.colors.textMuted }"></i>
          <p class="text-sm" :style="{ color: settingsStore.theme.colors.textMuted }">正在加载歌单...</p>
        </div>
      </div>

      <!-- 右侧纵向长条按钮 -->
      <div class="absolute top-1/2 -right-8 -translate-y-1/2">
        <button @click="toggleCollapse"
                class="w-8 h-24 rounded-r-lg shadow-lg flex items-center justify-center transition-all hover:w-10"
                :style="{ 
                  backgroundColor: settingsStore.theme.colors.accentColor,
                  color: '#fff'
                }"
                :title="isCollapsed ? '展开播放器' : '收起播放器'">
          <i class="fas" :class="isCollapsed ? 'fa-chevron-right' : 'fa-chevron-left'"></i>
        </button>
      </div>
    </div>

    <!-- 隐藏的音频播放器 -->
    <audio 
      v-if="isEnabled && currentPlaylist" 
      ref="audioRef"
      :src="currentAudioUrl"
      @ended="onEnded"
      @error="handleError"
      @canplay="onCanPlay"
      @timeupdate="onTimeUpdate"
      @loadedmetadata="onLoadedMetadata"
      style="display: none;"
    ></audio>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useSettingsStore } from '../store/useSettingsStore'
import http from '../api/http'

const settingsStore = useSettingsStore()

const currentPlaylist = ref(null)
const playlists = ref([])
const isEnabled = ref(localStorage.getItem('bgm_enabled') !== 'false')
const isCollapsed = ref(false)
const showPlaylist = ref(false)
const isPlaying = ref(false)
const currentTrackIndex = ref(0)
const audioRef = ref(null)
const playlistTracks = ref([])
const currentTime = ref(0)
const duration = ref(0)
const volume = ref(0.7)
const isMuted = ref(false)
const playMode = ref('list') // 'list', 'random', 'single'

// 网易云音乐歌单API
const NETEASE_API = 'https://api.i-meto.com/meting/api'

// 当前歌曲
const currentTrack = computed(() => {
  if (playlistTracks.value.length === 0) return null
  return playlistTracks.value[currentTrackIndex.value]
})

// 播放模式文本
const playModeText = computed(() => {
  const texts = { list: '列表循环', random: '随机播放', single: '单曲循环' }
  return texts[playMode.value]
})

// 专辑封面形状类名
const albumCoverShapeClass = computed(() => {
  const shape = settingsStore.appSettings.musicPlayer?.albumCoverShape || 'rounded'
  const shapeClasses = {
    square: 'rounded-none',
    rounded: 'rounded-lg',
    circle: 'rounded-full'
  }
  return shapeClasses[shape] || 'rounded-lg'
})

// 是否旋转专辑封面
const shouldRotate = computed(() => {
  const rotateEnabled = settingsStore.appSettings.musicPlayer?.albumCoverRotate !== false
  return isPlaying.value && rotateEnabled
})

// 是否是自定义歌单模式
const isCustomPlaylist = computed(() => {
  const musicSettings = settingsStore.appSettings.musicPlayer || {}
  return musicSettings.forceCustomPlaylist && musicSettings.customPlaylistId
})

// 自定义歌单显示名称
const customPlaylistDisplayName = computed(() => {
  const musicSettings = settingsStore.appSettings.musicPlayer || {}
  return musicSettings.customPlaylistName || '我的歌单'
})

// 获取所有歌单
const fetchPlaylists = async () => {
  console.log('Fetching playlists...')

  // 检查是否启用自定义歌单
  const musicSettings = settingsStore.appSettings.musicPlayer || {}
  if (musicSettings.forceCustomPlaylist && musicSettings.customPlaylistId) {
    console.log('Using custom playlist:', musicSettings.customPlaylistId)
    // 创建自定义歌单对象
    const customPlaylist = {
      id: 'custom',
      name: musicSettings.customPlaylistName || '自定义歌单',
      neteasePlaylistId: musicSettings.customPlaylistId,
      emotionMin: 0,
      emotionMax: 100,
      isDefault: false,
      isCustom: true
    }
    await loadPlaylist(customPlaylist)
    return
  }

  try {
    const data = await http.get('music/playlists').json()
    console.log('Playlists response:', data)
    if (data?.success) {
      playlists.value = data.data || []
      console.log('Loaded playlists:', playlists.value.length)
      if (!currentPlaylist.value && playlists.value.length > 0) {
        const defaultPlaylist = playlists.value.find(p => p.isDefault) || playlists.value[0]
        console.log('Default playlist:', defaultPlaylist.name)
        await loadPlaylist(defaultPlaylist)
      }
    } else {
      console.warn('Failed to load playlists:', data?.message)
    }
  } catch (error) {
    console.error('Failed to fetch playlists:', error)
  }
}

// 加载歌单详情
const loadPlaylist = async (playlist) => {
  if (!playlist || !isEnabled.value) return
  
  console.log('Loading playlist:', playlist.name, 'ID:', playlist.neteasePlaylistId)
  currentPlaylist.value = playlist
  currentTrackIndex.value = 0
  
  try {
    const url = `${NETEASE_API}?server=netease&type=playlist&id=${playlist.neteasePlaylistId}`
    console.log('Fetching tracks from:', url)
    const response = await fetch(url)
    const data = await response.json()
    console.log('Playlist tracks:', data)
    
    if (Array.isArray(data) && data.length > 0) {
      playlistTracks.value = data
      console.log('Loaded', data.length, 'tracks')
      isPlaying.value = false
    } else {
      console.warn('No tracks found in playlist')
      playlistTracks.value = []
    }
  } catch (error) {
    console.error('Failed to load playlist tracks:', error)
    playlistTracks.value = []
  }
}

// 获取当前音频URL
const currentAudioUrl = computed(() => {
  if (playlistTracks.value.length === 0) return ''
  const track = playlistTracks.value[currentTrackIndex.value]
  return track?.url || ''
})

// 播放指定曲目
const playTrack = (index) => {
  if (playlistTracks.value.length === 0) return
  
  currentTrackIndex.value = index % playlistTracks.value.length
  
  setTimeout(() => {
    if (audioRef.value) {
      audioRef.value.load()
      audioRef.value.volume = isMuted.value ? 0 : volume.value
      audioRef.value.play().then(() => {
        isPlaying.value = true
        console.log('Playing:', playlistTracks.value[currentTrackIndex.value]?.name)
      }).catch(err => {
        console.error('Play failed:', err)
        isPlaying.value = false
      })
    }
  }, 100)
}

// 播放下一首
const playNext = () => {
  if (playlistTracks.value.length === 0) return
  
  let nextIndex
  if (playMode.value === 'random') {
    nextIndex = Math.floor(Math.random() * playlistTracks.value.length)
  } else {
    nextIndex = (currentTrackIndex.value + 1) % playlistTracks.value.length
  }
  playTrack(nextIndex)
}

// 播放上一首
const playPrev = () => {
  if (playlistTracks.value.length === 0) return
  
  let prevIndex
  if (playMode.value === 'random') {
    prevIndex = Math.floor(Math.random() * playlistTracks.value.length)
  } else {
    prevIndex = (currentTrackIndex.value - 1 + playlistTracks.value.length) % playlistTracks.value.length
  }
  playTrack(prevIndex)
}

// 切换播放/暂停
const togglePlay = () => {
  if (!audioRef.value) return
  
  if (isPlaying.value) {
    audioRef.value.pause()
    isPlaying.value = false
  } else {
    audioRef.value.play().then(() => {
      isPlaying.value = true
    }).catch(err => {
      console.error('Play failed:', err)
    })
  }
}

// 收起/展开播放器
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

// 显示/隐藏歌单列表
const toggleShowPlaylist = () => {
  showPlaylist.value = !showPlaylist.value
}

// 歌曲结束
const onEnded = () => {
  if (playMode.value === 'single') {
    // 单曲循环，重新播放
    audioRef.value.currentTime = 0
    audioRef.value.play()
  } else {
    playNext()
  }
}

// 音频可以播放
const onCanPlay = () => {
  console.log('Audio can play')
}

// 音频错误
const handleError = (e) => {
  console.error('Audio error:', e)
  setTimeout(() => playNext(), 1000)
}

// 时间更新
const onTimeUpdate = () => {
  if (audioRef.value) {
    currentTime.value = audioRef.value.currentTime
  }
}

// 加载元数据
const onLoadedMetadata = () => {
  if (audioRef.value) {
    duration.value = audioRef.value.duration || 0
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time || isNaN(time)) return '00:00'
  const minutes = Math.floor(time / 60)
  const seconds = Math.floor(time % 60)
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
}

// 跳转
const seek = (e) => {
  if (!audioRef.value || !duration.value) return
  const rect = e.currentTarget.getBoundingClientRect()
  const percent = (e.clientX - rect.left) / rect.width
  audioRef.value.currentTime = percent * duration.value
}

// 设置音量
const setVolume = (e) => {
  const rect = e.currentTarget.getBoundingClientRect()
  const percent = (e.clientX - rect.left) / rect.width
  volume.value = Math.max(0, Math.min(1, percent))
  if (audioRef.value) {
    audioRef.value.volume = volume.value
  }
  isMuted.value = volume.value === 0
}

// 静音切换
const toggleMute = () => {
  isMuted.value = !isMuted.value
  if (audioRef.value) {
    audioRef.value.volume = isMuted.value ? 0 : volume.value
  }
}

// 切换播放模式
const togglePlayMode = () => {
  const modes = ['list', 'random', 'single']
  const currentIndex = modes.indexOf(playMode.value)
  playMode.value = modes[(currentIndex + 1) % modes.length]
}

// 根据情绪值获取歌单
const fetchPlaylistByEmotion = async (emotionScore) => {
  if (!isEnabled.value) return
  
  try {
    const data = await http.get(`music/playlist/by-emotion?emotionScore=${emotionScore}`).json()
    console.log('Playlist by emotion response:', data)
    if (data?.success) {
      const playlist = data.data
      if (playlist && (!currentPlaylist.value || currentPlaylist.value.id !== playlist.id)) {
        await loadPlaylist(playlist)
      }
    }
  } catch (error) {
    console.error('Failed to fetch playlist by emotion:', error)
  }
}

// 更新歌单（根据情绪）
const updatePlaylistByEmotion = async (emotionScore) => {
  await fetchPlaylistByEmotion(emotionScore)
}

// 切换BGM开关
const toggleBGM = () => {
  isEnabled.value = !isEnabled.value
  localStorage.setItem('bgm_enabled', isEnabled.value.toString())
  console.log('BGM toggled:', isEnabled.value)
  
  if (isEnabled.value) {
    isCollapsed.value = false
    if (!currentPlaylist.value) {
      fetchPlaylists()
    }
    if (playlistTracks.value.length > 0) {
      playTrack(currentTrackIndex.value)
    }
  } else {
    if (audioRef.value) {
      audioRef.value.pause()
    }
    isPlaying.value = false
    currentPlaylist.value = null
    isCollapsed.value = false
    showPlaylist.value = false
  }
}

// 监听音量变化
watch(volume, (newVal) => {
  if (audioRef.value && !isMuted.value) {
    audioRef.value.volume = newVal
  }
})

// 暴露方法给父组件
defineExpose({
  updatePlaylistByEmotion,
  toggleBGM,
  isEnabled
})

// 监听自定义歌单设置变化
watch(() => settingsStore.appSettings.musicPlayer, async (newSettings, oldSettings) => {
  if (!isEnabled.value) return

  const newForceCustom = newSettings?.forceCustomPlaylist
  const oldForceCustom = oldSettings?.forceCustomPlaylist
  const newPlaylistId = newSettings?.customPlaylistId
  const oldPlaylistId = oldSettings?.customPlaylistId

  // 如果强制自定义歌单开关或歌单ID发生变化，重新加载
  if (newForceCustom !== oldForceCustom || (newForceCustom && newPlaylistId !== oldPlaylistId)) {
    console.log('Custom playlist settings changed, reloading...')
    await fetchPlaylists()
  }
}, { deep: true })

onMounted(async () => {
  console.log('MusicPlayer mounted, BGM enabled:', isEnabled.value)
  if (isEnabled.value) {
    await fetchPlaylists()
  }
})

onUnmounted(() => {
  if (audioRef.value) {
    audioRef.value.pause()
  }
})
</script>

<style scoped>
.music-player {
  position: relative;
}

.player-drawer {
  filter: drop-shadow(0 10px 30px rgba(0, 0, 0, 0.2));
}

.album-cover {
  transition: transform 0.3s ease;
}

.album-cover.animate-spin-slow {
  animation: spin 8s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 音乐波形动画 */
.animate-music-bar-1 {
  animation: musicBar 0.8s ease-in-out infinite;
}

.animate-music-bar-2 {
  animation: musicBar 0.8s ease-in-out infinite 0.2s;
}

.animate-music-bar-3 {
  animation: musicBar 0.8s ease-in-out infinite 0.4s;
}

@keyframes musicBar {
  0%, 100% { transform: scaleY(0.5); }
  50% { transform: scaleY(1); }
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 4px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: rgba(128, 128, 128, 0.3);
  border-radius: 2px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(128, 128, 128, 0.5);
}
</style>
