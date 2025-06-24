<template>
    <view class="content">
        <view class="header">
            <text class="title">{{ isFans ? '粉丝' : '关注' }}</text>
        </view>
        <view v-if="list.length === 0" class="empty">暂无数据</view>
        <view v-else>
            <view v-for="item in list" :key="item.id" class="user-item">
                <image :src="userAvatar(item)" class="avatar" />
                <view class="info">
                    <view class="name">{{ userName(item) }}</view>
                    <view class="profile">{{ userProfile(item) }}</view>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { followApi } from '@/api/follow'
import { useUserStore } from '@/stores/userStore'
import RouterGuard from '@/utils/routerGuard'

const list = ref<any[]>([])
const isFans = ref(false)

// 获取用户头像、昵称、简介
const userAvatar = (item: any) => isFans.value ? item.fansUserVO?.userAvatar : item.followUserVO?.userAvatar
const userName = (item: any) => isFans.value ? item.fansUserVO?.userName : item.followUserVO?.userName
const userProfile = (item: any) => isFans.value ? item.fansUserVO?.userProfile || '' : item.followUserVO?.userProfile || ''

const fetchList = async () => {
    const params = { current: 1, pageSize: 100 }
    let res
    if (isFans.value) {
        res = await followApi.getMyFansVOlist(params)
    } else {
        res = await followApi.getMyFollowVOList(params)
    }
    if (res.code === 0 && res.data) {
        list.value = res.data.records || []
    }
}

onLoad((options) => {
    isFans.value = options.type === 'fans'
    fetchList()
})

// 在 onShow 生命周期中进行路由守卫
onMounted(() => {
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
    padding: 30rpx;
}

.header {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 30rpx;
}

.title {
    font-size: 36rpx;
    font-weight: bold;
}

.empty {
    text-align: center;
    color: #888;
    margin-top: 100rpx;
}

.user-item {
    display: flex;
    align-items: center;
    margin-bottom: 30rpx;

    .avatar {
        width: 80rpx;
        height: 80rpx;
        border-radius: 50%;
        margin-right: 20rpx;
    }

    .info {
        .name {
            font-size: 28rpx;
            font-weight: 500;
        }

        .profile {
            font-size: 22rpx;
            color: #999;
        }
    }
}
</style>
