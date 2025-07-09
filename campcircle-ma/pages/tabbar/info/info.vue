<template>
  <view class="message-container">
    <!-- 头部标题栏 -->
    <view class="header-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="title-section">
          <text class="main-title">消息</text>
          <text class="sub-title">Messages</text>
        </view>
       <!-- <view class="notification-badge" v-if="unreadCount > 0">
          <text class="badge-count">{{ unreadCount > 99 ? '99+' : unreadCount }}</text>
        </view> -->
      </view>
    </view>

    <!-- 主内容区域 -->
    <view class="main-content" :style="{ paddingTop: navbarHeight + 'px' }">
      <!-- 消息列表 -->
      <scroll-view 
        class="chat-scroll"
        scroll-y
        refresher-enabled
        :refresher-triggered="refresherTriggered"
        @refresherrefresh="onRefresh"
      >
        <view class="chat-list">
          <view 
            v-for="chat in chatList"
            :key="chat.chatUserId"
            class="chat-card"
            @click="handleChatClick(chat)"
          >
            <view class="card-inner">
              <!-- 头像区域 -->
              <view class="avatar-section">
                <view class="avatar-wrapper">
                  <image :src="chat.chatUser.userAvatar" class="avatar-image" />
                  <view class="online-indicator"></view>
                </view>
                <view v-if="chat.unreadCount > 0" class="unread-dot">
                  <text class="dot-text">{{ chat.unreadCount }}</text>
                </view>
              </view>

              <!-- 内容区域 -->
              <view class="content-section">
                <view class="user-info">
                  <text class="user-name">{{ chat.chatUser.userName }}</text>
                  <text class="chat-time">{{ formatMessageTime(chat.lastMessage.createTime) }}</text>
                </view>
                <view class="message-info">
                  <text class="message-preview" :class="{ 'recalled-message': chat.lastMessage.isRecalled }">
                    {{ getMessagePreview(chat.lastMessage) }}
                  </text>
                  <view class="message-type-icon" v-if="chat.lastMessage.messageType !== 1">
                    <text class="type-emoji">{{ getMessageTypeIcon(chat.lastMessage.messageType) }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 空状态展示 -->
        <view v-if="chatList.length === 0 && !loading" class="empty-state">
          <view class="empty-icon">💬</view>
          <text class="empty-title">还没有消息</text>
          <text class="empty-desc">开始和朋友们聊天吧</text>
        </view>

        <!-- 加载状态 -->
        <view v-if="loading" class="loading-container">
          <view class="loading-spinner"></view>
          <text class="loading-label">加载中...</text>
        </view>

        <!-- 底部间距 -->
        <view class="bottom-padding"></view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onShow, onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/userStore'
import { messageApi, type GetChatListRes } from '@/api/message'
import { formatTime } from '@/utils/format'
import RouterGuard from '@/utils/routerGuard'
import EmptyState from '@/components/EmptyState.vue'

// 系统信息
const statusBarHeight = ref(0)
const navbarHeight = ref(0)

// 用户store
const userStore = useUserStore()

// 消息列表数据
const chatList = ref<GetChatListRes['data']>([])
const unreadCount = ref(0)
const loading = ref(false)
const refresherTriggered = ref(false)

// 获取系统信息
const getSystemInfo = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
  navbarHeight.value = statusBarHeight.value + 44
}

// 格式化时间
const formatMessageTime = (createTime: Record<string, unknown> | string) => {
  try {
    if (typeof createTime === 'object' && createTime !== null) {
      const timeValue = createTime.time || createTime.timestamp || createTime.value || Object.values(createTime)[0]
      if (timeValue) {
        return formatTime(timeValue)
      }
    }
    return formatTime(createTime)
  } catch (error) {
    console.error('时间格式化失败:', error)
    return '刚刚'
  }
}

// 获取消息预览文本
const getMessagePreview = (lastMessage: any) => {
  if (lastMessage.isRecalled === 1) {
    return '消息已撤回'
  }

  switch (lastMessage.messageType) {
    case 0:
      return lastMessage.content || '文本消息'
    case 1:
      return '图片'
    case 2:
      return '语音'
    case 3:
      return '视频'
    default:
      return '未知消息'
  }
}

// 获取消息类型图标
const getMessageTypeIcon = (messageType: number) => {
  switch (messageType) {
    case 2:
      return '🖼️'
    case 3:
      return '🎵'
    case 4:
      return '🎬'
    default:
      return ''
  }
}



// 加载消息列表
const loadChatList = async () => {
  try {
    loading.value = true

    const res = await messageApi.getChatList()
    if (res.code === 0) {
      chatList.value = res.data || []
      unreadCount.value = chatList.value.reduce((total, chat) => total + chat.unreadCount, 0)
    } else {
      throw new Error(res.message || '获取消息列表失败')
    }
  } catch (error) {
    console.error('加载消息列表失败:', error)

    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    refresherTriggered.value = false
  }
}

