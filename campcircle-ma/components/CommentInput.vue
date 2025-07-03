<template>
  <view class="comment-input-wrapper">
    <!-- 输入框触发区域 -->
    <view class="input-trigger" @click="showPopup = true">
      <input class="input-el" type="text" :placeholder="replyTo ? `回复 ${replyTo.userName}` : placeholder"
             disabled />
      <view class="trigger-icon">
        <wd-icon name="edit" size="20px" color="#999" />
      </view>
    </view>

    <!-- 弹出层 - 全屏固定定位 -->
    <view v-if="showPopup"
          class="popup-mask"
          :class="{ 'dark-mode': isAnonymous }"
          :style="{ bottom: keyboardHeight + 'px' }"
          @click="handleMaskClick">
      <view class="comment-popup"
            :class="{ 'dark-mode': isAnonymous }"
            ref="popupRef"
            @click.stop="">
        <!-- 顶部拖拽指示器 -->
        <view class="popup-indicator">
          <view class="indicator-bar"></view>
        </view>

        <!-- 输入区域 -->
        <view class="input-section">
          <view class="textarea-wrapper" :class="{ 'focus': isFocused }">
                        <textarea ref="inputRef" v-model="commentText" :placeholder="replyTo ? `回复 ${replyTo.userName}` : placeholder"
                                  :focus="autoFocus" :adjust-position="false" :cursor-spacing="20" :show-confirm-bar="false"
                                  :auto-height="true" :hold-keyboard="true" maxlength="1000" class="comment-textarea"
                                  @focus="handleFocus" @blur="handleBlur" />
            <view class="char-count">{{ commentText.length }}/1000</view>
          </view>
        </view>

        <!-- 功能栏 -->
        <view class="function-bar">
          <view class="function-left">
            <view class="function-item" @click="handleUploadImage">
              <image src="/static/button/tupian.png" mode="aspectFit" class="function-icon" />
            </view>
            <view class="function-item" @click="handleMention">
              <image src="/static/button/at.png" mode="aspectFit" class="function-icon" />
            </view>
            <view class="function-item" @click="handleEmoji">
              <image src="/static/button/biaoqingbao.png" mode="aspectFit" class="function-icon" />
            </view>
          </view>

          <view class="function-right">
            <view class="user-switch-container">
              <wd-switch v-model="isAnonymous" size="small" />

              <!-- 用户信息切换区域 -->
              <view class="user-info-wrapper">
                <!-- 匿名模式 -->
                <view class="user-info anonymous-info" :class="{ 'active': isAnonymous }">
                  <image src="/static/button/anonymous.png" mode="aspectFit" class="user-icon" />
                  <text class="user-text">匿名</text>
                </view>

                <!-- 实名模式 -->
                <view class="user-info real-info" :class="{ 'active': !isAnonymous }">
                  <image :src="userStore.getUserInfo?.userAvatar || '/static/button/default-avatar.png'"
                         mode="aspectFit" class="user-icon avatar" />
                  <text class="user-text">{{ userStore.getUserInfo?.userName || '未登录' }}</text>
                </view>
              </view>
            </view>

            <view class="send-btn" @click="handleSubmit">
              <image
                  :src="commentText.trim() ? '/static/button/send.png' : '/static/button/send-none.png'"
                  mode="aspectFit"
                  class="send-icon"
              />
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { PropType } from 'vue'
import {useUserStore} from "../stores/userStore";

