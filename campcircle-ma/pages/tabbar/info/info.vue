<template>
  <view class="content">
    <text class="title">消息</text>
  </view>
</template>

<script setup lang="ts">
import { onShow, onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/userStore'
import RouterGuard from '@/utils/routerGuard'

// 在 onShow 生命周期中进行路由守卫
onShow(() => {
	console.log("触发了onShow")
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const route = currentPage?.route || ''


  // 检查是否需要登录
  if (RouterGuard.needAuth(route)) {
    const userStore = useUserStore()

    // 如果用户未登录，跳转到登录页
    if (!userStore.isLoggedIn) {
      console.log('TabBar页面需要登录，跳转到登录页面:', route)

      // 使用 nextTick 延迟执行跳转，避免可能的闪烁
        uni.navigateTo({
          url: `/pages/login/login?redirect=${encodeURIComponent(route)}`
        })
    }
  }
})

// 配置小程序分享功能
onShareAppMessage(() => {
  console.log('消息页面分享给朋友事件触发了')
  return {
    title: 'CampCircle - 校园消息互动平台',
    path: '/pages/tabbar/info/info',
    imageUrl: 'https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-06-13_12f2e457-9cae-4ffa-a149-1f480ddc221d.png'
  }
})

onShareTimeline(() => {
  console.log('消息页面分享到朋友圈事件触发了')
  return {
    title: '校园社交新体验 - CampCircle',
    query: 'from=timeline',
    imageUrl: 'https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-06-13_12f2e457-9cae-4ffa-a149-1f480ddc221d.png'
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
