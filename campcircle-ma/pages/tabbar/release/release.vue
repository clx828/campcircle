<template>
  <view class="release-container">
    <!-- 顶部栏 -->
    <view class="header-xhs" :style="headerStyle">
      <wd-navbar title="发笔记" left-text="返回" right-text="发布" left-arrow>
        <template #capsule>
          <wd-navbar-capsule @back="handleBack" @back-home="handleBackHome" />
        </template>
      </wd-navbar>
    </view>
    <!-- 内容输入区 -->
    <view class="note-area-xhs">
      <textarea v-model="noteContent" class="note-input-xhs" placeholder="分享你的生活、美好瞬间..." :focus="inputFocus"
        @focus="inputFocus = true" @blur="inputFocus = false" maxlength="1000" />
      <view class="note-tip-xhs">请勿发布违法、低俗、广告等内容，违者封号处理。</view>
    </view>
    <!-- 图片上传九宫格 -->
    <view class="image-grid">
      <view v-for="(img, idx) in images" :key="idx" class="img-item">
        <image :src="img" mode="aspectFill" class="img-preview" @click="previewImage(idx)" />
        <view class="img-delete" @click="removeImage(idx)"><text class="iconfont">&#xe601;</text></view>
      </view>
      <view v-if="images.length < 9" class="img-item add-item" @click="chooseImage">
        <text class="add-icon">+</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/userStore'
import RouterGuard from '@/utils/routerGuard'
import { postApi } from '@/api/post'

const noteContent = ref('')
const inputFocus = ref(false)
const images = ref<string[]>([])

// 胶囊按钮和状态栏信息
const capsuleInfo = ref({
  width: 87,
  height: 32,
  top: 0,
  right: 0,
  bottom: 0,
  left: 0
})
const statusBarHeight = ref(0)

// 计算 header-xhs 的样式
const headerStyle = computed(() => {
  return {
    paddingTop: `${statusBarHeight.value}px`,
    height: `${capsuleInfo.value.bottom - statusBarHeight.value + 8}px`
  }
})

// 获取系统信息
function getSystemInfo() {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0

  // #ifdef MP-WEIXIN
  const menuButtonInfo = uni.getMenuButtonBoundingClientRect()
  if (menuButtonInfo) {
    capsuleInfo.value = menuButtonInfo
  }
  // #endif
}

function onBack() {
  uni.navigateBack()
}

function onPublish() {
  if (!noteContent.value.trim() && images.value.length === 0) {
    uni.showToast({ title: '请输入内容或添加图片', icon: 'none' })
    return
  }
  // TODO: 发布逻辑
  uni.showToast({ title: '发布成功', icon: 'success' })
  noteContent.value = ''
  images.value = []
}

function chooseImage() {
  uni.chooseImage({
    count: 9 - images.value.length,
    sizeType: ['compressed'],
    success: async (res) => {
      // 上传所有新选图片
      const uploadPromises = res.tempFilePaths.map(filePath => postApi.uploadPicture(filePath))
      try {
        const results = await Promise.all(uploadPromises)
        // 使用后端返回的pictureUrl字段做回显
        const urls = results.map(r => r.data.pictureUrl)
        images.value = images.value.concat(urls).slice(0, 9)
      } catch (e) {
        uni.showToast({ title: '图片上传失败', icon: 'none' })
      }
    }
  })
}

function removeImage(idx: number) {
  images.value.splice(idx, 1)
}

function previewImage(idx: number) {
  uni.previewImage({
    urls: images.value,
    current: images.value[idx]
  })
}

//导航栏路由跳转逻辑
function handleBack() {
  uni.navigateBack({})
}

function handleBackHome() {
  uni.reLaunch({ url: '/pages/index/Index' })
}

// 页面加载时获取系统信息
onMounted(() => {
  getSystemInfo()
})

// 在 onShow 生命周期中进行路由守卫
onShow(() => {
  console.log("触发了onShow")
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const route = currentPage?.route || ''

  // 检查是否需要登录
  if (RouterGuard.needAuth(route)) {
    const userStore = useUserStore()

    // 如果用户未登录，跳转到登录页
    if (!userStore.isLoggedIn) {
      console.log('TabBar页面需要登录，跳转到登录页面:', route)

      // 使用 nextTick 延迟执行跳转，避免可能的闪烁
      uni.navigateTo({
        url: `/pages/login/login?redirect=${encodeURIComponent(route)}`
      })
    }
  }
})
</script>

<style lang="scss" scoped>
// @import url('//at.alicdn.com/t/c/font_4321234_xhsicon.css'); // 你可换成自己的iconfont

.release-container {
  min-height: 100vh;
  background: #fff;
  padding-bottom: 40rpx;
}

.header-xhs {
  align-items: center;
  justify-content: space-between;
  background: #fff;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;

  // 确保 wd-navbar 内容垂直居中对齐胶囊
  :deep(.wd-navbar) {
    height: 100%;
    display: flex;
    align-items: center;
  }
}

.note-area-xhs {
  margin: 32rpx 90rpx 0 24rpx;
  // 添加顶部间距避免被固定头部遮挡
  padding-top: 120rpx; // 根据实际头部高度调整

  .note-input-xhs {
    width: 100%;
    min-height: 180rpx;
    border: none;
    font-size: 30rpx;
    background: #fafbfc;
    border-radius: 18rpx;
    padding: 32rpx 24rpx;
    box-sizing: border-box;
    resize: none;
    margin-bottom: 12rpx;
  }

  .note-tip-xhs {
    color: #ff2948;
    font-size: 22rpx;
    margin-bottom: 0;
    margin-left: 8rpx;
  }
}

.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 18rpx;
  margin: 32rpx 24rpx 0 24rpx;

  .img-item {
    width: 32vw;
    height: 32vw;
    background: #f6f6f6;
    border-radius: 12rpx;
    position: relative;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;

    .img-preview {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 12rpx;
    }

    .img-delete {
      position: absolute;
      top: 8rpx;
      right: 8rpx;
      background: rgba(0, 0, 0, 0.5);
      border-radius: 50%;
      width: 40rpx;
      height: 40rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 2;

      .iconfont {
        color: #fff;
        font-size: 28rpx;
      }
    }
  }

  .add-item {
    background: #fff0f3;
    border: 2rpx dashed #ff2948;

    .add-icon {
      color: #ff2948;
      font-size: 60rpx;
      font-weight: bold;
    }
  }
}
</style>