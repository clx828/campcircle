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
      @refresherrefresh="onRefresh" :style="{ height: `calc(100vh - ${menuButtonHeight + statusBarHeight}px - 60px)` }">
      <view class="swiper-container" style="background: #FFFFFF; width: 100%;">
        <wd-swiper :list="swiperList" autoplay v-model:current="current" :indicator="{ type: 'dots-bar' }"
          @click="handleClick"></wd-swiper>
      </view>
      <view class="post-list">
        <SocialCard v-for="post in postList" :key="post.id" :cardInfo="post" @like="handleLike" @collect="handleCollect"
          @comment="handleComment" @share="handleShare" @follow="handleFollow" />
      </view>
      <view v-if="postList.length === 0" class="empty-tip">
        <text>暂无动态</text>
      </view>
      <view v-if="hasMore" class="loading-more">
        <text>加载更多...</text>
      </view>
      <view v-else class="no-more">
        <text>没有更多了</text>
      </view>
    </scroll-view>

    <!-- 评论弹窗 -->
    <wd-popup :z-index="9999" v-model="showCommentPopup" position="bottom" closable custom-style="height: 500px;"
      @close="handleCommentClose">
      <view class="comment-popup">
        <view class="comment-header">
          <text class="title">评论</text>
        </view>
        <view class="comment-content">
          <!-- 评论列表 -->
        </view>
        <view class="comment-input">
          <CommentInput placeholder="说点什么..." @submit="handleSubmit" @close="handleClose" />
        </view>
      </view>
    </wd-popup>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import SocialCard from '@/components/SocialCard.vue'
import { postApi } from '@/api/post'
import type { ListPostVOByPageParams } from '@/api/post'
import CommentInput from '@/components/CommentInput.vue'

// 帖子列表数据
const postList = ref<any[]>([])
const current = ref(1)
const pageSize = ref(10)
const refresherTriggered = ref(false)
const hasMore = ref(true)

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
  try {
    const params: ListPostVOByPageParams = {
      current: current.value,
      pageSize: pageSize.value
    }
    const res = await postApi.listPostVOByPage(params)
    if (res.code === 0 && res.data) {
      if (isRefresh) {
        postList.value = res.data.records
      } else {
        postList.value = [...postList.value, ...res.data.records]
      }
      // 更新 hasMore 状态
      hasMore.value = res.data.records.length === pageSize.value
    }
  } catch (error) {
    uni.showToast({
      title: '加载失败',
      icon: 'error'
    })
  }
}

// 加载更多
const loadMore = () => {
  current.value++
  loadPosts()
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
const commentText = ref('')
const currentPostId = ref('')

function handleComment(postId: string) {
  currentPostId.value = postId
  showCommentPopup.value = true
}

function handleCommentClose() {
  showCommentPopup.value = false
  commentText.value = ''
  currentPostId.value = ''
}

function handleSubmit(data) {
  console.log('评论内容：', data.content)
  console.log('是否匿名：', data.isAnonymous)
}

function handleClose() {
  console.log('评论框已关闭')
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
  padding-bottom: 80px;
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
  color: #999;
  font-size: 28rpx;
  padding: 40rpx 0;
}

.no-more {
  text-align: center;
  color: #999;
  font-size: 28rpx;
  padding: 40rpx 0;
}

.comment-popup {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
  border-top-left-radius: 20rpx;
  border-top-right-radius: 20rpx;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  border-bottom: 1px solid #eee;

  background: #fff;

  .title {
    font-size: 32rpx;
    font-weight: bold;
  }
}

.comment-content {
  flex: 1;
  overflow-y: auto;
  padding: 20rpx;
}

.comment-input {
  display: flex;
  align-items: center;
  padding: 20rpx;
  border-top: 1px solid #eee;
  background: #fff;
}
</style>
