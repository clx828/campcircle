<template>
  <view class="container">
    <!-- 固定在顶部的搜索容器 -->
    <view class="search-container" :style="headerStyle">
      <view class="logo" :style="logoStyle">
        <image
          src="https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-06-13_12f2e457-9cae-4ffa-a149-1f480ddc221d.png" />
      </view>
      <view class="search-bar" :style="searchBarStyle">
        <image src="/static/button/shousuo.png" mode="aspectFit" />
        <span>搜索</span>
      </view>
      <view class="placeholder"></view>
    </view>

    <!-- 可滚动的内容区域 -->
    <scroll-view scroll-y class="scroll-view" refresher-enabled :refresher-triggered="refresherTriggered"
      @refresherrefresh="onRefresh" @scrolltolower="onScrollToLower"
      :style="{ height: `calc(100vh - ${menuButtonHeight + statusBarHeight}px - 60px)` }">
      <view class="swiper-container" style="background: #FFFFFF; width: 100%;">
        <wd-swiper :list="swiperList" autoplay v-model:current="current" :indicator="{ type: 'dots-bar' }"
          @click="handleClick"></wd-swiper>
      </view>
      <view class="post-list">
        <SocialCard v-for="post in postList" :key="post.id" :cardInfo="post" @like="handleLike" @collect="handleCollect"
          @comment="handleComment" @share="handleShare" @follow="handleFollow" />
      </view>
      <view v-if="postList.length === 0 && !postLoading" class="empty-tip">
        <text>暂无动态</text>
      </view>
      <view v-if="postLoading" class="loading-more">
        <text>加载中...</text>
      </view>
      <view v-else-if="hasMore" class="loading-more">
        <text>上拉加载更多</text>
      </view>
      <view v-else-if="postList.length > 0" class="no-more">
        <text>—— THE END ——</text>
      </view>
      <!-- 底部占位，避免被tabbar遮挡 -->
      <view class="bottom-space"></view>
    </scroll-view>

    <!-- 评论弹窗 -->
    <CommentPopup v-model:show="showCommentPopup" :post-id="currentPostId" @close="handleCommentPopupClose"
      @comment-success="handleCommentSuccess" />
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import SocialCard from '@/components/SocialCard.vue'
import { postApi } from '@/api/post'
import type { ListPostVOByPageParams } from '@/api/post'
import CommentInput from '@/components/CommentInput.vue'
import CommentList from '@/components/CommentList.vue'
import { postCommentApi } from '@/api/postComment'
import CommentPopup from '@/components/CommentPopup.vue'

// 帖子列表数据
const postList = ref<any[]>([])
const current = ref(1)
const pageSize = ref(10)
const refresherTriggered = ref(false)
const hasMore = ref(true)
const postLoading = ref(false) // 帖子加载状态

// 胶囊按钮和状态栏信息
const menuButtonHeight = ref(32)
const menuButtonTop = ref(0)
const statusBarHeight = ref(0)
const searchBarWidth = ref(0)

// 计算 header 样式
const headerStyle = computed(() => {
  return {
    paddingTop: `${statusBarHeight.value}px`,
    height: `${menuButtonTop.value + menuButtonHeight.value - statusBarHeight.value}px`,
    position: 'relative'
  }
})

// 计算搜索框样式
const searchBarStyle = computed(() => {
  // 计算搜索框的顶部位置，使其底部与胶囊按钮底部对齐
  const searchBarTop = menuButtonTop.value + menuButtonHeight.value - menuButtonHeight.value
  return {
    width: `${searchBarWidth.value}px`,
    height: `${menuButtonHeight.value}px`,
    position: 'absolute',
    top: `${searchBarTop}px`,
    left: '100rpx' // logo宽度 + 间距
  }
})

// 计算 logo 样式
const logoStyle = computed(() => {
  // 使用与搜索框相同的计算方式
  const logoTop = menuButtonTop.value + menuButtonHeight.value - menuButtonHeight.value
  return {
    position: 'absolute',
    top: `${logoTop}px`,
    left: '20rpx',
    height: `${menuButtonHeight.value}px`,
    width: `${menuButtonHeight.value}px`
  }
})

