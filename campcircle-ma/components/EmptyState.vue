<template>
    <view class="empty-state">
        <image class="empty-img" src="/static/img/enpty.png" mode="widthFix" />
        <view class="empty-text">{{ text }}</view>
    </view>
</template>

<script setup lang="ts">
const props = defineProps({
    text: {
        type: String,
        default: '空空如也~'
    }
})
</script>

<style scoped lang="scss">
.empty-state {
    width: 100%;
    height: 100%;
    /* 占父容器上半部分 */
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-end;
    /* 靠下对齐，更贴近上半部分底部 */
    padding-bottom: 40rpx;
    box-sizing: border-box;
}

.empty-img {
    width: 180rpx;
    margin-bottom: 24rpx;
}

.empty-text {
    font-size: 28rpx;
    color: #aaa;
}
</style>