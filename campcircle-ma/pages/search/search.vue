<template>
  <view class="search-page">
    <!-- 头部搜索区域 -->
    <view class="search-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="back-button" @click="goBack">
          <wd-icon name="arrow-left" size="20px" color="#333"></wd-icon>
        </view>
        <view class="search-input-container" :class="{ 'focused': inputFocused }">
          <input
              class="search-input"
              v-model="searchKeyword"
              placeholder="搜索帖子、用户、话题..."
              @input="onSearchInput"
              @confirm="onSearchConfirm"
              @focus="inputFocused = true"
              @blur="inputFocused = false"
              :focus="inputFocus"
              confirm-type="search"
          />
          <view class="search-icon">
            <wd-icon name="search" size="16px" color="#9ca3af"></wd-icon>
          </view>
          <view v-if="searchKeyword" class="clear-button" @click="clearSearch">
            <wd-icon name="close" size="14px" color="#9ca3af"></wd-icon>
          </view>
        </view>
      </view>
    </view>

    <!-- 搜索内容区域 -->
    <view class="search-content" :style="{ paddingTop: (statusBarHeight + 70) + 'px' }">
      <!-- 搜索建议下拉框 -->
      <view v-if="showSuggestions && searchKeyword" class="suggestions-dropdown">
        <view v-if="suggestions.length > 0" class="suggestions-content">
          <view class="suggestions-header">
            <text class="suggestions-title">搜索建议</text>
          </view>
          <view
              v-for="(suggestion, index) in suggestions"
              :key="index"
              class="suggestion-item"
              :class="{ 'selected': selectedIndex === index }"
              @click="selectSuggestion(suggestion)"
          >
            <view class="suggestion-content">
              <text class="suggestion-icon">{{ getTypeIcon(suggestion.type) }}</text>
              <text class="suggestion-text">{{ suggestion.text }}</text>
            </view>
            <text class="suggestion-count">{{ suggestion.count }}</text>
          </view>
        </view>
        <view v-else class="no-suggestions">
          <text>没有找到相关建议</text>
        </view>
      </view>

      <!-- 搜索历史 -->
      <view class="search-history" v-if="searchHistory.length > 0 && !searchKeyword">
        <view class="section-header">
          <text class="section-title">
            <wd-icon name="clock" size="16px" color="#374151" style="margin-right: 8rpx;"></wd-icon>
            搜索记录
          </text>
          <view class="clear-history" @click="clearHistory">
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
              <wd-icon name="close" size="12px" color="#9ca3af"></wd-icon>
            </view>
          </view>
        </view>
      </view>

      <!-- 热门搜索 -->
      <view class="hot-search" v-if="!searchKeyword">
        <view class="section-header">
          <text class="section-title">
            <wd-icon name="fire" size="16px" color="#374151" style="margin-right: 8rpx;"></wd-icon>
            热门搜索
          </text>
        </view>
        <view class="hot-tags">
          <view
              class="hot-tag"
              v-for="(item, index) in hotSearchList"
              :key="index"
              @click="onHotSearchClick(item.keyword)"
              :class="{ 'hot-tag-trending': item.trending }"
          >
            <text class="tag-text">{{ item.keyword }}</text>
            <view class="hot-indicator" v-if="item.trending">🔥</view>
          </view>
        </view>
      </view>

      <!-- 热门帖子排行榜 -->
      <view class="hot-ranking-section" v-if="!searchKeyword">
        <HotRankingPostCard :limit="10" />
      </view>

      <!-- 搜索结果 -->
      <view class="search-results" v-if="searchKeyword && !showSuggestions">
        <!-- 搜索加载状态 -->
        <view v-if="searching" class="search-loading">
          <wd-loading size="20px" color="#007aff"></wd-loading>
          <text class="loading-text">搜索中...</text>
        </view>

        <!-- 搜索结果tabs -->
        <view v-else-if="searchResults.posts.length > 0 || searchResults.users.length > 0" class="search-tabs-container">
          <wd-tabs v-model="activeTab" @change="onTabChange">
            <wd-tab :title="`笔记 ${total.posts || searchResults.posts.length}`" name="posts"></wd-tab>
            <wd-tab :title="`用户 ${total.users || searchResults.users.length}`" name="users"></wd-tab>
          </wd-tabs>

          <!-- 笔记结果 -->
          <view v-if="activeTab === 'posts'" class="posts-results">
            <SocialCard
              v-for="post in searchResults.posts"
              :key="post.id"
              :cardInfo="post"
              @comment="handleComment"
              @share="handleShare"
            />

            <!-- 加载更多 -->
            <view v-if="searchResults.posts.length > 0" class="load-more">
              <view v-if="searching && activeTab === 'posts'" class="loading">
                <wd-loading size="20px" />
                <text class="loading-text">加载中...</text>
              </view>
              <view v-else-if="hasMore.posts" class="load-more-btn" @click="loadMore('posts')">
                <text>加载更多</text>
              </view>
              <view v-else class="no-more">
                <text>没有更多内容了</text>
              </view>
            </view>
          </view>

          <!-- 用户结果 -->
          <view v-if="activeTab === 'users'" class="users-results">
            <view class="user-list">
              <view v-for="user in searchResults.users" :key="user.id" class="user-item" @click="goToUserProfile(user)">
                <view class="user-avatar">
                  <image :src="user.userAvatar || '/static/images/default-avatar.png'" class="avatar-img" mode="aspectFill" />
                </view>
                <view class="user-info">
                  <view class="user-name">{{ user.userName }}</view>
                  <view class="user-profile">{{ user.userProfile || '这个人很懒，还没有简介' }}</view>
                </view>
                <view class="user-action">
                  <wd-button
                    size="small"
                    :type="user.hasFollow ? 'primary' : 'info'"
                    :plain="!user.hasFollow"
                    @click.stop="handleUserFollow(user)"
                    class="follow-btn"
                  >
                    {{ user.hasFollow ? '已关注' : '关注' }}
                  </wd-button>
                </view>
              </view>
            </view>

            <!-- 加载更多 -->
            <view v-if="searchResults.users.length > 0" class="load-more">
              <view v-if="searching && activeTab === 'users'" class="loading">
                <wd-loading size="20px" />
                <text class="loading-text">加载中...</text>
              </view>
              <view v-else-if="hasMore.users" class="load-more-btn" @click="loadMore('users')">
                <text>加载更多</text>
              </view>
              <view v-else class="no-more">
                <text>没有更多内容了</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 无搜索结果 -->
        <view v-else class="no-results">
          <view class="no-results-icon">
            <wd-icon name="search" size="64px" color="#d9d9d9"></wd-icon>
          </view>
          <text class="no-results-text">没有找到相关内容</text>
          <text class="no-results-sub">试试其他关键词吧</text>
        </view>
      </view>

      <!-- 底部占位 -->
      <view class="bottom-space"></view>
    </view>

    <!-- 全局评论弹窗 -->
    <CommentPopup
        v-model:show="showCommentPopup"
        :comment-num="commentNum"
        :post-id="currentPostId"
        @close="handleCommentPopupClose"
        @comment-success="handleCommentSuccess"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import HotRankingPostCard from '@/components/HotRankingPostCard.vue'
