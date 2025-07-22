<template>
  <view class="qr-poster-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @click="goBack">
          <view class="back-button">
            <wd-icon name="arrow-left" size="20px" color="#333"></wd-icon>
          </view>
        </view>
        <view class="nav-title">二维码海报</view>
        <view class="nav-right"></view>
      </view>
    </view>

    <!-- 页面内容 -->
    <view class="page-content" :style="{ paddingTop: navbarHeight + 'px' }">
      <!-- 海报预览区域 -->
      <view class="poster-container">
        <snapshot id="view" class="intro" @snapshotready="onSnapshotReady" @snapshoterror="onSnapshotError">
          <view class="poster-content">
            <!-- 背景装饰 -->
            <view class="poster-bg">
              <view class="bg-circle bg-circle-1"></view>
              <view class="bg-circle bg-circle-2"></view>
              <view class="bg-gradient"></view>
            </view>

            <!-- 主要内容 -->
            <view class="poster-main">
              <!-- 头部信息 -->
              <view class="poster-header">
                <view class="app-info">
                  <image src="/static/img/logo.png" class="app-logo" mode="aspectFit"></image>
                  <view class="app-text">
                    <text class="app-name">CampCircle</text>
                    <text class="app-desc">校园精彩生活</text>
                  </view>
                </view>
              </view>

              <!-- 用户信息 -->
              <view class="user-section" v-if="userInfo">
                <view class="user-avatar-container">
                  <image :src="userInfo.userAvatar || defaultAvatar" class="user-avatar" mode="aspectFill"></image>
                </view>
                <view class="user-info">
                  <text class="user-name">{{ userInfo.userName || '校园用户' }}</text>
                  <text class="user-desc">邀请你加入 CampCircle</text>
                </view>
              </view>

              <!-- 二维码区域 -->
              <view class="qr-section">
                <view class="qr-container">
                  <image v-if="qrCodeUrl" :src="qrCodeUrl" class="qr-code" mode="aspectFit"></image>
                  <view v-else class="qr-loading">
                    <text>生成中...</text>
                  </view>
                </view>
                <text class="qr-tip">长按识别二维码，查看我的主页</text>
              </view>

              <!-- 底部装饰 -->
              <view class="poster-footer">
                <text class="footer-text">发现更多校园精彩</text>
              </view>
            </view>
          </view>
        </snapshot>
      </view>

      <!-- 操作按钮 -->
      <view class="action-buttons">
        <button class="action-btn save-btn" @click="savePoster" :disabled="!posterReady">
          <image src="/static/button/save.png" class="btn-icon" mode="aspectFit"></image>
          <text class="btn-text">保存海报</text>
        </button>
        <button class="action-btn share-btn" @click="sharePoster" :disabled="!posterReady">
          <image src="/static/button/zhuanfa.png" class="btn-icon" mode="aspectFit"></image>
          <text class="btn-text">分享海报</text>
        </button>
      </view>

      <!-- 加载提示 -->
      <view v-if="loading" class="loading-overlay">
        <view class="loading-content">
          <text class="loading-text">{{ loadingText }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { qrApi, type GenerateQrCodeParams } from '@/api/qr'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/userStore'

// 系统信息
const statusBarHeight = ref(0)
const navbarHeight = ref(0)

// 用户信息
const userStore = useUserStore()
const userInfo = ref<any>(null)
const defaultAvatar = 'https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-06-13_12f2e457-9cae-4ffa-a149-1f480ddc221d.png'

// 二维码相关
const qrCodeUrl = ref('')
const loading = ref(false)
const loadingText = ref('生成二维码中...')

// 海报相关
const posterReady = ref(false)
const posterImageUrl = ref('')

// 获取系统信息
const getSystemInfo = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
  navbarHeight.value = statusBarHeight.value + 44
}

// 返回上一页
const goBack = () => {
  uni.vibrateShort()
  uni.navigateBack()
}

