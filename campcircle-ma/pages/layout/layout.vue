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

        <wd-tabbar-item :value="0" title="首页">
          <template v-if="activeTab !== 0" #icon>
            <wd-img round height="40rpx" width="40rpx" src="/static/img/tabbar/home.png" />
          </template>
          <template v-else #icon>
            <wd-img round height="40rpx" width="40rpx" src="/static/img/tabbar/homeactive.png" />
          </template>
        </wd-tabbar-item>

        <!-- 关注 -->
        <wd-tabbar-item :value="1" title="关注">
          <template v-if="activeTab !== 1" #icon>
            <wd-img round height="40rpx" width="40rpx" src="/static/img/tabbar/guanzhu.png" />
          </template>
          <template v-else #icon>
            <wd-img round height="40rpx" width="40rpx" src="/static/img/tabbar/guanzhuactive.png" />
          </template>
        </wd-tabbar-item>

        <!-- 发布 -->
        <wd-tabbar-item :value="2" title="发布">
          <template v-if="activeTab !== 2" #icon>
            <wd-img round height="40rpx" width="40rpx" src="/static/img/tabbar/add.png" />
          </template>
        </wd-tabbar-item>

        <!-- 消息 -->
        <wd-tabbar-item :value="3" title="消息">
          <template v-if="activeTab !== 3" #icon>
            <wd-img round height="40rpx" width="40rpx" src="/static/img/tabbar/news.png" />
          </template>
          <template v-else #icon>
            <wd-img round height="40rpx" width="40rpx" src="/static/img/tabbar/newsactive.png" />
          </template>
        </wd-tabbar-item>

        <!-- 我的 -->
        <wd-tabbar-item :value="4" title="我的">
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
import { ref } from 'vue'
import Home from '/pages/tabbar/home/home.vue'
import Follow from '/pages/tabbar/follow/follow.vue'
import Publish from '/pages/tabbar/release/release.vue'
import Info from '/pages/tabbar/info/info.vue'
import Mine from '/pages/tabbar/mine/mine.vue'
import { useUserStore } from '@/stores/userStore'


const activeTab = ref(0)
const userStore = useUserStore()
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



const handleChange = (index) => {
  uni.vibrateShort()
  console.log("这是", index)
  console.log('切换到tab:', tabPages.value[index.value].needLogin)
  if (tabPages.value[index.value].needLogin) {
    checkAuth()
  }
  return
}

const checkAuth = () => {
  const userInfo = userStore.getUserInfo
  const isLoggedIn = userInfo && userInfo.id > 0

  if (!isLoggedIn) {
    uni.navigateTo({
      url: '/pages/login/login'
    })
  }

  return isLoggedIn
}

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