import SocialCard from '@/components/SocialCard.vue'
import CommentPopup from '@/components/CommentPopup.vue'
import { postApi } from '@/api/post'
import { userApi } from '@/api/user'

// 系统信息
const statusBarHeight = ref(0)

// 搜索相关数据
const searchKeyword = ref('')
const inputFocus = ref(true)
const inputFocused = ref(false)
const searching = ref(false)
const searchResults = ref({
  posts: [],
  users: []
})
const suggestions = ref([])

// 评论弹窗相关
const showCommentPopup = ref(false)
const currentPostId = ref('')
const commentNum = ref(0)
const showSuggestions = ref(false)
const selectedIndex = ref(-1)
const searchHistory = ref<string[]>([])
const activeTab = ref('posts')

// 搜索参数
const searchParams = ref({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'desc'
})

// 分页相关
const hasMore = ref({
  posts: true,
  users: true
})
const total = ref({
  posts: 0,
  users: 0
})

// 模拟搜索建议数据
const mockSuggestions = [
  { type: 'user', text: '前端开发者', count: '1.2万' },
  { type: 'post', text: '前端开发技巧', count: '8.5k' },
  { type: 'topic', text: '前端框架对比', count: '6.3k' },
  { type: 'user', text: '前端小白', count: '4.1k' },
  { type: 'post', text: '前端面试题', count: '9.2k' },
]

