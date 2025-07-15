<template>
    <view class="social-card" :class="{ 'hide-actions': hideActions }">
        <view class="header">
            <image class="avatar-img" :src="cardInfo.user.userAvatar" />
            <view class="user-info">
                <view class="nickname">{{ cardInfo.user.userName }}</view>
                <view class="time">{{ formatTime(cardInfo.createTime) }}</view>
            </view>
            <!-- 关注按钮区域 -->
            <view v-if="cardInfo.user.id !== userStore.getUserInfo.id" class="follow">
                <button v-if="!cardInfo.hasFollow" class="follow-btn"
                    hover-class="follow-btn-hover" @click="handleFollow">关注</button>
                <button v-if="cardInfo.hasFollow" class="followed-btn"
                    hover-class="followed-btn-hover" @click="handleFollow">已关注</button>
            </view>

            <!-- 编辑菜单区域 -->
            <view v-if="cardInfo.user.id === userStore.getUserInfo.id" class="edit-menu">
                <view class="edit-btn" hover-class="edit-btn-hover" @click="toggleEditMenu">
                    <text class="edit-dots">···</text>
                </view>

                <!-- 下拉菜单 -->
                <view v-if="showEditMenu" class="edit-dropdown" @click.stop>
                    <view class="dropdown-item" @click="handleEdit">
                        <image class="dropdown-icon" src="/static/button/postedit/edit.png"></image>
                        <text class="dropdown-text">编辑</text>
                    </view>
                    <view class="dropdown-item" @click="handleVisibilityChange">
                      <image class="dropdown-icon" src="/static/button/postedit/open.png"></image>
                        <text class="dropdown-text">设为公开</text>
                    </view>
                  <view class="dropdown-item" @click="handleVisibilityChange">
                        <image class="dropdown-icon" src="/static/button/postedit/lock.png"></image>
                        <text class="dropdown-text">设为私密</text>
                    </view>
                  <view class="dropdown-item" @click="handleVisibilityChange">
                    <image class="dropdown-icon" src="/static/button/postedit/top.png"></image>
                    <text class="dropdown-text">设为置顶</text>
                  </view>
                    <view class="dropdown-item delete-item" @click="handleDelete">
                      <image class="dropdown-icon" src="/static/button/postedit/delete.png"></image>
                        <text class="dropdown-text">删除</text>
                    </view>
                </view>

                <!-- 遮罩层 -->
                <view v-if="showEditMenu" class="edit-mask" @click="closeEditMenu"></view>
            </view>
        </view>
        <view class="content-text" @tap="handleContentClick">{{ cardInfo.content }}</view>
        <view v-if="cardInfo.pictureUrlList && cardInfo.pictureUrlList.length > 0" class="image-list"
            :class="imageClass">
            <template v-if="cardInfo.pictureUrlList.length === 1">
                <image :src="cardInfo.pictureUrlList[0]" class="card-img single" mode="widthFix" @click="preview(0)" />
            </template>
            <template v-else>
                <template v-for="(img, idx) in cardInfo.pictureUrlList" :key="idx">
                    <image v-if="img" :src="img" class="card-img" :style="imgStyle" mode="aspectFill"
                        @click="preview(idx)" />
                </template>
            </template>
        </view>
        <view v-if="!hideActions" class="actions">
            <button class="action-btn left-btn" @click="handleShare" hover-class="btn-hover">
                <image src="/static/button/zhuanfa.png" />
            </button>
            <view class="right-btns">
                <view>
                    <button class="action-btn" @click="handleLike" hover-class="btn-hover">
                        <image v-if="!cardInfo.hasThumb" src="/static/button/xihuan.png" />
                        <image v-else src="/static/button/xihuan-active.png" />
                    </button>
                    <text v-if="cardInfo.thumbNum === 0" class="btn-text">喜欢</text>
                    <text v-else class="btn-text">{{ cardInfo.thumbNum }}</text>
                </view>
                <view>
                    <button class="action-btn" @click="handleCollect" hover-class="btn-hover">
                        <image v-if="!cardInfo.hasFavour" src="/static/button/shouchang.png" />
                        <image v-else src="/static/button/shouchang-active.png" />
                    </button>
                    <text v-if="cardInfo.favourNum === 0" class="btn-text">收藏</text>
                    <text v-else class="btn-text">{{ cardInfo.favourNum }}</text>
                </view>
                <view>
                    <button class="action-btn" @click="handleComment" hover-class="btn-hover">
                        <image src="/static/button/pinglun.png" />
                    </button>
                    <text v-if="cardInfo.commentNum === 0" class="btn-text">评论</text>
                    <text v-else class="btn-text">{{ cardInfo.commentNum }}</text>
                </view>
            </view>
        </view>
    </view>