//轮播图
const swiperList = ref([
  'https://registry.npmmirror.com/wot-design-uni-assets/*/files/redpanda.jpg',
  'https://registry.npmmirror.com/wot-design-uni-assets/*/files/capybara.jpg',
  'https://registry.npmmirror.com/wot-design-uni-assets/*/files/panda.jpg',
  'https://registry.npmmirror.com/wot-design-uni-assets/*/files/moon.jpg',
  'https://registry.npmmirror.com/wot-design-uni-assets/*/files/meng.jpg'
])
function handleClick(e) {
  console.log(e)
}
function onChange(e) {
  console.log(e)
}
// 获取系统信息
const getSystemInfo = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0

  // #ifdef MP-WEIXIN
  const menuButtonInfo = uni.getMenuButtonBoundingClientRect()
  if (menuButtonInfo) {
    menuButtonHeight.value = menuButtonInfo.height
    menuButtonTop.value = menuButtonInfo.top
    // 计算搜索框宽度：屏幕宽度 - 胶囊按钮宽度 - 左侧logo宽度 - 间距
    searchBarWidth.value = systemInfo.windowWidth - menuButtonInfo.width - 80 - 20
  }
  // #endif
}

// 跳转到搜索页面
const goToSearch = () => {
  uni.navigateTo({
    url: '/pages/search/search'
  })
}

// 加载帖子列表
const loadPosts = async (isRefresh = false) => {
  if (postLoading.value) return // 防止重复加载

  try {
    postLoading.value = true
    const params: ListPostVOByPageParams = {
      current: current.value,
      pageSize: pageSize.value
    }
    const res = await postApi.listPostVOByPage(params)
    if (res.code === 0 && res.data) {
      const newPosts = res.data.records || []
      if (isRefresh) {
        postList.value = newPosts
      } else {
        postList.value = [...postList.value, ...newPosts]
      }
      // 更新 hasMore 状态
      hasMore.value = newPosts.length === pageSize.value
    }
  } catch (error) {
    uni.showToast({
      title: '加载失败',
      icon: 'error'
    })
  } finally {
    postLoading.value = false
  }
}

// 滚动到底部时加载更多帖子
const onScrollToLower = () => {
  if (hasMore.value && !postLoading.value) {
    current.value++
    loadPosts()
  }
}

// 下拉刷新
const onRefresh = async () => {
  refresherTriggered.value = true
  current.value = 1
  await loadPosts(true)
  refresherTriggered.value = false
}

// 处理喜欢
function handleLike(data: { id: string; hasThumb: boolean; isRollback: boolean }) {
  // 更新本地数据
  console.log("点赞了", data)
  const post = postList.value.find(p => p.id === data.id)
  if (post) {
    post.hasThumb = data.hasThumb
    // 如果是回滚，需要恢复原来的计数
    if (data.isRollback) {
      post.thumbNum = data.hasThumb ? post.thumbNum + 1 : post.thumbNum - 1
    } else {
      post.thumbNum += data.hasThumb ? 1 : -1
    }
  }
}

// 处理收藏
function handleCollect(data: { id: string; hasFavour: boolean; isRollback: boolean }) {
  // 更新本地数据
  const post = postList.value.find(p => p.id === data.id)
  if (post) {
    post.hasFavour = data.hasFavour
    // 如果是回滚，需要恢复原来的计数
    if (data.isRollback) {
      post.favourNum = data.hasFavour ? post.favourNum - 1 : post.favourNum + 1
    } else {
      post.favourNum += data.hasFavour ? 1 : -1
    }
  }
}

// 评论相关
const showCommentPopup = ref(false)
const currentPostId = ref('')

// 处理评论按钮点击
function handleComment(postId: string) {
  currentPostId.value = postId
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
  loadPosts()
}

// 处理分享
function handleShare() {
  uni.showShareMenu({
    withShareTicket: true,
    menus: ['shareAppMessage', 'shareTimeline']
  })
}

// 处理关注
function handleFollow(data: { id: string; hasFollow: boolean; isRollback: boolean }) {
  const post = postList.value.find(p => p.user.id === data.id)
  if (post) {
    post.hasFollow = data.hasFollow
  }
}

onMounted(() => {
  getSystemInfo()
  loadPosts()
})
</script>

<style lang="scss" scoped>
.container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden; // 防止页面整体滚动
}

