<template>
    <view class="social-card">
        <view class="header">
            <image :src="cardInfo.user.userAvatar" class="avatar" />
            <view class="user-info">
                <view class="nickname">{{ cardInfo.user.userName }}</view>
                <view class="time">{{ formatTime(cardInfo.createTime) }}</view>
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
}

const props = defineProps<{
    cardInfo: CardInfo
}>()

const emit = defineEmits(['share', 'like', 'comment', 'collect'])

// 格式化时间
const formatTime = (time: string) => {
    const date = new Date(time)
    const now = new Date()
    const diff = now.getTime() - date.getTime()

    // 小于1分钟
    if (diff < 60000) {
        return '刚刚'
    }
    // 小于1小时
    if (diff < 3600000) {
        return `${Math.floor(diff / 60000)}分钟前`
    }
    // 小于24小时
    if (diff < 86400000) {
        return `${Math.floor(diff / 3600000)}小时前`
    }
    // 小于30天
    if (diff < 2592000000) {
        return `${Math.floor(diff / 86400000)}天前`
    }
    // 大于30天
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

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
</script>

<style lang="scss" scoped>
.social-card {
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    margin: 20rpx 0;
    padding: 24rpx 20rpx 16rpx 20rpx;
}

.header {
    display: flex;
    align-items: center;
    margin-bottom: 12rpx;
}

.avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    margin-right: 18rpx;
}

.user-info {
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.nickname {
    font-weight: bold;
    font-size: 30rpx;
    color: #222;
}

.time {
    font-size: 22rpx;
    color: #aaa;
    margin-top: 4rpx;
}

.content-text {
    font-size: 30rpx;
    color: #333;
    margin-bottom: 16rpx;
    line-height: 1.6;
    word-break: break-all;
}

.image-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8rpx;
    margin-bottom: 16rpx;
}

.single-img {
    .card-img.single {
        width: 50%;
        height: auto;
        border-radius: 8rpx;
    }
}

.two-img,
.multi-img {
    .card-img {
        width: calc((100vw - 80rpx) / 3 - 8rpx);
        height: calc((100vw - 80rpx) / 3 - 8rpx);
        object-fit: cover;
        border-radius: 6rpx;
        flex-shrink: 0;
    }
}

.card-img {
    border-radius: 8rpx;
    background: #f5f5f5;
}

.actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 10rpx;
}

.left-btn {
    margin-left: 0;
    padding-left: 0;
}

.right-btns {
    display: flex;
    align-items: center;
    gap: 8rpx;
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
    padding: 8rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 60rpx;
    min-width: 60rpx;
    border-radius: 50%;
    transition: all 0.2s ease;
    position: relative;

    &::after {
        border: none;
    }
}

.right-btns text {
    font-family: "宋体", SimSun, "宋体_GB2312", SimSun_GB2312;
    font-size: 14px;
    color: #666666;
    line-height: 1;
    margin-left: -3px;
}

.action-btn image {
    width: 40rpx;
    height: 40rpx;
    display: block;
    border: none;
    transition: transform 0.15s ease;
}

.btn-hover {
    transform: scale(0.95);
    background-color: rgba(0, 0, 0, 0.05);
}

.btn-hover image {
    transform: scale(0.9);
}

.action-btn:active {
    transform: scale(0.9);
    background-color: rgba(0, 0, 0, 0.1);
}

.action-btn:active image {
    transform: scale(0.85);
}
</style>