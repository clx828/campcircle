<template>
  <view class="chat-container">
    <!-- 简洁导航栏 -->
    <view class="navbar-wrapper" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @click="goBack">
          <view class="back-button">
            <wd-icon name="arrow-left" size="20px" color="#333"></wd-icon>
          </view>
        </view>
        <view class="navbar-center">
          <view class="user-info">
            <image :src="chatUser?.userAvatar" class="navbar-avatar" />
            <view class="user-details">
              <text class="user-name">{{ chatUser?.userName || '聊天' }}</text>
              <view class="status-indicator">
                <view class="status-dot"></view>
                <text class="status-text">{{ chatUser?.userProfile || '在线' }}</text>
              </view>
            </view>
          </view>
        </view>
        <view class="navbar-right">
          <view class="action-button">
            <text class="action-icon">⋯</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 聊天消息区域 -->
    <scroll-view
        class="chat-messages"
        :style="{ paddingTop: navbarHeight + 'px' }"
        scroll-y
        :scroll-top="scrollTop"
        scroll-with-animation
        refresher-enabled
        :refresher-triggered="refresherTriggered"
        @refresherrefresh="onRefresh"
    >
      <view class="messages-container" :style="{ paddingBottom: chatInputBottomSpace + 'px' }">

        <!-- 消息列表 - 反转顺序显示，最新消息在下面 -->
        <view
            v-for="(message, index) in reversedMessageList"
            :key="message.id"
            class="message-wrapper"
            :class="{ 'is-own': isOwnMessage(message) }"
        >
          <!-- 撤回消息提示 -->
          <view v-if="message.isRecalled === 1" class="recalled-message">
            <text class="recalled-text">
              {{ isOwnMessage(message) ? '你' : (message.fromUser?.userName || '对方') }}撤回了一条消息
            </text>
          </view>

          <!-- 正常消息 -->
          <template v-else>
            <!-- 消息时间 - 放在消息上方 -->
            <view class="message-time-wrapper">
              <text class="message-time">{{ formatChatTime(message.createTime) }}</text>
            </view>

            <!-- 对方消息 - 在左边 -->
            <view v-if="!isOwnMessage(message)" class="message-row other-message">
              <view class="avatar-wrapper">
                <image :src="message.fromUser.userAvatar" class="message-avatar" />
              </view>
              <view class="message-content-wrapper">
                <view class="message-bubble other-bubble">
                  <text v-if="message.messageType === 0" class="message-text">{{ message.content }}</text>
                  <image v-else-if="message.messageType === 1" :src="message.pictureUrl" class="message-image" mode="aspectFit" />
                  <view v-else class="unsupported-content">
                    <text class="unsupported-text">不支持的消息类型</text>
                  </view>
                </view>
              </view>
            </view>

            <!-- 自己的消息 - 在右边 -->
            <view v-else class="message-row own-message">
              <view class="message-content-wrapper">
                <view class="message-bubble own-bubble">
                  <text v-if="message.messageType === 0 || message.messageType === 1" class="message-text">{{ message.content }}</text>
                  <image v-else-if="message.messageType === 2" :src="message.pictureUrl" class="message-image" mode="aspectFit" />
                  <view v-else class="unsupported-content">
                    <text class="unsupported-text">不支持的消息类型</text>
                  </view>
                </view>
                <!-- 只有自己发送的消息才显示已读/未读状态 -->
                <view class="message-status own-status">
                  <text v-if="message.isRead === 1" class="read-status read">已读</text>
                  <text v-else class="read-status unread">未读</text>
                </view>
              </view>
              <view class="avatar-wrapper">
                <image :src="userStore.getUserInfo?.userAvatar" class="message-avatar" />
              </view>
            </view>
          </template>
        </view>

        <!-- 加载提示 -->
        <view v-if="hasMore" class="load-more">
          <text class="load-text">上拉查看更多消息</text>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="messageList.length === 0 && !loading" class="empty-state">
        <view class="empty-icon">💬</view>
        <text class="empty-title">开始对话</text>
        <text class="empty-subtitle">发送消息开始聊天</text>
      </view>

      <!-- 加载状态 -->
      <view v-if="loading" class="loading-state">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载中...</text>
      </view>
    </scroll-view>

    <!-- 聊天输入组件 -->
    <ChatInput
      @send="handleSendMessage"
      @selectImage="handleSelectImage"
      @selectFile="handleSelectFile"
      @panelChange="handlePanelChange"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { formatTime, createCompatibleDate } from '@/utils/format'
