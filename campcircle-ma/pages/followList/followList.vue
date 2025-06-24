<template>
    <view class="container">
        <!-- 自定义导航栏 -->
        <view class="custom-nav" :style="{ paddingTop: statusBarHeight + 'px' }">
            <view class="nav-content">
                <view class="back-icon" @click="goBack">
                    <wd-icon name="arrow-left" size="20px" color="#333"></wd-icon>
                </view>
                <text class="nav-title">{{ isFans ? '粉丝' : '关注' }}</text>
            </view>
        </view>

        <!-- 列表内容 -->
        <scroll-view class="content" :style="{ paddingTop: statusBarHeight + 44 + 20 + 'px' }" scroll-y="true"
            @scrolltolower="onLoadMore" @refresherrefresh="onRefresh" :refresher-enabled="true"
            :refresher-triggered="refresherTriggered" refresher-background="#f8f9fa" :show-scrollbar="false"
            enhanced="true">
            <!-- 空状态 -->
            <view v-if="list.length === 0 && !loading" class="empty-state">
                <view class="empty-icon">
                    <wd-icon name="user" size="64px" color="#d9d9d9"></wd-icon>
                </view>
                <text class="empty-text">{{ isFans ? '还没有粉丝' : '还没有关注任何人' }}</text>
                <text class="empty-sub">{{ isFans ? '分享精彩内容来吸引粉丝吧' : '去发现更多有趣的人' }}</text>
            </view>

            <!-- 用户列表 -->
            <view v-else class="user-list">
                <view v-for="item in list" :key="item.id" class="user-item" @click="goToUserProfile(item)">
                    <view class="user-avatar">
                        <image :src="userAvatar(item)" class="avatar-img" mode="aspectFill" />
                        <view v-if="isMutual(item)" class="mutual-badge">
                            <wd-icon name="check" size="12px" color="#fff"></wd-icon>
                        </view>
                    </view>

                    <view class="user-info">
                        <view class="user-name">{{ userName(item) }}</view>
                        <view class="user-profile">{{ userProfile(item) }}</view>
                    </view>

                    <view class="user-action">
                        <wd-button size="small" :type="getButtonType(item)" :plain="getButtonPlain(item)"
                            :disabled="isButtonDisabled(item)" @click.stop="handleFollowAction(item)"
                            class="follow-btn">
                            {{ getButtonText(item) }}
                        </wd-button>
                    </view>
                </view>
            </view>

            <!-- 加载状态 -->
            <view v-if="loading" class="loading-state">
                <wd-loading size="20px" color="#007aff"></wd-loading>
                <text class="loading-text">加载中...</text>
            </view>

            <!-- 没有更多数据 -->
            <view v-if="!hasMore && list.length > 0" class="no-more-state">
                <text class="no-more-text">没有更多了</text>
            </view>
        </scroll-view>
    </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { followApi } from '@/api/follow'

const list = ref<any[]>([])
const isFans = ref(false)
const statusBarHeight = ref(0)
const hasMore = ref(true)
const currentPage = ref(1)
const loading = ref(false)
const refresherTriggered = ref(false)

// 获取用户头像、昵称、简介
const userAvatar = (item: any) => {
    const avatar = isFans.value ? item.fansUserVO?.userAvatar : item.followUserVO?.userAvatar
    return avatar || '/static/images/default-avatar.png'
}

const userName = (item: any) => {
    const name = isFans.value ? item.fansUserVO?.userName : item.followUserVO?.userName
    return name || '用户'
}

const userProfile = (item: any) => {
    const profile = isFans.value ? item.fansUserVO?.userProfile : item.followUserVO?.userProfile
    return profile || '这个人很懒，还没有简介'
}

const isMutual = (item: any) => item.isMutual

// 按钮相关逻辑
const getButtonType = (item: any) => {
    if (isFans.value) {
        return isMutual(item) ? 'primary' : 'info'
    }
    return 'primary'
}

const getButtonPlain = (item: any) => {
    if (isFans.value) {
        return !isMutual(item)
    }
    return true
}

const isButtonDisabled = (item: any) => {
    // 如果是查看关注列表，按钮不可操作（或者根据业务需要调整）
    return false
}

const getButtonText = (item: any) => {
    if (isFans.value) {
        return isMutual(item) ? '互相关注' : '回关'
    } else {
        return isMutual(item) ? '互相关注' : '已关注'
    }
}

const fetchList = async (page = 1, isRefresh = false) => {
    if (loading.value) return

    loading.value = true
    const params = { current: page, pageSize: 20 }
    let res

    try {
        if (isFans.value) {
            res = await followApi.getMyFansVOlist(params)
        } else {
            res = await followApi.getMyFollowVOList(params)
        }

        if (res.code === 0 && res.data) {
            const newList = res.data.records || []
            if (page === 1 || isRefresh) {
                list.value = newList
            } else {
                list.value = [...list.value, ...newList]
            }
            hasMore.value = newList.length === params.pageSize
        }
    } catch (error) {
        console.error('获取列表失败:', error)
        uni.showToast({
            title: '加载失败',
            icon: 'none'
        })
    } finally {
        loading.value = false
        if (isRefresh) {
            refresherTriggered.value = false
        }
    }
}

