<!-- AvatarUpload.vue -->
<template>
  <view class="avatar-upload" :style="{ width: `${width}px`, height: `${width}px` }">
    <!-- 头像显示 -->
    <image
      :src="avatarUrl || defaultAvatar"
      :style="{ width: `${width}px`, height: `${width}px` }"
      class="avatar-image"
      mode="aspectFill"
    />
    
    <!-- 上传按钮 -->
    <view 
      class="upload-button"
      :style="uploadButtonStyle"
      @click="handleUpload"
    >
      <text class="upload-icon">+</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

// ===== Props =====
interface Props {
  url?: string           // 头像URL
  onChange?: () => void  // 点击回调函数
}

const props = withDefaults(defineProps<Props>(), {
  url: '',
  onChange: () => {}
})

// 固定头像大小为60px
const width = 60

// ===== 计算属性 =====
const avatarUrl = computed(() => props.url)

// 默认头像
const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iODAiIGhlaWdodD0iODAiIHZpZXdCb3g9IjAgMCA4MCA4MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPGNpcmNsZSBjeD0iNDAiIGN5PSI0MCIgcj0iNDAiIGZpbGw9IiNGNUY1RjUiLz4KPGC0Y2xlIGN4PSI0MCIgY3k9IjMwIiByPSIxMiIgZmlsbD0iI0NDQ0NDQyIvPgo8cGF0aCBkPSJNMTUgNjVDMTUgNTIuODQ5NyAyNC44NDk3IDQzIDM3IDQzSDQzQzU1LjE1MDMgNDMgNjUgNTIuODQ5NyA2NSA2NVY3MEgxNVY2NVoiIGZpbGw9IiNDQ0NDQ0MiLz4KPC9zdmc+'

// 上传按钮样式 - 固定在右下角
const uploadButtonStyle = computed(() => {
  const buttonSize = 10 // 固定按钮大小
  
  return {
    width: `${buttonSize}px`,
    height: `${buttonSize}px`,
    fontSize: `12px`,
    bottom: `${1}px`, // 一半压着头像
    right: `${1}px`   // 一半在头像外面
  }
})

// ===== 方法 =====
const handleUpload = () => {
  // 直接调用父组件传入的onChange函数
  if (props.onChange) {
    props.onChange()
  }
}
</script>

<script lang="ts">
export default {
  name: 'AvatarUpload'
}
</script>

<style lang="scss" scoped>
.avatar-upload {
  position: relative;
  display: inline-block;
  border-radius: 50%;
  overflow: visible;
}

.avatar-image {
  border-radius: 50%;
  border: 2px solid #ffffff;
  box-sizing: border-box;
  background-color: #f5f5f5;
  display: block;
}

.upload-button {
  position: absolute;
  background-color: #fb0055;
  border: 2px solid #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  
  .upload-icon {
    color: #ffffff;
	font-size: 14px;
    font-weight: bold;
    line-height: 1;
  }
  
}

// 响应式适配
@media (max-width: 750rpx) {
  .upload-button {
    &:active {
      transform: scale(0.9);
    }
  }
}
</style>

<!-- 使用示例 -->
<!--
<template>
  <view class="demo">
    <AvatarUpload 
      :url="userAvatar" 
      :onChange="handleAvatarClick" 
    />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import AvatarUpload from '@/components/AvatarUpload.vue'

const userAvatar = ref('https://example.com/avatar.jpg')

const handleAvatarClick = () => {
  // 跳转到头像编辑页面
  uni.navigateTo({
    url: '/pages/avatar-edit/avatar-edit'
  })
}
</script>
-->