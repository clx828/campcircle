<template>
  <view class="follow-page">
    <!-- 头部标题栏 -->
    <view class="header-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="title-section">
          <text class="main-title">消息</text>
          <text class="sub-title">发现你关注的精彩内容</text>
        </view>
        <!-- <view class="notification-badge" v-if="unreadCount > 0">
           <text class="badge-count">{{ unreadCount > 99 ? '99+' : unreadCount }}</text>
         </view> -->
      </view>
    </view>

    <!-- 内容区域 -->
    <scroll-view
      class="content"
      :style="{ paddingTop: (statusBarHeight + 44) + 'px' }"
      scroll-y="true"
      @scrolltolower="loadMore"
      refresher-enabled="true"
      :refresher-triggered="refresherTriggered"
      @refresherrefresh="onRefresh"
    >
      <!-- 帖子列表 -->
      <view v-if="postList.length > 0" class="post-list">
        <SocialCard
          v-for="post in postList"
          :key="post.id"
          :cardInfo="post"
          @follow="handleFollow"
          @like="handleLike"
          @comment="handleComment"
          @share="handleShare"
        />
      </view>

      <!-- 加载更多 -->
      <view v-if="postList.length > 0" class="load-status">
        <view v-if="loading" class="loading">
          <view class="loading-spinner"></view>
          <text>加载中...</text>
        </view>
        <view v-else-if="hasMore" class="load-tip">
          <text>上拉加载更多</text>
        </view>
        <view v-else class="no-more">
          <text>—— 已经到底了 ——</text>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && postList.length === 0" class="empty">
        <view class="empty-icon">📱</view>
        <text class="empty-title">还没有关注的动态</text>
        <text class="empty-desc">关注更多朋友，发现精彩内容</text>
        <button class="discover-btn" @click="goToDiscover">去发现</button>
      </view>

      <!-- 底部间距 -->
      <view class="bottom-space"></view>
    </scroll-view>

    <!-- 首次加载 -->
    <view v-if="loading && postList.length === 0" class="first-loading">
      <view class="loading-spinner big"></view>
      <text>加载中...</text>
    </view>
  </view>
</template>
<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import SocialCard from '@/components/SocialCard.vue'
import { followApi, pageRequest } from '@/api/follow'
import { useUserStore } from '@/stores/userStore'

// 用户store
const userStore = useUserStore()

// 页面状态
const refresherTriggered = ref(false)
const statusBarHeight = ref(0)

// 数据状态
const postList = ref([])
const loading = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = 10

// 获取系统信息
const getSystemInfo = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
}



// 加载关注用户的帖子列表
const loadFollowPosts = async (isRefresh = false) => {
  if (loading.value) return

  try {
    loading.value = true

    const pageRequest: pageRequest = {
      current: isRefresh ? 1 : currentPage.value,
      pageSize: pageSize
    }

    const response = await followApi.getFollowPostList(pageRequest)

    if (response.code === 0) {
      const { records, total } = response.data

      if (isRefresh) {
        postList.value = records || []
        currentPage.value = 1
      } else {
        postList.value.push(...(records || []))
      }

      // 判断是否还有更多数据
      hasMore.value = postList.value.length < total

      if (!isRefresh) {
        currentPage.value++
      }
    } else {
      uni.showToast({
        title: response.message || '加载失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('加载关注帖子失败:', error)
    uni.showToast({
      title: '网络错误，请重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
    if (isRefresh) {
      refresherTriggered.value = false
    }
  }
}
// 下拉刷新
const onRefresh = async () => {
  refresherTriggered.value = true
  await loadFollowPosts(true)
}

// 上拉加载更多
const loadMore = async () => {
  if (!hasMore.value || loading.value) return
  await loadFollowPosts(false)
}

// 处理关注/取消关注
const handleFollow = async (data: any) => {
  try {
    // 更新本地数据
    const post = postList.value.find(p => p.user.id === data.userId)
    if (post) {
      post.hasFollow = data.hasFollow
    }

    uni.showToast({
      title: data.hasFollow ? '关注成功' : '取消关注',
      icon: 'success'
    })
  } catch (error) {
    console.error('关注操作失败:', error)
  }
}

// 处理点赞
const handleLike = (data: any) => {
  const post = postList.value.find(p => p.id === data.postId)
  if (post) {
    post.hasThumb = data.hasThumb
    post.thumbNum = data.thumbNum
  }
}

// 处理评论
const handleComment = (data: any) => {
  // 跳转到帖子详情页
  uni.navigateTo({
    url: `/pages/postDetail/postDetail?id=${data.postId}`
  })
}

// 处理分享
const handleShare = (data: any) => {
  uni.showActionSheet({
    itemList: ['分享给朋友', '分享到朋友圈'],
    success: (res) => {
      if (res.tapIndex === 0) {
        uni.showToast({
          title: '请使用右上角分享',
          icon: 'none'
        })
      } else if (res.tapIndex === 1) {
        uni.showToast({
          title: '请使用右上角分享到朋友圈',
          icon: 'none'
        })
      }
    }
  })
}

// 去发现页面
const goToDiscover = () => {
  uni.reLaunch({
    url: '/pages/layout/layout'
  })
}

// 页面加载
onMounted(() => {
  getSystemInfo()
  loadFollowPosts(true)
})
</script>
<style lang="scss" scoped>
.follow-page {
  min-height: 100vh;
  background: #f8f9fa;
}

// 头部标题栏
.header-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(148, 163, 184, 0.1);
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
  font-size: 32rpx;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.2;
}

.sub-title {
  font-size: 20rpx;
  color: #64748b;
  font-weight: 500;
  margin-top: -4rpx;
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



// 内容区域
.content {
  height: 100vh;
  background: #f8f9fa;
}

// 帖子列表
.post-list {
  padding: 20rpx 20rpx 0;

  :deep(.social-card) {
    margin-bottom: 20rpx;
    border-radius: 12rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
    transition: all 0.2s ease;

    &:active {
      transform: scale(0.98);
    }
  }
}

// 加载状态
.load-status {
  padding: 40rpx 0;
  text-align: center;

  .loading {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16rpx;
    color: #666;
    font-size: 28rpx;
  }

  .load-tip {
    color: #999;
    font-size: 26rpx;
  }

  .no-more {
    color: #ccc;
    font-size: 26rpx;
  }
}

// 加载动画
.loading-spinner {
  width: 32rpx;
  height: 32rpx;
  border: 4rpx solid #f3f3f3;
  border-top: 4rpx solid #007AFF;
  border-radius: 50%;
  animation: spin 1s linear infinite;

  &.big {
    width: 60rpx;
    height: 60rpx;
    border-width: 6rpx;
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

// 空状态
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx 60rpx;
  text-align: center;

  .empty-icon {
    font-size: 120rpx;
    margin-bottom: 40rpx;
    opacity: 0.6;
  }

  .empty-title {
    font-size: 32rpx;
    color: #333;
    font-weight: 500;
    margin-bottom: 16rpx;
  }

  .empty-desc {
    font-size: 28rpx;
    color: #999;
    margin-bottom: 60rpx;
    line-height: 1.5;
  }

  .discover-btn {
    background: #007AFF;
    color: #fff;
    border: none;
    border-radius: 50rpx;
    padding: 24rpx 60rpx;
    font-size: 30rpx;
    font-weight: 500;
    transition: all 0.2s ease;

    &:active {
      transform: scale(0.95);
      background: #0056CC;
    }
  }
}

// 首次加载
.first-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20rpx;
  color: #666;
  font-size: 28rpx;
}

// 底部间距
.bottom-space {
  height: 120rpx;
}
</style>