import { useUserStore } from '@/stores/userStore'
import { messageApi, type GetChatHistoryParams, type GetChatHistoryRes, type SendMessageParams } from '@/api/message'
import ChatInput from '@/components/ChatInput.vue'

// 系统信息
const statusBarHeight = ref(0)
const navbarHeight = ref(0)

// ChatInput 底部间距管理
const chatInputBottomSpace = ref(80) // 默认底部间距（120rpx ≈ 60px + 20px 安全距离）

// 用户store
const userStore = useUserStore()

// 聊天数据
const chatUser = ref<any>(null)
const chatUserId = ref<number>(0)
const messageList = ref<any[]>([])
const scrollTop = ref(0)
const loading = ref(false)
const refresherTriggered = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = 30

// 计算属性：反转消息列表，使最新消息在最下面
const reversedMessageList = computed(() => {
  return [...messageList.value].reverse()
})

// 获取系统信息
const getSystemInfo = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
  // 计算导航栏总高度：状态栏 + 导航栏内容（120rpx转px）
  navbarHeight.value = statusBarHeight.value + uni.upx2px(120)
}

// 返回上一页
const goBack = () => {
  uni.vibrateShort()
  uni.navigateBack()
}

// 判断是否为自己的消息
const isOwnMessage = (message: any) => {
  return message.fromUserId === userStore.getUserInfo?.id
}

// 格式化消息时间
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

// 格式化聊天时间（按您的要求）
const formatChatTime = (createTime: Record<string, unknown> | string) => {
  try {
    let date: Date

    if (typeof createTime === 'object' && createTime !== null) {
      const timeValue = createTime.time || createTime.timestamp || createTime.value || Object.values(createTime)[0]
      date = createCompatibleDate(timeValue)
    } else {
      date = createCompatibleDate(createTime)
    }

    if (isNaN(date.getTime())) {
      return '刚刚'
    }

    const now = new Date()
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)
    const dayBeforeYesterday = new Date(today.getTime() - 2 * 24 * 60 * 60 * 1000)
    const messageDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())

    const timeStr = `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`

    // 今天
    if (messageDate.getTime() === today.getTime()) {
      return timeStr
    }

    // 昨天
    if (messageDate.getTime() === yesterday.getTime()) {
      return `昨天 ${timeStr}`
    }

    // 前天
    if (messageDate.getTime() === dayBeforeYesterday.getTime()) {
      return `前天 ${timeStr}`
    }

    // 一周内显示星期几
    const daysDiff = Math.floor((today.getTime() - messageDate.getTime()) / (24 * 60 * 60 * 1000))
    if (daysDiff <= 7) {
      const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
      return `${weekdays[date.getDay()]} ${timeStr}`
    }

    // 今年内显示月/日
    if (date.getFullYear() === now.getFullYear()) {
      return `${date.getMonth() + 1}/${date.getDate()} ${timeStr}`
    }

    // 其他显示年/月/日
    return `${date.getFullYear()}/${date.getMonth() + 1}/${date.getDate()} ${timeStr}`

  } catch (error) {
    console.error('聊天时间格式化失败:', error)
    return '刚刚'
  }
}

// 格式化日期
const formatDate = (date: Date) => {
  const today = new Date()
  const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)

  if (date.toDateString() === today.toDateString()) {
    return '今天'
  } else if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  } else {
    return `${date.getMonth() + 1}月${date.getDate()}日`
  }
}



