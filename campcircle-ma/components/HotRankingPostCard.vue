<template>
  <view class="hot-search-ranking">
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

    <!-- 搜索列表容器 -->
    <view class="search-container">
      <view class="search-list">
        <view
          v-for="(post, index) in hotPosts"
          :key="post.id"
          class="ranking-item"
          :class="{ 'top-item': index < 3 }"
          @click="handlePostClick(post.id)"
        >
          <!-- 排名标识 -->
          <view class="rank-badge">
            <image
              v-if="index < 3"
              :src="getRankImage(index)"
              class="rank-image"
            />
            <view v-else class="rank-number" :class="getRankClass(index)">
              {{ index + 1 }}
            </view>
          </view>

          <!-- 帖子内容 -->
          <view class="post-content">
            <text class="post-text">{{ post.content }}</text>
          </view>

          <!-- 热度分数 -->
          <view class="hot-score">
            <image src="/static/button/ranking/hotRank.png" class="score-icon"></image>
            <text class="score-value">{{ formatHotScore(post.hotScore) }}</text>
          </view>
        </view>
      </view>
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
}>(), {
  limit: 10
})

// 响应式数据
const hotPosts = ref<HotPost[]>([])
const loading = ref(false)
const updateMinutes = ref(1)
const updateTimer = ref<NodeJS.Timeout | null>(null)

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

// 处理帖子点击
const handlePostClick = (postId: string) => {
  uni.vibrateShort()
  uni.navigateTo({
    url: `/pages/postDetail/postDetail?id=${postId}`
  })
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
    return (score / 1000).toFixed(1) + 'k'
  }
  return score.toFixed(1)
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
.hot-search-ranking {
  max-width: calc(100vw - 64rpx);
  width: 100%;
  overflow: hidden;
  position: relative;
  backdrop-filter: blur(16rpx);
  border-radius: 16rpx;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3rpx;
    opacity: 0.8;
    z-index: 1;
  }
}

.ranking-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 24rpx;
  //background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  color: #212529;
  position: relative;
  z-index: 2;
  width: 100%;
  box-sizing: border-box;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 24rpx;
    right: 24rpx;
    height: 1rpx;
    background: linear-gradient(90deg, transparent, rgba(0, 0, 0, 0.15), transparent);
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 12rpx;
    flex: 1;
    min-width: 0;

    .header-icon {
      width: 32rpx;
      height: 32rpx;
      margin-right: 16rpx;
      filter: drop-shadow(0 2rpx 8rpx rgba(73, 80, 87, 0.3));
      animation: glow 2s ease-in-out infinite alternate;
      flex-shrink: 0;
    }

    .header-title {
      font-size: 32rpx;
      font-weight: 700;
      letter-spacing: 0.5rpx;
      background: linear-gradient(135deg, #212529 0%, #495057 100%);
      background-clip: text;
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.15);
      flex-shrink: 0;
    }

    .update-time {
      font-size: 20rpx;
      color: #6c757d;
      font-weight: 400;
      border: 1rpx solid #ced4da;
      padding: 4rpx 8rpx;
      border-radius: 10rpx;
      background: #f8f9fa;
      white-space: nowrap;
      flex-shrink: 0;
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 24rpx;
  }
}

.search-container {
  padding: 12rpx 24rpx 16rpx 24rpx;
  width: 100%;
  box-sizing: border-box;
}

.search-list {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  width: 100%;
}

.ranking-item {
  display: flex;
  align-items: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  border-radius: 12rpx;
  height: 52rpx;
  padding: 43rpx 12rpx;
  overflow: hidden;
  z-index: 1;
  width: 100%;
  box-sizing: border-box;

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
    filter: drop-shadow(0 2rpx 8rpx rgba(73, 80, 87, 0.2));

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
    font-size: 32rpx;
    font-weight: 800;
    font-family: 'Arial Black', 'PingFang SC', sans-serif;
    color: #d1d1d1;
    background: linear-gradient(135deg, #d1d1d1 0%, #d1d0d0 50%, #d1d1d1 100%);
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
  min-width: 0;

  .post-text {
    font-size: 32rpx;
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
    max-width: 100%;
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
  gap: 4rpx;

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

  .trend-indicator {
    width: 24rpx;
    height: 24rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;

    &.trend-up {
      background: rgba(40, 167, 69, 0.15);
    }

    &.trend-down {
      background: rgba(220, 53, 69, 0.15);
    }

    &.trend-same {
      background: rgba(108, 117, 125, 0.15);
    }
  }
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

/* 响应式设计 */
@media (max-width: 750rpx) {
  .hot-search-ranking {
    margin: 16rpx auto 0 auto;
    max-width: calc(100vw - 48rpx);
  }

  .ranking-header {
    padding: 16rpx 20rpx;

    .header-left {
      gap: 8rpx;

      .header-icon {
        width: 28rpx;
        height: 28rpx;
        margin-right: 12rpx;
      }

      .header-title {
        font-size: 28rpx;
      }

      .update-time {
        font-size: 18rpx;
        padding: 2rpx 6rpx;
      }
    }

    .header-right {
      gap: 12rpx;
    }
  }

  .search-container {
    padding: 12rpx 20rpx 16rpx 20rpx;
  }

  .search-list {
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

  .rank-badge {
    width: 32rpx;
    height: 32rpx;
    margin-right: 16rpx;

    .rank-image {
      width: 32rpx;
      height: 32rpx;
    }

    .rank-number {
      font-size: 28rpx;
      min-width: 40rpx;
    }
  }

  .hot-score {
    padding: 4rpx 8rpx;

    .score-icon {
      width: 16rpx;
      height: 16rpx;
    }

    .score-value {
      font-size: 18rpx;
    }

    .trend-indicator {
      width: 20rpx;
      height: 20rpx;
    }
  }
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  backdrop-filter: blur(4rpx);
}

.loading-spinner {
  width: 40rpx;
  height: 40rpx;
  border: 4rpx solid #f3f3f3;
  border-top: 4rpx solid #fb0055;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
