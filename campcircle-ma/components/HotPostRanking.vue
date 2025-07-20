<template>
  <view class="hot-ranking-swiper">
    <!-- 标题区域 -->
    <view class="ranking-header">
      <view class="header-left">
        <image src="/static/button/ranking/hotRank.png" class="header-icon"></image>
        <text class="header-title">热度排行榜</text>
        <text class="update-time">更新于{{ formatTime() }}分钟前</text>
      </view>
      <view class="header-right">
        <wd-icon name="refresh" @click="refreshData" size="16px" color="#999"></wd-icon>
      </view>
    </view>

    <!-- 轮播图容器 -->
    <view class="swiper-container">
      <swiper
          class="ranking-swip                 er"
          :indicator-dots="true"
          :autoplay="true"
          :interval="4000"
          :duration="500"
          indicator-color="rgba(128, 128, 128, 0.6)"
          indicator-active-color="#fb0055"
          @change="onSwiperChange"
      >
        <swiper-item v-for="(pageData, pageIndex) in paginatedData" :key="pageIndex">
          <view class="ranking-page">
            <view
                v-for="(post, index) in pageData"
                :key="post.id"
                class="ranking-item"
                :class="{ 'top-item': getRealIndex(pageIndex, index) < 3 }"
                @click="handlePostClick(post.id)"
            >
              <!-- 排名标识 -->
              <view class="rank-badge">
                <image
                    v-if="getRealIndex(pageIndex, index) < 3"
                    :src="getRankImage(getRealIndex(pageIndex, index))"
                    class="rank-image"
                />
                <view v-else class="rank-number" :class="getRankClass(getRealIndex(pageIndex, index))">
                  {{ getRealIndex(pageIndex, index) + 1 }}
                </view>
              </view>

              <!-- 帖子内容 -->
              <view class="post-content">
                <text class="post-text">{{ post.content }}</text>
              </view>

              <!-- 热度分数 -->
              <view class="hot-score">
                <image src="/static/button/ranking/hotRank.png" class="score-icon">
                </image>
                <text class="score-value">{{ formatHotScore(post.hotScore) }}</text>
              </view>
            </view>
          </view>
        </swiper-item>
      </swiper>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-overlay">
      <view class="loading-spinner"></view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { postApi } from '@/api/post'

// 热门帖子数据接口
interface HotPost {
  id: string
  content: string
  hotScore: number
  lastHotUpdateTime: string
}

// 响应数据接口
interface HotPostResponse {
  code: number
  data: HotPost[]
  message: string
}

// Props
const props = withDefaults(defineProps<{
  limit?: number
  pageSize?: number
}>(), {
  limit: 9,
  pageSize: 3
})

// 响应式数据
const hotPosts = ref<HotPost[]>([])
const loading = ref(false)
const currentPage = ref(0)
const updateMinutes = ref(1)
const updateTimer = ref<NodeJS.Timeout | null>(null)

// 分页数据
const paginatedData = computed(() => {
  const pages = []
  for (let i = 0; i < hotPosts.value.length; i += props.pageSize) {
    pages.push(hotPosts.value.slice(i, i + props.pageSize))
  }
  return pages
})

// 获取热门帖子数据
const fetchHotPosts = async () => {
  try {
    loading.value = true
    const response: HotPostResponse = await postApi.getHotPostList(props.limit)

    if (response.code === 0) {
      hotPosts.value = response.data || []
      // 计算初始更新时间
      calculateUpdateTime()
      // 启动定时器
      startUpdateTimer()
    } else {
      console.error('获取热门帖子失败:', response.message)
    }
  } catch (error) {
    console.error('获取热门帖子失败:', error)
  } finally {
    loading.value = false
  }
}

// 刷新数据
const refreshData = () => {
  uni.vibrateShort()
  // 清除现有定时器
  if (updateTimer.value) {
    clearInterval(updateTimer.value)
    updateTimer.value = null
  }
  fetchHotPosts()
}

// 处理轮播图切换
const onSwiperChange = (e: any) => {
  currentPage.value = e.detail.current
}