</template>

<script setup lang="ts">
import { computed, ref, getCurrentInstance } from 'vue'
import { postApi } from '@/api/post'
import { useUserStore } from '@/stores/userStore'
import { followApi } from '@/api/follow'
import { formatTime } from '@/utils/format'


interface UserInfo {
    id: string
    userName: string
    userAvatar: string
    userProfile: string | null
    userRole: string
    createTime: string
}

interface CardInfo {
    id: string
    content: string
    pictureUrlList: string[]
    thumbNum: number
    favourNum: number
    commentNum: number
    userId: string
    createTime: string
    updateTime: string
    tagList: string[]
    user: UserInfo
    hasThumb: boolean
    hasFavour: boolean
    hasFollow: boolean
}

const props = defineProps<{
    cardInfo: CardInfo
    hideActions?: boolean
}>()
const userStore = useUserStore()
const emit = defineEmits(['share', 'comment', 'edit', 'delete', 'visibilityChange'])

// 编辑菜单状态
const showEditMenu = ref(false)

// 根据图片数量计算图片容器的样式类
const imageClass = computed(() => {
    if (props.cardInfo.pictureUrlList.length === 1) return 'single-img'
    if (props.cardInfo.pictureUrlList.length === 2) return 'two-img'
    if (props.cardInfo.pictureUrlList.length >= 3) return 'multi-img'
    return ''
})

// 根据图片数量计算单个图片的样式
const imgStyle = computed(() => {
    if (props.cardInfo.pictureUrlList.length === 1) {
        return 'width: 50%; height: auto; border-radius: 8rpx;'
    } else {
        return 'border-radius: 6rpx;'
    }
})

// 预览图片
function preview(idx: number) {
    uni.previewImage({
        urls: props.cardInfo.pictureUrlList,
        current: props.cardInfo.pictureUrlList[idx]
    })
}

// 处理内容点击跳转
function handleContentClick() {
    if (!props.hideActions) {
        uni.navigateTo({
            url: `/pages/postDetail/postDetail?id=${props.cardInfo.id}`
        })
    }
}

// 处理分享操作 - 内部化处理
function handleShare() {
    uni.vibrateShort()
    // 可以在这里添加分享逻辑，暂时保留emit以便后续扩展
    emit('share', props.cardInfo)
}

// 处理喜欢
 // 处理点赞操作 - 完全内部化处理
const handleLike = async () => {
    uni.vibrateShort()
    const originalHasThumb = props.cardInfo.hasThumb
    const originalThumbNum = props.cardInfo.thumbNum

    // 立即更新本地状态以提供即时反馈
    props.cardInfo.hasThumb = !originalHasThumb
    props.cardInfo.thumbNum += props.cardInfo.hasThumb ? 1 : -1

    // 发送请求
    try {
        await postApi.doThumb({
            postId: props.cardInfo.id
        })
    } catch (error) {
        // 请求失败时回滚本地状态
        props.cardInfo.hasThumb = originalHasThumb
        props.cardInfo.thumbNum = originalThumbNum
        uni.showToast({
            title: '操作失败',
            icon: 'error'
        })
    }
}

// 处理评论操作 - 触发评论事件
function handleComment() {
    uni.vibrateShort()
    if (props.hideActions) {
        // 在帖子详情页，不做任何操作（因为已经在详情页了）
        return
    } else {
        // 触发评论事件
        emit('comment', props.cardInfo.id, props.cardInfo.commentNum)
    }
}

// 处理收藏
 // 处理收藏操作 - 完全内部化处理
