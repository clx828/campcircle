<template>
  <view class="layout">
    <view class="main-content">
      <Home v-if="activeTab === 0" />
      <Follow v-else-if="activeTab === 1" />
      <Publish v-else-if="activeTab === 2" />
      <Info v-else-if="activeTab === 3" />
      <Mine v-else-if="activeTab === 4" />
    </view>

    <view class="tabbar">
      <wd-tabbar v-model="activeTab" @change="handleChange" active-color="#ee0a24" inactive-color="#7d7e80">
        <!-- 首页 -->

        <wd-tabbar-item title="首页">
          <template v-if="activeTab !== 0" #icon>
            <wd-img round height="40rpx" width="40rpx" src="/static/img/tabbar/home.png" />
          </template>
          <template v-else #icon>
            <wd-img round height="40rpx" width="40rpx" src="/static/img/tabbar/homeactive.png" />
          </template>
        </wd-tabbar-item>

        <!-- 关注 -->
        <wd-tabbar-item  title="关注">
          <template v-if="activeTab !== 1" #icon>
            <wd-img round height="40rpx" width="40rpx" src="/static/img/tabbar/guanzhu.png" />
          </template>
          <template v-else #icon>
            <wd-img round height="40rpx" width="40rpx" src="/static/img/tabbar/guanzhuactive.png" />
          </template>
        </wd-tabbar-item>

        <!-- 发布 -->
        <wd-tabbar-item  title="发布">
          <template v-if="activeTab !== 2" #icon>
            <wd-img round height="40rpx" width="40rpx" src="/static/img/tabbar/add.png" />
          </template>
          <template v-else #icon>
            <wd-img round height="40rpx" width="40rpx" src="/static/img/tabbar/addactive.png" />
          </template>
        </wd-tabbar-item>

        <!-- 消息 -->
        <wd-tabbar-item :value="unReadCount" title="消息">
          <template v-if="activeTab !== 3" #icon>
            <wd-img round height="40rpx" width="40rpx" src="/static/img/tabbar/news.png" />
          </template>
          <template v-else #icon>
            <wd-img round height="40rpx" width="40rpx" src="/static/img/tabbar/newsactive.png" />
          </template>
        </wd-tabbar-item>

        <!-- 我的 -->
        <wd-tabbar-item  title="我的">
          <template v-if="activeTab !== 4" #icon>
            <wd-img round height="40rpx" width="40rpx" src="/static/img/tabbar/me.png" />
          </template>
          <template v-else #icon>
            <wd-img round height="40rpx" width="40rpx" src="/static/img/tabbar/meactive.png" />
          </template>
        </wd-tabbar-item>
      </wd-tabbar>
    </view>
  </view>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import Home from '/pages/tabbar/home/home.vue'
import Follow from '/pages/tabbar/follow/follow.vue'
import Publish from '/pages/tabbar/release/release.vue'
import Info from '/pages/tabbar/info/info.vue'
import Mine from '/pages/tabbar/mine/mine.vue'
import { useUserStore } from '@/stores/userStore'
import { onShow, onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'
import {messageApi} from '@/api/message'

const activeTab = ref(0)
const userStore = useUserStore()
const unReadCount = ref(0)
const tabPages = ref([
  {
    path: '/pages/tabbar/home/home',
    needLogin: false,
  },
  {
    path: '/pages/tabbar/follow/follow',
    needLogin: true,
  },
  {
    path: '/pages/tabbar/release/release',
    needLogin: true,
  },
  {
    path: '/pages/tabbar/info/info',
    needLogin: true,
  },
  {
    path: '/pages/tabbar/mine/mine',
    needLogin: false,
  }
])

const getUnReadCount = async() => {
  // 检查用户是否登录
  if (!userStore.getUserInfo.id || userStore.getUserInfo.id === 0) {
    unReadCount.value = 0
    return
  }

  try {
    const res = await messageApi.getUnReadMessageNum()
    if (res.code === 0) {
      // 确保数据是数字类型，并且不为负数
      unReadCount.value = Math.max(0, Number(res.data) || 0)
      console.log("获取到了未读消息数量:", unReadCount.value)
    } else {
      console.warn("获取未读消息数量失败:", res.message)
      unReadCount.value = 0
    }
  } catch (error) {
    console.error("获取未读消息数量异常:", error)
    unReadCount.value = 0
  }
}

const handleChange = (index) => {
  uni.vibrateShort()
  console.log("切换到tab:", index)

  // 确保index是数字
  const tabIndex = typeof index === 'object' ? index.value : index

  if (tabPages.value[tabIndex] && tabPages.value[tabIndex].needLogin) {
    if (!checkAuth()) {
      // 如果需要登录但用户未登录，不切换tab
      return
    }
  }

  // 切换tab
  activeTab.value = tabIndex
}

const checkAuth = () => {
  const userInfo = userStore.getUserInfo
  const isLoggedIn = userInfo && userInfo.id > 0

  if (!isLoggedIn) {
    uni.navigateTo({
      url: '/pages/login/login?redirect=pages/layout/layout'
    })
    return false
  }

  return true
}

// 配置小程序分享功能
onShareAppMessage(() => {
  console.log('主页面分享给朋友事件触发了')
  return {
    title: 'CampCircle - 校园社交平台',
    path: '/pages/layout/layout',
    imageUrl: 'https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-06-13_12f2e457-9cae-4ffa-a149-1f480ddc221d.png'
  }
})

onShareTimeline(() => {
  console.log('主页面分享到朋友圈事件触发了')
  return {
    title: 'CampCircle - 发现校园精彩生活',
    query: 'from=timeline',
    imageUrl: 'https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-06-13_12f2e457-9cae-4ffa-a149-1f480ddc221d.png'
  }
})
onMounted(() => {
  getUnReadCount()
})

// 页面显示时也更新未读消息数量
onShow(() => {
  getUnReadCount()
})
</script>

<style>
.tabbar {
  background-color: #ffffff;
  padding-bottom: 10px;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 99;
}
</style>
