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

        <!-- 弹出层 - 使用弹性布局 -->
        <view v-if="showPopup" class="popup-mask" @click="handleMaskClick">
            <view class="comment-popup" @click.stop="">
                <!-- 顶部拖拽指示器 -->
                <view class="popup-indicator">
                    <view class="indicator-bar"></view>
                </view>


                <!-- 输入区域 -->
                <view class="input-section">
                    <view class="textarea-wrapper" :class="{ 'focus': isFocused }">
                        <textarea ref="inputRef" v-model="commentText" :placeholder="replyTo ? `回复 ${replyTo.userName}` : placeholder"
                            :focus="autoFocus" :adjust-position="true" :cursor-spacing="20" :show-confirm-bar="false"
                            :auto-height="true" :hold-keyboard="true" maxlength="1000" class="comment-textarea"
                            @focus="handleFocus" @blur="handleBlur" />
                        <view class="char-count">{{ commentText.length }}/1000</view>
                    </view>
                </view>

                <!-- 功能栏 -->
                <view class="function-bar">
                    <view class="function-left">
                        <view class="function-item" @click="handleUploadImage">
                            <wd-icon name="image" size="24px" color="#666" />
                        </view>
                        <view class="function-item" @click="handleMention">
                            <wd-icon name="user" size="24px" color="#666" />
                        </view>
                        <view class="function-item" @click="handleEmoji">
                            <wd-icon name="emoji" size="24px" color="#666" />
                        </view>
                    </view>

                    <view class="function-right">
                        <view class="anonymous-switch">
                            <wd-switch v-model="isAnonymous" size="small" />
                            <text class="anonymous-text">匿名</text>
                        </view>
                        <button class="send-btn" :class="{ 'active': commentText.trim() }"
                            :disabled="!commentText.trim()" @click="handleSubmit">
                            <wd-icon name="send" size="18px" />
                        </button>
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { PropType } from 'vue'

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



// 监听键盘高度变化
onMounted(() => {
    // 监听键盘高度变化，用于自动关闭
    uni.onKeyboardHeightChange((res) => {
        console.log('键盘高度变化:', res.height)

        // 键盘关闭时自动隐藏弹出层
        if (res.height === 0 && showPopup.value && isFocused.value) {
            setTimeout(() => {
                handleClose()
            }, 100)
        }
    })
})

onUnmounted(() => {
    uni.offKeyboardHeightChange()
})

// 处理焦点获取
function handleFocus() {
    isFocused.value = true
    console.log('获取焦点')
    // 确保输入框在键盘弹出时可见
    nextTick(() => {
        const query = uni.createSelectorQuery()
        query.select('.comment-textarea').boundingClientRect()
        query.exec((res) => {
            if (res[0]) {
                uni.pageScrollTo({
                    scrollTop: res[0].top - 100, // 向上偏移100px，确保输入框在视图中央
                    duration: 300
                })
            }
        })
    })
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

// 弹出层蒙层 - 弹性布局
.popup-mask {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: 9999;
    display: flex;
    flex-direction: column;
}

// 弹出层主体 - 弹性布局
.comment-popup {
    background: #fff;
    border-top-left-radius: 24rpx;
    border-top-right-radius: 24rpx;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.15);
    margin-top: auto;
    min-height: 300rpx;
    max-height: 60vh;
    width: 100%;
}

.popup-indicator {
    padding: 16rpx 0 8rpx;
    display: flex;
    justify-content: center;
    background: #fff;
    flex-shrink: 0;

    .indicator-bar {
        width: 60rpx;
        height: 6rpx;
        background: #e0e0e0;
        border-radius: 3rpx;
    }
}

.input-section {
    padding: 0 24rpx 16rpx;
    background: #fff;
    flex-shrink: 0;

    .textarea-wrapper {
        position: relative;
        background: #f8f9fa;
        border-radius: 16rpx;
        padding: 24rpx;
        border: 2rpx solid #f0f0f0;
        transition: all 0.3s ease;

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

            &::placeholder {
                color: #999;
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

    .function-left {
        display: flex;
        align-items: center;
        gap: 32rpx;

        .function-item {
            width: 72rpx;
            height: 72rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            background: #f8f9fa;
            border-radius: 50%;
            transition: all 0.3s ease;

            &:active {
                background: #e9ecef;
                transform: scale(0.95);
            }
        }
    }

    .function-right {
        display: flex;
        align-items: center;
        gap: 24rpx;

        .anonymous-switch {
            display: flex;
            align-items: center;
            gap: 12rpx;

            .anonymous-text {
                font-size: 26rpx;
                color: #666;
            }
        }

        .send-btn {
            width: 72rpx;
            height: 72rpx;
            border-radius: 50%;
            background: #e9ecef;
            border: none;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

            &.active {
                background: linear-gradient(135deg, #007AFF, #5856D6);
                box-shadow: 0 8rpx 24rpx rgba(0, 122, 255, 0.3);
                transform: scale(1.05);

                :deep(.wd-icon) {
                    color: #fff !important;
                }
            }

            &:disabled {
                opacity: 0.5;
                transform: scale(1);
                box-shadow: none;
            }

            &:not(:disabled):active {
                transform: scale(0.95);
            }
        }
    }
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
    }
}

.reply-tip {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16rpx 24rpx;
    background: #f8f9fa;
    border-bottom: 1rpx solid #f0f0f0;

    text {
        font-size: 28rpx;
        color: #666;
    }

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