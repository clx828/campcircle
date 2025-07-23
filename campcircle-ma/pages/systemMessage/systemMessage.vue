<template>
  <view class="system-message-container">
    <!-- 头部导航栏 -->
    <view class="header-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="back-button" @click="goBack">
          <wd-icon name="arrow-left" size="20px" color="#333"></wd-icon>
        </view>
        <text class="header-title">系统通知</text>
        <view class="header-right"></view>
      </view>
    </view>

    <!-- 主内容区域 -->
    <view class="main-content" :style="{ paddingTop: navbarHeight + 'px' }">
      <scroll-view
        class="message-scroll"
        scroll-y
        refresher-enabled
        :refresher-triggered="refresherTriggered"
        @refresherrefresh="onRefresh"
        @scrolltolower="loadMore"
      >
        <!-- 消息列表 -->
        <view class="message-list">
          <view
            v-for="message in messageList"
            :key="message.id"
            class="message-item"
            @click="handleMessageClick(message)"
          >
            <!-- 小程序logo -->
            <view class="app-logo">
              <image src="/static/img/logo.png" class="logo-image" />
            </view>
            
            <!-- 消息内容 -->
            <view class="message-content">
              <view class="message-header">
                <text class="message-title">{{ message.title || '系统通知' }}</text>
                <text class="message-time">{{ formatMessageTime(message.createTime) }}</text>
              </view>
              <text class="message-text">{{ message.content }}</text>
              <!-- 未读标识 -->
              <view v-if="message.status === 1" class="unread-dot"></view>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-if="messageList.length === 0 && !loading" class="empty-state">
          <view class="empty-icon">📢</view>
          <text class="empty-title">暂无系统通知</text>
          <text class="empty-desc">系统通知将在这里显示</text>
        </view>

        <!-- 加载状态 -->
        <view v-if="loading" class="loading-container">
          <view class="loading-spinner"></view>
          <text class="loading-label">加载中...</text>
        </view>

        <!-- 没有更多数据 -->
        <view v-if="!hasMore && messageList.length > 0" class="no-more">
          <text class="no-more-text">没有更多消息了</text>
        </view>

        <!-- 底部间距 -->
        <view class="bottom-padding"></view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { systemMessageApi, type SystemMessageVO, type SystemMessageQueryParams } from '@/api/systemMessage'
import { formatTime } from '@/utils/format'

// 系统信息
const statusBarHeight = ref(0)
const navbarHeight = ref(0)

// 消息列表数据
const messageList = ref<SystemMessageVO[]>([])
const loading = ref(false)
const refresherTriggered = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = 20

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