const hotSearchList = ref([
  { keyword: 'React 18新特性', trending: true },
  { keyword: 'TypeScript入门', trending: false },
  { keyword: 'CSS Grid布局', trending: true },
  { keyword: '微前端架构', trending: false },
  { keyword: 'Vue3组合式API', trending: true },
  { keyword: '前端性能优化', trending: false },
])

// 获取系统信息
const getSystemInfo = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
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
  const value = e.detail.value
  searchKeyword.value = value

  // 重置搜索结果，只在按回车或选择建议时执行搜索
  if (value.trim()) {
    // 模拟搜索建议
    const filtered = mockSuggestions.filter(item =>
        item.text.toLowerCase().includes(value.toLowerCase())
    )
    suggestions.value = filtered
    showSuggestions.value = true

    // 清空搜索结果，保持在输入状态
    searchResults.value = {
      posts: [],
      users: []
    }
  } else {
    suggestions.value = []
    showSuggestions.value = false
  }
  selectedIndex.value = -1
}

// 搜索确认 - 按回车键触发
const onSearchConfirm = () => {
  if (searchKeyword.value.trim()) {
    showSuggestions.value = false // 隐藏搜索建议
    performSearch(searchKeyword.value.trim())
  }
}

// 选择建议项
const selectSuggestion = (suggestion: any) => {
  searchKeyword.value = suggestion.text
  showSuggestions.value = false
  selectedIndex.value = -1
  performSearch(suggestion.text)
}

// 执行搜索
const performSearch = async (keyword: string) => {
  searching.value = true
  showSuggestions.value = false

  // 添加到搜索历史
  addToHistory(keyword)

  try {
    // 并行请求帖子和用户搜索结果
    const [postResponse, userResponse] = await Promise.all([
      postApi.searchPostVOByKeyword(
        searchParams.value.current,
        keyword,
        searchParams.value.pageSize,
        searchParams.value.sortField,
        searchParams.value.sortOrder
      ),
      userApi.searchUserVOByKeyword(
        searchParams.value.current,
        keyword,
        searchParams.value.pageSize,
        searchParams.value.sortField,
        searchParams.value.sortOrder
      )
    ])

    // 处理搜索结果
    searchResults.value = {
      posts: postResponse.data?.records || [],
      users: userResponse.data?.records || []
    }

    // 更新分页信息
    total.value = {
      posts: postResponse.data?.total || 0,
      users: userResponse.data?.total || 0
    }

    // 判断是否还有更多数据
    hasMore.value = {
      posts: (postResponse.data?.records?.length || 0) >= searchParams.value.pageSize,
      users: (userResponse.data?.records?.length || 0) >= searchParams.value.pageSize
    }

    // 默认显示笔记tab
    activeTab.value = 'posts'

  } catch (error) {
    console.error('搜索失败:', error)
    uni.showToast({
      title: '搜索失败，请重试',
      icon: 'none'
    })

    // 搜索失败时清空结果
    searchResults.value = {
      posts: [],
      users: []
    }
  } finally {
    searching.value = false
  }
}