// 加载聊天记录
const loadChatHistory = async (isRefresh = false) => {
  if (loading.value) return

  try {
    loading.value = true

    const params: GetChatHistoryParams = {
      chatUserId: chatUserId.value,
      current: isRefresh ? 1 : currentPage.value,
      pageSize: pageSize
    }

    const response = await messageApi.getChatHistory(params)

    if (response.code === 0) {
      const { records, total, pages } = response.data

      if (isRefresh) {
        messageList.value = records || []
        currentPage.value = 1
      } else {
        messageList.value.push(...(records || []))
      }

      hasMore.value = currentPage.value < pages

      if (!isRefresh) {
        currentPage.value++
      }

      if (isRefresh) {
        nextTick(() => {
          scrollTop.value = 99999
        })
      }
    } else {
      throw new Error(response.message || '获取聊天记录失败')
    }
  } catch (error) {
    console.error('加载聊天记录失败:', error)

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
  if (!hasMore.value) {
    refresherTriggered.value = false
    return
  }

  refresherTriggered.value = true
  loadChatHistory(false)
}

// 强制刷新聊天记录（发送消息后使用）
const refreshChatHistory = async () => {
  return new Promise((resolve, reject) => {
    try {
      // 重置分页状态
      currentPage.value = 1
      hasMore.value = true
      loading.value = false

      // 延迟一下确保后端数据已更新
      setTimeout(async () => {
        try {
          console.log('开始刷新聊天记录...')
          await loadChatHistory(true)

          // 滚动到底部显示新消息
          nextTick(() => {
            scrollTop.value = 99999
            console.log('聊天记录刷新完成，已滚动到底部')
          })

          resolve(true)
        } catch (error) {
          console.error('第一次刷新失败，尝试重新刷新:', error)
          // 如果刷新失败，再尝试一次
          setTimeout(async () => {
            try {
              await loadChatHistory(true)
              nextTick(() => {
                scrollTop.value = 99999
              })
              resolve(true)
            } catch (retryError) {
              console.error('重试刷新也失败:', retryError)
              reject(retryError)
            }
          }, 1000)
        }
      }, 500) // 延迟500ms刷新，给后端更多时间处理

    } catch (error) {
      console.error('重置聊天状态失败:', error)
      reject(error)
    }
  })
}

// 发送消息处理函数
const handleSendMessage = async (messageData: { text?: string, type: string, data?: any }) => {
  if (!chatUserId.value) {
    uni.showToast({
      title: '聊天用户信息错误',
      icon: 'none'
    })
    return
  }

  // 显示发送中状态
  uni.showLoading({
    title: '发送中...',
    mask: true
  })

  try {
    let sendParams: SendMessageParams = {
      toUserId: chatUserId.value
    }

    // 根据消息类型设置参数
    switch (messageData.type) {
      case 'text':
        sendParams.content = messageData.text
        sendParams.messageType = 0 // 文本消息
        break
      case 'location':
        sendParams.content = `位置：${messageData.data.name || messageData.data.address}`
        sendParams.messageType = 0 // 位置作为文本消息发送
        break
      default:
        console.warn('不支持的消息类型:', messageData.type)
        uni.hideLoading()
        return
    }

    // 调用发送消息接口
    const response = await messageApi.sendMessage(sendParams)

    if (response.code === 0) {
      // 隐藏加载状态
      uni.hideLoading()

      // 发送成功提示
      uni.showToast({
        title: '发送成功',
        icon: 'success',
        duration: 1000
      })

      // 立即刷新聊天记录
      await refreshChatHistory()

    } else {
      throw new Error(response.message || '发送失败')
    }
  } catch (error) {
    // 隐藏加载状态
    uni.hideLoading()

    console.error('发送消息失败:', error)
    uni.showToast({
      title: error.message || '发送失败',
      icon: 'none'
    })
  }
}

// 处理图片选择（暂不对接）
const handleSelectImage = (imagePaths: string[]) => {
  console.log('选择的图片:', imagePaths)
  uni.showToast({
    title: '图片发送功能开发中',
    icon: 'none'
  })
}

// 处理文件选择（暂不对接）
const handleSelectFile = (files: any[]) => {
  console.log('选择的文件:', files)
  uni.showToast({
    title: '文件发送功能开发中',
    icon: 'none'
  })
}

// 处理 ChatInput 面板变化
const handlePanelChange = (panelInfo: { isExpanded: boolean, panelHeight: number, totalHeight: number }) => {
  console.log('面板状态变化:', panelInfo)

  // 根据面板状态动态调整底部间距
  if (panelInfo.isExpanded) {
    // 面板展开时：输入框高度 + 面板高度 + 安全距离
    chatInputBottomSpace.value = panelInfo.totalHeight + 20
  } else {
    // 面板收起时：只需要输入框高度 + 安全距离
    chatInputBottomSpace.value = 80 // 60px + 20px 安全距离
  }

  // 面板展开时自动滚动到底部
  if (panelInfo.isExpanded) {
    nextTick(() => {
      scrollTop.value = 99999
    })
  }
}

// 页面加载
onLoad((options) => {
  console.log('聊天页面参数:', options)

  if (options.chatUser) {
    try {
      chatUser.value = JSON.parse(decodeURIComponent(options.chatUser))
    } catch (error) {
      console.error('解析聊天用户信息失败:', error)
    }
  }

  if (options.chatUserId) {
    chatUserId.value = parseInt(options.chatUserId)
  }

  loadChatHistory(true)
})

onMounted(() => {
  getSystemInfo()
})
</script>

<style lang="scss" scoped>
.chat-container {
  background: #f5f5f5;
  min-height: 100vh;
}

// 导航栏样式
.navbar-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: #ffffff;
  border-bottom: 1rpx solid #f0f0f0;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.navbar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 120rpx;
  padding: 0 32rpx;
}

