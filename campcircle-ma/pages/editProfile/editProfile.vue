<template>
  <view class="edit-profile-container">
    <!-- 简洁导航栏 -->
    <view class="navbar-wrapper" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @click="handleCancel">
          <view class="back-button">
            <wd-icon name="arrow-left" size="20px" color="#333"></wd-icon>
          </view>
        </view>
        <view class="navbar-center">
          <text class="navbar-title">编辑资料</text>
        </view>
        <view class="navbar-right" @click="handleSave">
          <view class="save-button" :class="{ 'save-disabled': !canSave }">
            <text class="save-text">保存</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 内容区域 -->
    <view class="content-area" :style="{ paddingTop: navbarHeight + 'px' }">
      <!-- 头像编辑 -->
      <view class="form-section">
        <view class="section-title">头像</view>
        <view class="avatar-edit-container" @click="handleAvatarClick">
          <image 
            :src="formData.userAvatar || defaultAvatar" 
            class="avatar-preview" 
            mode="aspectFill"
          />
          <view class="avatar-edit-overlay">
            <wd-icon name="camera" size="16px" color="#fff"></wd-icon>
          </view>
        </view>
      </view>

      <!-- 昵称编辑 -->
      <view class="form-section">
        <view class="section-title">昵称</view>
        <view class="input-container">
          <input 
            v-model="formData.userName" 
            class="form-input"
            placeholder="请输入昵称"
            maxlength="20"
            @input="onInputChange"
          />
          <text class="char-count">{{ formData.userName.length }}/20</text>
        </view>
      </view>

      <!-- 个人简介编辑 -->
      <view class="form-section">
        <view class="section-title">个人简介</view>
        <view class="textarea-container">
          <textarea 
            v-model="formData.userProfile" 
            class="form-textarea"
            placeholder="介绍一下自己吧..."
            maxlength="100"
            @input="onInputChange"
          />
          <text class="char-count">{{ formData.userProfile.length }}/100</text>
        </view>
      </view>

      <!-- 提示信息 -->
      <view class="tips-section">
        <text class="tips-text">• 昵称和简介将在您的个人主页展示</text>
        <text class="tips-text">• 请勿使用违法、低俗等不当内容</text>
      </view>
    </view>

    <!-- 加载遮罩 -->
    <view v-if="loading" class="loading-overlay">
      <view class="loading-content">
        <wd-loading type="spinner" size="24px"></wd-loading>
        <text class="loading-text">{{ loadingText }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/userStore'
import { userApi, UpdateMyUserParams } from '@/api/user'
import { postApi } from '@/api/post'

// 用户store
const userStore = useUserStore()

// 表单数据
const formData = reactive({
  userName: '',
  userAvatar: '',
  userProfile: ''
})

// 原始数据（用于比较是否有变化）
const originalData = reactive({
  userName: '',
  userAvatar: '',
  userProfile: ''
})

// 状态管理
const loading = ref(false)
const loadingText = ref('保存中...')

// 默认头像
const defaultAvatar = 'https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-06-13_12f2e457-9cae-4ffa-a149-1f480ddc221d.png'

// 系统信息
const statusBarHeight = ref(0)
const navbarHeight = ref(0)

// 检查是否可以保存（数据是否有变化且必填项不为空）
const canSave = computed(() => {
  const hasChanges = 
    formData.userName !== originalData.userName ||
    formData.userAvatar !== originalData.userAvatar ||
    formData.userProfile !== originalData.userProfile
  
  const hasRequiredFields = formData.userName.trim().length > 0
  
  return hasChanges && hasRequiredFields
})

// 页面加载时初始化数据
onLoad(() => {
  initializeData()
  getSystemInfo()
})

// 获取系统信息
const getSystemInfo = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
  // 计算导航栏总高度：状态栏 + 导航栏内容（120rpx转px）
  navbarHeight.value = statusBarHeight.value + uni.upx2px(120)
}

