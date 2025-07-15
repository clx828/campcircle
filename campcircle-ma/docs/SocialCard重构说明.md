# SocialCard 组件重构说明

## 🎯 重构目标

将点赞、评论、收藏、关注等卡片操作逻辑从各个页面移动到 SocialCard 组件内部，实现组件的完全自治，简化使用方式。

## ✅ 已完成的变更

### 1. SocialCard 组件内部化

**点赞操作**：
- ✅ 移除 `@like` 事件emit
- ✅ 内部处理API调用和状态更新
- ✅ 失败时自动回滚状态
- ✅ 提供即时UI反馈

**收藏操作**：
- ✅ 移除 `@collect` 事件emit
- ✅ 内部处理API调用和状态更新
- ✅ 失败时自动回滚状态
- ✅ 显示操作成功/失败提示

**关注操作**：
- ✅ 移除 `@follow` 事件emit
- ✅ 内部处理API调用和状态更新
- ✅ 失败时自动回滚状态
- ✅ 显示操作成功/失败提示

**评论操作**：
- ✅ 完全内部化处理
- ✅ 直接在SocialCard内部集成CommentPopup组件
- ✅ 点击评论图标直接打开评论弹窗
- ✅ 在详情页（hideActions=true）时不执行任何操作

### 2. 页面代码简化

**home.vue**：
- ✅ 移除 `handleLike`、`handleCollect`、`handleFollow`、`handleComment` 函数
- ✅ 移除 `@like`、`@collect`、`@follow`、`@comment` 事件绑定
- ✅ 移除 CommentPopup 组件（已移至SocialCard内部）
- ✅ 保留 `@share` 事件处理

**postDetail.vue**：
- ✅ 移除 `handleFollow` 函数
- ✅ 移除 `@follow` 事件绑定
- ✅ 保持 `hideActions=true` 配置

**follow.vue**：
- ✅ 移除 `handleLike`、`handleComment`、`handleFollow` 函数
- ✅ 移除相关事件绑定
- ✅ 保留 `@share` 事件处理
- ✅ 评论操作自动导航到详情页

**search.vue**：
- ✅ 移除 `handleLike`、`handleCollect`、`handleComment`、`handleFollow` 函数
- ✅ 移除相关事件绑定
- ✅ 保留 `@share` 事件处理
- ✅ 评论操作自动导航到详情页

**mine.vue**：
- ✅ 无需修改（已经是简化使用方式）

## 🔧 技术实现

### 状态管理策略
```typescript
// 乐观更新 + 失败回滚
const handleLike = async () => {
    const originalState = props.cardInfo.hasThumb
    const originalCount = props.cardInfo.thumbNum
    
    // 立即更新UI
    props.cardInfo.hasThumb = !originalState
    props.cardInfo.thumbNum += props.cardInfo.hasThumb ? 1 : -1
    
    try {
        await postApi.doThumb({ postId: props.cardInfo.id })
    } catch (error) {
        // 失败时回滚
        props.cardInfo.hasThumb = originalState
        props.cardInfo.thumbNum = originalCount
        uni.showToast({ title: '操作失败', icon: 'error' })
    }
}
```

### 事件简化
```typescript
// 重构前
const emit = defineEmits(['share', 'like', 'comment', 'collect', 'follow', 'edit', 'delete', 'visibilityChange'])

// 重构后
const emit = defineEmits(['share', 'comment', 'edit', 'delete', 'visibilityChange'])
```

### 智能评论处理
```typescript
// 智能检测父组件是否监听了comment事件
function handleComment() {
    const hasCommentListener = getCurrentInstance()?.vnode.props?.onComment
    if (hasCommentListener) {
        // 触发评论事件（如home页面的评论弹窗）
        emit('comment', props.cardInfo.id, props.cardInfo.commentNum)
    } else {
        // 导航到详情页
        uni.navigateTo({ url: `/pages/postDetail/postDetail?id=${props.cardInfo.id}` })
    }
}
```

## 📱 使用方式

### 重构前（复杂）
```vue
<SocialCard
  :cardInfo="post"
  @like="handleLike"
  @collect="handleCollect"
  @comment="handleComment"
  @follow="handleFollow"
  @share="handleShare"
/>
```

### 重构后（简化）
```vue
<!-- 所有页面统一使用方式 -->
<SocialCard
  :cardInfo="post"
  @share="handleShare"
/>
```

## 🎉 重构收益

### 1. 代码简化
- **减少重复代码**：各页面不再需要重复实现相同的操作逻辑
- **统一行为**：所有页面的卡片操作行为完全一致
- **降低维护成本**：操作逻辑集中在组件内部，修改更容易

### 2. 用户体验提升
- **即时反馈**：操作立即反映在UI上，无需等待网络请求
- **失败处理**：网络失败时自动回滚，用户感知更好
- **统一交互**：所有页面的操作体验完全一致

### 3. 开发体验改善
- **使用简单**：只需传入数据，无需绑定大量事件
- **职责清晰**：组件自己管理自己的状态和行为
- **易于测试**：组件逻辑独立，便于单元测试

## 🚀 后续优化建议

1. **分享功能内部化**：考虑将分享逻辑也移入组件内部
2. **编辑功能完善**：完善编辑、删除、可见性修改的具体实现
3. **性能优化**：考虑添加防抖处理，避免重复点击
4. **错误处理**：增加更详细的错误处理和用户提示
5. **状态持久化**：考虑将操作状态同步到全局状态管理

## 📝 注意事项

1. **数据响应性**：直接修改 props 数据，确保父组件能感知变化
2. **网络处理**：所有网络请求都有失败回滚机制
3. **用户反馈**：重要操作都有相应的用户提示
4. **导航统一**：评论操作统一导航到帖子详情页

这次重构大大简化了 SocialCard 组件的使用方式，提升了代码的可维护性和用户体验。