// 加载消息列表
const loadMessageList = async (isRefresh = false) => {
  try {
    if (isRefresh) {
      currentPage.value = 1
      hasMore.value = true
    }

    loading.value = true

    const params: SystemMessageQueryParams = {
      current: currentPage.value,
      pageSize: pageSize,
      type: 0 // 系统通知
    }

    const res = await systemMessageApi.listMySystemMessageByPage(params)
    if (res.code === 0) {
      const newMessages = res.data.records || []

      if (isRefresh) {
        messageList.value = newMessages
      } else {
        messageList.value.push(...newMessages)
      }

      // 批量标记未读消息为已读并保存到本地
      await markMessagesAsReadAndSave(newMessages)

      // 判断是否还有更多数据
      hasMore.value = newMessages.length === pageSize

      if (hasMore.value) {
        currentPage.value++
      }
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
  loadMessageList(true)
}

// 加载更多
const loadMore = () => {
  if (!loading.value && hasMore.value) {
    loadMessageList(false)
  }
}

// 批量标记消息为已读并保存到本地
const markMessagesAsReadAndSave = async (messages: SystemMessageVO[]) => {
  try {
    // 筛选出未读消息
    const unreadMessages = messages.filter(msg => msg.status === 1)

    console.log(`查询到${messages.length}条系统消息，其中${unreadMessages.length}条未读`)

    if (unreadMessages.length === 0) {
      console.log('没有未读系统消息，跳过标记已读')
      return
    }

    console.log('开始批量标记系统消息为已读:', unreadMessages.map(msg => msg.id))

    // 批量标记为已读（后端API调用）
    const markReadPromises = unreadMessages.map(msg => {
      console.log(`调用标记已读接口: messageId=${msg.id}`)
      return systemMessageApi.markAsRead(msg.id).catch(error => {
        console.error(`标记消息${msg.id}已读失败:`, error)
        return null
      })
    })

    const results = await Promise.allSettled(markReadPromises)
    console.log('标记已读接口调用结果:', results)

    // 更新本地消息状态
    unreadMessages.forEach(msg => {
      msg.status = 0
    })

    // 保存到本地存储
    await saveMessagesToLocal(messages)

    console.log(`成功标记${unreadMessages.length}条系统消息为已读`)
  } catch (error) {
    console.error('批量标记已读失败:', error)
  }
}

// 保存消息到本地存储
const saveMessagesToLocal = async (messages: SystemMessageVO[]) => {
  try {
    const storageKey = 'system_messages'
    const existingData = uni.getStorageSync(storageKey) || []

    // 合并新消息，避免重复
    const messageMap = new Map()

    // 先添加现有消息
    existingData.forEach((msg: SystemMessageVO) => {
      messageMap.set(msg.id, msg)
    })

    // 添加新消息（会覆盖相同ID的旧消息）
    messages.forEach(msg => {
      messageMap.set(msg.id, msg)
    })

    // 转换为数组并按时间排序
    const mergedMessages = Array.from(messageMap.values()).sort((a, b) => {
      const timeA = getMessageTimestamp(a.createTime)
      const timeB = getMessageTimestamp(b.createTime)
      return timeB - timeA // 降序排列
    })

    // 只保留最近的100条消息
    const limitedMessages = mergedMessages.slice(0, 100)

    uni.setStorageSync(storageKey, limitedMessages)
    console.log(`已保存${limitedMessages.length}条消息到本地`)
  } catch (error) {
    console.error('保存消息到本地失败:', error)
  }
}

// 获取消息时间戳
const getMessageTimestamp = (createTime: Record<string, unknown> | string): number => {
  try {
    if (typeof createTime === 'object' && createTime !== null) {
      const timeValue = createTime.time || createTime.timestamp || createTime.value || Object.values(createTime)[0]
      if (timeValue) {
        return new Date(timeValue).getTime()
      }
    }
    return new Date(createTime).getTime()
  } catch (error) {
    return Date.now()
  }
}

// 从本地存储加载消息
const loadMessagesFromLocal = () => {
  try {
    const storageKey = 'system_messages'
    const localMessages = uni.getStorageSync(storageKey) || []

    if (localMessages.length > 0) {
      messageList.value = localMessages
      console.log(`从本地加载了${localMessages.length}条消息`)
    }
  } catch (error) {
    console.error('从本地加载消息失败:', error)
  }
}

// 点击消息
const handleMessageClick = async (message: SystemMessageVO) => {
  // 系统消息点击不需要特殊处理
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 页面初始化
onMounted(() => {
  getSystemInfo()
  // 先从本地加载消息，再从网络获取最新消息
  loadMessagesFromLocal()
  loadMessageList(true)
})

// 页面显示时刷新
onShow(() => {
  if (messageList.value.length > 0) {
    loadMessageList(true)
  }
})
</script>

<style lang="scss" scoped>
.system-message-container {
  width: 100%;
  height: 100vh;
  background: #f8f9fa;
  overflow: hidden;
}

// 头部导航栏
.header-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: #ffffff;
  border-bottom: 1rpx solid #e5e5e5;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 24rpx;
}

.back-button {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s ease;

  &:active {
    background-color: #f5f5f5;
  }
}

.header-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333333;
}

.header-right {
  width: 60rpx;
}

// 主内容区域
.main-content {
  height: 100vh;
  width: 100%;
}

.message-scroll {
  height: 100%;
  width: 100%;
}

.message-list {
  background: white;
  width: 100%;
}

.message-item {
  display: flex;
  align-items: flex-start;
  padding: 24rpx 40rpx;
  min-height: 120rpx;
  border-bottom: 1rpx solid #f0f0f0;
  position: relative;
  transition: background-color 0.2s ease;

  &:active {
    background-color: #f8f9fa;
  }

  &:last-child {
    border-bottom: none;
  }
}

.app-logo {
  width: 96rpx;
  height: 96rpx;
  margin-right: 32rpx;
  flex-shrink: 0;
}

.logo-image {
  width: 100%;
  height: 100%;
  border-radius: 12rpx;
  object-fit: cover;
}

.message-content {
  flex: 1;
  min-width: 0;
  position: relative;
}

.message-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.message-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-time {
  font-size: 24rpx;
  color: #999999;
  margin-left: 20rpx;
  flex-shrink: 0;
}

.message-text {
  font-size: 28rpx;
  color: #666666;
  line-height: 1.5;
  word-break: break-all;
}

.unread-dot {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 16rpx;
  height: 16rpx;
  background: #ff4757;
  border-radius: 50%;
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
  padding: 40rpx;
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

// 没有更多数据
.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

.no-more-text {
  font-size: 24rpx;
  color: #999999;
}

.bottom-padding {
  height: 100rpx;
}
</style>