// 获取用户信息
const getUserInfo = async () => {
  try {
    const response = await userApi.getLoginUser()
    if (response.code === 0) {
      userInfo.value = response.data
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 生成二维码
const generateQrCode = async () => {
  try {
    loading.value = true
    loadingText.value = '生成二维码中...'

    const params: GenerateQrCodeParams = {
      scene: `userId=${userInfo.value?.id || ''}`,
      page: 'pages/userProfile/userProfile',
      width: 280,
      autoColor: false,
      checkPath: false
    }

    const response = await qrApi.generateQrCode(params)
    if (response.code === 0 && response.data.qrCodeBase64) {
      qrCodeUrl.value = `data:image/png;base64,${response.data.qrCodeBase64}`
    } else {
      throw new Error(response.message || '生成二维码失败')
    }
  } catch (error) {
    console.error('生成二维码失败:', error)
    uni.showToast({
      title: '生成二维码失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// snapshot 准备就绪
const onSnapshotReady = () => {
  console.log('海报快照准备就绪')
  posterReady.value = true
}

// snapshot 错误
const onSnapshotError = (error: any) => {
  console.error('海报快照生成失败:', error)
  uni.showToast({
    title: '海报生成失败',
    icon: 'none'
  })
}

// 保存海报
const savePoster = () => {
  if (!posterReady.value) {
    uni.showToast({
      title: '海报还未准备好',
      icon: 'none'
    })
    return
  }

  uni.vibrateShort()
  loading.value = true
  loadingText.value = '保存海报中...'

  // 使用 snapshot 组件的截图功能
  uni.createSelectorQuery()
    .select('.intro')
    .node()
    .exec((res) => {
      if (res[0] && res[0].node) {
        const snapshotComponent = res[0].node
        snapshotComponent.takeSnapshot({
          success: (result: any) => {
            posterImageUrl.value = result.tempImagePath

            // 保存到相册
            uni.saveImageToPhotosAlbum({
              filePath: result.tempImagePath,
              success: () => {
                uni.showToast({
                  title: '保存成功',
                  icon: 'success'
                })
              },
              fail: () => {
                uni.showToast({
                  title: '保存失败',
                  icon: 'none'
                })
              },
              complete: () => {
                loading.value = false
              }
            })
          },
          fail: (error: any) => {
            console.error('截图失败:', error)
            uni.showToast({
              title: '截图失败',
              icon: 'none'
            })
            loading.value = false
          }
        })
      }
    })
}

// 分享海报
const sharePoster = () => {
  if (!posterReady.value) {
    uni.showToast({
      title: '海报还未准备好',
      icon: 'none'
    })
    return
  }

  uni.vibrateShort()

  // 如果已经有海报图片，直接分享
  if (posterImageUrl.value) {
    shareImage()
    return
  }

  // 否则先生成海报图片
  loading.value = true
  loadingText.value = '生成海报中...'

  uni.createSelectorQuery()
    .select('.intro')
    .node()
    .exec((res) => {
      if (res[0] && res[0].node) {
        const snapshotComponent = res[0].node
        snapshotComponent.takeSnapshot({
          success: (result: any) => {
            posterImageUrl.value = result.tempImagePath
            shareImage()
          },
          fail: (error: any) => {
            console.error('截图失败:', error)
            uni.showToast({
              title: '生成失败',
              icon: 'none'
            })
            loading.value = false
          }
        })
      }
    })
}

// 分享图片
const shareImage = () => {
  // #ifdef MP-WEIXIN
  uni.shareAppMessage({
    title: `${userInfo.value?.userName || '我'}的 CampCircle 主页`,
    path: `/pages/userProfile/userProfile?id=${userInfo.value?.id || ''}`,
    imageUrl: posterImageUrl.value
  })
  // #endif

  loading.value = false
}

// 页面初始化
onMounted(async () => {
  getSystemInfo()
  await getUserInfo()
  await generateQrCode()
})
</script>

<style lang="scss" scoped>
.qr-poster-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

// 自定义导航栏
.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);

  .navbar-content {
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20rpx;

    .nav-left {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;

      .back-icon {
        width: 40rpx;
        height: 40rpx;
      }
    }

    .nav-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }

    .nav-right {
      width: 60rpx;
    }
  }
}

// 页面内容
.page-content {
  min-height: 100vh;
  padding: 40rpx 20rpx 120rpx;
}

// 海报容器
.poster-container {
  margin-bottom: 60rpx;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.15);
}

// 海报内容
.poster-content {
  width: 100%;
  height: 1000rpx;
  position: relative;
  background: #fff;
  overflow: hidden;
}

// 背景装饰
.poster-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;

  .bg-circle {
    position: absolute;
    border-radius: 50%;
    opacity: 0.1;

    &.bg-circle-1 {
      width: 300rpx;
      height: 300rpx;
      background: linear-gradient(45deg, #667eea, #764ba2);
      top: -100rpx;
      right: -100rpx;
    }

    &.bg-circle-2 {
      width: 200rpx;
      height: 200rpx;
      background: linear-gradient(45deg, #f093fb, #f5576c);
      bottom: -50rpx;
      left: -50rpx;
    }
  }

  .bg-gradient {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 200rpx;
    background: linear-gradient(180deg, rgba(102, 126, 234, 0.05) 0%, transparent 100%);
  }
}

// 主要内容
.poster-main {
  position: relative;
  z-index: 1;
  padding: 60rpx 40rpx;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

// 头部信息
.poster-header {
  .app-info {
    display: flex;
    align-items: center;
    margin-bottom: 60rpx;

    .app-logo {
      width: 80rpx;
      height: 80rpx;
      border-radius: 16rpx;
      margin-right: 24rpx;
    }

    .app-text {
      .app-name {
        display: block;
        font-size: 36rpx;
        font-weight: 700;
        color: #333;
        margin-bottom: 8rpx;
      }

      .app-desc {
        font-size: 24rpx;
        color: #666;
      }
    }
  }
}

// 用户信息
.user-section {
  text-align: center;
  margin-bottom: 80rpx;

  .user-avatar-container {
    margin-bottom: 30rpx;

    .user-avatar {
      width: 120rpx;
      height: 120rpx;
      border-radius: 50%;
      border: 6rpx solid #fff;
      box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
    }
  }

  .user-info {
    .user-name {
      display: block;
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 12rpx;
    }

    .user-desc {
      font-size: 26rpx;
      color: #666;
    }
  }
}

// 二维码区域
.qr-section {
  text-align: center;
  margin-bottom: 60rpx;

  .qr-container {
    width: 280rpx;
    height: 280rpx;
    margin: 0 auto 30rpx;
    background: #fff;
    border-radius: 16rpx;
    padding: 20rpx;
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);

    .qr-code {
      width: 100%;
      height: 100%;
    }

    .qr-loading {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #999;
      font-size: 24rpx;
    }
  }

  .qr-tip {
    font-size: 24rpx;
    color: #666;
  }
}

// 底部装饰
.poster-footer {
  text-align: center;

  .footer-text {
    font-size: 24rpx;
    color: #999;
  }
}

// 操作按钮
.action-buttons {
  display: flex;
  gap: 30rpx;
  padding: 0 20rpx;

  .action-btn {
    flex: 1;
    height: 88rpx;
    border-radius: 44rpx;
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16rpx;
    font-size: 28rpx;
    font-weight: 600;
    transition: all 0.3s ease;

    &:active {
      transform: scale(0.98);
    }

    &[disabled] {
      opacity: 0.5;
    }

    .btn-icon {
      width: 32rpx;
      height: 32rpx;
    }

    &.save-btn {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);
    }

    &.share-btn {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      color: #fff;
      box-shadow: 0 8rpx 24rpx rgba(240, 147, 251, 0.3);
    }
  }
}

// 加载遮罩
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;

  .loading-content {
    background: #fff;
    padding: 40rpx 60rpx;
    border-radius: 16rpx;
    text-align: center;

    .loading-text {
      font-size: 28rpx;
      color: #333;
    }
  }
}
</style>
