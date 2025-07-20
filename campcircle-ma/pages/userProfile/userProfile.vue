<template>
  <!-- 固定在顶部的头像，不影响下面布局 -->
  <view v-if="!isAtTop" class="fixed-top-header" :style="headerStyle">
    <image :src="userInfo.userAvatar" class="fixed-avatar" :style="avatarStyle"></image>
  </view>

  <scroll-view class="profile-page" scroll-y="true" @scroll="onScrollThrottled" @scrolltolower="loadMore"
    refresher-enabled="true" :refresher-triggered="refresherTriggered" @refresherrefresh="onRefresh"
    refresher-background="#ffffff">
    <!-- 用户信息区域 -->
    <view class="user-header">
      <view class="user-header__overlay">
        <!-- 返回按钮 -->
        <view class="back-button" @click="goBack">
          <image src="../../static/img/tabbar/home.png" class="back-icon" />
        </view>

        <!-- 用户头像和基本信息 -->
        <view class="user-basic-info">
          <image :src="userInfo.userAvatar" class="user-avatar" />
          <view class="user-text-info">
            <view class="user-nickname">{{ userInfo.userName }}</view>
            <view class="user-id">UID: {{ userInfo.id }}</view>
          </view>
        </view>

        <!-- 用户简介 -->
        <view class="user-profile">
          {{ userInfo.userProfile || '这个人很懒，还没有简介' }}
        </view>

        <!-- 统计数据和操作按钮 -->
        <view class="user-stats-actions">
          <wd-row>
            <!-- 统计数据 -->
            <wd-col :span="4">
              <view class="stat-item">
                <view class="stat-number">{{ followNum }}</view>
                <view class="stat-label">关注</view>
              </view>
            </wd-col>
            <wd-col :span="4">
              <view class="stat-item">
                <view class="stat-number">{{ fansNum }}</view>
                <view class="stat-label">粉丝</view>
              </view>
            </wd-col>
            <wd-col :span="4">
              <view class="stat-item">
                <view class="stat-number">{{ thumbNum }}</view>
                <view class="stat-label">获赞</view>
              </view>
            </wd-col>

            <!-- 操作按钮 -->
            <wd-col :span="8" :offset="4">
              <view class="action-buttons">
                <wd-button 
                  :type="isFollowed ? 'default' : 'primary'" 
                  size="small" 
                  class="follow-btn" 
                  @click="handleFollow"
                  :loading="followLoading"
                >
                  {{ isFollowed ? '已关注' : '关注' }}
                </wd-button>
                <wd-button plain hairline size="small" type="info" class="message-btn" @click="sendMessage">
                  私信
                </wd-button>
              </view>
            </wd-col>
          </wd-row>
        </view>
      </view>
    </view>

    <!-- 内容区域 -->
    <view class="content-section">
      <view class="content-tabs">
        <wd-tabs v-model="tab">
          <wd-tab :title="`笔记 ${postNum}`">
          </wd-tab>
        </wd-tabs>
      </view>
      <view class="tab-content" :class="{ 'tab-content--empty': cardList.length === 0 }">
        <SocialCard
          :cardInfo="item"
          v-for="(item, i) in cardList"
          :key="i"
          :hideActions="false"
          @comment="handleComment"
          @share="handleShare"
        />
        <EmptyState text="暂无内容" v-if="cardList.length === 0 && !loading" />
        <view v-if="loading" class="loading-state">
          <wd-loading />
        </view>
        <view v-if="!hasMore && cardList.length > 0" class="no-more-state">
          <text class="no-more-text">没有更多了</text>
        </view>
      </view>
    </view>

  </scroll-view>

  <!-- 全局评论弹窗 -->
  <CommentPopup
      v-model:show="showCommentPopup"
      :comment-num="commentNum"
      :post-id="currentPostId"
      @close="handleCommentPopupClose"
      @comment-success="handleCommentSuccess"
  />
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, watch, computed } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { IUser } from '@/model/user'
import SocialCard from '@/components/SocialCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import CommentPopup from '@/components/CommentPopup.vue'
import { postApi } from '@/api/post'
import { followApi } from '@/api/follow'
import { userApi } from '@/api/user'
import { onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'

// 获取页面参数
const pages = getCurrentPages()
const currentPage = pages[pages.length - 1]
const userId = ref<number>(0)

onMounted(() => {
  // 从页面参数获取用户ID
  const options = currentPage.options || {}
  userId.value = Number(options.id || options.userId || 0)
  
  if (userId.value <= 0) {
    uni.showToast({
      title: '用户ID无效',
      icon: 'error'
    })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
    return
  }
  
  // 加载用户信息和数据
  loadUserData()
})

// ===== 响应式数据 =====
const tab = ref<number>(0)
const cardList = ref<any[]>([])
const loading = ref(false)
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
const hasMore = ref(true)
const postNum = ref(0)
const followNum = ref(0)
const fansNum = ref(0)
const thumbNum = ref(0)
const isFollowed = ref(false)
const followLoading = ref(false)

// 评论弹窗相关
const showCommentPopup = ref(false)
const currentPostId = ref('')
const commentNum = ref(0)

// 用户信息
let userInfo: IUser = reactive({
  id: 0,
  userName: '加载中...',
  userAvatar: '',
  userProfile: '',
  userRole: 'user',
})

// 加载用户数据
const loadUserData = async () => {
  try {
    await Promise.all([
      fetchUserInfo(),
      fetchUserStats(),
      fetchFollowStatus(),
      fetchUserPosts()
    ])
  } catch (error) {
    console.error('加载用户数据失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'error'
    })
  }
}

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const res = await userApi.getUserVOById(userId.value)
    if (res.code === 0 && res.data) {
      Object.assign(userInfo, res.data)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 获取用户统计数据
const fetchUserStats = async () => {
  try {
    const res = await followApi.getUserFollowNum(userId.value)
    if (res.code === 0 && res.data) {
      followNum.value = Number(res.data.followNum || 0)
      fansNum.value = Number(res.data.fansNum || 0)
      thumbNum.value = Number(res.data.thumbNum || 0)
    }
  } catch (error) {
    console.error('获取用户统计数据失败:', error)
  }
}

// 检查关注状态
const fetchFollowStatus = async () => {
  try {
    const res = await followApi.checkFollowStatus(userId.value)
    if (res.code === 0) {
      isFollowed.value = res.data === true
    }
  } catch (error) {
    console.error('检查关注状态失败:', error)
  }
}

// 获取用户帖子列表
const fetchUserPosts = async (isLoadMore = false) => {
  try {
    loading.value = true
    const res = await postApi.listPostVOByPage({
      userId: userId.value,
      current: current.value,
      pageSize: pageSize.value
    })
    if (res.code === 0) {
      if (isLoadMore) {
        cardList.value = [...cardList.value, ...res.data.records]
      } else {
        cardList.value = res.data.records
      }
      total.value = res.data.total
      postNum.value = res.data.total
      hasMore.value = cardList.value.length < res.data.total
    }
  } catch (error) {
    console.error('获取用户帖子失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理关注/取消关注
const handleFollow = async () => {
  try {
    followLoading.value = true
    const res = await followApi.doFollow(userId.value.toString())
    if (res.code === 0) {
      isFollowed.value = !isFollowed.value
      // 更新粉丝数
      if (isFollowed.value) {
        fansNum.value++
        uni.showToast({
          title: '关注成功',
          icon: 'success'
        })
      } else {
        fansNum.value--
        uni.showToast({
          title: '取消关注',
          icon: 'success'
        })
      }
    }
  } catch (error) {
    console.error('关注操作失败:', error)
    uni.showToast({
      title: '操作失败',
      icon: 'error'
    })
  } finally {
    followLoading.value = false
  }
}

// 发送私信
const sendMessage = () => {
  const chatUserParam = encodeURIComponent(JSON.stringify({
    id: userInfo.id,
    userName: userInfo.userName,
    userAvatar: userInfo.userAvatar,
    userProfile: userInfo.userProfile
  }))

  uni.navigateTo({
    url: `/pages/chatDetail/chatDetail?chatUser=${chatUserParam}&chatUserId=${userId.value}`
  })
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 加载更多数据
const loadMore = async () => {
  if (loading.value || !hasMore.value) return
  current.value++
  await fetchUserPosts(true)
}

// 下拉刷新
const refresherTriggered = ref(false)
const onRefresh = async () => {
  refresherTriggered.value = true
  try {
    current.value = 1
    hasMore.value = true
    await loadUserData()
  } catch (error) {
    console.error('刷新失败:', error)
  } finally {
    refresherTriggered.value = false
  }
}

// ===== 滚动监听相关 =====
const isAtTop = ref(true)
const scrollTop = ref(0)

const scrollProgress = computed(() => {
  const startFade = 50
  const endFade = 200
  if (scrollTop.value <= startFade) {
    return 0
  } else if (scrollTop.value >= endFade) {
    return 1
  } else {
    return (scrollTop.value - startFade) / (endFade - startFade)
  }
})

const headerStyle = computed(() => {
  const opacity = scrollProgress.value * 0.95
  return {
    backgroundColor: `rgba(0, 0, 0, ${opacity})`,
    transition: scrollTop.value === 0 ? 'background-color 0.3s ease' : 'none'
  }
})

const avatarStyle = computed(() => {
  const translateY = (1 - scrollProgress.value) * 30
  const opacity = scrollProgress.value
  return {
    transform: `translateY(${translateY}px)`,
    opacity: opacity,
    transition: scrollTop.value === 0 ? 'all 0.3s ease' : 'none'
  }
})

const throttle = (func: Function, delay: number) => {
  let timeoutId: NodeJS.Timeout | null = null
  let lastExecTime = 0
  return function (...args: any[]) {
    const currentTime = Date.now()
    if (currentTime - lastExecTime > delay) {
      func.apply(this, args)
      lastExecTime = currentTime
    } else {
      if (timeoutId) clearTimeout(timeoutId)
      timeoutId = setTimeout(() => {
        func.apply(this, args)
        lastExecTime = Date.now()
      }, delay - (currentTime - lastExecTime))
    }
  }
}

const onScroll = (e: any) => {
  const { scrollTop: currentScrollTop } = e.detail
  scrollTop.value = currentScrollTop
  isAtTop.value = currentScrollTop <= 50
}

const onScrollThrottled = throttle(onScroll, 16)

// 处理评论按钮点击
function handleComment(postId: string, postCommentNum: number) {
  currentPostId.value = postId
  commentNum.value = postCommentNum
  showCommentPopup.value = true
}

function handleCommentPopupClose() {
  showCommentPopup.value = false
  currentPostId.value = ''
}

function handleCommentSuccess() {
  showCommentPopup.value = false
  // 刷新帖子列表
  current.value = 1
  fetchUserPosts()
}

// 处理分享
function handleShare(post: any) {
  uni.showActionSheet({
    itemList: ['分享给朋友', '分享到朋友圈', '复制链接'],
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
      } else if (res.tapIndex === 2) {
        const shareUrl = `pages/postDetail/postDetail?id=${post.id}`
        uni.setClipboardData({
          data: shareUrl,
          success: () => {
            uni.showToast({
              title: '链接已复制',
              icon: 'success'
            })
          }
        })
      }
    }
  })
}

// 配置小程序分享功能
onShareAppMessage(() => {
  return {
    title: `${userInfo.userName}的个人主页 - CampCircle`,
    path: `/pages/userProfile/userProfile?id=${userId.value}`,
    imageUrl: userInfo.userAvatar || 'https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-06-13_12f2e457-9cae-4ffa-a149-1f480ddc221d.png'
  }
})

onShareTimeline(() => {
  return {
    title: `${userInfo.userName}的校园生活 - CampCircle`,
    query: `id=${userId.value}&from=timeline`,
    imageUrl: userInfo.userAvatar || 'https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-06-13_12f2e457-9cae-4ffa-a149-1f480ddc221d.png'
  }
})
</script>

<style lang="scss">
// ===== 固定顶部头像 =====
.fixed-top-header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 10vh;
  z-index: 999;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);

  .fixed-avatar {
    margin-top: 15px;
    width: 30px;
    height: 30px;
    border-radius: 50%;
    border: 2px solid rgba(255, 255, 255, 0.8);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  }
}

// ===== 主容器 =====
.profile-page {
  height: 100vh;
  background-color: #ffffff;
  overflow: scroll;
}

// ===== 用户头部区域 =====
.user-header {
  min-height: 40vh;
  background-image: url("https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/public/1898735003367223297/2025-05-11_2f217692-cb17-4c64-8bcb-8afd1ed14b5a.webp");
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
  padding-top: var(--status-bar-height, 44px);
  box-sizing: border-box;
  overflow: hidden;

  &::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(to bottom,
        rgba(0, 0, 0, 0.1),
        rgba(0, 0, 0, 0.3) 50%,
        rgba(0, 0, 0, 0.8));
    pointer-events: none;
    z-index: 1;
  }

  &__overlay {
    position: relative;
    z-index: 2;
    height: 100%;
    padding: 0 15px;
    padding-top: 80px;
  }
}

