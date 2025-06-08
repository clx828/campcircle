<template>
  <!-- 固定在顶部的头像，不影响下面布局 -->
  <view v-if="!isAtTop" class="fixed-top-header">
    <image :src="userInfo.userAvatar" class="fixed-avatar"></image>
  </view>

  <scroll-view class="profile-page" scroll-y="true" @scroll="onScrollThrottled">
    <!-- 用户信息区域 -->
    <view class="user-header">
      <view class="user-header__overlay">
        <!-- 用户头像和基本信息 -->
        <view class="user-basic-info">
          <AvatarUpload :url="userInfo.userAvatar" :width="60" :onChange="handleAvatarChange" />
          <view class="user-text-info">
            <view class="user-nickname">{{ userInfo.userName }}</view>
            <view class="user-id">UID: {{ userInfo.id }}</view>
          </view>
        </view>

        <!-- 用户简介 -->
        <view class="user-profile">
          {{ userInfo.userProfile }}
        </view>

        <!-- 统计数据和操作按钮 -->
        <view class="user-stats-actions">
          <wd-row>
            <!-- 统计数据 -->
            <wd-col :span="4">
              <view class="stat-item">
                <view class="stat-number">1</view>
                <view class="stat-label">关注</view>
              </view>
            </wd-col>
            <wd-col :span="4">
              <view class="stat-item">
                <view class="stat-number">0</view>
                <view class="stat-label">粉丝</view>
              </view>
            </wd-col>
            <wd-col :span="4">
              <view class="stat-item">
                <view class="stat-number">0</view>
                <view class="stat-label">被喜欢</view>
              </view>
            </wd-col>

            <!-- 操作按钮 -->
            <wd-col :span="8" :offset="4">
              <view class="action-buttons">
                <wd-button plain hairline size="small" type="info" class="edit-profile-btn">
                  编辑资料
                </wd-button>
                <wd-button plain size="small" icon="setting" type="info" class="settings-btn" />
              </view>
            </wd-col>
          </wd-row>
        </view>
      </view>
    </view>

    <!-- 内容区域 -->
    <view class="content-section">
      <view class="content-tabs">
        <wd-tabs v-model="tab">
          <wd-tab title="笔记 0">
          </wd-tab>
          <wd-tab title="收藏 0">
          </wd-tab>
          <wd-tab title="喜欢 0">
          </wd-tab>
        </wd-tabs>
      </view>
      <view class="tab-content">
        <SocialCard v-for="(item, i) in cardList" :key="i" :avatar="item.avatar" :nickname="item.nickname"
          :time="item.time" :content="item.content" :images="item.images" />
        <EmptyState text="暂无内容" v-if="cardList.length === 0" />
      </view>
    </view>

  </scroll-view>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { IUser } from '@/model/user'
import AvatarUpload from '@/components/AvatarUpload.vue'

import SocialCard from '@/components/SocialCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const cardList = [
  // {
  //   avatar: 'https://randomuser.me/api/portraits/men/32.jpg',
  //   nickname: '小明',
  //   time: '1分钟前',
  //   content: '今天的天气真不错，适合出去玩！',
  //   images: ['https://picsum.photos/400/300']
  // },
  // {
  //   avatar: 'https://randomuser.me/api/portraits/women/45.jpg',
  //   nickname: '小红',
  //   time: '5分钟前',
  //   content: '分享一下我的美食照片！',
  //   images: ['https://picsum.photos/300/300', 'https://picsum.photos/301/301']
  // },
  // {
  //   avatar: 'https://randomuser.me/api/portraits/men/12.jpg',
  //   nickname: '老王',
  //   time: '10分钟前',
  //   content: '三张图片测试，看看排版效果如何。',
  //   images: [
  //     'https://picsum.photos/200/200',
  //     'https://picsum.photos/201/201',
  //     'https://picsum.photos/202/202'
  //   ]
  // },
  // {
  //   avatar: 'https://randomuser.me/api/portraits/women/22.jpg',
  //   nickname: '小美',
  //   time: '20分钟前',
  //   content: '多图测试，最多一排三个。',
  //   images: [
  //     'https://picsum.photos/210/210',
  //     'https://picsum.photos/211/211',
  //     'https://picsum.photos/212/212',
  //     'https://picsum.photos/213/213'
  //   ]
  // }
]
// ===== 滚动监听相关 =====
const isAtTop = ref(true) // 是否在顶部