const props = defineProps({
  placeholder: {
    type: String,
    default: '说点什么...'
  },
  replyTo: {
    type: Object as PropType<{
      userName: string
    }>,
    default: null
  },
  show: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['submit', 'close', 'update:show'])

// 状态管理
const showPopup = ref(false)
const commentText = ref('')
const isAnonymous = ref(false)
const isFocused = ref(false)
const autoFocus = ref(false)
const inputRef = ref(null)  // 输入框引用
const popupRef = ref(null)  // 弹出层引用
const keyboardHeight = ref(0)  // 键盘高度

const userStore = useUserStore();

// 监听show属性变化
watch(() => props.show, (newVal) => {
  showPopup.value = newVal
  if (newVal) {
    // 延迟自动聚焦，确保弹出层完全显示
    setTimeout(() => {
      autoFocus.value = true
    }, 100)
  } else {
    autoFocus.value = false
    isFocused.value = false
    // 关闭时清空输入内容
    commentText.value = ''
  }
})

// 监听弹出层显示状态
watch(showPopup, (newVal) => {
  if (newVal !== props.show) {
    emit('update:show', newVal)
  }
  if (newVal) {
    // 延迟自动聚焦，确保弹出层完全显示
    setTimeout(() => {
      autoFocus.value = true
    }, 100)
  } else {
    autoFocus.value = false
    isFocused.value = false
    commentText.value = ''
    isAnonymous.value = false
  }
})

// 键盘高度变化监听
const onKeyboardHeightChange = (res) => {
  console.log('键盘高度变化:', res.height)
  // 直接使用键盘高度，不做复杂计算
  keyboardHeight.value = res.height

  // 键盘关闭时自动隐藏弹出层
  if (res.height === 0 && showPopup.value && isFocused.value) {
    setTimeout(() => {
      handleClose()
    }, 100)
  }
}

// 监听键盘高度变化
onMounted(() => {
  uni.onKeyboardHeightChange(onKeyboardHeightChange)
})

onUnmounted(() => {
  uni.offKeyboardHeightChange(onKeyboardHeightChange)
})

// 处理焦点获取
function handleFocus() {
  isFocused.value = true
  console.log('获取焦点')
}

// 处理焦点失去
function handleBlur() {
  console.log('失去焦点')
  // 延迟处理，避免点击发送按钮时焦点丢失
  setTimeout(() => {
    isFocused.value = false
  }, 200)
}

// 处理关闭弹出层
function handleClose() {
  showPopup.value = false
  emit('close')
  emit('update:show', false)
  // 失去焦点，关闭键盘
  if (inputRef.value) {
    inputRef.value.blur()
  }
  isFocused.value = false
  commentText.value = ''
}

// 处理蒙层点击
function handleMaskClick() {
  handleClose()
}

// 处理提交
function handleSubmit() {
  if (!commentText.value.trim()) return

  emit('submit', {
    content: commentText.value,
    isAnonymous: isAnonymous.value
  })

  // 只清空评论内容，保持弹窗打开
  commentText.value = ''
  isAnonymous.value = false
}

// 处理上传图片
function handleUploadImage() {
  uni.chooseImage({
    count: 9,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      console.log('选择的图片：', res.tempFilePaths)
      // TODO: 处理图片上传逻辑
    }
  })
}

// 处理@好友
function handleMention() {
  console.log('点击@好友')
}

// 处理表情
function handleEmoji() {
  console.log('点击表情')
}
</script>

<style lang="scss" scoped>
.comment-input-wrapper {
  width: 100%;
  position: relative;
  z-index: 1;
}

.input-trigger {
  width: 88%;
  height: 80rpx;
  background: #f8f9fa;
  border-radius: 40rpx;
  padding: 0 30rpx;
  display: flex;
  align-items: center;
  border: 2rpx solid transparent;
  transition: all 0.3s ease;

  &.active {
    background: #f0f0f0;
    transform: scale(0.98);
  }

  .input-el {
    flex: 1;
    height: 100%;
    font-size: 28rpx;
    color: #333;

    &::placeholder {
      color: #999;
    }
  }

  .trigger-icon {
    margin-left: 20rpx;
    opacity: 0.6;
  }
}

// 弹出层蒙层 - 全屏覆盖
.popup-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 99999;
  display: flex;
  align-items: flex-end;
  transition: all 0.2s ease-out;

  &.dark-mode {
    background: rgba(0, 0, 0, 0.8);
  }
}

// 弹出层主体 - 相对定位
.comment-popup {
  background: #fff;
  border-top-left-radius: 24rpx;
  border-top-right-radius: 24rpx;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.15);
  min-height: 300rpx;
  max-height: 500rpx;
  width: 100%;
  transition: all 0.2s ease-out;

  &.dark-mode {
    background: #1a1a1a;
    box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.4);
  }
}

.popup-indicator {
  padding: 16rpx 0 8rpx;
  display: flex;
  justify-content: center;
  background: #fff;
  flex-shrink: 0;
  transition: background-color 0.4s ease;

  .indicator-bar {
    width: 60rpx;
    height: 6rpx;
    background: #e0e0e0;
    border-radius: 3rpx;
    transition: background-color 0.4s ease;
  }
}

.comment-popup.dark-mode .popup-indicator {
  background: #1a1a1a;

  .indicator-bar {
    background: #444;
  }
}

.input-section {
  padding: 0 24rpx 16rpx;
  background: #fff;
  flex-shrink: 0;
  transition: background-color 0.4s ease;

  .textarea-wrapper {
    position: relative;
    background: #f8f9fa;
    border-radius: 16rpx;
    padding: 24rpx;
    border: 2rpx solid #f0f0f0;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);

    &.focus {
      border-color: #007AFF;
      background: #fff;
      box-shadow: 0 0 0 4rpx rgba(0, 122, 255, 0.1);
    }

    .comment-textarea {
      width: 100%;
      min-height: 80rpx;
      max-height: 200rpx;
      font-size: 32rpx;
      line-height: 1.6;
      color: #333;
      background: transparent;
      border: none;
      outline: none;
      resize: none;
      transition: color 0.4s ease;

      &::placeholder {
        color: #999;
        transition: color 0.4s ease;
      }
    }

    .char-count {
      position: absolute;
      bottom: 8rpx;
      right: 16rpx;
      font-size: 24rpx;
      color: #999;
      background: rgba(255, 255, 255, 0.9);
      padding: 4rpx 8rpx;
      border-radius: 8rpx;
      transition: all 0.4s ease;
    }
  }
}