// ===== 返回按钮 =====
.back-button {
  position: absolute;
  top: 20px;
  left: 15px;
  width: 40px;
  height: 40px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);

  .back-icon {
    width: 20px;
    height: 20px;
    filter: brightness(0) invert(1);
  }
}

// ===== 用户基本信息 =====
.user-basic-info {
  display: flex;
  align-items: center;
  margin-top: 40px;
  margin-bottom: 20px;

  .user-avatar {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    border: 3px solid rgba(255, 255, 255, 0.8);
  }

  .user-text-info {
    margin-left: 15px;
    flex: 1;
  }

  .user-nickname {
    font-size: 18px;
    font-weight: bold;
    color: #ffffff;
    margin-bottom: 10px;
  }

  .user-id {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.7);
  }
}

// ===== 用户简介 =====
.user-profile {
  font-size: 14px;
  color: #ffffff;
  margin-bottom: 25px;
  line-height: 1.4;
}

// ===== 统计数据和操作区域 =====
.user-stats-actions {
  margin-bottom: 30px;
  margin-left: -20px;

  :deep(.wd-col) {
    display: flex;
    justify-content: center;
    word-break: break-word;
  }
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  color: #ffffff;

  .stat-number {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 4px;
  }

  .stat-label {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.7);
  }
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;

  .follow-btn, .message-btn {
    min-width: 60px;
  }
}

// ===== 内容区域 =====
.content-section {
  border-top-left-radius: 15px;
  border-top-right-radius: 15px;
  padding-top: 8px;
  background: #fff;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
  margin-top: -3vh;
  position: relative;
  z-index: 2;
}

.content-tabs {
  position: sticky;
  top: 10vh;
  z-index: 10;
  margin-top: 5px;
  background: #fff;
  border-top-left-radius: 15px;
  border-top-right-radius: 15px;
}

// ===== 标签页内容 =====
.tab-content {
  padding: 20rpx;
  padding-top: -7px;
  min-height: 200rpx;
  background-color: #f1f1f1;

  &--empty {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 60vh;
  }
}

.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20rpx;
}

.no-more-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20rpx;
}

.no-more-text {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.5);
}
</style>