// 下拉刷新
const onRefresh = () => {
  refresherTriggered.value = true
  loadChatList()
}

// 点击聊天项
const handleChatClick = (chat: any) => {
  uni.vibrateShort()

  // 跳转到聊天详情页面，传递聊天用户信息
  const chatUserParam = encodeURIComponent(JSON.stringify(chat.chatUser))
  uni.navigateTo({
    url: `/pages/chatDetail/chatDetail?chatUser=${chatUserParam}&chatUserId=${chat.chatUserId}`
  })
}

// 页面初始化
onMounted(() => {
  getSystemInfo()
  loadChatList()
})

// 页面显示时的处理
onShow(() => {
  console.log("触发了onShow")
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const route = currentPage?.route || ''

  if (RouterGuard.needAuth(route)) {
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) {
      console.log('TabBar页面需要登录，跳转到登录页面:', route)
      uni.navigateTo({
        url: `/pages/login/login?redirect=${encodeURIComponent(route)}`
      })
      return
    }
  }

  if (chatList.value.length > 0 || !loading.value) {
    loadChatList()
  }
})

// 分享功能
onShareAppMessage(() => {
  console.log('消息页面分享给朋友事件触发了')
  return {
    title: 'CampCircle - 校园消息互动平台',
    path: '/pages/tabbar/info/info',
    imageUrl: 'https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-06-13_12f2e457-9cae-4ffa-a149-1f480ddc221d.png'
  }
})

onShareTimeline(() => {
  console.log('消息页面分享到朋友圈事件触发了')
  return {
    title: '校园社交新体验 - CampCircle',
    query: 'from=timeline',
    imageUrl: 'https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-06-13_12f2e457-9cae-4ffa-a149-1f480ddc221d.png'
  }
})
</script>

<style lang="scss" scoped>
.message-container {
  width: 100%;
  height: 100vh;
  background: #f8f9fa;
  overflow: hidden;
}

// 头部标题栏
.header-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: #ffffff;
  border-bottom: 1rpx solid #f0f0f0;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 24rpx;
}

.title-section {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.main-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #333333;
  line-height: 1.2;
}

.sub-title {
  font-size: 20rpx;
  color: #999999;
  font-weight: 400;
  margin-top: -2rpx;
}

.notification-badge {
  width: 48rpx;
  height: 48rpx;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 107, 0.3);
}

.badge-count {
  color: white;
  font-size: 20rpx;
  font-weight: 700;
}

// 主内容区域
.main-content {
  height: 100vh;
  padding: 0 20rpx;
}

.chat-scroll {
  height: 100%;
  padding-bottom: 160rpx;
}

.chat-list {
  padding: 20rpx 0;
}

.chat-card {
  margin-bottom: 20rpx;
  background: white;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 20rpx rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(148, 163, 184, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:active {
    transform: translateY(2rpx);
    box-shadow: 0 1rpx 10rpx rgba(0, 0, 0, 0.08);
  }
}

.card-inner {
  display: flex;
  align-items: center;
  padding: 28rpx 24rpx;
  position: relative;
}

.avatar-section {
  position: relative;
  margin-right: 24rpx;
}

.avatar-wrapper {
  position: relative;
  width: 84rpx;
  height: 84rpx;
}

.avatar-image {
  width: 100%;
  height: 100%;
  border-radius: 20rpx;
  object-fit: cover;
}

.online-indicator {
  position: absolute;
  bottom: 2rpx;
  right: 2rpx;
  width: 18rpx;
  height: 18rpx;
  background: #22c55e;
  border-radius: 50%;
  border: 2rpx solid white;
}

.unread-dot {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  min-width: 32rpx;
  height: 32rpx;
  background: #ef4444;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 6rpx;
  border: 2rpx solid white;
}

.dot-text {
  color: white;
  font-size: 18rpx;
  font-weight: 700;
}

.content-section {
  flex: 1;
  min-width: 0;
}

.user-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8rpx;
}

.user-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #1e293b;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-time {
  font-size: 22rpx;
  color: #94a3b8;
  font-weight: 500;
  margin-left: 16rpx;
}

.message-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.message-preview {
  font-size: 26rpx;
  color: #64748b;
  line-height: 1.4;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  &.recalled-message {
    color: #94a3b8;
    font-style: italic;
  }
}

.message-type-icon {
  margin-left: 16rpx;
}

.type-emoji {
  font-size: 24rpx;
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx;
  text-align: center;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
  opacity: 0.5;
}

.empty-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #475569;
  margin-bottom: 8rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: #94a3b8;
  line-height: 1.5;
}

// 加载状态
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid #e2e8f0;
  border-top: 4rpx solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-label {
  font-size: 26rpx;
  color: #64748b;
  margin-top: 16rpx;
}

.bottom-padding {
  height: 100rpx;
}
</style>