// 初始化表单数据
const initializeData = () => {
  const userInfo = userStore.getUserInfo
  
  formData.userName = userInfo.userName || ''
  formData.userAvatar = userInfo.userAvatar || ''
  formData.userProfile = userInfo.userProfile || ''
  
  // 保存原始数据
  originalData.userName = formData.userName
  originalData.userAvatar = formData.userAvatar
  originalData.userProfile = formData.userProfile
}

// 输入变化处理
const onInputChange = () => {
  // 可以在这里添加实时验证逻辑
}

// 头像点击处理
const handleAvatarClick = async () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['camera', 'album'],
    success: async (res) => {
      if (res.tempFilePaths && res.tempFilePaths.length > 0) {
        const filePath = res.tempFilePaths[0]
        
        try {
          loading.value = true
          loadingText.value = '上传头像中...'
          
          // 上传图片
          const uploadResult = await postApi.uploadPicture(filePath)
          
          if (uploadResult.code === 0 && uploadResult.data?.pictureUrl) {
            formData.userAvatar = uploadResult.data.pictureUrl
            uni.showToast({
              title: '头像上传成功',
              icon: 'success'
            })
          } else {
            throw new Error('头像上传失败')
          }
        } catch (error) {
          console.error('头像上传失败:', error)
          uni.showToast({
            title: '头像上传失败',
            icon: 'none'
          })
        } finally {
          loading.value = false
        }
      }
    },
    fail: (err) => {
      console.error('选择图片失败:', err)
      uni.showToast({
        title: '选择图片失败',
        icon: 'none'
      })
    }
  })
}

// 取消编辑
const handleCancel = () => {
  if (canSave.value) {
    uni.showModal({
      title: '提示',
      content: '您有未保存的修改，确定要离开吗？',
      success: (res) => {
        if (res.confirm) {
          uni.navigateBack()
        }
      }
    })
  } else {
    uni.navigateBack()
  }
}