// 处理帖子点击
const handlePostClick = (postId: string) => {
  uni.vibrateShort()
  uni.navigateTo({
    url: `/pages/postDetail/postDetail?id=${postId}`
  })
}

// 获取真实索引
const getRealIndex = (pageIndex: number, itemIndex: number) => {
  return pageIndex * props.pageSize + itemIndex
}

// 获取排名样式类
const getRankClass = (index: number) => {
  if (index === 0) return 'rank-first'
  if (index === 1) return 'rank-second'
  if (index === 2) return 'rank-third'
  return 'rank-normal'
}

// 获取排名图片
const getRankImage = (index: number) => {
  const images = ['/static/button/ranking/1th.png', '/static/button/ranking/2th.png', '/static/button/ranking/3th.png']
  return images[index] || ''
}

// 格式化热度分数
const formatHotScore = (score: number) => {
  if (score >= 1000) {
    return Math.round(score / 1000) + 'k'
  }
  return Math.round(score).toString()
}

// 计算更新时间（分钟）
const calculateUpdateTime = () => {
  if (hotPosts.value.length === 0) {
    updateMinutes.value = 1
    return
  }

  // 找到最新的 lastHotUpdateTime
  let latestUpdateTime: Date | null = null
  for (const post of hotPosts.value) {
    if (post.lastHotUpdateTime) {
      const updateTime = new Date(post.lastHotUpdateTime)
      if (!latestUpdateTime || updateTime > latestUpdateTime) {
        latestUpdateTime = updateTime
      }
    }
  }

  if (latestUpdateTime) {
    const now = new Date()
    const diffMinutes = Math.floor((now.getTime() - latestUpdateTime.getTime()) / (1000 * 60))
    // 确保最小值为1分钟
    updateMinutes.value = Math.max(1, diffMinutes)
  } else {
    updateMinutes.value = 1
  }
}

// 启动定时器，每分钟更新一次时间显示
const startUpdateTimer = () => {
  // 清除现有定时器
  if (updateTimer.value) {
    clearInterval(updateTimer.value)
  }

  // 每分钟更新一次
  updateTimer.value = setInterval(() => {
    calculateUpdateTime()
  }, 60000) // 60秒
}

// 格式化时间显示
const formatTime = () => {
  return updateMinutes.value
}

// 组件挂载时获取数据
onMounted(() => {
  fetchHotPosts()
})

// 组件卸载时清理定时器
onUnmounted(() => {
  if (updateTimer.value) {
    clearInterval(updateTimer.value)
    updateTimer.value = null
  }
})
</script>

<style lang="scss" scoped>
.hot-ranking-swiper {
  background: linear-gradient(to bottom, #fdf4f4 0%, #fffcff 100%);
  margin: 20rpx 0 0 0;
  overflow: hidden;
  position: relative;
  backdrop-filter: blur(16rpx);

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3rpx;
    //background: linear-gradient(90deg, transparent, #ff6b9d, #ffffff, transparent);
    opacity: 0.8;
    z-index: 1;
    //animation: shimmer 3s ease-in-out infinite;
  }
}

