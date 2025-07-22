<template>
  <!-- 固定在顶部的头像，不影响下面布局 -->
  <view v-if="!isAtTop" class="fixed-top-header" :style="headerStyle">
    <image :src="userInfo.userAvatar" class="fixed-avatar" :style="avatarStyle"></image>
  </view>

  <scroll-view class="profile-page" scroll-y="true" @scroll="onScrollThrottled" @scrolltolower="loadMore"
    refresher-enabled="true" :refresher-triggered="refresherTriggered" @refresherrefresh="onRefresh"
    refresher-background="#ffffff"
    @refresherpulling="onRefresherPulling"
    @refresherrestore="onRefresherRestore">
    <!-- 用户信息区域 -->
    <view class="user-header">
      <!-- 背景图片 -->
      <image :src="userInfo.backgroundUrl" class="bg-image" mode="aspectFill" :style="bgImageStyle">
      </image>
      <view class="user-header__overlay">
        <!-- 用户头像和基本信息 -->
        <view class="user-basic-info">
          <AvatarUpload :url="userInfo.userAvatar" :width="60" :onChange="handleAvatarChange" />
          <view class="user-text-info">
            <view class="user-nickname">{{ userInfo.userName }}</view>
            <view class="user-id-container">
              <text class="user-id">UID: {{ userInfo.id }}</text>
              <image src="/static/button/qr/qr.png" class="qr-icon" @click="showQrCode"></image>
            </view>
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
              <view class="stat-item" @click="goToFollowList">
                <view class="stat-number">{{ followNum }}</view>
                <view class="stat-label">关注</view>
              </view>
            </wd-col>
            <wd-col :span="4">
              <view class="stat-item" @click="goToFansList">
                <view class="stat-number">{{ fansNum }}</view>
                <view class="stat-label">粉丝</view>
              </view>
            </wd-col>
            <wd-col :span="4">
              <view class="stat-item" @click="showThumbModal">
                <view class="stat-number">{{ thumbNum2 }}</view>
                <view class="stat-label">获赞</view>
              </view>
            </wd-col>

            <!-- 操作按钮 -->
            <wd-col :span="8" :offset="4">
              <view class="action-buttons">
                <wd-button plain hairline size="small" type="info" class="edit-profile-btn" @click="goToEditProfile">
                  编辑资料
                </wd-button>
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
          <wd-tab :title="`笔记 ${ownPostNum}`">
          </wd-tab>
          <wd-tab :title="`收藏 ${favourPostNum}`">
          </wd-tab>
          <wd-tab :title="`喜欢 ${thumbPostNum}`">
          </wd-tab>
        </wd-tabs>
      </view>
      <view class="tab-content" :class="{ 'tab-content--empty': cardList.length === 0 }">
        <SocialCard
          :cardInfo="item"
          v-for="(item, i) in cardList"
          :key="i"
          :ref="(el) => setSocialCardRef(el, item.id)"
          @comment="handleComment"
          @share="handleShare"
          @edit="handleEdit"
          @delete="handleDelete"
          @menuToggle="handleMenuToggle"
          @visibilityChange="handleVisibilityChange"
        />
        <EmptyState text="暂无内容" v-if="cardList.length === 0 && !loading" />
        <view v-if="loading" class="loading-state">
          <wd-loading />
        </view>
        <view v-if="!hasMore && cardList.length > 0" class="no-more-state">
          <text class="no-more-text">没有更多了</text>
        </view>
      </view>
    </view>

  </scroll-view>

  <!-- 全局评论弹窗 -->
  <CommentPopup
      v-model:show="showCommentPopup"
      :comment-num="commentNum"
      :post-id="currentPostId"
      @close="handleCommentPopupClose"
      @comment-success="handleCommentSuccess"
  />

  <!-- 获赞弹窗 -->
  <wd-popup v-model="showThumb" position="center" :close-on-click-modal="false" custom-style="">
    <view class="thumb-modal">
      <view class="thumb-image">
        <image src="/static/img/thumbBg.png" mode="widthFix" class="bg-image" />
      </view>
      <view class="thumb-content">
        <text class="thumb-text">"{{ userInfo.userName }}" 共获得 {{ thumbNum2 }} 个赞</text>
      </view>
      <view class="thumb-actions">
        <button class="thumb-button" @click="closeThumbModal">确认</button>
      </view>
    </view>
  </wd-popup>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, watch, computed } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { IUser } from '@/model/user'
