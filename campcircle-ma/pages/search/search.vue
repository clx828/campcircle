<template>
  <view class="search-page">
    <!-- 头部搜索区域 -->
    <view class="search-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="back-button" @click="goBack">
          <wd-icon name="arrow-left" size="20px" color="#333"></wd-icon>
        </view>
        <view class="search-input-container">
          <input
            class="search-input"
            v-model="searchKeyword"
            placeholder="搜索帖子、用户..."
            @input="onSearchInput"
            @confirm="onSearchConfirm"
            :focus="inputFocus"
            confirm-type="search"
          />
          <view class="search-icon">
            <wd-icon name="search" size="16px" color="#999"></wd-icon>
          </view>
        </view>
      </view>
    </view>

    <!-- 搜索内容区域 -->
    <scroll-view scroll-y class="search-content" :style="{ paddingTop: (statusBarHeight + 44) + 'px' }">
      <!-- 搜索历史 -->
      <view class="search-history" v-if="searchHistory.length > 0 && !searchKeyword">
        <view class="section-header">
          <text class="section-title">搜索记录</text>
          <view class="clear-history" @click="clearHistory">
            <wd-icon name="delete" size="14px" color="#999"></wd-icon>
            <text class="clear-text">清空</text>
          </view>
        </view>
        <view class="history-tags">
          <view 
            class="history-tag" 
            v-for="(item, index) in searchHistory" 
            :key="index"
            @click="selectHistoryItem(item)"
          >
            <text class="tag-text">{{ item }}</text>
            <view class="tag-close" @click.stop="removeHistoryItem(index)">
              <wd-icon name="close" size="12px" color="#999"></wd-icon>
            </view>
          </view>
        </view>
      </view>

      <!-- 热门搜索 -->
      <view class="hot-search" v-if="!searchKeyword">
        <HotSearchCard :hotSearchList="hotSearchList" @search="onHotSearchClick" />
      </view>

      <!-- 搜索结果 -->
      <view class="search-results" v-if="searchKeyword && searchResults.length > 0">
        <view class="section-header">
          <text class="section-title">搜索结果</text>
          <text class="result-count">共{{ searchResults.length }}条结果</text>
        </view>
        <view class="result-list">
          <SocialCard 
            v-for="post in searchResults" 
            :key="post.id" 
            :cardInfo="post" 
            @like="handleLike" 
            @collect="handleCollect"
            @comment="handleComment" 
            @share="handleShare" 
            @follow="handleFollow" 
          />
        </view>
      </view>

      <!-- 无搜索结果 -->
      <view class="no-results" v-if="searchKeyword && searchResults.length === 0 && !searching">
        <image src="/static/img/enpty.png" class="no-results-img"></image>
        <text class="no-results-text">未找到相关内容</text>
        <text class="no-results-tip">试试其他关键词吧</text>
      </view>

      <!-- 搜索中状态 -->
      <view class="searching" v-if="searching">
        <view class="loading-spinner"></view>
        <text class="searching-text">搜索中...</text>
      </view>

      <!-- 底部占位 -->
      <view class="bottom-space"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import HotSearchCard from '@/components/HotSearchCard.vue'
import SocialCard from '@/components/SocialCard.vue'

// 系统信息
const statusBarHeight = ref(0)
const menuButtonInfo = ref<any>(null)

// 搜索相关数据
const searchKeyword = ref('')
const inputFocus = ref(true)
const searching = ref(false)
const searchResults = ref([])
const searchHistory = ref<string[]>([])

// 热门搜索数据
const hotSearchList = ref([
  { id: 1, keyword: '校园生活', hotScore: 1250, trend: 'up' },
  { id: 2, keyword: '学习方法', hotScore: 980, trend: 'up' },
  { id: 3, keyword: '社团活动', hotScore: 856, trend: 'down' },
  { id: 4, keyword: '考试复习', hotScore: 742, trend: 'up' },
  { id: 5, keyword: '实习经验', hotScore: 689, trend: 'same' },
  { id: 6, keyword: '毕业设计', hotScore: 634, trend: 'up' },
  { id: 7, keyword: '求职面试', hotScore: 578, trend: 'down' },
  { id: 8, keyword: '课程推荐', hotScore: 523, trend: 'up' },
  { id: 9, keyword: '校园美食', hotScore: 467, trend: 'same' },
  { id: 10, keyword: '宿舍生活', hotScore: 412, trend: 'down' }
])

// 获取系统信息
const getSystemInfo = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0

  // #ifdef MP-WEIXIN
  menuButtonInfo.value = uni.getMenuButtonBoundingClientRect()
  // #endif
}

// 页面初始化
onMounted(() => {
  getSystemInfo()
  loadSearchHistory()
})

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 搜索输入处理
const onSearchInput = (e: any) => {
  searchKeyword.value = e.detail.value
}

// 搜索确认
const onSearchConfirm = () => {
  if (searchKeyword.value.trim()) {
    performSearch(searchKeyword.value.trim())
  }
}

// 执行搜索
const performSearch = (keyword: string) => {
  searching.value = true
  
  // 添加到搜索历史
  addToHistory(keyword)
  
  // 模拟搜索请求
  setTimeout(() => {
    // 这里后续对接真实的搜索接口
    searchResults.value = []
    searching.value = false
  }, 1000)
}

// 添加到搜索历史
const addToHistory = (keyword: string) => {
  const history = searchHistory.value.filter(item => item !== keyword)
  history.unshift(keyword)
  searchHistory.value = history.slice(0, 10) // 最多保存10条
  saveSearchHistory()
}

// 选择历史搜索项
const selectHistoryItem = (keyword: string) => {
  searchKeyword.value = keyword
  performSearch(keyword)
}

