<template>
    <view class="follow-page">
        <view class="header">
            <text class="title">关注</text>
        </view>

        <view class="content">
            <view class="share-section">
                <button class="share-btn" open-type="share">
                    <text>分享给朋友</text>
                </button>
                <button class="share-btn" @click="shareToTimeline">
                    <text>分享到朋友圈</text>
                </button>
            </view>

            <view class="follow-list">
                <!-- 这里可以添加关注列表内容 -->
                <text class="placeholder">关注功能开发中...</text>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'

// 配置分享给朋友
onShareAppMessage(() => {
    console.log('分享给朋友事件触发了')
    return {
        title: 'CampCircle - 校园社交平台',
        path: '/pages/tabbar/follow/follow',
        imageUrl: 'https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-06-13_12f2e457-9cae-4ffa-a149-1f480ddc221d.png'
    }
})

// 配置分享到朋友圈
onShareTimeline(() => {
    console.log('分享到朋友圈事件触发了')
    return {
        title: 'CampCircle - 发现校园精彩生活',
        query: 'from=timeline',
        imageUrl: 'https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-06-13_12f2e457-9cae-4ffa-a149-1f480ddc221d.png'
    }
})

// 手动触发分享到朋友圈
const shareToTimeline = () => {
    uni.vibrateShort()
    // 注意：朋友圈分享只能通过右上角菜单触发，这里只是示例
    uni.showToast({
        title: '请点击右上角菜单分享到朋友圈',
        icon: 'none',
        duration: 2000
    })
}
</script>

<style lang="scss" scoped>
.follow-page {
    min-height: 100vh;
    background-color: #f5f5f5;

    .header {
        background-color: #fff;
        padding: 20rpx 30rpx;
        border-bottom: 1rpx solid #eee;

        .title {
            font-size: 36rpx;
            font-weight: bold;
            color: #333;
        }
    }

    .content {
        padding: 30rpx;

        .share-section {
            background-color: #fff;
            border-radius: 20rpx;
            padding: 40rpx 30rpx;
            margin-bottom: 30rpx;

            .share-btn {
                width: 100%;
                height: 80rpx;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: #fff;
                border: none;
                border-radius: 40rpx;
                font-size: 32rpx;
                margin-bottom: 20rpx;
                display: flex;
                align-items: center;
                justify-content: center;

                &:last-child {
                    margin-bottom: 0;
                    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
                }

                &:active {
                    opacity: 0.8;
                }
            }
        }

        .follow-list {
            background-color: #fff;
            border-radius: 20rpx;
            padding: 40rpx 30rpx;
            text-align: center;

            .placeholder {
                color: #999;
                font-size: 28rpx;
            }
        }
    }
}
</style>