// 加载更多数据
const loadMore = async (type: 'posts' | 'users') => {
  if (searching.value || !hasMore.value[type] || !searchKeyword.value.trim()) {
    return
  }

  searching.value = true
  searchParams.value.current += 1

  try {
    let response
    if (type === 'posts') {
      response = await postApi.searchPostVOByKeyword(
        searchParams.value.current,
        searchKeyword.value.trim(),
        searchParams.value.pageSize,
        searchParams.value.sortField,
        searchParams.value.sortOrder
      )

      // 追加到现有结果
      searchResults.value.posts.push(...(response.data?.records || []))

      // 更新是否还有更多数据
      hasMore.value.posts = (response.data?.records?.length || 0) >= searchParams.value.pageSize

    } else {
      response = await userApi.searchUserVOByKeyword(
        searchParams.value.current,
        searchKeyword.value.trim(),
        searchParams.value.pageSize,
        searchParams.value.sortField,
        searchParams.value.sortOrder
      )

      // 追加到现有结果
      searchResults.value.users.push(...(response.data?.records || []))

      // 更新是否还有更多数据
      hasMore.value.users = (response.data?.records?.length || 0) >= searchParams.value.pageSize
    }

  } catch (error) {
    console.error('加载更多失败:', error)
    uni.showToast({
      title: '加载失败，请重试',
      icon: 'none'
    })
    // 回滚页码
    searchParams.value.current -= 1
  } finally {
    searching.value = false
  }
}

// 清空搜索
const clearSearch = () => {
  searchKeyword.value = ''
  suggestions.value = []
  showSuggestions.value = false
  selectedIndex.value = -1
  searchResults.value = {
    posts: [],
    users: []
  }
  // 重置分页参数
  searchParams.value.current = 1
  hasMore.value = {
    posts: true,
    users: true
  }
  total.value = {
    posts: 0,
    users: 0
  }
}

// 获取图标
const getTypeIcon = (type: string) => {
  switch (type) {
    case 'user': return '👤'
    case 'post': return '📝'
    case 'topic': return '🔥'
    default: return '🔍'
  }
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

// tab切换
const onTabChange = (tabName: string) => {
  activeTab.value = tabName

  // 如果当前tab没有数据但有搜索关键词，尝试加载数据
  if (searchKeyword.value.trim() && searchResults.value[tabName]?.length === 0) {
    // 重置分页参数
    searchParams.value.current = 1
    // 执行搜索
    performSearch(searchKeyword.value.trim())
  }
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
  showCommentPopup.value = false
  // 可以在这里刷新搜索结果
}

// 处理分享操作
const handleShare = (post: any) => {
  console.log('分享操作:', post)
}

// 处理用户相关操作
const handleUserFollow = async (user: any) => {
  uni.vibrateShort()
  const originalStatus = user.hasFollow

  // 立即更新UI
  user.hasFollow = !user.hasFollow

  try {
    // 调用关注API
    const { followApi } = await import('@/api/follow')
    await followApi.doFollow(user.id)

    uni.showToast({
      title: user.hasFollow ? '关注成功' : '已取消关注',
      icon: 'none'
    })
  } catch (error) {
    // 失败时回滚状态
    user.hasFollow = originalStatus
    console.error('关注操作失败:', error)
    uni.showToast({
      title: '操作失败，请重试',
      icon: 'none'
    })
  }
}

const goToUserProfile = (user: any) => {
  uni.navigateTo({
    url: `/pages/info/info?userId=${user.id}`
  })
}
</script>

<style lang="scss" scoped>
.search-page {
  height: 100vh;
  background: #ffffff;
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
  padding-bottom: 26rpx;

  .header-content {
    display: flex;
    align-items: center;
    height: 44px;
    padding: 0 24rpx;

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
      border-radius: 24rpx;
      border: 4rpx solid #e5e7eb;
      transition: all 0.3s ease;
      box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
      margin-right: 160rpx; // 为微信胶囊按钮留出空间

      &.focused {
        border-color: #3b82f6;
        background: #ffffff;
      }

      .search-input {
        width: 100%;
        height: 72rpx;
        padding: 0 120rpx 0 56rpx;
        font-size: 28rpx;
        color: #374151;
        background: transparent;
        border: none;
        outline: none;

        &::placeholder {
          color: #9ca3af;
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

      .clear-button {
        position: absolute;
        right: 20rpx;
        top: 50%;
        transform: translateY(-50%);
        display: flex;
        align-items: center;
        justify-content: center;
        width: 48rpx;
        height: 48rpx;
        border-radius: 50%;
        background: transparent;
        transition: all 0.3s ease;

        &:active {
          background: rgba(0, 0, 0, 0.05);
          transform: translateY(-50%) scale(0.95);
        }
      }
    }
  }
}

.search-content {
  flex: 1;
  padding: 32rpx 32rpx 0 32rpx; // 增加顶部内边距，避免被头部遮挡
}

.suggestions-dropdown {
  background: #ffffff;
  border: 2rpx solid #e5e7eb;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
  margin-bottom: 32rpx;
  max-height: 640rpx;
  overflow-y: auto;
}

.suggestions-content {
  padding: 16rpx 0;
}

.suggestions-header {
  padding: 16rpx 32rpx;
  border-bottom: 2rpx solid #f3f4f6;
}

.suggestions-title {
  font-size: 24rpx;
  color: #6b7280;
  font-weight: 500;
}

.suggestion-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover,
  &.selected {
    background: #eff6ff;
  }

  &:active {
    background: #f3f4f6;
  }
}

.suggestion-content {
  display: flex;
  align-items: center;
  flex: 1;
}

.suggestion-icon {
  font-size: 32rpx;
  margin-right: 24rpx;
}

.suggestion-text {
  color: #374151;
  font-size: 28rpx;
}

.suggestion-count {
  font-size: 24rpx;
  color: #9ca3af;
}

.no-suggestions {
  padding: 48rpx 32rpx;
  text-align: center;
  color: #6b7280;
  font-size: 28rpx;
}

.search-history {
  background: #f9fafb;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 32rpx;

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24rpx;
  }

  .section-title {
    font-size: 28rpx;
    font-weight: 500;
    color: #374151;
    display: flex;
    align-items: center;
  }

  .clear-history {
    display: flex;
    align-items: center;
    padding: 8rpx 12rpx;
    border-radius: 12rpx;
    background: transparent;
    transition: all 0.3s ease;

    &:active {
      background: #e5e7eb;
      transform: scale(0.95);
    }

    .clear-text {
      font-size: 24rpx;
      color: #6b7280;
    }
  }
}

