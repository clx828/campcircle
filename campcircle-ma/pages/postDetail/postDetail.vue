<template>
  <view class="post-detail-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <text v-if="canGoBack" class="back-icon">‹</text>
          <text v-else class="home-icon">⌂</text>
        </view>
        <view class="navbar-title">
          <text>帖子详情</text>
        </view>
        <view class="navbar-right">
          <view class="share-btn" @tap="handleShare">
            <text class="share-icon">⋯</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 页面内容 -->
    <scroll-view
        class="page-content"
        :style="{ paddingTop: navbarHeight + 'px' }"
        scroll-y
        refresher-enabled
        :refresher-triggered="refresherTriggered"
        @refresherrefresh="onRefresh"
    >
      <!-- 帖子详情卡片 -->
      <view class="post-card-container">
        <SocialCard
            v-if="postDetail"
            :cardInfo="postDetail"
            :hideActions="true"
            @follow="handleFollow"
        />
      </view>

      <!-- 评论区 -->
      <view class="comment-section" >
        <!-- 评论区头部 - 用户头像和评论输入框 -->
        <view class="comment-header">
          <view class="user-avatar">
            <image
                :src="userStore.getUserInfo?.userAvatar || '/static/images/default-avatar.png'"
                class="avatar-img"
                mode="aspectFill"
            />
          </view>
          <!-- 评论输入框 -->
          <view class="comment-input-container">
            <CommentInput
                :show="showInput"
                :replyTo="replyInfo.isReply ? { userName: replyInfo.targetUser } : null"
                @close="hideCommentInput"
                @submit="handleCommentSubmit"
            />
          </view>
        </view>

        <!-- 评论列表 -->
        <view class="comment-list-container">
          <CommentList
              :commentList="commentList"
              @reply="handleReply"
          />
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import SocialCard from '@/components/SocialCard.vue'
import CommentInput from '@/components/CommentInput.vue'
import CommentList from '@/components/CommentList.vue'
import { postApi } from '@/api/post'
import { postCommentApi } from '@/api/postComment'
import { useUserStore } from '@/stores/userStore'

// 页面参数
const postId = ref('')
const postDetail = ref(null)
const commentList = ref([])
const commentTotal = ref(0)
const loading = ref(false)

// 导航栏相关
const statusBarHeight = ref(0)
const navbarHeight = ref(88)
const refresherTriggered = ref(false)
const canGoBack = ref(false)  // 是否可以返回上一页

// 评论输入相关
const showInput = ref(false)
const replyInfo = ref({
  isReply: false,
  parentId: '',
  replyId: '',
  targetUser: ''
})

const userStore = useUserStore()

// 格式化时间显示（相对时间）
const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return Math.floor(diff / minute) + '分钟前'
  } else if (diff < day) {
    return Math.floor(diff / hour) + '小时前'
  } else {
    return Math.floor(diff / day) + '天前'
  }
}

// 页面加载时初始化
onMounted(() => {
  // 获取系统信息
  getSystemInfo()

  // 检查是否可以返回上一页
  const pages = getCurrentPages()
  canGoBack.value = pages.length > 1

  // 获取页面参数
  const currentPageInfo = pages[pages.length - 1]
  const options = currentPageInfo.options || {}

  if (options.id) {
    postId.value = options.id
    initPage()
  }
})

// 获取系统信息
const getSystemInfo = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
  navbarHeight.value = statusBarHeight.value + 44
}


// 初始化页面数据
const initPage = async () => {
  await loadPostDetail()
  await loadComments()
}

