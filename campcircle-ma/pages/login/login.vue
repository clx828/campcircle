<template>
    <view class="login-container">
        <!-- 背景滚动区域 -->
        <view class="background-scroll">
            <image class="background-image bg-1" src="https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-07-18_762a65b2-ce9a-4461-9d1f-82e69206520a.jpg" mode="aspectFill"></image>
            <image class="background-image bg-2" src="https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-07-18_762a65b2-ce9a-4461-9d1f-82e69206520a.jpg" mode="aspectFill"></image>
        </view>

        <!-- 内容区域 -->
        <view class="content-wrapper">
            <!-- Logo区域 -->
            <view class="logo-area">
                <image class="logo"
                    src="/static/img/logo.png"
                    mode="aspectFit" />
                <text class="slogan">探索精彩，分享生活</text>
            </view>

            <!-- 表单区域 -->
            <view class="form-area">

                <!-- 登录按钮 -->
                <button class="login-btn" @tap="handleLogin()">
                    使用微信登录
                </button>

                <!-- 第三方登录 -->
                <view class="third-party">
                    <view class="divider">
                        <view class="line"></view>
                        <text class="text">其他登录方式</text>
                        <view class="line"></view>
                    </view>
                    <view class="third-party-icons">
                        <view class="icon-item" @tap="handleThirdPartyLogin('qq')">
                            <image src="../../static/button/phone-login.png"></image>
                            <text class="icon-text">手机号</text>
                        </view>
                    </view>
                </view>
            </view>

            <!-- 用户协议 -->
            <view class="agreement">
                <text class="agreement-text">登录即代表同意</text>
                <text class="link" @tap="goToUserAgreement">《用户协议》</text>
                <text class="agreement-text">和</text>
                <text class="link" @tap="goToPrivacyPolicy">《隐私政策》</text>
            </view>
        </view>

        <!-- 首次登录授权弹窗 -->
        <view v-if="showAuthModal" class="auth-modal">
            <view class="modal-overlay" @tap="closeAuthModal"></view>
            <view class="modal-content">
                <view class="modal-header">
                    <text class="modal-title">完善个人信息</text>
                    <text class="modal-subtitle">获取您的头像和昵称，完善个人资料</text>
                </view>

                <view class="auth-content">
                    <view class="avatar-section">
                        <image v-if="tempUserInfo.avatarUrl" :src="tempUserInfo.avatarUrl" class="preview-avatar" />
                        <view v-else class="default-avatar">
                            <text class="iconfont icon-user"></text>
                        </view>
                        <button class="choose-avatar-btn" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
                            选择头像
                        </button>
                    </view>

                    <view class="nickname-section">
                        <text class="label">昵称</text>
                        <input class="nickname-input" type="nickname" v-model="tempUserInfo.nickName"
                            placeholder="请输入昵称" :maxlength="20" />
                    </view>
                </view>

                <view class="modal-actions">
                    <button class="skip-btn" @tap="skipAuth">暂不设置</button>
                    <button class="confirm-btn" @tap="confirmAuth">确认设置</button>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/userStore'
import RouterGuard from '@/utils/routerGuard'


// 用户store
const userStore = useUserStore()
const firstLogin = ref(false)
const showAuthModal = ref(false)
const tempUserInfo = ref({
    avatarUrl: '',
    nickName: ''
})

// 微信登录
const handleLogin = async () => {
    try {
        uni.vibrateShort()
        // 显示加载提示
        uni.showLoading({
            title: '登录中...',
            mask: true
        })

        const loginRes = await new Promise((resolve, reject) => {
            uni.login({
                provider: 'weixin',
                success: (result) => {
                    console.log('微信登录结果:', result)
                    resolve(result)
                },
                fail: reject
            })
        })

        console.log('loginRes:', loginRes)
        console.log('loginRes.code:', loginRes.code)

        const res = await userApi.loginByWxMiniapp(loginRes.code)

        // 隐藏加载提示
        uni.hideLoading()
        userStore.setUserInfo(res.data)

        // 检查是否首次登录（头像为空）
        if (!res.data.userAvatar || res.data.userAvatar === null) {
            firstLogin.value = true
            showAuthModal.value = true

            // 显示欢迎提示
            uni.showToast({
                title: '欢迎加入！',
                icon: 'success',
                duration: 2000
            })
        } else {
            // 非首次登录，直接跳转
            uni.showToast({
                title: '登录成功',
                icon: 'success',
                duration: 2000
            })

            navigateToHome()
        }

    } catch (e) {
        console.log("登录失败：", e)

        // 隐藏加载提示
        uni.hideLoading()

        // 显示错误提示
        uni.showToast({
            title: e.message || '登录失败，请重试',
            icon: 'error',
            duration: 2000
        })
    }
}

