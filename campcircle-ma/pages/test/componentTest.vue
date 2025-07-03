<template>
    <view class="test-page">
        <view class="header">
            <text class="title">组件测试页面</text>
        </view>
        
        <view class="test-section">
            <text class="section-title">CommentInput 测试</text>
            <button class="test-btn" @click="showCommentInput">显示评论输入框</button>
        </view>
        
        <view class="test-section">
            <text class="section-title">CommentPopup 测试</text>
            <button class="test-btn" @click="showCommentPopup">显示评论弹窗</button>
        </view>
        
        <!-- CommentInput 组件 -->
        <CommentInput 
            :show="inputShow"
            placeholder="测试评论输入..."
            @submit="handleCommentSubmit"
            @close="handleCommentClose"
        />
        
        <!-- CommentPopup 组件 -->
        <CommentPopup
            :show="popupShow"
            :postId="'test-post-id'"
            :commentNum="5"
            @close="handlePopupClose"
        />
    </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CommentInput from '../../components/CommentInput.vue'
import CommentPopup from '../../components/CommentPopup.vue'

const inputShow = ref(false)
const popupShow = ref(false)

function showCommentInput() {
    inputShow.value = true
}

function handleCommentSubmit(comment) {
    console.log('评论提交:', comment)
    uni.showToast({
        title: '评论提交成功',
        icon: 'success'
    })
}

function handleCommentClose() {
    inputShow.value = false
}

function showCommentPopup() {
    popupShow.value = true
}

function handlePopupClose() {
    popupShow.value = false
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
    text-align: center;
    
    .title {
        font-size: 36rpx;
        font-weight: bold;
        color: #333;
    }
}

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
    
    .test-btn {
        width: 100%;
        height: 80rpx;
        background: linear-gradient(135deg, #007AFF, #5856D6);
        color: #fff;
        border: none;
        border-radius: 12rpx;
        font-size: 28rpx;
        font-weight: 500;
    }
}
</style>
