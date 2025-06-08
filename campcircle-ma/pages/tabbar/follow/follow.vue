<template>
    <view class="content">
        <text class="title">关注</text>
    </view>
</template>

<script setup lang="ts">
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/userStore'
import RouterGuard from '@/utils/routerGuard'

// 在 onShow 生命周期中进行路由守卫
onShow(() => {
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const route = currentPage?.route || ''

    // 确保是 tabBar 页面
    if (!RouterGuard.isTabBarPage(route)) {
        return
    }

    // 检查是否需要登录
    if (RouterGuard.needAuth(route)) {
        const userStore = useUserStore()

        // 如果用户未登录，跳转到登录页
        if (!userStore.isLoggedIn) {
            console.log('TabBar页面需要登录，跳转到登录页面:', route)

            // 使用 nextTick 延迟执行跳转，避免可能的闪烁
            uni.nextTick(() => {
                uni.navigateTo({
                    url: `/pages/login/login?redirect=${encodeURIComponent(route)}`
                })
            })
        }
    }
})
</script>

<style lang="scss" scoped>
.content {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100vh;

    .title {
        font-size: 32rpx;
        color: #333;
    }
}
</style>
