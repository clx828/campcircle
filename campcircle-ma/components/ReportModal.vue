<template>
    <!-- 举报弹窗 -->
    <view class="report-modal" v-if="visible" @touchmove.stop.prevent>
        <view class="report-mask" @tap="handleClose"></view>
        <view class="report-content">
            <view class="report-header">
                <text class="report-title">举报内容</text>
                <view class="report-close" @tap="handleClose">
                    <text class="close-icon">×</text>
                </view>
            </view>
            
            <view class="report-body">
                <view class="report-section">
                    <text class="section-title">请选择举报类型：</text>
                    <view class="report-types">
                        <view 
                            v-for="(type, index) in reportTypes" 
                            :key="index"
                            class="report-type-item"
                            :class="{ 'selected': type.checked }"
                            @tap="toggleReportType(index)"
                        >
                            <view class="type-icon">
                                <text v-if="type.checked" class="check-icon">✓</text>
                            </view>
                            <text class="type-text">{{ type.label }}</text>
                        </view>
                    </view>
                </view>
                
                <view class="report-section">
                    <text class="section-title">详细说明（选填）：</text>
                    <textarea 
                        class="report-textarea"
                        v-model="reportDescription"
                        placeholder="请详细描述举报原因，有助于我们更好地处理..."
                        maxlength="200"
                    ></textarea>
                    <text class="char-count">{{ reportDescription.length }}/200</text>
                </view>
            </view>
            
            <view class="report-actions">
                <button class="report-btn cancel-btn" @tap="handleClose">取消</button>
                <button class="report-btn submit-btn" @tap="submitReport" :disabled="!canSubmit">举报</button>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref, computed, defineProps, defineEmits, watch } from 'vue'

interface PostInfo {
    id: string | number
    title?: string
    content?: string
    userId?: string | number
}

const props = defineProps<{
    visible: boolean
    postInfo: PostInfo
}>()

const emit = defineEmits<{
    close: []
    success: []
}>()

// 举报相关状态
const reportDescription = ref('')
const reportTypes = ref([
    { label: '色情低俗', checked: false },
    { label: '违法违规', checked: false },
    { label: '血腥暴力', checked: false },
    { label: '人身攻击', checked: false },
    { label: '骚扰谩骂', checked: false },
    { label: '垃圾广告', checked: false },
    { label: '虚假信息', checked: false },
    { label: '侵犯隐私', checked: false },
    { label: '其他', checked: false }
])

// 计算是否可以提交举报
const canSubmit = computed(() => {
    return reportTypes.value.some(type => type.checked)
})



// 关闭举报弹窗并重置数据
const handleClose = () => {
    uni.vibrateShort()
    // 重置举报数据
    reportDescription.value = ''
    reportTypes.value.forEach(type => {
        type.checked = false
    })
    emit('close')
}

// 切换举报类型选择状态
const toggleReportType = (index: number) => {
    uni.vibrateShort()
    reportTypes.value[index].checked = !reportTypes.value[index].checked
}

// 提交举报信息到服务器
const submitReport = async () => {
    uni.vibrateShort()
    
    if (!canSubmit.value) {
        uni.showToast({
            title: '请选择举报类型',
            icon: 'none'
        })
        return
    }
    
    try {
        uni.showLoading({ title: '提交中...', mask: true })
        
        // 获取选中的举报类型
        const selectedTypes = reportTypes.value
            .filter(type => type.checked)
            .map(type => type.label)
        
        const reportData = {
            postId: props.postInfo.id,
            types: selectedTypes,
            description: reportDescription.value.trim(),
            reportTime: new Date().toISOString(),
            postTitle: props.postInfo.title || props.postInfo.content || '',
            reportedUserId: props.postInfo.userId
        }
        
        console.log('举报数据:', reportData)
        
        // TODO: 调用举报接口
        // await reportApi.submitReport(reportData)
        
        // 模拟接口调用
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        uni.hideLoading()
        uni.showToast({
            title: '举报成功，感谢您的反馈',
            icon: 'success',
            duration: 2000
        })
        
        emit('success')
        handleClose()
        
    } catch (error) {
        console.error('举报失败:', error)
        uni.hideLoading()
        uni.showToast({
            title: '举报失败，请重试',
            icon: 'none'
        })
    }
}
</script>