const handleCollect = async () => {
    uni.vibrateShort()
    const originalHasFavour = props.cardInfo.hasFavour
    const originalFavourNum = props.cardInfo.favourNum

    // 立即更新本地状态以提供即时反馈
    props.cardInfo.hasFavour = !originalHasFavour
    props.cardInfo.favourNum += props.cardInfo.hasFavour ? 1 : -1

    // 显示收藏/取消收藏提示
    uni.showToast({
        title: props.cardInfo.hasFavour ? '收藏成功' : '已取消收藏',
        icon: 'none',
        duration: 2000
    })

    // 发送请求
    try {
        await postApi.doFavour({
            postId: props.cardInfo.id
        })
    } catch (error) {
        // 请求失败时回滚本地状态
        props.cardInfo.hasFavour = originalHasFavour
        props.cardInfo.favourNum = originalFavourNum
        uni.showToast({
            title: '操作失败',
            icon: 'error'
        })
    }
}

// 处理关注
 // 处理关注操作 - 完全内部化处理
const handleFollow = async () => {
    uni.vibrateShort()
    const originalHasFollow = props.cardInfo.hasFollow

    // 立即更新本地状态以提供即时反馈
    props.cardInfo.hasFollow = !originalHasFollow

    // 发送请求
    try {
        await followApi.doFollow(props.cardInfo.user.id)
        // 显示关注/取消关注提示
        uni.showToast({
            title: props.cardInfo.hasFollow ? '关注成功' : '已取消关注',
            icon: 'none',
            duration: 2000
        })
    } catch (error) {
        // 请求失败时回滚本地状态
        props.cardInfo.hasFollow = originalHasFollow
        uni.showToast({
            title: '操作失败',
            icon: 'error'
        })
    }
}

// 编辑菜单相关函数
// 切换编辑菜单显示状态
const toggleEditMenu = () => {
    uni.vibrateShort()
    showEditMenu.value = !showEditMenu.value
}

// 关闭编辑菜单
const closeEditMenu = () => {
    showEditMenu.value = false
}

// 处理编辑操作
const handleEdit = () => {
    uni.vibrateShort()
    closeEditMenu()
    emit('edit', props.cardInfo.id)
}

// 处理删除操作
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

// 处理修改可见范围操作
const handleVisibilityChange = () => {
    uni.vibrateShort()
    closeEditMenu()
    emit('visibilityChange', props.cardInfo.id)
}
</script>

<style lang="scss" scoped>
.social-card {
    background: linear-gradient(135deg, #ffffff 0%, #fafafa 100%);
    border-radius: 16rpx;
    box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);
    margin: 24rpx 0;
    padding: 32rpx 24rpx 20rpx 24rpx;
    border: 1rpx solid rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(10rpx);
    position: relative;
    overflow: hidden;

    &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 2rpx;
        opacity: 0.8;
    }
}

.header {
    display: flex;
    align-items: center;
    margin-bottom: 16rpx;
}

.avatar-img {
    width: 88rpx;
    height: 88rpx;
    border-radius: 50%;
    margin-right: 20rpx;
    border: 3rpx solid #fff;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease;

    &.active {
        transform: scale(0.95);
    }
}

.user-info {
    display: flex;
    flex-direction: column;
    justify-content: center;
    flex: 1;
}

.nickname {
    font-weight: 600;
    font-size: 32rpx;
    color: #576b95;
    letter-spacing: 0.5rpx;
    margin-bottom: 2rpx;
}

.time {
    font-size: 24rpx;
    color: #999;
    font-weight: 400;
}

.content-text {
    font-size: 30rpx;
    color: #333;
    margin-bottom: 20rpx;
    line-height: 1.7;
    word-break: break-all;
    letter-spacing: 0.3rpx;
}

.image-list {
    display: flex;
    flex-wrap: wrap;
    gap: 12rpx;
    margin-bottom: 20rpx;
}

.single-img {
    .card-img.single {
        width: 50%;
        height: auto;
        border-radius: 12rpx;
        box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
    }
}

.two-img,
.multi-img {
    .card-img {
        width: calc((100vw - 104rpx) / 3 - 8rpx);
        height: calc((100vw - 104rpx) / 3 - 8rpx);
        object-fit: cover;
        border-radius: 10rpx;
        flex-shrink: 0;
        box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
        transition: transform 0.2s ease;

        &:active {
            transform: scale(0.98);
        }
    }
}

.card-img {
    border-radius: 12rpx;
    background: #f8f8f8;
}

.actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 16rpx;
    padding-top: 16rpx;
    border-top: 1rpx solid rgba(0, 0, 0, 0.05);
}

.left-btn {
    margin-left: 0;
    padding-left: 0;
}