.ranking-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 32rpx;
  //background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  color: #212529;
  position: relative;
  z-index: 2;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 32rpx;
    right: 32rpx;
    height: 1rpx;
    background: linear-gradient(90deg, transparent, rgba(0, 0, 0, 0.15), transparent);
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 16rpx;

    .header-icon {
      width: 36rpx;
      height: 36rpx;
      margin-right: 20rpx;
      filter: drop-shadow(0 2rpx 8rpx rgba(0, 0, 0, 0.2));
      animation: glow 2s ease-in-out infinite alternate;
    }

    .header-title {
      font-size: 36rpx;
      font-weight: 700;
      letter-spacing: 0.5rpx;
      background: linear-gradient(135deg, #212529 0%, #495057 100%);
      background-clip: text;
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.15);
    }

    .update-time {
      font-size: 22rpx;
      color: #6c757d;
      font-weight: 400;
      border: 1rpx solid #ced4da;
      padding: 6rpx 12rpx;
      border-radius: 12rpx;
      background: #f8f9fa;
      white-space: nowrap;
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 24rpx;

    .refresh-btn {
      font-size: 32rpx;
      padding: 16rpx;
      border-radius: 50%;
      background: linear-gradient(135deg, #495057 0%, #6c757d 100%);
      color: white;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      backdrop-filter: blur(10rpx);
      border: 1rpx solid rgba(255, 255, 255, 0.2);
      box-shadow:
          0 4rpx 16rpx rgba(73, 80, 87, 0.3),
          0 2rpx 8rpx rgba(0, 0, 0, 0.1);

      &:active {
        transform: rotate(360deg) scale(0.95);
        background: linear-gradient(135deg, #343a40 0%, #495057 100%);
        box-shadow:
            0 2rpx 12rpx rgba(73, 80, 87, 0.4),
            0 1rpx 4rpx rgba(0, 0, 0, 0.2);
      }
    }
  }
}

.swiper-container {
  height: 290rpx; /* 增加总高度 */
  position: relative;
}

.ranking-swiper {
  height: 285rpx; /* 固定轮播内容高度，给导航点留出更多空间 */

  /* 自定义导航点样式 */
  ::v-deep .uni-swiper-dots {
    bottom: 5rpx !important; /* 调整导航点位置 */
    height: 50rpx !important; /* 增加导航点区域高度 */
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    position: absolute !important;
    left: 0 !important;
    right: 0 !important;
    z-index: 5 !important; /* 降低z-index，避免过度遮挡 */
    background: linear-gradient(to top, rgba(247, 231, 237, 0.8) 0%, transparent 100%) !important; /* 添加渐变背景 */
    backdrop-filter: blur(10rpx) !important;
  }

  ::v-deep .uni-swiper-dot {
    width: 12rpx !important;
    height: 12rpx !important;
    margin: 0 6rpx !important;
    border-radius: 50% !important;
    background-color: rgba(128, 128, 128, 0.6) !important;
    transition: all 0.3s ease !important;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1) !important; /* 添加阴影增强可见性 */
  }

  ::v-deep .uni-swiper-dot-active {
    background-color: #fb0055 !important;
    transform: scale(1.2) !important;
    box-shadow: 0 2rpx 12rpx rgba(251, 0, 85, 0.4) !important;
  }
}

.ranking-page {
  padding: 12rpx 32rpx 16rpx 32rpx; /* 调整底部padding */
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-start; /* 改为flex-start，避免内容被挤到底部 */
  gap: 8rpx; /* 增加间距 */
}

.ranking-item {

  display: flex;
  align-items: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  border-radius: 12rpx;
  height: 52rpx; /* 稍微增加高度 */
  padding: 6rpx 12rpx;
  overflow: hidden;
  z-index: 1; /* 确保ranking-item在合适的层级 */

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, transparent 0%, rgba(73, 80, 87, 0.02) 100%);
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &:hover::before {
    opacity: 1;
  }

  &.top-item {

    transform: translateY(-2rpx);

    &::after {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 4rpx;
      border-radius: 0 4rpx 4rpx 0;
    }
  }

  &:active {
    transform: scale(0.98) translateY(1rpx);
    background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
    box-shadow:
        0 2rpx 12rpx rgba(73, 80, 87, 0.15),
        0 1rpx 4rpx rgba(0, 0, 0, 0.1);
  }

  /* 确保最后一个item有足够的底部间距 */
  &:last-child {
    margin-bottom: 8rpx;
  }
}