import AvatarUpload from '@/components/AvatarUpload.vue'
import SocialCard from '@/components/SocialCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import CommentPopup from '@/components/CommentPopup.vue'
import { postApi } from '@/api/post'
import { followApi } from '@/api/follow'
import { useRouter } from 'vue-router'
import { onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'

// ===== 响应式数据 =====
const tab = ref<number>(0)
const cardList = ref<any[]>([])
const loading = ref(false)
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
const hasMore = ref(true) // 是否还有更多数据
const ownPostNum = ref(0)
const favourPostNum = ref(0)
const thumbPostNum = ref(0)
const followNum = ref(0)
const fansNum = ref(0)
const thumbNum2 = ref(0) // 避免与已有 thumbPostNum 冲突

// 评论弹窗相关
const showCommentPopup = ref(false)
const currentPostId = ref('')
const commentNum = ref(0)

// 获取帖子数量
const fetchPostNums = async () => {
  try {
    const res = await postApi.getMyPostNum()
    if (res.code === 0 && res.data) {
      ownPostNum.value = res.data.ownPostNum
      favourPostNum.value = res.data.favourPostNum
      thumbPostNum.value = res.data.thumbPostNum
    }
  } catch (error) {
    console.error('获取帖子数量失败:', error)
  }
}

// 获取关注/粉丝/被喜欢数
const fetchFollowNums = async () => {
  try {
    const res = await followApi.getMyFollowNum()
    if (res.code === 0 && res.data) {
      followNum.value = Number(res.data.followingCount)
      fansNum.value = Number(res.data.followersCount)
      thumbNum2.value = Number(res.data.likesCount)
    }
  } catch (error) {
    console.error('获取关注/粉丝/被喜欢数失败:', error)
  }
}

// 获取笔记列表
const fetchMyPosts = async (isLoadMore = false) => {
  console.log('fetchMyPosts')
  try {
    loading.value = true
    const res = await postApi.listMyPostVOByPage({
      current: current.value,
      pageSize: pageSize.value
    })
    if (res.code === 0) {
      if (isLoadMore) {
        cardList.value = [...cardList.value, ...res.data.records]
      } else {
        cardList.value = res.data.records
      }
      total.value = res.data.total
      hasMore.value = cardList.value.length < res.data.total
    }
  } catch (error) {
    console.error('获取笔记列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取收藏列表
const fetchFavourPosts = async (isLoadMore = false) => {
  try {
    loading.value = true
    const res = await postApi.listMyFavourPostVOByPage({
      current: current.value,
      pageSize: pageSize.value
    })
    if (res.code === 0) {
      if (isLoadMore) {
        cardList.value = [...cardList.value, ...res.data.records]
      } else {
        cardList.value = res.data.records
      }
      total.value = res.data.total
      hasMore.value = cardList.value.length < res.data.total
    }
  } catch (error) {
    console.error('获取收藏列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取喜欢列表
const fetchLikePosts = async (isLoadMore = false) => {
  try {
    loading.value = true
    const res = await postApi.listMyThumbPostVOByPage({
      current: current.value,
      pageSize: pageSize.value
    })
    if (res.code === 0) {
      if (isLoadMore) {
        cardList.value = [...cardList.value, ...res.data.records]
      } else {
        cardList.value = res.data.records
      }
      total.value = res.data.total
      hasMore.value = cardList.value.length < res.data.total
    }
  } catch (error) {
    console.error('获取喜欢列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载更多数据
const loadMore = async () => {
  if (loading.value || !hasMore.value) return

  current.value++
  switch (tab.value) {
    case 0: // 笔记
      await fetchMyPosts(true)
      break
    case 1: // 收藏
      await fetchFavourPosts(true)
      break
    case 2: // 喜欢
      await fetchLikePosts(true)
      break
  }
}

// 监听tab变化
watch(tab, (newTab) => {
  uni.vibrateShort()
  current.value = 1
  cardList.value = []
  hasMore.value = true
  switch (newTab) {
    case 0: // 笔记
      fetchMyPosts()
      break
    case 1: // 收藏
      fetchFavourPosts()
      break
    case 2: // 喜欢
      fetchLikePosts()
      break
  }
})

// 下拉刷新
const onRefresh = async () => {
  refresherTriggered.value = true
  try {
    current.value = 1
    hasMore.value = true
    await fetchPostNums()
    await fetchFollowNums()
    switch (tab.value) {
      case 0:
        await fetchMyPosts()
        break
      case 1:
        await fetchFavourPosts()
        break
      case 2:
        await fetchLikePosts()
        break
    }
  } catch (error) {
    console.error('刷新失败:', error)
  } finally {
    refresherTriggered.value = false
  }
}

// 根据当前tab加载数据
const loadData = () => {
  current.value = 1
  cardList.value = []
  hasMore.value = true
  switch (tab.value) {
    case 0: // 笔记
      fetchMyPosts()
      break
    case 1: // 收藏
      fetchFavourPosts()
      break
    case 2: // 喜欢
      fetchLikePosts()
      break
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchMyPosts()
  fetchPostNums()
  fetchFollowNums()
})



// ===== 下拉刷新相关 =====
const refresherTriggered = ref(false)

// ===== 滚动监听相关 =====
const isAtTop = ref(true) // 是否在顶部
const scrollTop = ref(0) // 当前滚动位置

// ===== 背景图片动画相关 =====
const bgImageStyle = ref({
  transform: 'scale(1)',
  transformOrigin: 'center top',
  transition: 'transform 0.3s ease-out'
})

// 下拉刷新时的背景图片缩放
const pullDistance = ref(0)

// 处理下拉刷新拉动
const onRefresherPulling = (e) => {
  const distance = e.detail.dy || 0
  pullDistance.value = Math.max(0, distance)

  // 计算缩放比例，最大放大30%
  const maxDistance = 120
  const normalizedDistance = Math.min(distance / maxDistance, 1)

  // 使用缓动函数让效果更自然
  const easeOutQuart = 1 - Math.pow(1 - normalizedDistance, 4)
  const finalScale = 1 + easeOutQuart * 0.3

  bgImageStyle.value = {
    transform: `scale(${finalScale})`,
    transformOrigin: 'center top',
    transition: 'none' // 拉动时不要过渡动画
  }
}

// 处理下拉刷新恢复
const onRefresherRestore = () => {
  pullDistance.value = 0
  bgImageStyle.value = {
    transform: 'scale(1)',
    transformOrigin: 'center top',
    transition: 'transform 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94)' // 丝滑的恢复动画
  }
}

// 滚动渐变效果的计算属性
const scrollProgress = computed(() => {
  // 设置渐变区间：50px-200px
  const startFade = 50
  const endFade = 200

  if (scrollTop.value <= startFade) {
    return 0
  } else if (scrollTop.value >= endFade) {
    return 1
  } else {
    return (scrollTop.value - startFade) / (endFade - startFade)
  }
})

// 头部样式
const headerStyle = computed(() => {
  const opacity = scrollProgress.value * 0.95 // 最大不透明度95%
  return {
    backgroundColor: `rgba(0, 0, 0, ${opacity})`,
    transition: scrollTop.value === 0 ? 'background-color 0.3s ease' : 'none'
  }
})

// 头像样式
const avatarStyle = computed(() => {
  // 头像从下方30px的位置逐渐上升到正常位置
  const translateY = (1 - scrollProgress.value) * 30
  const opacity = scrollProgress.value
  return {
    transform: `translateY(${translateY}px)`,
    opacity: opacity,
    transition: scrollTop.value === 0 ? 'all 0.3s ease' : 'none'
  }
})

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
  scrollTop.value = currentScrollTop
  isAtTop.value = currentScrollTop <= 50
}

// 使用节流的滚动监听
const onScrollThrottled = throttle(onScroll, 16)

const handleAvatarChange = () => {
  uni.vibrateShort()
  // 点击头像也跳转到编辑资料页面
  uni.navigateTo({ url: '/pages/editProfile/editProfile' })
}

// ===== 响应式数据 =====
let userInfo: IUser = reactive({
  id: 10001,
  userName: '未登录',
  userAvatar: 'https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/public/1898735003367223297/2025-05-11_2f217692-cb17-4c64-8bcb-8afd1ed14b5a.webp',
  userProfile: '这个人很懒，还没有简介',
  userRole: 'user',
})

const userStore = useUserStore()
userInfo = {
  ...userStore.getUserInfo
}

const router = useRouter()

const goToFollowList = () => {
  uni.vibrateShort()
  uni.navigateTo({ url: '/pages/followList/followList?type=follow' })
}

const goToFansList = () => {
  uni.vibrateShort()
  uni.navigateTo({ url: '/pages/followList/followList?type=fans' })
}

// 跳转到编辑资料页面
const goToEditProfile = () => {
  uni.vibrateShort()
  uni.navigateTo({ url: '/pages/editProfile/editProfile' })
}

const showThumb = ref(false)

const showThumbModal = () => {
  uni.vibrateShort()
  showThumb.value = true
}

const closeThumbModal = () => {
  uni.vibrateShort()
  showThumb.value = false
}

// 处理评论按钮点击
function handleComment(postId: string, postCommentNum: number) {
  currentPostId.value = postId
  commentNum.value = postCommentNum
  showCommentPopup.value = true
}

// 处理评论弹窗关闭
function handleCommentPopupClose() {
  showCommentPopup.value = false
  currentPostId.value = ''
}

// 处理评论成功
function handleCommentSuccess() {
  showCommentPopup.value = false
  // 刷新当前tab的数据
  loadData()
}

// 处理分享
function handleShare(post: any) {
  uni.showActionSheet({
    itemList: ['分享给朋友', '分享到朋友圈', '复制链接'],
    success: (res) => {
      if (res.tapIndex === 0) {
        uni.showToast({
          title: '请使用右上角分享',
          icon: 'none'
        })
      } else if (res.tapIndex === 1) {
        uni.showToast({
          title: '请使用右上角分享到朋友圈',
          icon: 'none'
        })
      } else if (res.tapIndex === 2) {
        // 复制链接
        const shareUrl = `pages/postDetail/postDetail?id=${post.id}`
        uni.setClipboardData({
          data: shareUrl,
          success: () => {
            uni.showToast({
              title: '链接已复制',
              icon: 'success'
            })
          }
        })
      }
    }
  })
}

// SocialCard 组件引用管理
const socialCardRefs = ref(new Map())

// 设置 SocialCard 组件引用
function setSocialCardRef(el, postId) {
  if (el) {
    socialCardRefs.value.set(postId, el)
  } else {
    socialCardRefs.value.delete(postId)
  }
}

// 处理菜单切换 - 确保一次只能打开一个菜单
function handleMenuToggle(currentPostId) {
  // 关闭所有其他菜单
  socialCardRefs.value.forEach((cardRef, postId) => {
    if (postId !== currentPostId && cardRef && cardRef.closeEditMenu) {
      cardRef.closeEditMenu()
    }
  })
}

// 处理可见性变更
async function handleVisibilityChange(postId, action) {
  console.log(`我的页面帖子 ${postId} 执行操作: ${action}`)

  try {
    if (action === 'public' || action === 'private') {
      // 调用后端API更新帖子的可见性
      const updateParams = {
        id: postId,
        isPublic: action === 'public' ? 1 : 0
      }

      const response = await postApi.updatePost(updateParams)

      if (response.code === 0) {
        uni.showToast({
          title: action === 'public' ? '已设为公开' : '已设为私密',
          icon: 'success'
        })
        // 刷新当前tab的数据
        loadData()
      } else {
        // API调用成功但业务失败，回滚本地状态
        const targetPost = cardList.value.find(post => post.id === postId)
        if (targetPost) {
          targetPost.isPublic = action === 'public' ? 0 : 1
        }
        uni.showToast({
          title: response.message || '操作失败',
          icon: 'error'
        })
      }
    }
  } catch (error) {
    console.error('更新帖子状态失败:', error)

    // API调用失败，回滚本地状态
    const targetPost = cardList.value.find(post => post.id === postId)
    if (targetPost && (action === 'public' || action === 'private')) {
      targetPost.isPublic = action === 'public' ? 0 : 1
    }
    uni.showToast({
      title: '网络错误，请重试',
      icon: 'error'
    })
  }
}

// 处理编辑事件
function handleEdit(postId) {
  console.log('我的页面编辑帖子:', postId)
  // 跳转到编辑页面
  uni.navigateTo({
    url: `/pages/editPost/editPost?id=${postId}`
  })
}

// 处理删除事件
function handleDelete(postId) {
  console.log('我的页面删除帖子:', postId)
  // 从列表中移除已删除的帖子
  const index = cardList.value.findIndex(post => post.id === postId)
  if (index !== -1) {
    cardList.value.splice(index, 1)
  }
  // 刷新当前tab的数据
  loadData()
}

// 配置小程序分享功能
onShareAppMessage(() => {
  console.log('个人主页分享给朋友事件触发了')
  return {
    title: `${userInfo.userName}的个人主页 - CampCircle`,
    path: `/pages/tabbar/mine/mine?id=${userInfo.id}`,
    imageUrl: userInfo.userAvatar || 'https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-06-13_12f2e457-9cae-4ffa-a149-1f480ddc221d.png'
  }
})

onShareTimeline(() => {
  console.log('个人主页分享到朋友圈事件触发了')
  return {
    title: `${userInfo.userName}的校园生活 - CampCircle`,
    query: `id=${userInfo.id}&from=timeline`,
    imageUrl: userInfo.userAvatar || 'https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-06-13_12f2e457-9cae-4ffa-a149-1f480ddc221d.png'
  }
})

const showQrCode = () => {
  uni.vibrateShort()
  // 跳转到二维码海报页面
  uni.navigateTo({
    url: '/pages/qrPoster/qrPoster'
  })
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
  // 背景色通过 JavaScript 动态设置

  .fixed-avatar {
    margin-top: 15px;
    width: 30px;
    height: 30px;
    border-radius: 50%;
    border: 2px solid rgba(255, 255, 255, 0.8);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
    // transform 和 opacity 通过 JavaScript 动态设置
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
  position: relative;
  padding-top: var(--status-bar-height, 44px);
  box-sizing: border-box;
  overflow: hidden;

  // 背景图片
  .bg-image {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 120%; // 增加高度以支持放大效果
    z-index: 0;
    object-fit: cover;
    transform-origin: center top;
    transition: transform 0.1s ease-out; // 备用过渡效果
  }

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

  .user-id-container {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .user-id {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.7);
  }

  .qr-icon {
    width: 16px;
    height: 16px;
    opacity: 0.8;

    &:active {
      opacity: 0.6;
    }
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
  border-top-left-radius: 15px;
  border-top-right-radius: 15px;
  padding-top: 8px;
  background: #fff;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
  margin-top: -3vh;
  position: relative;
  z-index: 2;
  // min-height: 63vh; // 精确计算：60vh + 3vh(margin-top补偿)
}

.content-tabs {
  position: sticky;
  top: 10vh;
  z-index: 10;
  margin-top: 5px;
  background: #fff; // 确保tabs背景是白色
  border-top-left-radius: 15px;
  border-top-right-radius: 15px;
}

.tabbar-placeholder {
  height: 90px;
}

// ===== 标签页内容 =====
.tab-content {
  padding: 20rpx;
  padding-top: -7px;
  min-height: 200rpx;
  background-color: #f1f1f1;

  &--empty {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 60vh;
  }
}

.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20rpx;
}

.no-more-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20rpx;
}

.no-more-text {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.5);
}

//======共获得点赞数======
.thumb-modal {
  width: 290px;
  height: 280px;
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  position: relative;
  display: flex;
  flex-direction: column;
}

.thumb-image {
  flex: 1;
  position: relative;
  overflow: hidden;
  background: #f5f5f5;

  .bg-image {
    width: 100%;
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
  }
}

.thumb-content {
  padding: 24px 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;

  .thumb-text {
    font-size: 16px;
    font-weight: 500;
    color: #333333;
    text-align: center;
    line-height: 1.4;
  }
}

.thumb-actions {
  padding: 0;
  background: #ffffff;
  margin-top: 20px;

  .thumb-button {
    width: 100%;
    height: 44px;
    background: transparent;
    border: none;
    font-size: 16px;
    color: #333333;
    font-weight: 500;
    font-weight: bold;
    padding: 0;
    margin: 0;
    line-height: 44px;
    text-align: center;

    &:active {
      opacity: 0.7;
    }
  }
}

/* 如果需要更紧凑的布局 */
.thumb-modal.compact {
  height: 320px;

  .thumb-content {
    padding: 20px;
  }

  .thumb-actions {
    margin-top: 16px;
    margin-bottom: 12px;
  }
}

/* 如果需要调整图片占比 */
.thumb-modal.image-larger {
  .thumb-image {
    flex: 1.5;
  }
}
</style>