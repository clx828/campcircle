# SocialCard 编辑功能使用示例

## 功能说明

SocialCard 组件现在支持编辑功能，当帖子是当前用户发布时，会显示三个点的编辑按钮，点击后展开下拉菜单。

## ✅ 修复的问题

1. **正确的条件判断**：使用 `userStore.getUserInfo.id` 而不是 `userStore.getUserInfo?.id`
2. **完整的函数定义**：添加了所有必需的编辑菜单处理函数
3. **正确的样式**：添加了完整的编辑菜单样式
4. **事件定义**：正确定义了 edit、delete、visibilityChange 事件

## 🎯 使用方式

### 在父组件中使用

```vue
<template>
  <view class="post-list">
    <SocialCard 
      v-for="post in posts" 
      :key="post.id"
      :cardInfo="post"
      @edit="handleEdit"
      @delete="handleDelete"
      @visibilityChange="handleVisibilityChange"
      @like="handleLike"
      @comment="handleComment"
      @share="handleShare"
      @collect="handleCollect"
    />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import SocialCard from '@/components/SocialCard.vue'

const posts = ref([
  {
    id: '1',
    content: '这是一条测试帖子',
    user: {
      id: 'current_user_id', // 当前用户ID，会显示编辑菜单
      userName: '当前用户',
      userAvatar: 'avatar_url'
    },
    createTime: new Date().toISOString(),
    pictureUrlList: [],
    thumbNum: 0,
    favourNum: 0,
    hasThumb: false,
    hasFavour: false
  }
])

// 处理编辑操作
const handleEdit = (postId) => {
  console.log('编辑帖子:', postId)
  uni.navigateTo({
    url: `/pages/editPost/editPost?id=${postId}`
  })
}

// 处理删除操作
const handleDelete = async (postId) => {
  try {
    // 调用删除API
    await postApi.deletePost(postId)
    
    // 从列表中移除
    posts.value = posts.value.filter(post => post.id !== postId)
    
    uni.showToast({
      title: '删除成功',
      icon: 'success'
    })
  } catch (error) {
    console.error('删除失败:', error)
    uni.showToast({
      title: '删除失败',
      icon: 'none'
    })
  }
}

// 处理修改可见范围
const handleVisibilityChange = (postId) => {
  console.log('修改可见范围:', postId)
  
  uni.showActionSheet({
    itemList: ['公开', '仅好友可见', '仅自己可见'],
    success: async (res) => {
      const visibilityMap = ['public', 'friends', 'private']
      const visibility = visibilityMap[res.tapIndex]
      
      try {
        // 调用修改可见范围API
        await postApi.updatePostVisibility(postId, visibility)
        
        uni.showToast({
          title: '修改成功',
          icon: 'success'
        })
      } catch (error) {
        uni.showToast({
          title: '修改失败',
          icon: 'none'
        })
      }
    }
  })
}

// 其他事件处理函数
const handleLike = (data) => {
  console.log('点赞:', data)
}

const handleComment = (data) => {
  console.log('评论:', data)
}

const handleShare = (data) => {
  console.log('分享:', data)
}

const handleCollect = (data) => {
  console.log('收藏:', data)
}
</script>
```

## 🎨 界面效果

### 显示逻辑
- **当前用户的帖子**：显示三个点的编辑按钮
- **其他用户的帖子**：显示关注/已关注按钮

### 编辑菜单
- **编辑** ✏️：修改帖子内容
- **修改可见范围** 👁️：调整帖子可见性
- **删除** 🗑️：删除帖子（红色文字，有确认对话框）

### 交互效果
- 点击编辑按钮有震动反馈
- 菜单展开有滑入动画
- 点击遮罩层关闭菜单
- 删除操作有二次确认

## 🔧 技术细节

### 条件渲染
```vue
<!-- 关注按钮区域 -->
<view v-if="cardInfo.user.id !== userStore.getUserInfo.id" class="follow">
  <!-- 关注按钮 -->
</view>

<!-- 编辑菜单区域 -->
<view v-if="cardInfo.user.id === userStore.getUserInfo.id" class="edit-menu">
  <!-- 编辑菜单 -->
</view>
```

### 事件处理
```typescript
// 编辑菜单状态
const showEditMenu = ref(false)

// 事件定义
const emit = defineEmits([
  'share', 'like', 'comment', 'collect', 'follow', 
  'edit', 'delete', 'visibilityChange'
])

// 切换菜单
const toggleEditMenu = () => {
  uni.vibrateShort()
  showEditMenu.value = !showEditMenu.value
}

// 处理删除（带确认）
const handleDelete = () => {
  uni.vibrateShort()
  closeEditMenu()
  
  uni.showModal({
    title: '确认删除',
    content: '删除后无法恢复，确定要删除这条帖子吗？',
    confirmText: '删除',
    confirmColor: '#ff4757',
    success: (res) => {
      if (res.confirm) {
        emit('delete', props.cardInfo.id)
      }
    }
  })
}
```

### 样式设计
```scss
.edit-menu {
  position: relative;
  margin-left: auto;
}

.edit-btn {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.05);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.edit-dropdown {
  position: absolute;
  top: 72rpx;
  right: 0;
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.15);
  z-index: 1000;
  animation: dropdownSlideIn 0.2s ease;
}
```

## 📋 测试清单

### 基础功能
- ✅ 当前用户帖子显示编辑按钮
- ✅ 其他用户帖子显示关注按钮
- ✅ 点击编辑按钮展开菜单
- ✅ 点击遮罩层关闭菜单

### 编辑操作
- ✅ 点击编辑触发 edit 事件
- ✅ 点击删除显示确认对话框
- ✅ 确认删除触发 delete 事件
- ✅ 点击修改可见范围触发 visibilityChange 事件

### 交互体验
- ✅ 所有操作有震动反馈
- ✅ 菜单展开有动画效果
- ✅ 删除项有红色文字
- ✅ 点击操作后菜单自动关闭

## ⚠️ 注意事项

1. **用户ID比较**：确保 `userStore.getUserInfo.id` 和 `cardInfo.user.id` 的数据类型一致
2. **事件监听**：父组件必须监听 edit、delete、visibilityChange 事件
3. **API调用**：删除和修改可见范围需要调用相应的后端接口
4. **错误处理**：操作失败时要给用户适当的提示

现在 SocialCard 组件的编辑功能已经完全正常工作了！