.history-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;

  .history-tag {
    display: flex;
    align-items: center;
    background: #ffffff;
    border: 1rpx solid #e5e7eb;
    border-radius: 48rpx;
    padding: 8rpx 16rpx;
    transition: all 0.3s ease;

    &:active {
      background: #f3f4f6;
      transform: scale(0.98);
    }

    .tag-text {
      font-size: 26rpx;
      color: #374151;
      margin-right: 8rpx;
    }

    .tag-close {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 32rpx;
      height: 32rpx;
      border-radius: 50%;
      background: rgba(0, 0, 0, 0.05);
      transition: all 0.3s ease;

      &:active {
        background: rgba(0, 0, 0, 0.1);
        transform: scale(0.9);
      }
    }
  }
}

.hot-search {
  background: #f9fafb;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 32rpx;

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24rpx;
  }

  .section-title {
    font-size: 28rpx;
    font-weight: 500;
    color: #374151;
    display: flex;
    align-items: center;
  }
}

.hot-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;

  .hot-tag {
    display: flex;
    align-items: center;
    padding: 8rpx 24rpx;
    background: #ffffff;
    border-radius: 48rpx;
    cursor: pointer;
    transition: all 0.3s ease;
    border: 1rpx solid #e5e7eb;

    &:hover {
      background: #f3f4f6;
    }

    &:active {
      transform: scale(0.98);
    }

    &.hot-tag-trending {
      background: #fef2f2;
      color: #dc2626;
      border-color: #fecaca;

      &:hover {
        background: #fecaca;
      }

      .tag-text {
        color: #dc2626;
      }
    }

    .tag-text {
      font-size: 26rpx;
      color: #374151;
    }

    .hot-indicator {
      margin-left: 8rpx;
      font-size: 24rpx;
    }
  }
}

.hot-ranking-section {
  margin-bottom: 32rpx;
}

.search-results {
  margin-top: 32rpx;
}

.search-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 32rpx;

  .loading-text {
    font-size: 28rpx;
    color: #999;
    margin-top: 16rpx;
  }
}

