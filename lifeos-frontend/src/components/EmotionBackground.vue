<template>
  <div ref="canvasContainer" class="fixed inset-0 -z-10 pointer-events-none"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as THREE from 'three'

const props = defineProps({
  emotionScore: {
    type: Number,
    default: 0
  },
  intensity: {
    type: String,
    default: 'medium' // low, medium, high
  }
})

const canvasContainer = ref(null)
let scene, camera, renderer, particleSystem
let animationId

const getParticleCount = () => {
  const counts = {
    low: 50,
    medium: 150,
    high: 300
  }
  return counts[props.intensity] || counts.medium
}

const getEmotionColor = (score) => {
  if (score > 5) return { r: 1, g: 0.4, b: 0.2 } // 热情橙红
  if (score > 0) return { r: 1, g: 0.7, b: 0.3 } // 温暖黄
  if (score === 0) return { r: 0.6, g: 0.6, b: 0.6 } // 中性灰
  if (score > -5) return { r: 0.3, g: 0.5, b: 0.8 } // 忧郁蓝
  return { r: 0.2, g: 0.3, b: 0.6 } // 深沉蓝紫
}

const getEmotionSpeed = (score) => {
  const baseSpeed = 0.001
  const intensityMultiplier = props.intensity === 'high' ? 1.5 : props.intensity === 'low' ? 0.5 : 1
  if (score > 5) return baseSpeed * 3 * intensityMultiplier
  if (score > 0) return baseSpeed * 2 * intensityMultiplier
  if (score === 0) return baseSpeed * intensityMultiplier
  if (score > -5) return baseSpeed * 0.5 * intensityMultiplier
  return baseSpeed * 0.2 * intensityMultiplier
}

const getOpacity = () => {
  const opacities = {
    low: 0.3,
    medium: 0.6,
    high: 0.9
  }
  return opacities[props.intensity] || opacities.medium
}

const initThree = () => {
  if (!canvasContainer.value) return

  scene = new THREE.Scene()
  camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000)
  camera.position.z = 50

  renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true })
  renderer.setSize(window.innerWidth, window.innerHeight)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  canvasContainer.value.appendChild(renderer.domElement)

  const particleCount = getParticleCount()
  const positions = new Float32Array(particleCount * 3)
  const colors = new Float32Array(particleCount * 3)
  const sizes = new Float32Array(particleCount)

  const color = getEmotionColor(props.emotionScore)

  for (let i = 0; i < particleCount; i++) {
    positions[i * 3] = (Math.random() - 0.5) * 100
    positions[i * 3 + 1] = (Math.random() - 0.5) * 100
    positions[i * 3 + 2] = (Math.random() - 0.5) * 50

    colors[i * 3] = Math.max(0, Math.min(1, color.r + (Math.random() - 0.5) * 0.3))
    colors[i * 3 + 1] = Math.max(0, Math.min(1, color.g + (Math.random() - 0.5) * 0.3))
    colors[i * 3 + 2] = Math.max(0, Math.min(1, color.b + (Math.random() - 0.5) * 0.3))

    sizes[i] = Math.random() * 2 + 0.5
  }

  const geometry = new THREE.BufferGeometry()
  geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))
  geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3))
  geometry.setAttribute('size', new THREE.BufferAttribute(sizes, 1))

  const material = new THREE.PointsMaterial({
    size: 0.5,
    vertexColors: true,
    transparent: true,
    opacity: getOpacity(),
    blending: THREE.AdditiveBlending
  })

  particleSystem = new THREE.Points(geometry, material)
  scene.add(particleSystem)

  animate()
}

const animate = () => {
  animationId = requestAnimationFrame(animate)

  if (particleSystem) {
    const speed = getEmotionSpeed(props.emotionScore)
    particleSystem.rotation.y += speed
    particleSystem.rotation.x += speed * 0.5

    const positions = particleSystem.geometry.attributes.position.array
    for (let i = 0; i < positions.length; i += 3) {
      positions[i + 1] += Math.sin(Date.now() * 0.001 + positions[i]) * 0.02
    }
    particleSystem.geometry.attributes.position.needsUpdate = true
  }

  renderer.render(scene, camera)
}

const updateEmotion = (newScore) => {
  if (!particleSystem) return

  const color = getEmotionColor(newScore)
  const colors = particleSystem.geometry.attributes.color.array

  for (let i = 0; i < colors.length; i += 3) {
    colors[i] = Math.max(0, Math.min(1, color.r + (Math.random() - 0.5) * 0.3))
    colors[i + 1] = Math.max(0, Math.min(1, color.g + (Math.random() - 0.5) * 0.3))
    colors[i + 2] = Math.max(0, Math.min(1, color.b + (Math.random() - 0.5) * 0.3))
  }

  particleSystem.geometry.attributes.color.needsUpdate = true
}

const updateIntensity = () => {
  if (!particleSystem) return
  
  // 更新透明度
  particleSystem.material.opacity = getOpacity()
  
  // 如果需要改变粒子数量，需要重新初始化
  // 这里简化处理，只调整透明度
}

const handleResize = () => {
  if (!camera || !renderer) return
  camera.aspect = window.innerWidth / window.innerHeight
  camera.updateProjectionMatrix()
  renderer.setSize(window.innerWidth, window.innerHeight)
}

onMounted(() => {
  initThree()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (animationId) cancelAnimationFrame(animationId)
  if (renderer) {
    renderer.dispose()
    if (canvasContainer.value && renderer.domElement) {
      canvasContainer.value.removeChild(renderer.domElement)
    }
  }
})

watch(() => props.emotionScore, updateEmotion)
watch(() => props.intensity, updateIntensity)
</script>
