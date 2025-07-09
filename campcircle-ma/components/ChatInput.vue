<template>
  <view class="chat-input-container">
    <!-- 主输入栏 -->
    <view class="input-bar" :style="{ bottom: keyboardHeight + panelHeight + 'px' }">
      <view class="input-wrapper">
        <textarea
            ref="inputRef"
            v-model="inputText"
            placeholder="说点什么..."
            :focus="inputFocused"
            :adjust-position="false"
            :cursor-spacing="20"
            :show-confirm-bar="false"
            :auto-height="true"
            :hold-keyboard="true"
            maxlength="1000"
            class="chat-input"
            @focus="handleInputFocus"
            @blur="handleInputBlur"
            @input="handleInput"
        />
      </view>

      <view class="action-buttons">
        <!-- 表情按钮 -->
        <view
            class="action-btn emoji-btn"
            :class="{ active: showEmojiPanel }"
            @click="toggleEmojiPanel"
        >
          <image
              src='/static/button/biaoqingbao.png'
              class="btn-icon"
              mode="aspectFit"
          />
        </view>

        <!-- 加号按钮 (当输入框为空时显示) -->
        <view
            v-if="!inputText.trim()"
            class="action-btn plus-btn"
            :class="{ active: showPlusPanel }"
            @click="togglePlusPanel"
        >
          <image
              src="/static/button/plus.png"
              class="btn-icon"
              mode="aspectFit"
          />
        </view>

        <!-- 发送按钮 (当有输入内容时显示) -->
        <view
            v-if="inputText.trim()"
            class="send-btn"
            @click="handleSend"
        >
          发送
        </view>
      </view>
    </view>

    <!-- 表情面板 -->
    <view
        v-if="showEmojiPanel"
        class="emoji-panel"
        :style="{ bottom: keyboardHeight + 'px' }"
    >
      <view class="emoji-grid">
        <view
            v-for="(emoji, index) in emojiList"
            :key="index"
            class="emoji-item"
            @click="selectEmoji(emoji)"
        >
          {{ emoji }}
        </view>
      </view>
    </view>

    <!-- 功能面板 -->
    <view
        v-if="showPlusPanel"
        class="plus-panel"
        :style="{ bottom: keyboardHeight + 'px' }"
    >
      <view class="plus-grid">
        <view class="plus-item" @click="chooseImage">
          <view class="plus-icon">
            <image src="/static/button/tupian.png" class="icon" mode="aspectFit" />
          </view>
          <text class="plus-text">相册</text>
        </view>

        <view class="plus-item" @click="takePhoto">
          <view class="plus-icon">
            <image src="/static/button/camera.png" class="icon" mode="aspectFit" />
          </view>
          <text class="plus-text">拍摄</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed, watch } from 'vue'

const emit = defineEmits(['send', 'selectImage', 'selectFile', 'panelChange'])

// 状态管理
const inputText = ref('')
const inputFocused = ref(false)
const showEmojiPanel = ref(false)
const showPlusPanel = ref(false)
const keyboardHeight = ref(0)
const inputBarHeight = ref(60) // 输入栏高度 (120rpx转px大约60px)

const inputRef = ref(null)

// 计算面板高度
const panelHeight = computed(() => {
  if (showEmojiPanel.value || showPlusPanel.value) {
    return 200 // 400rpx转px大约200px
  }
  return 0
})

// 监听面板状态变化，通知父组件
watch([showEmojiPanel, showPlusPanel], () => {
  const currentPanelHeight = panelHeight.value
  const isExpanded = showEmojiPanel.value || showPlusPanel.value

  emit('panelChange', {
    isExpanded,
    panelHeight: currentPanelHeight,
    totalHeight: 60 + currentPanelHeight // 输入框高度(120rpx≈60px) + 面板高度
  })
}, { immediate: true })

// 表情列表
const emojiList = [
  '😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣',
  '😊', '😇', '🙂', '🙃', '😉', '😌', '😍', '🥰',
  '😘', '😗', '😙', '😚', '😋', '😛', '😝', '😜',
  '🤪', '🤨', '🧐', '🤓', '😎', '🥸', '🤩', '🥳',
  '😏', '😒', '😞', '😔', '😟', '😕', '🙁', '☹️',
  '😣', '😖', '😫', '😩', '🥺', '😢', '😭', '😤',
  '😠', '😡', '🤬', '🤯', '😳', '🥵', '🥶', '😱'
]

// 键盘高度监听
const onKeyboardHeightChange = (res) => {
  keyboardHeight.value = res.height

  // 键盘弹起时隐藏其他面板
  if (res.height > 0) {
    showEmojiPanel.value = false
    showPlusPanel.value = false
  }
}

onMounted(() => {
  uni.onKeyboardHeightChange(onKeyboardHeightChange)
})

onUnmounted(() => {
  uni.offKeyboardHeightChange(onKeyboardHeightChange)
})

// 输入框事件处理
const handleInputFocus = () => {
  inputFocused.value = true
  showEmojiPanel.value = false
  showPlusPanel.value = false
}