.comment-popup.dark-mode .input-section {
  background: #1a1a1a;

  .textarea-wrapper {
    background: #2a2a2a;
    border-color: #444;

    &.focus {
      border-color: #007AFF;
      background: #333;
      box-shadow: 0 0 0 4rpx rgba(0, 122, 255, 0.2);
    }

    .comment-textarea {
      color: #fff;

      &::placeholder {
        color: #aaa;
      }
    }

    .char-count {
      color: #bbb;
      background: rgba(42, 42, 42, 0.9);
    }
  }
}

.function-bar {
  padding: 16rpx 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-top: 1rpx solid #f0f0f0;
  flex-shrink: 0;
  transition: all 0.4s ease;

  .function-left {
    display: flex;
    align-items: center;
    gap: 32rpx;

    .function-item {
      width: 80rpx;
      height: 80rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f8f9fa;
      border-radius: 50%;
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);

      &:active {
        background: #e9ecef;
        transform: scale(0.95);
      }

      .function-icon {
        width: 44rpx;
        height: 44rpx;
        transition: filter 0.4s ease;
      }
    }
  }

  .function-right {
    display: flex;
    align-items: center;
    gap: 24rpx;

    .user-switch-container {
      display: flex;
      align-items: center;
      gap: 16rpx;
    }

    .user-info-wrapper {
      position: relative;
      width: 160rpx;
      height: 60rpx;
      overflow: hidden;
    }

    .user-info {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      gap: 12rpx;
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
      opacity: 0;
      transform: translateY(100%);

      &.active {
        opacity: 1;
        transform: translateY(0);
      }

      .user-icon {
        width: 48rpx;
        height: 48rpx;
        border-radius: 50%;
        flex-shrink: 0;

        &.avatar {
          border: 2rpx solid #e0e0e0;
        }
      }

      .user-text {
        font-size: 26rpx;
        color: #666;
        max-width: 100rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .anonymous-info {
      .user-text {
        color: #999;
      }
    }

    .real-info {
      .user-text {
        color: #333;
        font-weight: 500;
        transition: color 0.4s ease;
      }
    }

    // 暗黑模式下的用户信息样式
    .comment-popup.dark-mode & {
      .user-info {
        .user-icon {
          &.avatar {
            border-color: #555;
          }
        }

        .user-text {
          color: #fff;
        }
      }

      .anonymous-info {
        .user-text {
          color: #bbb;
        }
      }

      .real-info {
        .user-text {
          color: #fff;
          font-weight: 500;
        }
      }
    }

    .send-btn {
      width: 80rpx;
      height: 80rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: transform 0.2s ease;

      .send-icon {
        width: 80rpx;
        height: 80rpx;
      }

      &:active {
        transform: scale(0.95);
      }
    }
  }
}

// 暗黑模式下的功能栏样式
.comment-popup.dark-mode .function-bar {
  background: #1a1a1a;
  border-top-color: #444;

  .function-left {
    .function-item {
      &:active {
        background: #3a3a3a;
      }

      .function-icon {
        filter: brightness(0.8) contrast(1.2);
      }
    }
  }

  // 发送按钮使用图片，不需要特殊的暗黑模式样式
}

// 弹出动画
.popup-mask {
  animation: fadeIn 0.3s ease-out;
}

.comment-popup {
  animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }

  to {
    transform: translateY(0);
  }
}

// 适配暗黑模式
@media (prefers-color-scheme: dark) {
  .popup-mask {
    background: rgba(0, 0, 0, 0.7);
  }

  .comment-popup {
    background: #1c1c1e;
  }

  .input-section .textarea-wrapper {
    background: #2c2c2e;
    border-color: #3a3a3c;

    &.focus {
      border-color: #007AFF;
      background: #1c1c1e;
    }

    .comment-textarea {
      color: #fff;

      &::placeholder {
        color: #8e8e93;
      }
    }
  }

  .function-bar {
    background: #1c1c1e;
    border-top-color: #3a3a3c;

    .function-item {
      background: #2c2c2e;

      &:active {
        background: #3a3a3c;
      }
    }

    .user-info {
      .user-text {
        color: #fff;
      }
    }

    .anonymous-info {
      .user-text {
        color: #8e8e93;
      }
    }
  }
}

.reply-tip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 24rpx;
  background: #f8f9fa;
  border-bottom: 1rpx solid #f0f0f0;

  .close-btn {
    width: 48rpx;
    height: 48rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background: #f0f0f0;
    transition: all 0.3s ease;

    &:active {
      background: #e0e0e0;
      transform: scale(0.95);
    }
  }
}
</style>