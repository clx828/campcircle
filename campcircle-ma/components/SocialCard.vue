<template>
    <view class="social-card">
        <view class="header">
            <image :src="cardInfo.user.userAvatar" class="avatar" />
            <view class="user-info">
                <view class="nickname">{{ cardInfo.user.userName }}</view>
                <view class="time">{{ formatTime(cardInfo.createTime) }}</view>
            </view>
            <view class="follow">
                <button v-if="!cardInfo.hasFollow && cardInfo.user.id !== userStore.getUserInfo?.id" class="follow-btn"
                    hover-class="follow-btn-hover" @click="handleFollow">关注</button>
                <button v-if="cardInfo.hasFollow && cardInfo.user.id !== userStore.getUserInfo?.id" class="followed-btn"
                    hover-class="followed-btn-hover" @click="handleFollow">已关注</button>
            </view>
        </view>
        <view class="content-text">{{ cardInfo.content }}</view>
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
        <view class="actions">
            <button class="action-btn left-btn" @click="handleShare" hover-class="btn-hover">
                <image src="/static/button/zhuanfa.png" />
            </button>
            <view class="right-btns">
                <view>
                    <button class="action-btn" @click="handleLike" hover-class="btn-hover">
                        <image v-if="!cardInfo.hasThumb" src="/static/button/xihuan.png" />
                        <image v-else src="/static/button/xihuan-active.png" />
                    </button>
                    <text v-if="cardInfo.thumbNum === 0">喜欢</text>
                    <text v-else>{{ cardInfo.thumbNum }}</text>
                </view>
                <view>
                    <button class="action-btn" @click="handleCollect" hover-class="btn-hover">
                        <image v-if="!cardInfo.hasFavour" src="/static/button/shouchang.png" />
                        <image v-else src="/static/button/shouchang-active.png" />
                    </button>
                    <text v-if="cardInfo.favourNum === 0">收藏</text>
                    <text v-else>{{ cardInfo.favourNum }}</text>
                </view>
                <view>
                    <button class="action-btn" @click="handleComment" hover-class="btn-hover">
                        <image src="/static/button/pinglun.png" />
                    </button>
                    <text v-if="cardInfo.commentNum === 0">评论</text>
                    <text v-else>{{ cardInfo.commentNum }}</text>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
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
}>()
const userStore = useUserStore()
console.log("这是当前用户信息")
const emit = defineEmits(['share', 'like', 'comment', 'collect', 'follow'])

const imageClass = computed(() => {
    if (props.cardInfo.pictureUrlList.length === 1) return 'single-img'
    if (props.cardInfo.pictureUrlList.length === 2) return 'two-img'
    if (props.cardInfo.pictureUrlList.length >= 3) return 'multi-img'
    return ''
})

const imgStyle = computed(() => {
    if (props.cardInfo.pictureUrlList.length === 1) {
        return 'width: 50%; height: auto; border-radius: 8rpx;'
    } else {
        return 'border-radius: 6rpx;'
    }
})

function preview(idx: number) {
    uni.previewImage({
        urls: props.cardInfo.pictureUrlList,
        current: props.cardInfo.pictureUrlList[idx]
    })
}

function handleShare() {
    uni.vibrateShort()
    emit('share')
}

// 处理喜欢
async function handleLike() {
    uni.vibrateShort()
    const newHasThumb = !props.cardInfo.hasThumb
    // 先更新本地状态
    emit('like', {
        id: props.cardInfo.id,
        hasThumb: newHasThumb,
        isRollback: false
    })

    // 发送请求
    try {
        await postApi.doThumb({
            postId: props.cardInfo.id  // 直接使用字符串 ID
        })
    } catch (error) {
        // 请求失败时回滚本地状态
        emit('like', {
            id: props.cardInfo.id,
            hasThumb: !newHasThumb,
            isRollback: true
        })
        uni.showToast({
            title: '操作失败',
            icon: 'error'
        })
    }
}

function handleComment() {
    uni.vibrateShort()
    emit('comment', props.cardInfo.id)
}

// 处理收藏
async function handleCollect() {
    uni.vibrateShort()
    const newHasFavour = !props.cardInfo.hasFavour
    // 先更新本地状态
    emit('collect', {
        id: props.cardInfo.id,
        hasFavour: newHasFavour,
        isRollback: false
    })

    // 显示收藏/取消收藏提示
    uni.showToast({
        title: newHasFavour ? '收藏成功' : '已取消收藏',
        icon: 'none',
        duration: 2000
    })

    // 发送请求
    try {
        await postApi.doFavour({
            postId: props.cardInfo.id  // 直接使用字符串 ID
        })
    } catch (error) {
        // 请求失败时回滚本地状态
        emit('collect', {
            id: props.cardInfo.id,
            hasFavour: !newHasFavour,
            isRollback: true
        })
        uni.showToast({
            title: '操作失败',
            icon: 'error'
        })
    }
}

// 处理关注
async function handleFollow() {
    uni.vibrateShort()
    const newHasFollow = !props.cardInfo.hasFollow
    // 先更新本地状态
    emit('follow', {
        id: props.cardInfo.user.id,
        hasFollow: newHasFollow,
        isRollback: false
    })

    // 发送请求
    try {
        await followApi.doFollow(props.cardInfo.user.id)
        // 显示关注/取消关注提示
        uni.showToast({
            title: newHasFollow ? '关注成功' : '已取消关注',
            icon: 'none',
            duration: 2000
        })
    } catch (error) {
        // 请求失败时回滚本地状态
        emit('follow', {
            id: props.cardInfo.user.id,
            hasFollow: !newHasFollow,
            isRollback: true
        })
        uni.showToast({
            title: '操作失败',
            icon: 'error'
        })
    }
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

.avatar {
    width: 88rpx;
    height: 88rpx;
    border-radius: 50%;
    margin-right: 20rpx;
    border: 3rpx solid #fff;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease;

    &:active {
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
    color: #1a1a1a;
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

.right-btns text {
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
</style>