const handleInputBlur = () => {
  inputFocused.value = false
}

const handleInput = () => {
  // 输入时自动隐藏功能面板，因为要显示发送按钮
  if (inputText.value.trim() && showPlusPanel.value) {
    showPlusPanel.value = false
  }
}

// 切换表情面板
const toggleEmojiPanel = () => {
  showPlusPanel.value = false

  if (showEmojiPanel.value) {
    // 隐藏表情面板，显示键盘
    showEmojiPanel.value = false
    nextTick(() => {
      if (inputRef.value) {
        inputRef.value.focus()
      }
    })
  } else {
    // 显示表情面板，隐藏键盘
    if (inputRef.value) {
      inputRef.value.blur()
    }
    setTimeout(() => {
      showEmojiPanel.value = true
    }, 100)
  }
}

// 切换功能面板
const togglePlusPanel = () => {
  showEmojiPanel.value = false

  if (showPlusPanel.value) {
    showPlusPanel.value = false
    nextTick(() => {
      if (inputRef.value) {
        inputRef.value.focus()
      }
    })
  } else {
    if (inputRef.value) {
      inputRef.value.blur()
    }
    setTimeout(() => {
      showPlusPanel.value = true
    }, 100)
  }
}

// 选择表情
const selectEmoji = (emoji) => {
  inputText.value += emoji
  // 保持输入框焦点
  nextTick(() => {
    if (inputRef.value) {
      inputRef.value.focus()
    }
  })
}

// 发送消息
const handleSend = () => {
  if (inputText.value.trim()) {
    emit('send', {
      text: inputText.value.trim(),
      type: 'text'
    })
    inputText.value = ''
  }
}

// 选择图片
const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album'],
    success: (res) => {
      emit('selectImage', res.tempFilePaths)
      showPlusPanel.value = false
    }
  })
}

// 拍照
const takePhoto = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['camera'],
    success: (res) => {
      emit('selectImage', res.tempFilePaths)
      showPlusPanel.value = false
    }
  })
}

</script>

<style lang="scss" scoped>
.chat-input-container {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
}

.input-bar {
  position: fixed;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 1rpx solid #e5e5e5;
  padding: 12rpx 16rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
  height: 120rpx;
  box-sizing: border-box;
  transition: bottom 0.3s ease;
  z-index: 1001;
}

.input-wrapper {
  flex: 1;
  background: #f7f7f7;
  border-radius: 20rpx;
  padding: 8rpx 16rpx;
  min-height: 80rpx;
  max-height: 200rpx;
  display: flex;
  align-items: center;
}

.chat-input {
  width: 100%;
  min-height: 64rpx;
  font-size: 32rpx;
  line-height: 1.4;
  color: #333;
  background: transparent;
  border: none;
  outline: none;

  &::placeholder {
    color: #999;
  }
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.action-btn {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #f7f7f7;
  transition: all 0.2s ease;

  &:active {
    background: #e5e5e5;
    transform: scale(0.95);
  }

  &.active {
    background: #1aad19;

    .btn-icon {
      filter: brightness(0) invert(1);
    }
  }
}

.btn-icon {
  width: 44rpx;
  height: 44rpx;
}

.send-btn {
  background: #1aad19;
  color: #fff;
  font-size: 28rpx;
  font-weight: 500;
  padding: 0 24rpx;
  height: 80rpx;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;

  &:active {
    background: #179b16;
    transform: scale(0.95);
  }
}

// 表情面板 - 从下方展开，顶起输入框
.emoji-panel {
  position: fixed;
  left: 0;
  right: 0;
  height: 400rpx;
  background: #f7f7f7;
  border-top: 1rpx solid #e5e5e5;
  z-index: 1000;
  animation: panelSlideUp 0.3s ease;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 16rpx;
  padding: 24rpx 20rpx;
  height: 100%;
  overflow-y: auto;
}

.emoji-item {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 44rpx;
  border-radius: 8rpx;
  background: #fff;
  transition: all 0.2s ease;

  &:active {
    background: #f0f0f0;
    transform: scale(0.9);
  }
}

// 功能面板 - 从下方展开，顶起输入框
.plus-panel {
  position: fixed;
  left: 0;
  right: 0;
  height: 400rpx;
  background: #f7f7f7;
  border-top: 1rpx solid #e5e5e5;
  z-index: 1000;
  animation: panelSlideUp 0.3s ease;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.plus-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32rpx;
  padding: 48rpx 32rpx;
}

.plus-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.95);
  }
}

.plus-icon {
  width: 120rpx;
  height: 120rpx;
  background: #fff;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.1);

  .icon {
    width: 60rpx;
    height: 60rpx;
  }
}

.plus-text {
  font-size: 24rpx;
  color: #666;
}

// 面板滑入动画
@keyframes panelSlideUp {
  from {
    transform: translateY(100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

// 响应式适配
@media (max-width: 750rpx) {
  .emoji-grid {
    grid-template-columns: repeat(6, 1fr);
  }

  .plus-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>