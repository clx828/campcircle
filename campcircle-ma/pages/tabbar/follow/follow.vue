<template>
  <view class="follow-page">
    <!-- 头部标题栏 -->
    <view class="header-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="title-section">
          <text class="main-title">关注</text>
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
          :ref="(el) => setSocialCardRef(el, post.id)"
          @comment="handleComment"
          @share="handleShare"
          @edit="handleEdit"
          @delete="handleDelete"
          @menuToggle="handleMenuToggle"
          @visibilityChange="handleVisibilityChange"
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

    <!-- 全局评论弹窗 -->
    <CommentPopup
        v-model:show="showCommentPopup"
        :comment-num="commentNum"
        :post-id="currentPostId"
        @close="handleCommentPopupClose"
        @comment-success="handleCommentSuccess"
    />

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
import CommentPopup from '@/components/CommentPopup.vue'
import { followApi, pageRequest } from '@/api/follow'
import { postApi } from '@/api/post'
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

// 评论弹窗相关
const showCommentPopup = ref(false)
const currentPostId = ref('')
const commentNum = ref(0)
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



// 处理评论按钮点击
function handleComment(postId: string, postCommentNum: number) {
  currentPostId.value = postId
  commentNum.value = postCommentNum
  showCommentPopup.value = true
}

// 处理评论弹窗关闭
function handleCommentPopupClose() {
  showCommentPopup.value = false
  currentPostId.value = ''
}

// 处理评论成功
function handleCommentSuccess() {
  // 刷新帖子列表
  loadFollowPosts(true)
  showCommentPopup.value = false
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

// SocialCard 组件引用管理
const socialCardRefs = ref(new Map())

// 设置 SocialCard 组件引用
function setSocialCardRef(el, postId) {
  if (el) {
    socialCardRefs.value.set(postId, el)
  } else {
    socialCardRefs.value.delete(postId)
  }
}

// 处理菜单切换 - 确保一次只能打开一个菜单
function handleMenuToggle(currentPostId) {
  // 关闭所有其他菜单
  socialCardRefs.value.forEach((cardRef, postId) => {
    if (postId !== currentPostId && cardRef && cardRef.closeEditMenu) {
      cardRef.closeEditMenu()
    }
  })
}

// 处理可见性变更
async function handleVisibilityChange(postId, action) {
  console.log(`帖子 ${postId} 执行操作: ${action}`)

  try {
    if (action === 'public' || action === 'private') {
      // 调用后端API更新帖子的可见性
      const updateParams = {
        id: postId,
        isPublic: action === 'public' ? 1 : 0
      }

      const response = await postApi.updatePost(updateParams)

      if (response.code === 0) {
        uni.showToast({
          title: action === 'public' ? '已设为公开' : '已设为私密',
          icon: 'success'
        })
      } else {
        // API调用成功但业务失败，回滚本地状态
        const targetPost = postList.value.find(post => post.id === postId)
        if (targetPost) {
          targetPost.isPublic = action === 'public' ? 0 : 1
        }
        uni.showToast({
          title: response.message || '操作失败',
          icon: 'error'
        })
      }
    } else if (action === 'top') {
      // 置顶功能暂时显示提示
      uni.showToast({
        title: '置顶功能开发中',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('更新帖子状态失败:', error)

    // API调用失败，回滚本地状态
    const targetPost = postList.value.find(post => post.id === postId)
    if (targetPost && (action === 'public' || action === 'private')) {
      targetPost.isPublic = action === 'public' ? 0 : 1
    }

    uni.showToast({
      title: '网络错误，请重试',
      icon: 'error'
    })
  }
}

// 处理编辑事件
function handleEdit(postId) {
  console.log('编辑帖子:', postId)
  // 跳转到编辑页面
  uni.navigateTo({
    url: `/pages/editPost/editPost?id=${postId}`
  })
}

// 处理删除事件
function handleDelete(postId) {
  console.log('删除帖子:', postId)
  // 从列表中移除已删除的帖子
  const index = postList.value.findIndex(post => post.id === postId)
  if (index !== -1) {
    postList.value.splice(index, 1)
  }
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