// 节流函数
const throttle = (func: Function, delay: number) => {
  let timeoutId: NodeJS.Timeout | null = null
  let lastExecTime = 0
  return function (...args: any[]) {
    const currentTime = Date.now()
    if (currentTime - lastExecTime > delay) {
      func.apply(this, args)
      lastExecTime = currentTime
    } else {
      if (timeoutId) clearTimeout(timeoutId)
      timeoutId = setTimeout(() => {
        func.apply(this, args)
        lastExecTime = Date.now()
      }, delay - (currentTime - lastExecTime))
    }
  }
}

// 监听滚动事件
const onScroll = (e: any) => {
  const { scrollTop: currentScrollTop } = e.detail
  isAtTop.value = currentScrollTop <= 50
}

// 使用节流的滚动监听
const onScrollThrottled = throttle(onScroll, 16)

const handleAvatarChange = (file: any) => {
  console.log("头像变更：", file)
}

// ===== 响应式数据 =====
const tab = ref<number>(0)

let userInfo: IUser = reactive({
  id: 10001,
  userName: '微信用户_10001',
  userAvatar: 'https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/public/1898735003367223297/2025-05-11_2f217692-cb17-4c64-8bcb-8afd1ed14b5a.webp',
  userProfile: '这个人很懒，还没有简介',
  userRole: 'user',
})

const userStore = useUserStore()
userInfo = {
  ...userStore.getUserInfo
}
</script>

<style lang="scss">
// ===== 固定顶部头像 =====
.fixed-top-header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 10vh;
  z-index: 999;
  display: flex;
  justify-content: center;
  align-items: center;

  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  background-color: rgba(0, 0, 0, 0.8);

  .fixed-avatar {
    margin-top: 15px;
    width: 30px;
    height: 30px;
    border-radius: 50%;
    border: 2px solid rgba(255, 255, 255, 0.8);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  }
}

// ===== 主容器 =====
.profile-page {
  height: 100vh;
  background-color: #ffffff;
  overflow: scroll;
}

// ===== 用户头部区域 =====
.user-header {
  min-height: 40vh;
  background-image: url("https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/public/1898735003367223297/2025-05-11_2f217692-cb17-4c64-8bcb-8afd1ed14b5a.webp");
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
  padding-top: var(--status-bar-height, 44px);
  box-sizing: border-box;
  overflow: hidden;

  &::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(to bottom,
        rgba(0, 0, 0, 0.1),
        rgba(0, 0, 0, 0.3) 50%,
        rgba(0, 0, 0, 0.8));
    pointer-events: none;
    z-index: 1;
  }

  &__overlay {
    position: relative;
    z-index: 2;
    height: 100%;
    padding: 0 15px;
    padding-top: 80px;
  }
}

// ===== 用户基本信息 =====
.user-basic-info {
  display: flex;
  align-items: center;
  margin-top: 40px;
  margin-bottom: 20px;

  .user-text-info {
    margin-left: 15px;
    flex: 1;
  }

  .user-nickname {
    font-size: 18px;
    font-weight: bold;
    color: #ffffff;
    margin-bottom: 10px;
  }

  .user-id {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.7);
  }
}

// ===== 用户简介 =====
.user-profile {
  font-size: 14px;
  color: #ffffff;
  margin-bottom: 25px;
  line-height: 1.4;
}

// ===== 统计数据和操作区域 =====
.user-stats-actions {
  margin-bottom: 30px;
  margin-left: -20px;

  :deep(.wd-col) {
    display: flex;
    justify-content: center;
    word-break: break-word;
  }
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  color: #ffffff;

  .stat-number {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 4px;
  }

  .stat-label {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.7);
  }
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
}

// ===== 内容区域 =====
.content-section {
  // background-color: #fafafa;
  border-top-left-radius: 15px;
  border-top-right-radius: 15px;
  padding-top: 8px;
  background: #fff;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
  margin-top: -3vh;
  position: relative;
  z-index: 2;
  min-height: 60vh;
}

.content-tabs {
  position: sticky;
  top: 10vh;
  z-index: 10;
  margin-top: 5px;
  // background-color: #fafafa;
  border-top-left-radius: 15px;
  border-top-right-radius: 15px;
}

.tabbar-placeholder {
  height: 90px;
}

// ===== 标签页内容 =====
.tab-content {
  padding: 15px;
  background-color: #fafafa;

  .content-item {
    padding: 15px;
    margin-bottom: 10px;
    background-color: #ffffff;
    border-radius: 8px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  }
}
</style>