<template>
  <scroll-view class="scroll-content" scroll-y="true">
    <view style="height: 20vh;width: 100%; background: #f5f5f5;">顶部占位</view>

    <!-- 吸顶区域 -->
    <view class="sticky-header">
      <view class="title">这是内容区域(标题)</view>
    </view>

    <!-- 内容区域 -->
    <view class="content-list">
      <SocialCard v-for="(item, i) in cardList" :key="i" :avatar="item.avatar" :nickname="item.nickname"
        :time="item.time" :content="item.content" :images="item.images" />
    </view>
  </scroll-view>
</template>

<script setup lang="ts">
import SocialCard from '@/components/SocialCard.vue'

const cardList = [
  {
    avatar: 'https://randomuser.me/api/portraits/men/32.jpg',
    nickname: '小明',
    time: '1分钟前',
    content: '今天的天气真不错，适合出去玩！',
    images: ['https://picsum.photos/400/300']
  },
  {
    avatar: 'https://randomuser.me/api/portraits/women/45.jpg',
    nickname: '小红',
    time: '5分钟前',
    content: '分享一下我的美食照片！',
    images: ['https://picsum.photos/300/300', 'https://picsum.photos/301/301']
  },
  {
    avatar: 'https://randomuser.me/api/portraits/men/12.jpg',
    nickname: '老王',
    time: '10分钟前',
    content: '三张图片测试，看看排版效果如何。',
    images: [
      'https://picsum.photos/200/200',
      'https://picsum.photos/201/201',
      'https://picsum.photos/202/202'
    ]
  },
  {
    avatar: 'https://randomuser.me/api/portraits/women/22.jpg',
    nickname: '小美',
    time: '20分钟前',
    content: '多图测试，最多一排三个。',
    images: [
      'https://picsum.photos/210/210',
      'https://picsum.photos/211/211',
      'https://picsum.photos/212/212',
      'https://picsum.photos/213/213'
    ]
  }
]
</script>

<style lang="scss" scoped>
.scroll-content {
  height: 100vh;
  overflow: scroll;
}

.sticky-header {
  position: sticky;
  top: 10vh;
  background: #fff;
  z-index: 10;
  padding: 20rpx;
  border-bottom: 1px solid #eee;
}

.title {
  font-size: 32rpx;
  color: #333;
  font-weight: bold;
}

.content-list {
  padding: 20rpx;
}
</style>