.navbar-left {
  display: flex;
  align-items: center;
}

.back-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.05);
}

.back-icon {
  font-size: 32rpx;
  color: #333333;
  font-weight: bold;
}

.navbar-center {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.navbar-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: #f0f0f0;
}

.user-details {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.user-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #333333;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 4rpx;
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #34c759;
}

.status-text {
  font-size: 24rpx;
  color: #666666;
}

.navbar-right {
  display: flex;
  align-items: center;
}

.action-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.05);
}

.action-icon {
  font-size: 32rpx;
  color: #333333;
  font-weight: bold;
}

// 聊天消息区域
.chat-messages {
  height: 100vh;
  background: #f5f5f5;
}

.messages-container {
  padding: 24rpx;
  // 底部间距通过动态样式设置，确保不被 ChatInput 遮住
  min-height: 100%;
  box-sizing: border-box;
}

// 日期分割线
.time-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 32rpx 0;
}

.time-text {
  background: rgba(0, 0, 0, 0.1);
  color: #999999;
  font-size: 24rpx;
  padding: 8rpx 24rpx;
  border-radius: 24rpx;
}

// 消息项
.message-wrapper {
  margin-bottom: 32rpx;
}

// 消息时间显示在消息上方
.message-time-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 16rpx;
}

.message-time {
  font-size: 22rpx;
  color: #999999;
  background: rgba(255, 255, 255, 0.8);
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  backdrop-filter: blur(10rpx);
}

// 撤回消息样式
.recalled-message {
  display: flex;
  justify-content: center;
  margin: 24rpx 0;
}

.recalled-text {
  background: rgba(0, 0, 0, 0.1);
  color: #999999;
  font-size: 24rpx;
  padding: 12rpx 24rpx;
  border-radius: 24rpx;
  font-style: italic;
}

.message-row {
  display: flex;
  align-items: flex-start;

  &.other-message {
    justify-content: flex-start;
  }

  &.own-message {
    justify-content: flex-end;
  }
}

.avatar-wrapper {
  margin: 0 16rpx;
}

.message-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #f0f0f0;
}

.message-content-wrapper {
  max-width: 60%;
  display: flex;
  flex-direction: column;
}

// 消息气泡
.message-bubble {
  padding: 24rpx 32rpx;
  border-radius: 20rpx;
  position: relative;
  word-break: break-all;

  &.other-bubble {
    background: #ffffff;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
    align-self: flex-start;
  }

  &.own-bubble {
    background: #1e7ce3;
    color: #ffffff;
    align-self: flex-end;
  }
}

.message-text {
  font-size: 30rpx;
  line-height: 1.5;
}

.message-image {
  max-width: 400rpx;
  max-height: 400rpx;
  border-radius: 16rpx;
}

.unsupported-content {
  padding: 16rpx;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 16rpx;
}

.unsupported-text {
  font-size: 26rpx;
  color: #999999;
  font-style: italic;
}

// 消息状态（已读/未读）- 只显示在自己发送的消息下
.message-status {
  display: flex;
  align-items: center;
  margin-top: 8rpx;

  &.own-status {
    justify-content: flex-end;
  }
}

.read-status {
  font-size: 22rpx;
  font-weight: 400;

  &.read {
    color: #666666;
  }

  &.unread {
    color: #999999;
  }
}

// 加载更多提示
.load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx 0;
}

.load-text {
  font-size: 24rpx;
  color: #cccccc;
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 60rpx;
  text-align: center;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 32rpx;
  opacity: 0.5;
}

.empty-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #666666;
  margin-bottom: 16rpx;
}

.empty-subtitle {
  font-size: 28rpx;
  color: #999999;
  line-height: 1.5;
}

// 加载状态
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid #f0f0f0;
  border-top: 4rpx solid #1e7ce3;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  font-size: 28rpx;
  color: #999999;
  margin-top: 16rpx;
}


</style>