<style lang="scss" scoped>
.report-modal {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 10001;
}

.report-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.6);
}

.report-content {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    background: #fff;
    border-radius: 20rpx;
    width: 90%;
    max-width: 640rpx;
    max-height: 70vh;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);

    .report-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 40rpx 32rpx 20rpx;
        flex-shrink: 0;

        .report-title {
            font-size: 36rpx;
            font-weight: 600;
            color: #333;
        }

        .report-close {
            width: 56rpx;
            height: 56rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
            background: #f5f5f5;
            transition: background-color 0.2s;

            &:active {
                background: #e9ecef;
            }

            .close-icon {
                font-size: 36rpx;
                color: #999;
            }
        }
    }

    .report-body {
        flex: 1;
        padding: 0 32rpx 20rpx;
        overflow-y: auto;

        .report-section {
            margin-bottom: 32rpx;

            &:last-child {
                margin-bottom: 0;
            }

            .section-title {
                font-size: 28rpx;
                font-weight: 500;
                color: #333;
                margin-bottom: 24rpx;
                display: block;
            }
        }

        .report-types {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 16rpx;

            .report-type-item {
                display: flex;
                flex-direction: column;
                align-items: center;
                padding: 24rpx 16rpx;
                border-radius: 16rpx;
                border: 2rpx solid #f0f0f0;
                background: #fafafa;
                transition: all 0.3s ease;
                cursor: pointer;

                &:active {
                    transform: scale(0.95);
                }

                &.selected {
                    border-color: #007aff;
                    background: linear-gradient(135deg, #007aff 0%, #0056b3 100%);
                    color: #fff;

                    .type-icon {
                        background: rgba(255, 255, 255, 0.2);
                        border-color: rgba(255, 255, 255, 0.3);

                        .check-icon {
                            color: #fff;
                        }
                    }

                    .type-text {
                        color: #fff;
                    }
                }

                .type-icon {
                    width: 40rpx;
                    height: 40rpx;
                    border: 2rpx solid #ddd;
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    margin-bottom: 12rpx;
                    background: #fff;
                    transition: all 0.3s ease;

                    .check-icon {
                        font-size: 24rpx;
                        font-weight: bold;
                        color: #007aff;
                    }
                }

                .type-text {
                    font-size: 24rpx;
                    color: #333;
                    text-align: center;
                    line-height: 1.2;
                    transition: color 0.3s ease;
                }
            }
        }

        .report-textarea {
            width: 100%;
            min-height: 160rpx;
            padding: 24rpx;
            border: 2rpx solid #f0f0f0;
            border-radius: 16rpx;
            font-size: 28rpx;
            color: #333;
            line-height: 1.5;
            resize: none;
            box-sizing: border-box;
            background: #fafafa;
            transition: all 0.3s ease;

            &:focus {
                border-color: #007aff;
                background: #fff;
                outline: none;
            }

            &::placeholder {
                color: #999;
            }
        }

        .char-count {
            font-size: 24rpx;
            color: #999;
            text-align: right;
            margin-top: 12rpx;
            display: block;
        }
    }

    .report-actions {
        display: flex;
        gap: 24rpx;
        padding: 24rpx 32rpx 40rpx;
        flex-shrink: 0;

        .report-btn {
            flex: 1;
            height: 88rpx;
            border-radius: 44rpx;
            font-size: 32rpx;
            font-weight: 500;
            border: none;
            transition: all 0.3s ease;

            &.cancel-btn {
                background: #f8f9fa;
                color: #666;

                &:active {
                    background: #e9ecef;
                    transform: scale(0.98);
                }
            }

            &.submit-btn {
                background: linear-gradient(135deg, #007aff 0%, #0056b3 100%);
                color: #fff;
                box-shadow: 0 4rpx 16rpx rgba(0, 122, 255, 0.3);

                &:active {
                    transform: scale(0.98);
                    box-shadow: 0 2rpx 8rpx rgba(0, 122, 255, 0.3);
                }

                &:disabled {
                    background: #ccc;
                    color: #999;
                    box-shadow: none;
                    transform: none;
                }
            }

            &::after {
                border: none;
            }
        }
    }
}
</style>