.search-tabs-container {
  background: #ffffff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.posts-results {
  padding: 0 16rpx;
}

.users-results {
  padding: 24rpx;
}

.user-list {
  .user-item {
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 16rpx;
    display: flex;
    align-items: center;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
    transition: all 0.2s ease;
    position: relative;
    overflow: hidden;

    &:active {
      transform: scale(0.98);
      box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
    }

    &:last-child {
      margin-bottom: 0;
    }

    .user-avatar {
      position: relative;
      margin-right: 24rpx;

      .avatar-img {
        width: 96rpx;
        height: 96rpx;
        border-radius: 50%;
        background: #f0f0f0;
        border: 4rpx solid #fff;
        box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
      }
    }

    .user-info {
      flex: 1;
      min-width: 0;

      .user-name {
        font-size: 32rpx;
        font-weight: 600;
        color: #1a1a1a;
        margin-bottom: 8rpx;
        line-height: 1.3;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .user-profile {
        font-size: 26rpx;
        color: #8e8e93;
        line-height: 1.4;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        max-width: 400rpx;
      }
    }

    .user-action {
      margin-left: 24rpx;
      flex-shrink: 0;

      .follow-btn {
        min-width: 120rpx;
        height: 64rpx;
        border-radius: 32rpx;
        font-size: 26rpx;
        font-weight: 500;
        transition: all 0.2s ease;

        &:active {
          transform: scale(0.95);
        }
      }
    }
  }
}

.no-results {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 32rpx;

  .no-results-icon {
    margin-bottom: 32rpx;
  }

  .no-results-text {
    font-size: 32rpx;
    color: #666;
    margin-bottom: 16rpx;
    font-weight: 500;
  }

  .no-results-sub {
    font-size: 26rpx;
    color: #999;
  }
}

.bottom-space {
  height: 120rpx;
}

/* 响应式设计 */
@media (max-width: 750rpx) {
  .search-header .header-content {
    padding: 0 16rpx;

    .back-button {
      width: 56rpx;
      height: 56rpx;
      margin-right: 16rpx;
    }

    .search-input-container {
      margin-right: 140rpx; // 小屏幕上减少右边距

      .search-input {
        height: 64rpx;
        font-size: 26rpx;
      }
    }
  }

  .search-content {
    padding: 24rpx 24rpx 0 24rpx; // 小屏幕上也保持顶部间距
  }

  .search-history,
  .hot-search {
    padding: 24rpx;
  }

  .hot-ranking-section {
    margin-bottom: 24rpx;
  }

  .section-title {
    font-size: 26rpx;
  }

  .tag-text {
    font-size: 24rpx;
  }

  .users-results {
    padding: 16rpx;
  }

  .user-list .user-item {
    padding: 16rpx;

    .user-avatar .avatar-img {
      width: 80rpx;
      height: 80rpx;
    }

    .user-info .user-name {
      font-size: 28rpx;
    }

    .user-info .user-profile {
      font-size: 24rpx;
    }

    .user-action .follow-btn {
      min-width: 100rpx;
      height: 56rpx;
      font-size: 24rpx;
    }
  }

  .search-loading {
    padding: 60rpx 24rpx;
  }

  .no-results {
    padding: 80rpx 24rpx;

    .no-results-text {
      font-size: 28rpx;
    }

    .no-results-sub {
      font-size: 24rpx;
    }
  }

  /* 加载更多样式 */
  .load-more {
    padding: 40rpx 0;
    text-align: center;
  }

  .loading {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 20rpx;
    color: #999;
    font-size: 28rpx;
  }

  .load-more-btn {
    padding: 20rpx 40rpx;
    background: #f5f5f5;
    border-radius: 40rpx;
    color: #666;
    font-size: 28rpx;
    display: inline-block;
    cursor: pointer;
    transition: all 0.3s;
  }

  .load-more-btn:active {
    background: #e5e5e5;
    transform: scale(0.95);
  }

  .no-more {
    color: #999;
    font-size: 26rpx;
  }
}
</style>