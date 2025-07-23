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
        <!-- 系统消息功能区 -->
        <view class="system-message-section">
          <view class="system-item" @click="handleSystemNotification">
            <view class="system-icon">
              <image src="/static/img/notification.png" class="icon-image" />
            </view>
            <text class="system-text">系统通知</text>
            <view v-if="systemUnreadCount.system > 0" class="system-badge">
              <text class="badge-text">{{ systemUnreadCount.system > 99 ? '99+' : systemUnreadCount.system }}</text>
            </view>
          </view>

          <view class="system-item" @click="handleLikeAndFavour">
            <view class="system-icon">
              <image src="/static/img/like.png" class="icon-image" />
            </view>
            <text class="system-text">赞和收藏</text>
            <view v-if="systemUnreadCount.likeFavour > 0" class="system-badge">
              <text class="badge-text">{{ systemUnreadCount.likeFavour > 99 ? '99+' : systemUnreadCount.likeFavour }}</text>
            </view>
          </view>

          <view class="system-item" @click="handleCommentAndMention">
            <view class="system-icon">
              <image src="/static/img/message.png" class="icon-image" />
            </view>
            <text class="system-text">评论和@</text>
            <view v-if="systemUnreadCount.commentFollow > 0" class="system-badge">
              <text class="badge-text">{{ systemUnreadCount.commentFollow > 99 ? '99+' : systemUnreadCount.commentFollow }}</text>
            </view>
          </view>
        </view>
        
        <view class="chat-list">
          <view
            v-for="chat in chatList"
            :key="chat.chatUserId"
            class="chat-item"
            @click="handleChatClick(chat)"
          >
            <!-- 头像区域 -->
            <view class="avatar-section">
              <image :src="chat.chatUser.userAvatar" class="avatar-image" />
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
import { systemMessageApi } from '@/api/systemMessage'
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

// 系统消息未读数量
const systemUnreadCount = ref({
  total: 0,
  system: 0,
  likeFavour: 0,
  commentFollow: 0
})

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



// 加载系统消息未读数量
const loadSystemUnreadCount = async () => {
  try {
    const res = await systemMessageApi.getUnreadCount()
    if (res.code === 0) {
      systemUnreadCount.value = {
        total: parseInt(res.data.total) || 0,
        system: parseInt(res.data.system) || 0,
        likeFavour: parseInt(res.data.likeFavour) || 0,
        commentFollow: parseInt(res.data.commentFollow) || 0
      }
    }
  } catch (error) {
    console.error('获取系统消息未读数量失败:', error)
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
  loadSystemUnreadCount()
}

// 处理系统通知点击
const handleSystemNotification = () => {
  uni.vibrateShort()
  uni.navigateTo({
    url: '/pages/systemMessage/systemMessage'
  })
}

// 处理赞和收藏点击
const handleLikeAndFavour = () => {
  uni.vibrateShort()
  uni.navigateTo({
    url: '/pages/interactionMessage/interactionMessage?type=likeFavour'
  })
}

// 处理评论和@点击
const handleCommentAndMention = () => {
  uni.vibrateShort()
  uni.navigateTo({
    url: '/pages/interactionMessage/interactionMessage?type=commentFollow'
  })
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
  loadSystemUnreadCount()
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
    loadSystemUnreadCount()
  }
})

// 分享功能
onShareAppMessage(() => {
  console.log('消息页面分享给朋友事件触发了')
  return {
    title: 'CampCircle - 校园消息互动平台',
    path: '/pages/tabbar/message/message',
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
  width: 100%;
}

// 系统消息功能区
.system-message-section {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 40rpx 20rpx;
  background: white;
  width: 100%;
}

.system-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  padding: 20rpx;
  border-radius: 12rpx;
  transition: all 0.3s ease;

  &:active {
    background: #f8f9fa;
    transform: scale(0.95);
  }
}

.system-icon {
  width: 80rpx;
  height: 80rpx;
  margin-bottom: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.system-text {
  font-size: 24rpx;
  color: #666666;
  font-weight: 500;
}

.system-badge {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  min-width: 32rpx;
  height: 32rpx;
  background: #ff4757;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 6rpx;
}

.badge-text {
  color: white;
  font-size: 18rpx;
  font-weight: 700;
}

.chat-scroll {
  height: 100%;
  width: 100%;
  padding-bottom: 160rpx;
}

.chat-list {
  background: white;
  width: 100%;
}

.chat-item {
  display: flex;
  align-items: center;
  padding: 24rpx 20rpx;
  transition: background-color 0.2s ease;

  &:active {
    background-color: #f8f9fa;
  }
}

.avatar-section {
  position: relative;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.avatar-image {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  object-fit: cover;
}

.unread-dot {
  position: absolute;
  top: -6rpx;
  right: -6rpx;
  min-width: 28rpx;
  height: 28rpx;
  background: #ff4757;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4rpx;
}

.dot-text {
  color: white;
  font-size: 16rpx;
  font-weight: 700;
}

.content-section {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.user-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6rpx;
}

.user-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #333333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-time {
  font-size: 20rpx;
  color: #999999;
  font-weight: 400;
  margin-left: 16rpx;
  flex-shrink: 0;
}

.message-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.message-preview {
  font-size: 24rpx;
  color: #666666;
  line-height: 1.4;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  &.recalled-message {
    color: #999999;
    font-style: italic;
  }
}

.message-type-icon {
  margin-left: 12rpx;
  flex-shrink: 0;
}

.type-emoji {
  font-size: 20rpx;
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
