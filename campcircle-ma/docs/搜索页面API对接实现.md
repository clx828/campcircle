# 搜索页面API对接实现

## 🎯 实现目标

将两个搜索接口对接到search.vue页面，实现真实的搜索功能，替换原有的模拟数据。

## ✅ 已实现的功能

### 1. API接口集成

**新增的搜索接口**：
- `postApi.searchPostVOByKeyword()` - 搜索帖子
- `userApi.searchUserVOByKeyword()` - 搜索用户

**接口参数**：
```typescript
searchPostVOByKeyword(current, keyWord, pageSize, sortField, sortOrder)
searchUserVOByKeyword(current, keyWord, pageSize, sortField, sortOrder)
```

### 2. 搜索功能实现

**核心搜索函数**：
```typescript
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
    
  } catch (error) {
    console.error('搜索失败:', error)
    uni.showToast({
      title: '搜索失败，请重试',
      icon: 'none'
    })
  } finally {
    searching.value = false
  }
}
```

### 3. 分页加载功能

**加载更多实现**：
```typescript
const loadMore = async (type: 'posts' | 'users') => {
  if (searching.value || !hasMore.value[type] || !searchKeyword.value.trim()) {
    return
  }

  searching.value = true
  searchParams.value.current += 1

  try {
    let response
    if (type === 'posts') {
      response = await postApi.searchPostVOByKeyword(...)
      searchResults.value.posts.push(...(response.data?.records || []))
    } else {
      response = await userApi.searchUserVOByKeyword(...)
      searchResults.value.users.push(...(response.data?.records || []))
    }
    
    // 更新是否还有更多数据
    hasMore.value[type] = (response.data?.records?.length || 0) >= searchParams.value.pageSize
    
  } catch (error) {
    console.error('加载更多失败:', error)
    // 回滚页码
    searchParams.value.current -= 1
  } finally {
    searching.value = false
  }
}
```

### 4. 用户关注功能

**关注/取消关注实现**：
```typescript
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
    uni.showToast({
      title: '操作失败，请重试',
      icon: 'none'
    })
  }
}
```

## 🎉 功能特性

### 1. 搜索体验
- ✅ **实时搜索**：输入关键词后按回车或点击搜索建议即可搜索
- ✅ **并行请求**：同时搜索帖子和用户，提高响应速度
- ✅ **搜索历史**：自动保存搜索历史，方便用户重复搜索
- ✅ **错误处理**：网络失败时有友好的错误提示

### 2. 分页加载
- ✅ **加载更多**：支持点击加载更多数据
- ✅ **状态管理**：正确管理加载状态和分页参数
- ✅ **数据追加**：新数据追加到现有列表，不覆盖
- ✅ **失败回滚**：加载失败时自动回滚页码

### 3. 结果展示
- ✅ **Tab切换**：帖子和用户结果分别展示
- ✅ **数量显示**：Tab标题显示搜索结果总数
- ✅ **空状态**：没有结果时显示友好的空状态提示
- ✅ **加载状态**：搜索和加载更多时显示加载动画

### 4. 交互功能
- ✅ **帖子操作**：支持评论、分享等操作
- ✅ **用户关注**：支持关注/取消关注用户
- ✅ **用户资料**：点击用户可跳转到用户资料页
- ✅ **即时反馈**：所有操作都有震动和提示反馈

## 📱 UI界面

### 搜索结果展示
```vue
<!-- 搜索结果tabs -->
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
  <view class="load-more">
    <view v-if="searching" class="loading">
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
```

### 用户结果展示
```vue
<!-- 用户结果 -->
<view v-if="activeTab === 'users'" class="users-results">
  <view class="user-list">
    <view v-for="user in searchResults.users" :key="user.id" class="user-item">
      <view class="user-avatar">
        <image :src="user.userAvatar" class="avatar-img" />
      </view>
      <view class="user-info">
        <view class="user-name">{{ user.userName }}</view>
        <view class="user-profile">{{ user.userProfile }}</view>
      </view>
      <view class="user-action">
        <wd-button @click.stop="handleUserFollow(user)">
          {{ user.hasFollow ? '已关注' : '关注' }}
        </wd-button>
      </view>
    </view>
  </view>
</view>
```

## 🔧 技术实现

### 1. 状态管理
```typescript
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
```

### 2. 错误处理
- **网络错误**：显示"搜索失败，请重试"提示
- **加载失败**：自动回滚页码，显示错误提示
- **关注失败**：回滚关注状态，显示操作失败提示

### 3. 性能优化
- **并行请求**：同时请求帖子和用户数据
- **按需加载**：只在需要时加载更多数据
- **状态缓存**：保持搜索结果状态，避免重复请求

## 🚀 使用效果

现在搜索页面具备完整的搜索功能：

1. **输入关键词** → 自动显示搜索建议
2. **按回车或点击建议** → 执行搜索
3. **查看结果** → 在帖子和用户tab间切换
4. **加载更多** → 点击加载更多按钮获取更多结果
5. **交互操作** → 评论帖子、关注用户等

搜索功能现在完全基于真实的后端API，提供了完整的搜索体验！