// 加载帖子详情
const loadPostDetail = async () => {
  try {
    loading.value = true
    const response = await postApi.getPostById(postId.value)
    if (response.code === 0) {
      postDetail.value = response.data
    }
  } catch (error) {
    console.error('加载帖子详情失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 加载评论列表
const loadComments = async () => {
  try {
    loading.value = true

    const response = await postCommentApi.getPostCommentByPage({
      postId: postId.value,
      current: 1,
      pageSize: 20
    })

    if (response.code === 0) {
      const { records, total } = response.data
      commentList.value = records || []
      commentTotal.value = total || 0
    }
  } catch (error) {
    console.error('加载评论失败:', error)
    uni.showToast({
      title: '加载评论失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}



// 处理用户关注状态变化
const handleFollow = (data: any) => {
  if (postDetail.value) {
    postDetail.value.hasFollow = data.hasFollow
  }
}

// 返回上一页或跳转到首页
const goBack = () => {
  if (canGoBack.value) {
    // 有上一页，返回上一页
    uni.navigateBack()
  } else {
    // 没有上一页，跳转到首页
    uni.reLaunch({
      url: '/pages/tabbar/home/home'
    })
  }
}

// 下拉刷新
const onRefresh = async () => {
  refresherTriggered.value = true
  await Promise.all([loadPostDetail(), loadComments()])
  refresherTriggered.value = false
}

// 处理分享
const handleShare = () => {
  uni.showActionSheet({
    itemList: ['分享给朋友', '分享到朋友圈'],
    success: (res) => {
      if (res.tapIndex === 0) {
        // 分享给朋友
        uni.showToast({
          title: '请使用右上角分享',
          icon: 'none'
        })
      } else if (res.tapIndex === 1) {
        // 分享到朋友圈
        uni.showToast({
          title: '请使用右上角分享到朋友圈',
          icon: 'none'
        })
      }
    }
  })
}

// 显示评论输入框
const showCommentInput = () => {
  replyInfo.value = {
    isReply: false,
    parentId: '',
    replyId: '',
    targetUser: ''
  }
  showInput.value = true
}

// 隐藏评论输入框
const hideCommentInput = () => {
  showInput.value = false
  replyInfo.value = {
    isReply: false,
    parentId: '',
    replyId: '',
    targetUser: ''
  }
}

// 处理回复评论（来自 CommentList 组件的事件）
const handleReply = (comment: any, replyTo?: any) => {
  replyInfo.value = {
    isReply: true,
    parentId: comment.id,
    replyId: replyTo ? replyTo.id : '',
    targetUser: replyTo ? replyTo.user?.userName : comment.user?.userName || '匿名用户'
  }
  showInput.value = true
}

// 处理评论提交（来自 CommentInput 组件的事件）
const handleCommentSubmit = async (data: { content: string; isAnonymous: boolean }) => {
  if (!postId.value) return

  try {
    uni.showLoading({ title: '发布中...', mask: true })

    const params = {
      postId: postId.value,
      content: data.content.trim(),
      // 如果是回复评论，使用 parentId
      parentId: replyInfo.value.parentId || undefined,
      // 如果是回复评论，使用被回复用户的 ID
      replyUserId: replyInfo.value.replyId || undefined
    }

    const response = await postCommentApi.addComment(params)

    if (response.code === 0) {
      uni.hideLoading()
      uni.showToast({
        title: '发布成功',
        icon: 'success'
      })

      hideCommentInput()
      // 刷新评论列表
      await loadComments()
    }
  } catch (error) {
    console.error('发布评论失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: '发布失败',
      icon: 'none'
    })
  }
}
</script>

<style lang="scss" scoped>
.post-detail-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  position: relative;
}

// 自定义导航栏
.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: #fff;
  z-index: 1000;
  border-bottom: 1rpx solid #f0f0f0;

  .navbar-content {
    height: 88rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 32rpx;

    .navbar-left {
      width: 80rpx;
      height: 80rpx;
      display: flex;
      align-items: center;
      justify-content: center;

      .back-icon {
        font-size: 48rpx;
        color: #333;
        font-weight: bold;
      }

      .home-icon {
        font-size: 40rpx;
        color: #333;
        font-weight: bold;
      }
    }

    .navbar-title {
      flex: 1;
      text-align: center;

      text {
        font-size: 32rpx;
        font-weight: 600;
        color: #333;
      }
    }

    .navbar-right {
      width: 80rpx;
      height: 80rpx;
      display: flex;
      align-items: center;
      justify-content: center;

      .share-btn {
        width: 60rpx;
        height: 60rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        background: #f8f9fa;

        .share-icon {
          font-size: 32rpx;
          color: #666;
          font-weight: bold;
        }

        &:active {
          background: #e9ecef;
        }
      }
    }
  }
}

// 页面内容
.page-content {
  height: 100vh;
  background-color: #f5f5f5;
  overflow: hidden;
  box-sizing: border-box;
}

.post-card-container {
  background: #fff;
  flex-shrink: 0;
}

.loading {
  text-align: center;
  padding: 80rpx;
  font-size: 28rpx;
  color: #999;
}

.comment-section {
  background: #fff;
  margin-top: 20rpx;
  display: flex;
  flex-direction: column;
  max-height: 70vh;
  overflow: hidden;
}

.comment-header {
  display: flex;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
  flex-shrink: 0;
  gap: 24rpx;

  .user-avatar {
    flex-shrink: 0;

    .avatar-img {
      width: 72rpx;
      height: 72rpx;
      border-radius: 50%;
      background-color: #f5f5f5;
    }
  }

  .comment-input-container {
    flex: 1;
  }
}

.comment-list-container {
  flex: none;
  max-height: 60vh;
  overflow-y: auto;
}

.load-more {
  text-align: center;
  padding: 40rpx;
  font-size: 28rpx;
  color: #007aff;
}

.end-tip {
  text-align: center;
  padding: 40rpx;

  .end-text {
    font-size: 24rpx;
    color: #999;
  }
}
</style>