.rank-badge {
  width: 36rpx;
  height: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
  transition: all 0.3s ease;

  .rank-image {
    width: 36rpx;
    height: 36rpx;
    transition: all 0.3s ease;
    filter: drop-shadow(0 2rpx 8rpx rgba(251, 0, 85, 0.2));

    &:active {
      transform: scale(0.9);
    }
  }

  .rank-number {
    width: auto;
    height: auto;
    min-width: 48rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 36rpx;
    font-weight: 800;
    font-family: 'Arial Black', 'PingFang SC', sans-serif;
    color: #d4d2d1;
    background: linear-gradient(135deg, #d4d2d1 0%, #c6c3c3 50%, #cccccc 100%);
    background-clip: text;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    filter: drop-shadow(0 2rpx 4rpx rgba(102, 102, 102, 0.3));
    transform: perspective(100rpx) rotateX(15deg);
    transition: all 0.3s ease;
    letter-spacing: 1rpx;
    position: relative;

    &::before {
      content: '';
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 120%;
      height: 120%;
      background: linear-gradient(45deg, rgba(102, 102, 102, 0.15), transparent);
      border-radius: 8rpx;
      z-index: -1;
      opacity: 0;
      transition: opacity 0.3s ease;
    }

    &:active {
      transform: perspective(100rpx) rotateX(15deg) scale(0.95);
      filter: drop-shadow(0 1rpx 2rpx rgba(102, 102, 102, 0.5));

      &::before {
        opacity: 1;
      }
    }
  }
}
.post-content {
  flex: 1;
  margin-right: 16rpx;
  overflow: hidden;

  .post-text {
    font-size: 28rpx;
    color: #212529;
    line-height: 1.3;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 1;
    overflow: hidden;
    font-weight: 500;
    letter-spacing: 0.2rpx;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.hot-score {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, rgba(73, 80, 87, 0.1) 0%, rgba(108, 117, 125, 0.08) 100%);
  padding: 6rpx 12rpx;
  border-radius: 16rpx;
  border: 1rpx solid rgba(73, 80, 87, 0.2);
  flex-shrink: 0;
  box-shadow: 0 2rpx 8rpx rgba(73, 80, 87, 0.1);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-1rpx);
    box-shadow: 0 4rpx 12rpx rgba(73, 80, 87, 0.2);
  }

  .score-icon {
    width: 20rpx;
    height: 20rpx;
    margin-right: 4rpx;
    filter: drop-shadow(0 1rpx 2rpx rgba(73, 80, 87, 0.3));
  }

  .score-value {
    font-size: 20rpx;
    color: #495057;
    font-weight: 600;
    letter-spacing: 0.2rpx;
  }
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 250, 252, 0.98) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  backdrop-filter: blur(20rpx);

  .loading-spinner {
    width: 56rpx;
    height: 56rpx;
    border: 4rpx solid rgba(251, 0, 85, 0.1);
    border-top: 4rpx solid #fb0055;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    box-shadow: 0 4rpx 16rpx rgba(251, 0, 85, 0.2);
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes glow {
  0% {
    transform: scale(1);
    filter: drop-shadow(0 2rpx 8rpx rgba(73, 80, 87, 0.3));
  }
  100% {
    transform: scale(1.05);
    filter: drop-shadow(0 4rpx 12rpx rgba(73, 80, 87, 0.5));
  }
}

@keyframes shimmer {
  0% {
    background: linear-gradient(90deg, transparent, #fb0055, #ff6b9d, transparent);
    opacity: 0.8;
  }
  50% {
    background: linear-gradient(90deg, transparent, #ff6b9d, #fb0055, transparent);
    opacity: 0.6;
  }
  100% {
    background: linear-gradient(90deg, transparent, #fb0055, #ff6b9d, transparent);
    opacity: 0.8;
  }
}

/* 响应式设计 */
@media (max-width: 750rpx) {
  .ranking-header {
    padding: 24rpx 28rpx;

    .header-left .header-title {
      font-size: 32rpx;
    }

    .header-right {
      gap: 16rpx;
    }
  }

  .swiper-container {
    height: 320rpx; /* 在小屏幕上增加更多高度 */
    padding-bottom: 70rpx;
  }

  .ranking-swiper {
    height: 250rpx;
  }

  .ranking-page {
    padding: 16rpx 28rpx 20rpx 28rpx;
    gap: 10rpx;
  }

  .ranking-item {
    height: 60rpx;
    padding: 4px 16rpx;

    &:last-child {
      margin-bottom: 12rpx;
    }
  }

  .post-content .post-text {
    font-size: 26rpx;
  }
}
</style>