.search-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: #fff;
  z-index: 100;
  padding: 0 20rpx;
  padding-bottom: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .logo {
    border-radius: 50%;
    overflow: hidden;

    image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .search-bar {
    background: #f5f5f5;
    border-radius: 36rpx;
    display: flex;
    align-items: center;
    padding: 0 24rpx;

    image {
      width: 32rpx;
      height: 32rpx;
      margin-right: 12rpx;
    }

    span {
      color: #9c9c9c;
      font-size: 28rpx;
    }
  }

  .placeholder {
    width: 60rpx;
  }
}

.scroll-view {
  flex: 1;
  width: 100%;
  box-sizing: border-box;
}

.swiper-container {
  padding: 0 20rpx;
}

.post-list {
  padding: 10rpx 20rpx;
  padding-bottom: 30px;
  background-color: #fafafa;
}

.empty-tip {
  text-align: center;
  color: #999;
  font-size: 28rpx;
  padding: 40rpx 0;
}

.loading-more {
  text-align: center;
  color: #666;
  font-size: 28rpx;
  padding: 20rpx 0;
  background: #f8f9fa;
  margin: 10rpx 20rpx;
  border-radius: 12rpx;
}

.no-more {
  text-align: center;
  color: #999;
  font-size: 28rpx;
  padding: 20rpx 0;
}

.comment-popup {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
  border-top-left-radius: 20rpx;
  border-top-right-radius: 20rpx;
  position: relative;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  border-bottom: 1px solid #eee;
  background: #fff;
  flex-shrink: 0;
  z-index: 1;

  .title {
    font-size: 32rpx;
    font-weight: bold;
  }
}

.comment-container {
  flex: 1;
  overflow: hidden;
  position: relative;
}

.comment-scroll {
  height: 100%;
  width: 100%;
}

.comment-input-wrapper {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  border-top: 1px solid #eee;
  padding: 20rpx;
  z-index: 1;
}

.empty-comment {
  text-align: center;
  color: #999;
  font-size: 28rpx;
  padding: 60rpx 0;
}

.loading-comment {
  text-align: center;
  color: #666;
  font-size: 28rpx;
  padding: 20rpx 0;
}

.load-more-comment {
  text-align: center;
  color: #666;
  font-size: 26rpx;
  padding: 15rpx 0;
  background: #f8f9fa;
  margin: 10rpx 20rpx;
  border-radius: 8rpx;
}

.no-more-comment {
  text-align: center;
  color: #999;
  font-size: 26rpx;
  padding: 15rpx 0;
}

.comment-bottom-space {
  height: 100rpx; // 底部占位空间，避免被输入框遮挡
}

.comment-list {
  padding: 20rpx;
}

.comment-item {
  margin-bottom: 30rpx;

  .comment-user {
    display: flex;
    align-items: center;
    margin-bottom: 16rpx;

    .avatar {
      width: 64rpx;
      height: 64rpx;
      border-radius: 50%;
      margin-right: 16rpx;
    }

    .user-info {
      flex: 1;

      .username {
        font-size: 28rpx;
        color: #333;
        font-weight: 500;
      }

      .time {
        font-size: 24rpx;
        color: #999;
        margin-left: 16rpx;
      }
    }
  }

  .comment-content {
    font-size: 28rpx;
    color: #333;
    line-height: 1.5;
    margin-left: 80rpx;
  }

  .reply-list {
    margin-left: 80rpx;
    margin-top: 16rpx;
    background: #f8f9fa;
    border-radius: 12rpx;
    padding: 16rpx;

    .reply-item {
      margin-bottom: 16rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .reply-user {
        display: flex;
        align-items: center;
        margin-bottom: 8rpx;

        .avatar {
          width: 48rpx;
          height: 48rpx;
          border-radius: 50%;
          margin-right: 12rpx;
        }

        .user-info {
          flex: 1;
          display: flex;
          align-items: center;
          flex-wrap: wrap;

          .username {
            font-size: 26rpx;
            color: #333;
          }

          .reply-to {
            font-size: 26rpx;
            color: #999;
            margin: 0 8rpx;
          }

          .time {
            font-size: 24rpx;
            color: #999;
            margin-left: 16rpx;
          }
        }
      }

      .reply-content {
        font-size: 26rpx;
        color: #333;
        line-height: 1.5;
        margin-left: 60rpx;
      }
    }
  }
}

.bottom-space {
  height: 120rpx; // 为底部tabbar预留空间
  width: 100%;
}
</style>