// 保存资料
const handleSave = async () => {
  if (!canSave.value) {
    return
  }
  
  // 表单验证
  if (!formData.userName.trim()) {
    uni.showToast({
      title: '请输入昵称',
      icon: 'none'
    })
    return
  }
  
  if (formData.userName.trim().length < 2) {
    uni.showToast({
      title: '昵称至少2个字符',
      icon: 'none'
    })
    return
  }
  
  try {
    loading.value = true
    loadingText.value = '保存中...'
    
    // 构建更新参数
    const updateParams: UpdateMyUserParams = {}
    
    if (formData.userName !== originalData.userName) {
      updateParams.userName = formData.userName.trim()
    }
    
    if (formData.userAvatar !== originalData.userAvatar) {
      updateParams.userAvatar = formData.userAvatar
    }
    
    if (formData.userProfile !== originalData.userProfile) {
      updateParams.userProfile = formData.userProfile.trim()
    }
    
    // 调用更新接口
    const result = await userApi.updateMyUser(updateParams)
    
    if (result.code === 0) {
      // 更新本地store
      userStore.setUserInfo({
        userName: formData.userName.trim(),
        userAvatar: formData.userAvatar,
        userProfile: formData.userProfile.trim()
      })
      
      uni.showToast({
        title: '保存成功',
        icon: 'success'
      })
      
      // 延迟返回，让用户看到成功提示
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    } else {
      throw new Error(result.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    uni.showToast({
      title: error.message || '保存失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.edit-profile-container {
  min-height: 100vh;
  background: #f8f9fa;
}

// 导航栏样式
.navbar-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: #ffffff;
  border-bottom: 1rpx solid #f0f0f0;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.navbar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 120rpx;
  padding: 0 32rpx;
}

.navbar-left {
  display: flex;
  align-items: center;
}

.back-button {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;

  &:active {
    background: rgba(0, 0, 0, 0.1);
    transform: scale(0.95);
  }
}

.navbar-center {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.navbar-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
}

.navbar-right {
  display: flex;
  align-items: center;
}

.save-button {
  padding: 16rpx 32rpx;
  border-radius: 32rpx;
  background: #fb0055;
  transition: all 0.2s ease;

  &.save-disabled {
    background: #f0f0f0;

    .save-text {
      color: #ccc;
    }
  }

  &:not(.save-disabled):active {
    background: #e6004d;
    transform: scale(0.95);
  }

  .save-text {
    font-size: 28rpx;
    color: #fff;
    font-weight: 500;
  }
}

// 内容区域
.content-area {
  padding: 32rpx 32rpx 40rpx;
  background: #f8f9fa;
  min-height: 100vh;
}

// 表单区块
.form-section {
  background: #fff;
  border-radius: 20rpx;
  padding: 40rpx 32rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);

  .section-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #666;
    margin-bottom: 32rpx;
    text-transform: uppercase;
    letter-spacing: 1rpx;
  }
}

// 头像编辑
.avatar-edit-container {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  margin: 0 auto;

  .avatar-preview {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    border: 6rpx solid #fff;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  }

  .avatar-edit-overlay {
    position: absolute;
    bottom: 0;
    right: 0;
    width: 48rpx;
    height: 48rpx;
    background: #fb0055;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 4rpx solid #fff;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.15);
    transition: all 0.2s ease;

    .edit-hint {
      display: none;
    }
  }

  &:active {
    transform: scale(0.95);

    .avatar-edit-overlay {
      background: #e6004d;
    }
  }
}

// 输入框容器
.input-container {
  position: relative;

  .form-input {
    width: 100%;
    height: 96rpx;
    border: 2rpx solid #f0f0f0;
    border-radius: 16rpx;
    padding: 0 32rpx;
    font-size: 32rpx;
    color: #333;
    background: #fafbfc;
    transition: all 0.2s ease;

    &:focus {
      border-color: #fb0055;
      background: #fff;
      box-shadow: 0 0 0 6rpx rgba(251, 0, 85, 0.1);
    }
  }

  .char-count {
    position: absolute;
    right: 32rpx;
    top: 50%;
    transform: translateY(-50%);
    font-size: 24rpx;
    color: #999;
    background: #fafbfc;
    padding: 0 8rpx;
  }
}

// 文本域容器
.textarea-container {
  position: relative;

  .form-textarea {
    width: 100%;
    min-height: 180rpx;
    border: 2rpx solid #f0f0f0;
    border-radius: 16rpx;
    padding: 32rpx;
    font-size: 32rpx;
    color: #333;
    background: #fafbfc;
    line-height: 1.6;
    transition: all 0.2s ease;

    &:focus {
      border-color: #fb0055;
      background: #fff;
      box-shadow: 0 0 0 6rpx rgba(251, 0, 85, 0.1);
    }
  }

  .char-count {
    position: absolute;
    right: 32rpx;
    bottom: 20rpx;
    font-size: 24rpx;
    color: #999;
    background: #fafbfc;
    padding: 0 8rpx;
  }
}

// 提示信息
.tips-section {
  padding: 24rpx 32rpx;
  background: rgba(251, 0, 85, 0.05);
  border-radius: 16rpx;
  margin-top: 20rpx;

  .tips-text {
    display: block;
    font-size: 26rpx;
    color: #666;
    line-height: 1.8;
    margin-bottom: 8rpx;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

// 加载遮罩
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(4rpx);

  .loading-content {
    background: #fff;
    border-radius: 24rpx;
    padding: 60rpx 48rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.2);

    .loading-text {
      margin-top: 32rpx;
      font-size: 28rpx;
      color: #666;
      font-weight: 500;
    }
  }
}

// 响应式适配
@media (max-width: 750rpx) {
  .avatar-edit-container:active {
    transform: scale(0.95);
  }
}
</style>