.right-btns {
    display: flex;
    align-items: center;
    gap: 16rpx;
}

.right-btns>view {
    display: flex;
    flex-direction: row;
    align-items: center;
}

.action-btn {
    background: none;
    border: none;
    outline: none;
    padding: 12rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 64rpx;
    min-width: 64rpx;
    border-radius: 50%;
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;

    &::after {
        border: none;
    }
}

.right-btns .btn-text {
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    font-size: 24rpx;
    color: #666;
    line-height: 1;
    margin-left: 4rpx;
    font-weight: 500;
}

.action-btn image {
    width: 44rpx;
    height: 44rpx;
    display: block;
    border: none;
    transition: transform 0.2s ease;
}

.btn-hover {
    transform: scale(0.92);
    background-color: rgba(251, 0, 85, 0.08);
}

.btn-hover image {
    transform: scale(0.9);
}

.action-btn:active {
    transform: scale(0.88);
    background-color: rgba(251, 0, 85, 0.12);
}

.action-btn:active image {
    transform: scale(0.85);
}

.follow {
    margin-left: auto;
    padding-right: 0;
}

.follow-btn,
.followed-btn {
    font-size: 26rpx;
    font-weight: 500;
    padding: 12rpx 28rpx;
    border-radius: 32rpx;
    border: none;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    line-height: 1.4;
    min-width: 120rpx;
    height: 56rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    letter-spacing: 0.5rpx;
    position: relative;
    overflow: hidden;

    &::after {
        border: none;
    }

    &::before {
        content: '';
        position: absolute;
        top: 0;
        left: -100%;
        width: 100%;
        height: 100%;
        background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
        transition: left 0.5s ease;
    }
}

.follow-btn {
    background: linear-gradient(135deg, #fb0055 0%, #ff1744 100%);
    color: #fff;
    box-shadow: 0 6rpx 20rpx rgba(251, 0, 85, 0.25);

    &:hover::before {
        left: 100%;
    }
}

.followed-btn {
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    color: #6c757d;
    border: 2rpx solid #dee2e6;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.follow-btn-hover {
    transform: translateY(-2rpx) scale(0.98);
    box-shadow: 0 8rpx 24rpx rgba(251, 0, 85, 0.35);
}

.followed-btn-hover {
    transform: translateY(-1rpx) scale(0.98);
    background: linear-gradient(135deg, #e9ecef 0%, #dee2e6 100%);
    box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.08);
}

// 隐藏操作按钮时的样式
.hide-actions {
    box-shadow: none !important;

    .actions {
        display: none;
    }
}

// 编辑菜单样式
.edit-menu {
    position: relative;
    margin-left: auto;
}

.edit-btn {
    width: 64rpx;
    height: 64rpx;
    background: none;
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;
    margin: 0;

    .edit-dots {
        font-size: 32rpx;
        font-weight: bold;
        color: #666;
        line-height: 1;
        letter-spacing: 2rpx;
    }
}

.edit-btn-hover {
    background: none;
    transform: scale(0.95);
}

.edit-dropdown {
    position: absolute;
    top: 72rpx;
    right: 0;
    background: #fff;
    border-radius: 16rpx;
    box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.15);
    border: 1rpx solid #f0f0f0;
    z-index: 1000;
    min-width: 250rpx;
    overflow: hidden;
    animation: dropdownSlideIn 0.2s ease;
}

@keyframes dropdownSlideIn {
    from {
        opacity: 0;
        transform: translateY(-10rpx) scale(0.95);
    }
    to {
        opacity: 1;
        transform: translateY(0) scale(1);
    }
}

.dropdown-item {
    display: flex;
    align-items: center;
    padding: 24rpx 32rpx;
    transition: background-color 0.2s ease;

    &:not(:last-child) {
        border-bottom: 1rpx solid #f5f5f5;
    }

    &:active {
        background: #f8f9fa;
    }

    &.delete-item {
        .dropdown-text {
            color: #ff4757;
        }

        &:active {
            background: rgba(255, 71, 87, 0.05);
        }
    }
}

.dropdown-icon {
    width: 32rpx;
    height: 32rpx;
    margin-right: 20rpx;
    opacity: 0.8;
    display: block;
}

.dropdown-text {
    font-size: 28rpx;
    color: #333;
    font-weight: 500;
}

.edit-mask {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 999;
    background: transparent;
}
</style>