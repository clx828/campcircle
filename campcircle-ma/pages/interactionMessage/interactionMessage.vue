<template>
  <view class="interaction-message-container">
    <!-- 头部导航栏 -->
    <view class="header-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="back-button" @click="goBack">
          <wd-icon name="arrow-left" size="20px" color="#333"></wd-icon>
        </view>
        <text class="header-title">{{ pageTitle }}</text>
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
            <!-- 头像区域 -->
            <view class="avatar-section" @click.stop="goToUserProfile(message.fromUser)">
              <image :src="message.fromUser?.userAvatar || 'https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-06-13_12f2e457-9cae-4ffa-a149-1f480ddc221d.png'" class="avatar-image" />
              <view v-if="message.status === 1" class="unread-dot"></view>
            </view>

            <!-- 内容区域 -->
            <view class="content-section">
              <view class="user-info">
                <text class="user-name" @click.stop="goToUserProfile(message.fromUser)">{{ message.fromUser?.userName || '系统' }}</text>
                <text class="message-time">{{ formatMessageTime(message.createTime) }}</text>
              </view>
              <view class="message-info">
                <text class="message-preview">{{ message.content }}</text>
                <view class="message-type-icon">
                  <text class="type-emoji">{{ getMessageTypeIcon(message.type) }}</text>
                </view>
              </view>
            </view>

            <!-- 帖子图片 -->
            <view v-if="message.post && message.post.pictureUrlList && message.post.pictureUrlList.length > 0" class="post-image-section">
              <image :src="message.post.pictureUrlList[0]" class="post-image" />
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-if="messageList.length === 0 && !loading" class="empty-state">
          <view class="empty-icon">💬</view>
          <text class="empty-title">暂无消息</text>
          <text class="empty-desc">相关消息将在这里显示</text>
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
import { ref, onMounted, computed } from 'vue'
import { onShow, onLoad } from '@dcloudio/uni-app'
import { systemMessageApi, type SystemMessageVO, type SystemMessageQueryParams } from '@/api/systemMessage'
import { formatTime } from '@/utils/format'

// 页面参数
const messageType = ref<string>('')

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

// 页面标题
const pageTitle = computed(() => {
  switch (messageType.value) {
    case 'likeFavour':
      return '赞和收藏'
    case 'commentFollow':
      return '评论和@'
    default:
      return '消息'
  }
})

// 获取消息类型列表
const getMessageTypes = () => {
  switch (messageType.value) {
    case 'likeFavour':
      return [1, 2] // 点赞和收藏
    case 'commentFollow':
      return [3, 4] // 评论和关注
    default:
      return []
  }
}

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

// 获取消息类型图标
const getMessageTypeIcon = (type: number) => {
  switch (type) {
    case 1:
      return '👍'
    case 2:
      return '⭐'
    case 3:
      return '💬'
    case 4:
      return '👥'
    default:
      return ''
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

    const types = getMessageTypes()
    if (types.length === 0) {
      return
    }

    const params: SystemMessageQueryParams = {
      current: currentPage.value,
      pageSize: pageSize,
      types: types
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

    console.log(`查询到${messages.length}条消息，其中${unreadMessages.length}条未读`)

    if (unreadMessages.length === 0) {
      console.log('没有未读消息，跳过标记已读')
      return
    }

    console.log('开始批量标记消息为已读:', unreadMessages.map(msg => msg.id))

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

    console.log(`成功标记${unreadMessages.length}条消息为已读`)
  } catch (error) {
    console.error('批量标记已读失败:', error)
  }
}

// 保存消息到本地存储
const saveMessagesToLocal = async (messages: SystemMessageVO[]) => {
  try {
    const storageKey = `interaction_messages_${messageType.value}`
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

// 点击消息
const handleMessageClick = async (message: SystemMessageVO) => {
  // 如果有关联帖子，跳转到帖子详情
  if (message.postId) {
    uni.navigateTo({
      url: `/pages/postDetail/postDetail?id=${message.postId}`
    })
  }
}

// 跳转到用户主页
const goToUserProfile = (user: any) => {
  if (!user || !user.id) {
    console.warn('用户信息不完整，无法跳转')
    return
  }

  uni.navigateTo({
    url: `/pages/userProfile/userProfile?userId=${user.id}`
  })
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 页面加载
onLoad((options) => {
  messageType.value = options?.type || ''
})

// 从本地存储加载消息
const loadMessagesFromLocal = () => {
  try {
    const storageKey = `interaction_messages_${messageType.value}`
    const localMessages = uni.getStorageSync(storageKey) || []

    if (localMessages.length > 0) {
      messageList.value = localMessages
      console.log(`从本地加载了${localMessages.length}条消息`)
    }
  } catch (error) {
    console.error('从本地加载消息失败:', error)
  }
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
.interaction-message-container {
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
  align-items: center;
  padding: 24rpx 40rpx;
  min-height: 120rpx;
  border-bottom: 1rpx solid #f0f0f0;
  transition: background-color 0.2s ease;

  &:active {
    background-color: #f8f9fa;
  }

  &:last-child {
    border-bottom: none;
  }
}

.avatar-section {
  position: relative;
  margin-right: 32rpx;
  flex-shrink: 0;
  cursor: pointer;
  transition: transform 0.2s ease;

  &:active {
    transform: scale(0.95);
  }
}

.avatar-image {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  object-fit: cover;
}

.unread-dot {
  position: absolute;
  top: -6rpx;
  right: -6rpx;
  width: 24rpx;
  height: 24rpx;
  background: #ff4757;
  border-radius: 12rpx;
  border: 2rpx solid white;
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
  margin-bottom: 12rpx;
}

.user-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #333333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
  transition: color 0.2s ease;

  &:active {
    color: #007aff;
  }
}

.message-time {
  font-size: 24rpx;
  color: #999999;
  margin-left: 20rpx;
  flex-shrink: 0;
}

.message-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.message-preview {
  font-size: 28rpx;
  color: #666666;
  line-height: 1.5;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-type-icon {
  margin-left: 16rpx;
  flex-shrink: 0;
}

.type-emoji {
  font-size: 24rpx;
}

.post-image-section {
  margin-left: 32rpx;
  flex-shrink: 0;
  height: 96rpx;
}

.post-image {
  width: 72rpx;
  height: 96rpx;
  border-radius: 12rpx;
  object-fit: cover;
}

// 空状态、加载状态、没有更多数据样式与系统消息页面相同
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
