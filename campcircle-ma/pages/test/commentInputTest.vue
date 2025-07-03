<template>
    <view class="test-page">
        <view class="header">
            <text class="title">CommentInput 组件测试</text>
        </view>
        
        <view class="content">
            <view class="test-section">
                <text class="section-title">测试说明：</text>
                <text class="description">1. 点击输入框触发弹出层</text>
                <text class="description">2. 测试发送按钮（有内容时send.png，无内容时send-none.png）</text>
                <text class="description">3. 测试匿名/实名切换过渡效果</text>
                <text class="description">4. 测试匿名模式暗黑主题切换（文字变白色系）</text>
                <text class="description">5. 测试键盘弹出时弹出层位置适配</text>
            </view>
            
            <view class="comment-container">
                <CommentInput 
                    placeholder="测试评论输入..."
                    @submit="handleCommentSubmit"
                    @close="handleCommentClose"
                />
            </view>
            
            <view class="result-section" v-if="lastComment">
                <text class="section-title">最后提交的评论：</text>
                <view class="comment-result">
                    <text class="comment-content">{{ lastComment.content }}</text>
                    <text class="comment-type">{{ lastComment.isAnonymous ? '匿名' : '实名' }}</text>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CommentInput from '../../components/CommentInput.vue'

const lastComment = ref(null)

function handleCommentSubmit(comment) {
    console.log('提交评论:', comment)
    lastComment.value = comment
    uni.showToast({
        title: `${comment.isAnonymous ? '匿名' : '实名'}评论提交成功`,
        icon: 'success'
    })
}

function handleCommentClose() {
    console.log('关闭评论输入')
}
</script>

<style lang="scss" scoped>
.test-page {
    min-height: 100vh;
    background: #f5f5f5;
    padding: 20rpx;
}

.header {
    background: #fff;
    padding: 40rpx 30rpx;
    border-radius: 16rpx;
    margin-bottom: 30rpx;
    
    .title {
        font-size: 36rpx;
        font-weight: bold;
        color: #333;
    }
}

.content {
    .test-section {
        background: #fff;
        padding: 30rpx;
        border-radius: 16rpx;
        margin-bottom: 30rpx;
        
        .section-title {
            font-size: 32rpx;
            font-weight: bold;
            color: #333;
            display: block;
            margin-bottom: 20rpx;
        }
        
        .description {
            font-size: 28rpx;
            color: #666;
            display: block;
            margin-bottom: 10rpx;
        }
    }
    
    .comment-container {
        background: #fff;
        padding: 30rpx;
        border-radius: 16rpx;
        margin-bottom: 30rpx;
    }
    
    .result-section {
        background: #fff;
        padding: 30rpx;
        border-radius: 16rpx;
        
        .section-title {
            font-size: 32rpx;
            font-weight: bold;
            color: #333;
            display: block;
            margin-bottom: 20rpx;
        }
        
        .comment-result {
            background: #f8f9fa;
            padding: 20rpx;
            border-radius: 12rpx;
            
            .comment-content {
                font-size: 28rpx;
                color: #333;
                display: block;
                margin-bottom: 10rpx;
            }
            
            .comment-type {
                font-size: 24rpx;
                color: #666;
                display: block;
            }
        }
    }
}
</style>