// 选择头像
const onChooseAvatar = (e: any) => {
    tempUserInfo.value.avatarUrl = e.detail.avatarUrl
}

// 确认授权设置
const confirmAuth = async () => {
    try {
        // 验证信息
        if (!tempUserInfo.value.nickName.trim()) {
            uni.showToast({
                title: '请输入昵称',
                icon: 'none'
            })
            return
        }

        uni.showLoading({
            title: '保存中...',
            mask: true
        })

        // 调用接口更新用户信息
        const updateData = {
            userName: tempUserInfo.value.nickName.trim(),
            userAvatar: tempUserInfo.value.avatarUrl
        }

        await userApi.updateUserInfo(updateData)

        // 更新本地用户信息
        const updatedUserInfo = {
            ...userStore.getUserInfo,
            ...updateData,
            userAvatar: updateData.avatarUrl,
            userName: updateData.nickName
        }
        userStore.setUserInfo(updatedUserInfo)

        uni.hideLoading()

        uni.showToast({
            title: '设置成功',
            icon: 'success',
            duration: 1500
        })

        // 延迟关闭弹窗并跳转
        setTimeout(() => {
            closeAuthModal()
            navigateToHome()
        }, 1500)

    } catch (error) {
        uni.hideLoading()
        console.error('更新用户信息失败:', error)
        uni.showToast({
            title: '保存失败，请重试',
            icon: 'error'
        })
    }
}

// 跳过授权设置
const skipAuth = () => {
    uni.showModal({
        title: '提示',
        content: '跳过设置后可在个人中心完善信息',
        confirmText: '确定跳过',
        cancelText: '继续设置',
        success: (res) => {
            if (res.confirm) {
                closeAuthModal()
                navigateToHome()
            }
        }
    })
}

// 关闭授权弹窗
const closeAuthModal = () => {
    showAuthModal.value = false
    tempUserInfo.value = {
        avatarUrl: '',
        nickName: ''
    }
}
// 跳转到首页
const navigateToHome = () => {
    uni.reLaunch({
        url: '/pages/layout/layout'
    })
}

// 第三方登录处理
const handleThirdPartyLogin = (type: 'weixin' | 'qq') => {
    uni.showToast({
        title: `${type}登录开发中`,
        icon: 'none'
    })
}

const goToUserAgreement = () => {
    uni.navigateTo({
        url: '/pages/agreement/user'
    })
}

const goToPrivacyPolicy = () => {
    uni.navigateTo({
        url: '/pages/agreement/privacy'
    })
}
</script>

<style lang="scss">
.login-container {
    position: relative;
    min-height: 100vh;
    overflow: hidden;
}

// 背景滚动区域
.background-scroll {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 200vh;
    z-index: -1;

    .background-image {
        position: absolute;
        width: 100%;
        height: 100vh;

        &.bg-1 {
            top: 0;
            animation: scrollUp 20s linear infinite;
        }

        &.bg-2 {
            top: 100vh;
            animation: scrollUp 20s linear infinite;
        }
    }
}

@keyframes scrollUp {
    0% {
        transform: translateY(0);
    }

    100% {
        transform: translateY(-100vh);
    }
}

.content-wrapper {
    position: relative;
    z-index: 1;
    min-height: 100vh;
    padding: 0 40rpx;
    display: flex;
    flex-direction: column;
    background: rgba(0, 0, 0, 0.3);
    backdrop-filter: blur(5rpx);
}

.logo-area {
    margin-top: 420rpx;
    text-align: center;

    .logo {
        width: 160rpx;
        height: 160rpx;
    }

    .slogan {
        display: block;
        margin-top: 20rpx;
        font-size: 32rpx;
        color: #ffffff;
        font-weight: 300;
    }
}

