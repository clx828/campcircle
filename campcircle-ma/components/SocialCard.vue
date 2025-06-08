<template>
    <view class="social-card">
        <view class="header">
            <image :src="avatar" class="avatar" />
            <view class="user-info">
                <view class="nickname">{{ nickname }}</view>
                <view class="time">{{ time }}</view>
            </view>
        </view>
        <view class="content-text">{{ content }}</view>
        <view class="image-list" :class="imageClass">
            <template v-for="(img, idx) in images" :key="idx">
                <image :src="img" class="card-img" :style="imgStyle" mode="aspectFill" @click="preview(idx)" />
            </template>
        </view>
        <view class="actions">
            <button class="action-btn left-btn" @click="handleShare" hover-class="btn-hover">
                <image src="/static/button/zhuanfa.png" />
            </button>
            <view class="right-btns">
                <button class="action-btn" @click="handleLike" hover-class="btn-hover">
                    <image src="/static/button/xihuan.png" />
                </button>
                <button class="action-btn" @click="handleComment" hover-class="btn-hover">
                    <image src="/static/button/shouchang.png" />
                </button>
                <button class="action-btn" @click="handleCollect" hover-class="btn-hover">
                    <image src="/static/button/pinglun.png" />
                </button>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
const props = defineProps({
    avatar: { type: String, required: true },
    nickname: { type: String, required: true },
    time: { type: String, required: true },
    content: { type: String, required: true },
    images: { type: Array as () => string[], default: () => [] }
})

const emit = defineEmits(['share', 'like', 'comment', 'collect'])

const imageClass = computed(() => {
    if (props.images.length === 1) return 'single-img'
    if (props.images.length === 2) return 'two-img'
    if (props.images.length >= 3) return 'multi-img'
    return ''
})

const imgStyle = computed(() => {
    if (props.images.length === 1) {
        return 'width: 100%; height: auto; border-radius: 8rpx;'
    } else {
        // 多图时统一使用CSS控制尺寸
        return 'border-radius: 6rpx;'
    }
})

function preview(idx: number) {
    uni.previewImage({
        urls: props.images,
        current: props.images[idx]
    })
}

// 添加震动反馈的处理函数
function handleShare() {
    uni.vibrateShort()
    emit('share')
}

function handleLike() {
    uni.vibrateShort()
    emit('like')
}

function handleComment() {
    uni.vibrateShort()
    emit('comment')
}

function handleCollect() {
    uni.vibrateShort()
    emit('collect')
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
    .card-img {
        width: 100%;
        height: auto;
        max-height: 60vh;
        max-width: 100%;
        object-fit: contain;
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
    gap: 18rpx;
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

.action-btn image {
    width: 40rpx;
    height: 40rpx;
    display: block;
    border: none;
    transition: transform 0.15s ease;
}

// 按压效果
.btn-hover {
    transform: scale(0.95);
    background-color: rgba(0, 0, 0, 0.05);
}

.btn-hover image {
    transform: scale(0.9);
}

// 活跃状态效果
.action-btn:active {
    transform: scale(0.9);
    background-color: rgba(0, 0, 0, 0.1);
}

.action-btn:active image {
    transform: scale(0.85);
}
</style>