// 删除历史搜索项
const removeHistoryItem = (index: number) => {
  searchHistory.value.splice(index, 1)
  saveSearchHistory()
}

// 清空搜索历史
const clearHistory = () => {
  uni.showModal({
    title: '提示',
    content: '确定要清空搜索记录吗？',
    success: (res) => {
      if (res.confirm) {
        searchHistory.value = []
        saveSearchHistory()
      }
    }
  })
}

// 热门搜索点击
const onHotSearchClick = (keyword: string) => {
  searchKeyword.value = keyword
  performSearch(keyword)
}

// 保存搜索历史到本地存储
const saveSearchHistory = () => {
  uni.setStorageSync('searchHistory', searchHistory.value)
}

// 加载搜索历史
const loadSearchHistory = () => {
  const history = uni.getStorageSync('searchHistory')
  if (history) {
    searchHistory.value = history
  }
}

// 社交卡片事件处理
const handleLike = (postId: string) => {
  console.log('点赞:', postId)
}

const handleCollect = (postId: string) => {
  console.log('收藏:', postId)
}

const handleComment = (postId: string) => {
  console.log('评论:', postId)
}

const handleShare = (post: any) => {
  console.log('分享:', post)
}

const handleFollow = (userId: string) => {
  console.log('关注:', userId)
}
</script>

<style lang="scss" scoped>
.search-page {
  height: 100vh;
  background: linear-gradient(to bottom, #fdf4f4 0%, #fffcff 100%);
  display: flex;
  flex-direction: column;
}

.search-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20rpx);
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);

  .header-content {
    display: flex;
    align-items: center;
    height: 44px;
    padding: 0 24rpx;
    /* 为胶囊按钮预留空间 */
    padding-right: 200rpx;

    .back-button {
      width: 64rpx;
      height: 64rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 24rpx;
      border-radius: 50%;
      background: rgba(0, 0, 0, 0.05);
      transition: all 0.3s ease;
      flex-shrink: 0;

      &:active {
        background: rgba(0, 0, 0, 0.1);
        transform: scale(0.95);
      }
    }

    .search-input-container {
      flex: 1;
      position: relative;
      background: #f8f9fa;
      border-radius: 32rpx;
      border: 2rpx solid transparent;
      transition: all 0.3s ease;
      max-width: calc(100vw - 300rpx); /* 限制最大宽度，避免被胶囊按钮遮挡 */

      &:focus-within {
        border-color: #495057;
        background: #ffffff;
        box-shadow: 0 4rpx 12rpx rgba(73, 80, 87, 0.1);
      }

      .search-input {
        width: 100%;
        height: 72rpx;
        padding: 0 24rpx 0 56rpx;
        font-size: 28rpx;
        color: #212529;
        background: transparent;
        border: none;
        outline: none;

        &::placeholder {
          color: #6c757d;
        }
      }

      .search-icon {
        position: absolute;
        left: 20rpx;
        top: 50%;
        transform: translateY(-50%);
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }
}

.search-content {
  flex: 1;
  padding: 0 32rpx;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 40rpx 32rpx 24rpx 0;

  .section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #212529;
  }

  .clear-history, .refresh-hot {
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 12rpx 16rpx;
    border-radius: 20rpx;
    background: rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;

    &:active {
      background: rgba(0, 0, 0, 0.1);
      transform: scale(0.95);
    }

    .clear-text {
      font-size: 24rpx;
      color: #6c757d;
    }
  }

  .result-count {
    font-size: 24rpx;
    color: #6c757d;
  }
}

.history-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;

  .history-tag {
    display: flex;
    align-items: center;
    background: rgba(255, 255, 255, 0.8);
    border: 1rpx solid rgba(0, 0, 0, 0.1);
    border-radius: 24rpx;
    padding: 12rpx 16rpx;
    transition: all 0.3s ease;

    &:active {
      background: rgba(73, 80, 87, 0.1);
      transform: scale(0.98);
    }

    .tag-text {
      font-size: 26rpx;
      color: #495057;
      margin-right: 8rpx;
    }

    .tag-close {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 32rpx;
      height: 32rpx;
      border-radius: 50%;
      background: rgba(0, 0, 0, 0.1);
      transition: all 0.3s ease;

      &:active {
        background: rgba(0, 0, 0, 0.2);
        transform: scale(0.9);
      }
    }
  }
}

.no-results {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;

  .no-results-img {
    width: 200rpx;
    height: 200rpx;
    margin-bottom: 32rpx;
    opacity: 0.6;
  }

  .no-results-text {
    font-size: 32rpx;
    color: #495057;
    margin-bottom: 16rpx;
  }

  .no-results-tip {
    font-size: 26rpx;
    color: #6c757d;
  }
}

.searching {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;

  .loading-spinner {
    width: 64rpx;
    height: 64rpx;
    border: 4rpx solid rgba(73, 80, 87, 0.1);
    border-top: 4rpx solid #495057;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 32rpx;
  }

  .searching-text {
    font-size: 28rpx;
    color: #6c757d;
  }
}

.bottom-space {
  height: 120rpx;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 750rpx) {
  .search-header .header-content {
    padding: 0 16rpx;
    padding-right: 180rpx; /* 小屏幕上减少右侧预留空间 */

    .back-button {
      width: 56rpx;
      height: 56rpx;
      margin-right: 16rpx;
    }

    .search-input-container {
      max-width: calc(100vw - 260rpx); /* 小屏幕上调整最大宽度 */

      .search-input {
        height: 64rpx;
        font-size: 26rpx;
      }
    }
  }

  .search-content {
    padding: 0 24rpx;
  }

  .section-header {
    margin: 32rpx 0 20rpx 0;

    .section-title {
      font-size: 28rpx;
    }
  }
}
</style>