// 下拉刷新
const onRefresh = async () => {
    refresherTriggered.value = true
    currentPage.value = 1
    hasMore.value = true
    await fetchList(1, true)
}

// 上划加载更多
const onLoadMore = async () => {
    if (loading.value || !hasMore.value) return

    currentPage.value++
    await fetchList(currentPage.value)
}

const goBack = () => {
    uni.navigateBack()
}

const goToUserProfile = (item: any) => {
    const userId = isFans.value ? item.fansUserVO?.id : item.followUserVO?.id
    uni.navigateTo({
        url: `/pages/user/profile?id=${userId}`
    })
}

const handleFollowAction = (item: any) => {
    // 处理关注/取消关注逻辑
    console.log('关注操作:', item)
    // 这里可以调用相应的API
}

onLoad((options) => {
    isFans.value = options.type === 'fans'
    // 获取系统信息设置状态栏高度
    const systemInfo = uni.getSystemInfoSync()
    statusBarHeight.value = systemInfo.statusBarHeight
    fetchList()
})
</script>

<style lang="scss" scoped>
.container {
    min-height: 100vh;
    background: #f8f9fa;
}

.custom-nav {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 100;
    background: #fff;
    box-shadow: 0 1px 8px rgba(0, 0, 0, 0.06);

    .nav-content {
        height: 44px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 16px;

        .back-icon {
            width: 44px;
            height: 44px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
            transition: background-color 0.2s;

            &:active {
                background-color: #f5f5f5;
            }
        }

        .nav-title {
            font-size: 17px;
            font-weight: 600;
            color: #1a1a1a;
            letter-spacing: -0.5px;
        }

        .nav-count {
            min-width: 44px;
            height: 24px;
            background: #007aff;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 0 8px;

            .count-text {
                font-size: 12px;
                color: #fff;
                font-weight: 500;
            }
        }

        .placeholder {
            width: 44px;
        }
    }
}

.content {
    height: calc(100vh - var(--status-bar-height) - 44px - 20px);
    padding: 0 16px 20px;
    box-sizing: border-box;
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 40px;
    text-align: center;

    .empty-icon {
        width: 80px;
        height: 80px;
        background: #f5f5f5;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 20px;
    }

    .empty-text {
        font-size: 16px;
        color: #666;
        margin-bottom: 8px;
        font-weight: 500;
    }

    .empty-sub {
        font-size: 14px;
        color: #999;
        line-height: 1.4;
    }
}

.user-list {
    .user-item {
        background: #fff;
        border-radius: 16px;
        padding: 16px;
        margin-bottom: 12px;
        display: flex;
        align-items: center;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
        transition: all 0.2s ease;
        position: relative;
        overflow: hidden;

        &:active {
            transform: scale(0.98);
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
        }

        &:last-child {
            margin-bottom: 0;
        }

        .user-avatar {
            position: relative;
            margin-right: 12px;

            .avatar-img {
                width: 48px;
                height: 48px;
                border-radius: 50%;
                background: #f0f0f0;
                border: 2px solid #fff;
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            }

            .mutual-badge {
                position: absolute;
                right: -2px;
                bottom: -2px;
                width: 20px;
                height: 20px;
                background: #52c41a;
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                border: 2px solid #fff;
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            }
        }

        .user-info {
            flex: 1;
            min-width: 0;

            .user-name {
                font-size: 16px;
                font-weight: 600;
                color: #1a1a1a;
                margin-bottom: 4px;
                line-height: 1.3;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }

            .user-profile {
                font-size: 13px;
                color: #8e8e93;
                line-height: 1.4;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                max-width: 200px;
            }
        }

        .user-action {
            margin-left: 12px;
            flex-shrink: 0;

            .follow-btn {
                min-width: 80px;
                height: 32px;
                border-radius: 16px;
                font-size: 13px;
                font-weight: 500;
                transition: all 0.2s ease;

                &:active {
                    transform: scale(0.95);
                }
            }
        }
    }
}

.loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 20px;
    margin-top: 20px;

    .loading-text {
        font-size: 14px;
        color: #999;
        margin-top: 8px;
    }
}

.no-more-state {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
    margin-top: 20px;

    .no-more-text {
        font-size: 14px;
        color: #999;
    }
}

// 响应式适配
@media (max-width: 375px) {
    .custom-nav .nav-content {
        padding: 0 12px;
    }

    .content {
        padding: 0 12px 20px;
    }

    .user-list .user-item {
        padding: 12px;

        .user-avatar .avatar-img {
            width: 44px;
            height: 44px;
        }

        .user-info .user-name {
            font-size: 15px;
        }
    }
}

// 暗黑模式适配
@media (prefers-color-scheme: dark) {
    .container {
        background: #000;
    }

    .custom-nav {
        background: #1c1c1e;

        .nav-content .nav-title {
            color: #fff;
        }
    }

    .user-list .user-item {
        background: #1c1c1e;

        .user-info {
            .user-name {
                color: #fff;
            }

            .user-profile {
                color: #8e8e93;
            }
        }
    }

    .empty-state {
        .empty-text {
            color: #fff;
        }

        .empty-sub {
            color: #8e8e93;
        }
    }
}
</style>