.login-btn {
    width: 100%;
    height: 90rpx;
    margin-top: 300rpx;
    border-radius: 20rpx;
    background: linear-gradient(135deg, #ff2442, #ff6b6b);
    color: #ffffff;
    font-size: 32rpx;
    line-height: 90rpx;
    text-align: center;
    opacity: 0.9;
    transition: all 0.3s ease;

    &::after {
        border: none;
    }

    &:active {
        transform: translateY(2rpx);
        box-shadow: 0 2rpx 8rpx rgba(255, 36, 66, 0.4);
    }
}

.third-party {
    margin-top: 100rpx;

    .divider {
        display: flex;
        align-items: center;
        justify-content: center;

        .line {
            width: 100rpx;
            height: 1px;
            background-color: rgba(255, 255, 255, 0.3);
        }

        .text {
            margin: 0 20rpx;
            font-size: 24rpx;
            color: #cccccc;
        }
    }

    .third-party-icons {
        display: flex;
        justify-content: center;
        margin-top: 30rpx;
    }

    .icon-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 0 40rpx;
    }

    .icon-item image {
        width: 80rpx;
        height: 80rpx;
        margin-bottom: 10rpx;
    }

    .icon-text {
        font-size: 24rpx;
        color: #666;
        margin-top: 8rpx;
    }
}

.agreement {
    margin-top: auto;
    margin-bottom: 40rpx;
    text-align: center;
    font-size: 24rpx;

    .agreement-text {
        color: #bbbbbb;
    }

    .link {
        color: #ffffff;
        font-weight: 500;
    }
}

// 授权弹窗样式
.auth-modal {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 9999;
    display: flex;
    align-items: center;
    justify-content: center;

    .modal-overlay {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.5);
        backdrop-filter: blur(10rpx);
    }

    .modal-content {
        position: relative;
        width: 80%;
        max-width: 500rpx;
        background: #ffffff;
        border-radius: 20rpx;
        padding: 40rpx;
        animation: modalSlideIn 0.3s ease-out;
    }
}

@keyframes modalSlideIn {
    from {
        transform: translateY(50rpx);
        opacity: 0;
    }

    to {
        transform: translateY(0);
        opacity: 1;
    }
}

.modal-header {
    text-align: center;
    margin-bottom: 40rpx;

    .modal-title {
        font-size: 36rpx;
        font-weight: bold;
        color: #333333;
    }

    .modal-subtitle {
        display: block;
        margin-top: 16rpx;
        font-size: 26rpx;
        color: #666666;
    }
}

.auth-content {
    .avatar-section {
        text-align: center;
        margin-bottom: 40rpx;

        .preview-avatar {
            width: 120rpx;
            height: 120rpx;
            border-radius: 50%;
            margin-bottom: 20rpx;
        }

        .default-avatar {
            width: 120rpx;
            height: 120rpx;
            border-radius: 50%;
            background: #f5f5f5;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 20rpx;

            .iconfont {
                font-size: 60rpx;
                color: #cccccc;
            }
        }

        .choose-avatar-btn {
            background: #ffffff;
            border: 2rpx solid #ff2442;
            color: #ff2442;
            border-radius: 40rpx;
            padding: 12rpx 32rpx;
            font-size: 26rpx;

            &::after {
                border: none;
            }
        }
    }

    .nickname-section {
        .label {
            font-size: 28rpx;
            color: #333333;
            font-weight: 500;
        }

        .nickname-input {
            width: 100%;
            height: 80rpx;
            margin-top: 16rpx;
            padding: 0 20rpx;
            border: 2rpx solid #e6e6e6;
            border-radius: 10rpx;
            font-size: 28rpx;
            background: #ffffff;

            &:focus {
                border-color: #ff2442;
            }
        }
    }
}

.modal-actions {
    display: flex;
    gap: 20rpx;
    margin-top: 40rpx;

    .skip-btn {
        flex: 1;
        height: 80rpx;
        background: #f5f5f5;
        color: #666666;
        border-radius: 10rpx;
        font-size: 28rpx;

        &::after {
            border: none;
        }
    }

    .confirm-btn {
        flex: 1;
        height: 80rpx;
        background: linear-gradient(135deg, #ff2442, #ff6b6b);
        color: #ffffff;
        border-radius: 10rpx;
        font-size: 28rpx;

        &::after {
            border: none;
        }
    }
}
</style>