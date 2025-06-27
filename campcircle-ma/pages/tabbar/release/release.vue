<template>
  <view class="release-container">
    <!-- 顶部栏 -->
    <view class="header-xhs" :style="headerStyle">
      <wd-navbar title="发笔记" left-text="返回" right-text="发布" @click-right="onPublish" left-arrow>
        <template #capsule>
          <wd-navbar-capsule @back="handleBack" @back-home="handleBackHome" />
        </template>
      </wd-navbar>
    </view>
    <!-- 内容输入区 -->
    <view class="note-area-xhs">
      <textarea v-model="state.noteContent" class="note-input-xhs" placeholder="分享你的生活、美好瞬间..."
        :focus="state.inputFocus" @focus="state.inputFocus = true" @blur="state.inputFocus = false" maxlength="1000" />
      <view class="note-tip-xhs">请勿发布违法、低俗、广告等内容，违者封号处理。</view>
    </view>
    <!-- 图片上传九宫格 -->
    <view class="image-grid">
      <view v-for="(img, idx) in state.pictures" :key="img.pictureId || idx" class="img-item">
        <image :src="img.url" mode="aspectFill" class="img-preview" @click="previewImage(idx)" />
        <view class="img-delete" @click="removeImage(idx)">
          <wd-icon name="close" color="#ff2948" size="16px"></wd-icon>
        </view>
      </view>
      <view v-if="state.pictures.length < 9" class="img-item add-item" @click="chooseImage">
        <text class="add-icon">+</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { onShow, onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/userStore'
import RouterGuard from '@/utils/routerGuard'
import { postApi } from '@/api/post'
interface PostParams {
  pictureList: string[],
  tags: string[],
  content: string
}
interface PictureParam {
  url: string
  pictureId: string
}

const state = reactive({
  noteContent: '',
  inputFocus: false,
  pictures: [] as PictureParam[]
})
const addPostParams = reactive<PostParams>({
  pictureList: [],
  tags: [],
  content: ''
})

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

const onPublish = async () => {
  uni.vibrateShort()
  if (!state.noteContent.trim() && state.pictures.length === 0) {
    uni.showToast({ title: '请输入内容或添加图片', icon: 'none' })
    return
  }
  uni.showLoading({ title: '发布中...', mask: true })
  addPostParams.pictureList = state.pictures.map(p => p.pictureId)
  addPostParams.content = state.noteContent
  try {
    const res = await postApi.addPost(addPostParams)
    if (res.code === 0 && res.data) {
      // 发布成功，重置数据
      state.noteContent = ''
      state.pictures = []
      addPostParams.pictureList = []
      addPostParams.content = ''
      uni.showToast({ title: '发布成功', icon: 'success' })
    } else {
      uni.showToast({ title: res.msg || '发布失败', icon: 'error' })
    }
  } catch (e) {
    uni.showToast({ title: '发布失败', icon: 'error' })
  } finally {
    uni.hideLoading()
  }
}

const chooseImage = async () => {
  const remainingSlots = 9 - state.pictures.length
  if (remainingSlots <= 0) {
    uni.showToast({ title: '最多只能上传9张图片', icon: 'none' })
    return
  }

  uni.chooseImage({
    count: remainingSlots,
    sizeType: ['compressed'],
    success: async (res) => {
      uni.showLoading({ title: '上传中...', mask: true })
      const uploadPromises = res.tempFilePaths.map(filePath => postApi.uploadPicture(filePath))
      try {
        const results = await Promise.all(uploadPromises)
        const newPics: PictureParam[] = results.map(r => ({
          url: r.data.pictureUrl,
          pictureId: r.data.id
        }))
        // 确保不超过9张图片
        const totalPics = [...state.pictures, ...newPics].slice(0, 9)
        state.pictures = totalPics
      } catch (e) {
        console.error('图片上传失败:', e)
        uni.showToast({ title: '图片上传失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },
    fail: (err) => {
      console.error('选择图片失败:', err)
    }
  })
}

function removeImage(idx: number) {
  if (idx >= 0 && idx < state.pictures.length) {
    state.pictures.splice(idx, 1)
  }
}

function previewImage(idx: number) {
  if (state.pictures.length === 0 || idx < 0 || idx >= state.pictures.length) {
    return
  }

  uni.previewImage({
    urls: state.pictures.map(item => item.url),
    current: state.pictures[idx].url
  })
}

// 导航栏路由跳转逻辑
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

// 配置小程序分享功能
onShareAppMessage(() => {
  console.log('发布页面分享给朋友事件触发了')
  return {
    title: '来CampCircle分享你的校园生活吧！',
    path: '/pages/tabbar/release/release',
    imageUrl: 'https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-06-13_12f2e457-9cae-4ffa-a149-1f480ddc221d.png'
  }
})

onShareTimeline(() => {
  console.log('发布页面分享到朋友圈事件触发了')
  return {
    title: '记录校园美好时光 - CampCircle',
    query: 'from=timeline',
    imageUrl: 'https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-06-13_12f2e457-9cae-4ffa-a149-1f480ddc221d.png'
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
    width: calc((100% - 36rpx) / 3);
    aspect-ratio: 1 / 1;
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
      background: rgba(255, 41, 72, 0.3);
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