<template>
  <view class="container">
    <!-- 固定在顶部的搜索容器 -->
    <view class="search-container" :style="headerStyle">
      <view class="logo" :style="logoStyle">
        <image
            src="https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-06-13_12f2e457-9cae-4ffa-a149-1f480ddc221d.png" />
      </view>
      <view class="search-bar" :style="searchBarStyle">
        <image src="/static/button/shousuo.png" mode="aspectFit" class="search-bar-img" />
        <span class="search-bar-span">搜索</span>
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

      <!-- 热度排行榜 -->
      <HotPostRanking :limit="9" :pageSize="3" />
      <view class="post-list">
        <SocialCard v-for="post in postList" :key="post.id" :cardInfo="post" @like="handleLike" @collect="handleCollect"
                    @comment="handleComment(post.id,post.commentNum)" @share="handleShare(post)" @follow="handleFollow" />
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
    <CommentPopup v-model:show="showCommentPopup" :comment-num="commentNum" :post-id="currentPostId" @close="handleCommentPopupClose"
                  @comment-success="handleCommentSuccess" />
    <ShareModal :visible="showShareModal" :shareData="shareData" @close="showShareModal = false" />
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import SocialCard from '@/components/SocialCard.vue'
import HotPostRanking from '@/components/HotPostRanking.vue'
import { postApi } from '@/api/post'
import type { ListPostVOByPageParams } from '@/api/post'
import CommentPopup from '@/components/CommentPopup.vue'
import ShareModal from '@/components/ShareModal.vue'
import { onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'

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

// 轮播图数据
const swiperList = ref([
  'https://registry.npmmirror.com/wot-design-uni-assets/*/files/redpanda.jpg',
  'https://registry.npmmirror.com/wot-design-uni-assets/*/files/capybara.jpg',
  'https://registry.npmmirror.com/wot-design-uni-assets/*/files/panda.jpg',
  'https://registry.npmmirror.com/wot-design-uni-assets/*/files/moon.jpg',
  'https://registry.npmmirror.com/wot-design-uni-assets/*/files/meng.jpg'
])

// 处理轮播图点击事件
function handleClick(e) {
  console.log('轮播图点击:', e)
}

// 获取系统信息，用于计算顶部布局
const getSystemInfo = () => {
  const systemInfo = uni.getWindowInfo()
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

// 加载帖子列表数据
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

// 下拉刷新处理
const onRefresh = async () => {
  refresherTriggered.value = true
  current.value = 1
  await loadPosts(true)
  refresherTriggered.value = false
}

// 处理帖子点赞操作
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

// 处理帖子收藏操作
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
const commentNum = ref(0)

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
  loadPosts()
}

// 分享相关状态 - 简化版
const showShareModal = ref(false)
const shareData = ref({
  title: '',
  desc: '',
  path: '',
  imageUrl: ''
})

// 处理帖子分享操作 - 简化版
function handleShare(cardInfo) {
  const postTitle = cardInfo?.content ?
      (cardInfo.content.length > 30 ? cardInfo.content.substring(0, 30) + '...' : cardInfo.content) :
      'CampCircle - 校园精彩内容'

  const postImage = cardInfo?.pictureUrlList && cardInfo.pictureUrlList.length > 0 ?
      cardInfo.pictureUrlList[0] :
      'https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-06-13_12f2e457-9cae-4ffa-a149-1f480ddc221d.png'

  shareData.value = {
    title: postTitle,
    desc: `来自 ${cardInfo?.user?.userName || '校园用户'} 的精彩分享`,
    path: `/pages/postDetail/postDetail?id=${cardInfo?.id || ''}`,
    imageUrl: postImage
  }

  console.log("分享了", shareData.value)
  uni.vibrateShort()
  showShareModal.value = true
}

// 处理关注
function handleFollow(data: { id: string; hasFollow: boolean; isRollback: boolean }) {
  const post = postList.value.find(p => p.user.id === data.id)
  if (post) {
    post.hasFollow = data.hasFollow
  }
}

// 配置分享给朋友 - 简化版
onShareAppMessage(() => {
  return {
    title: shareData.value.title || 'CampCircle - 校园社交平台',
    path: shareData.value.path || '/pages/tabbar/home/home',
    imageUrl: shareData.value.imageUrl || 'https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-06-13_12f2e457-9cae-4ffa-a149-1f480ddc221d.png'
  }
})

// 配置分享到朋友圈 - 简化版
onShareTimeline(() => {
  return {
    title: shareData.value.title || 'CampCircle - 发现校园精彩生活',
    query: shareData.value.path ? `postId=${shareData.value.path.split('=')[1]}` : 'from=timeline',
    imageUrl: shareData.value.imageUrl || 'https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-06-13_12f2e457-9cae-4ffa-a149-1f480ddc221d.png'
  }
})

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
  overflow: hidden;
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
  }

  .logo-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .search-bar {
    background: #f5f5f5;
    border-radius: 36rpx;
    display: flex;
    align-items: center;
    padding: 0 24rpx;
  }

  .search-bar-img {
    width: 32rpx;
    height: 32rpx;
    margin-right: 12rpx;
  }

  .search-bar-span {
    color: #9c9c9c;
    font-size: 28rpx;
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

.bottom-space {
  height: 120rpx; // 为底部tabbar预留空间
  width: 100%;
}
</style>