<template>
    <view class="share-modal" v-if="visible" @touchmove.stop.prevent>
        <view class="modal-mask" @tap="handleClose"></view>

        <view class="modal-content" :class="{ 'show': visible }">
            <view class="modal-header">
                <text class="modal-title">分享内容</text>
                <view class="close-btn" @tap="handleClose">
                    <text class="close-icon">×</text>
                </view>
            </view>

            <!-- 分享预览卡片 -->
            <view class="share-preview">
                <view class="preview-card">
                    <image
                        class="preview-image"
                        :src="previewImage"
                        mode="aspectFill"
                    />
                    <view class="preview-content">
                        <text class="preview-title">{{ shareData.title }}</text>
                        <text class="preview-desc">{{ shareData.desc }}</text>
                        <view class="preview-footer">
                            <image class="app-logo" :src="appLogo" />
                            <text class="app-name">CampCircle</text>
                        </view>
                    </view>
                </view>
            </view>

            <!-- 分享选项 -->
            <view class="share-options">
                <button class="share-item share-button" open-type="share">
                    <view class="share-icon wechat-icon">
                        <image src="/static/button/wx.png" class="icon-image" />
                    </view>
                    <text class="share-text">微信好友</text>
                </button>

                <view class="share-item" @tap="shareToTimeline">
                    <view class="share-icon timeline-icon">
                        <image src="/static/button/pyq.png" class="icon-image" />
                    </view>
                    <text class="share-text">朋友圈</text>
                </view>

                <view class="share-item" @tap="generatePoster">
                    <view class="share-icon poster-icon">
                        <image src="/static/button/shengchenghaibao.png" class="icon-image" />
                    </view>
                    <text class="share-text">生成海报</text>
                </view>

                <view class="share-item" @tap="showReportModal">
                    <view class="share-icon report-icon">
                        <image src="/static/button/jubao.png" class="icon-image" />
                    </view>
                    <text class="share-text">举报</text>
                </view>
            </view>
        </view>
    </view>

    <!-- 海报预览弹窗 -->
    <view class="poster-modal" v-if="showPosterModal" @touchmove.stop.prevent>
        <view class="poster-mask" @tap="closePosterModal"></view>
        <view class="poster-content">
            <view class="poster-header">
                <text class="poster-title">分享海报</text>
                <view class="poster-close" @tap="closePosterModal">
                    <text class="close-icon">×</text>
                </view>
            </view>
            <canvas
                type="2d"
                id="shareCanvas"
                class="share-canvas"
                :style="{ width: canvasWidth + 'px', height: canvasHeight + 'px' }"
            ></canvas>
            <view class="poster-actions">
                <button class="poster-btn save-btn" @tap="savePoster">保存到相册</button>
                <button class="poster-btn share-btn" @tap="sharePoster">分享海报</button>
            </view>
        </view>
    </view>

    <!-- 举报组件 -->
    <ReportModal
        :visible="showReportDialog"
        :postInfo="reportPostInfo"
        @close="closeReportModal"
        @success="handleReportSuccess"
    />
</template>

<script setup lang="ts">
import { defineProps, defineEmits, computed, ref, nextTick } from 'vue'
import ReportModal from './ReportModal.vue'

const props = defineProps({
    visible: Boolean,
    shareData: {
        type: Object,
        default: () => ({
            title: '默认标题',
            desc: '默认描述',
            path: '/pages/index/index',
            imageUrl: '',
            avatarUrl: '',
            nickname: ''
        })
    }
})
const emit = defineEmits(['close'])

// 海报相关状态
const showPosterModal = ref(false)
const canvasWidth = 375
const canvasHeight = 500  // 缩短高度
const posterImageUrl = ref('')
const isGeneratingPoster = ref(false)

// 举报相关状态
const showReportDialog = ref(false)
const reportPostInfo = ref({
    id: '',
    title: '',
    content: '',
    userId: ''
})

// 小程序Logo
const appLogo = 'https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/campcircle/post/1928998042208366594/2025-06-13_12f2e457-9cae-4ffa-a149-1f480ddc221d.png'

// 计算预览图片
const previewImage = computed(() => {
    return props.shareData.imageUrl || appLogo
})

// 关闭分享弹窗
const handleClose = () => emit('close')

// 分享到朋友圈（提示用户使用右上角菜单）
const shareToTimeline = () => {
    uni.vibrateShort()

    // 朋友圈分享只能通过右上角菜单触发，这里提示用户
    uni.showModal({
        title: '分享到朋友圈',
        content: '请点击右上角菜单选择"分享到朋友圈"',
        showCancel: false,
        confirmText: '我知道了'
    })

    // 触发页面的朋友圈分享配置
    // 这需要在页面级别配置 onShareTimeline
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]

    // 动态设置分享数据到页面
    if (currentPage && currentPage.$vm) {
        currentPage.$vm.shareTimelineData = {
            title: props.shareData.title,
            query: `postId=${props.shareData.path.split('postId=')[1] || ''}&from=timeline`,
            imageUrl: previewImage.value
        }
    }

    handleClose()
}

// 生成分享海报
const generatePoster = async () => {
    uni.vibrateShort()

    if (isGeneratingPoster.value) {
        uni.showToast({
            title: '正在生成中，请稍候',
            icon: 'none'
        })
        return
    }

    showPosterModal.value = true
    isGeneratingPoster.value = true

    await nextTick()

    try {
        uni.showLoading({ title: '生成海报中...', mask: true })
        await drawPoster()
        uni.hideLoading()
    } catch (error) {
        console.error('生成海报失败:', error)
        uni.hideLoading()
        uni.showToast({
            title: '生成失败，请重试',
            icon: 'none'
        })
    } finally {
        isGeneratingPoster.value = false
    }
}

// 绘制海报 - Canvas 2D 版本
const drawPoster = () => {
    return new Promise(async (resolve, reject) => {
        try {
            // 获取 Canvas 2D 上下文
            const query = uni.createSelectorQuery()
            query.select('#shareCanvas')
                .fields({ node: true, size: true })
                .exec(async (res) => {
                    if (!res[0]) {
                        reject(new Error('Canvas 节点获取失败'))
                        return
                    }

                    const canvas = res[0].node
                    const ctx = canvas.getContext('2d')

                    // 设置 Canvas 尺寸
                    const dpr = uni.getSystemInfoSync().pixelRatio
                    canvas.width = canvasWidth * dpr
                    canvas.height = canvasHeight * dpr
                    ctx.scale(dpr, dpr)

                    // 设置背景
                    ctx.fillStyle = '#ffffff'
                    ctx.fillRect(0, 0, canvasWidth, canvasHeight)

                    // 绘制主图片区域
                    const mainImageY = 60
                    const mainImageHeight = 200

                    if (props.shareData.imageUrl) {
                        try {
                            // 加载图片
                            const img = canvas.createImage()
                            img.onload = () => {
                                ctx.drawImage(img, 20, mainImageY, canvasWidth - 40, mainImageHeight)
                                drawTextContent(ctx, canvas, resolve, reject)
                            }
                            img.onerror = () => {
                                drawDefaultImage(ctx)
                                drawTextContent(ctx, canvas, resolve, reject)
                            }
                            img.src = props.shareData.imageUrl
                        } catch (error) {
                            drawDefaultImage(ctx)
                            drawTextContent(ctx, canvas, resolve, reject)
                        }
                    } else {
                        drawDefaultImage(ctx)
                        drawTextContent(ctx, canvas, resolve, reject)
                    }

                    function drawDefaultImage(ctx) {
                        // 绘制默认背景
                        ctx.fillStyle = '#f8f9fa'
                        ctx.fillRect(20, mainImageY, canvasWidth - 40, mainImageHeight)

                        // 绘制Logo
                        const logoSize = 60
                        const logoX = canvasWidth/2 - logoSize/2
                        const logoY = mainImageY + mainImageHeight/2 - logoSize/2

                        ctx.fillStyle = '#007aff'
                        ctx.fillRect(logoX, logoY, logoSize, logoSize)
                        ctx.fillStyle = '#ffffff'
                        ctx.font = '24px sans-serif'
                        ctx.textAlign = 'center'
                        ctx.fillText('CC', logoX + logoSize/2, logoY + logoSize/2 + 8)
                    }

                    function drawTextContent(ctx, canvas, resolve, reject) {
                        // 绘制标题
                        ctx.fillStyle = '#333333'
                        ctx.font = '22px sans-serif'
                        ctx.textAlign = 'left'

                        const titleY = mainImageY + mainImageHeight + 40
                        const title = props.shareData.title.length > 40 ?
                            props.shareData.title.substring(0, 40) + '...' :
                            props.shareData.title

                        // 简单的两行标题处理
                        if (title.length > 20) {
                            ctx.fillText(title.substring(0, 20), 20, titleY)
                            ctx.fillText(title.substring(20), 20, titleY + 30)
                        } else {
                            ctx.fillText(title, 20, titleY)
                        }

                        // 绘制用户信息
                        const userY = titleY + (title.length > 20 ? 60 : 40)

                        if (props.shareData.nickname) {
                            ctx.fillStyle = '#666666'
                            ctx.font = '16px sans-serif'
                            ctx.fillText(`来自 ${props.shareData.nickname}`, 20, userY)
                        }

                        // 绘制底部信息
                        const footerY = canvasHeight - 80

                        // 绘制分割线
                        ctx.strokeStyle = '#eeeeee'
                        ctx.lineWidth = 1
                        ctx.beginPath()
                        ctx.moveTo(20, footerY - 20)
                        ctx.lineTo(canvasWidth - 20, footerY - 20)
                        ctx.stroke()

                        // 绘制App信息
                        ctx.fillStyle = '#333333'
                        ctx.font = '18px sans-serif'
                        ctx.fillText('CampCircle', 20, footerY + 15)

                        ctx.fillStyle = '#999999'
                        ctx.font = '14px sans-serif'
                        ctx.fillText('发现校园精彩生活', 20, footerY + 35)

                        // 绘制二维码占位
                        ctx.fillStyle = '#f0f0f0'
                        ctx.fillRect(canvasWidth - 80, footerY - 10, 60, 60)
                        ctx.fillStyle = '#cccccc'
                        ctx.font = '10px sans-serif'
                        ctx.textAlign = 'center'
                        ctx.fillText('扫码', canvasWidth - 50, footerY + 20)

                        // 生成图片
                        uni.canvasToTempFilePath({
                            canvas: canvas,
                            success: (res) => {
                                posterImageUrl.value = res.tempFilePath
                                resolve(res.tempFilePath)
                            },
                            fail: reject
                        })
                    }
                })
        } catch (error) {
            reject(error)
        }
    })
}

// 简化的文本处理 - 已在drawPoster中直接处理，此函数保留备用
const wrapText = (ctx, text, maxWidth) => {
    // 简化版本，直接按字符数截断
    const maxChars = Math.floor(maxWidth / 12) // 估算每个字符12px宽度
    if (text.length <= maxChars) return [text]

    const lines = []
    for (let i = 0; i < text.length; i += maxChars) {
        lines.push(text.substring(i, i + maxChars))
    }
    return lines
}

// 关闭海报弹窗
const closePosterModal = () => {
    showPosterModal.value = false
}

// 保存海报到相册
const savePoster = () => {
    uni.vibrateShort()
    if (!posterImageUrl.value) {
        uni.showToast({
            title: '海报生成中，请稍候',
            icon: 'none'
        })
        return
    }

    uni.saveImageToPhotosAlbum({
        filePath: posterImageUrl.value,
        success: () => {
            uni.showToast({
                title: '保存成功',
                icon: 'success'
            })
        },
        fail: () => {
            uni.showToast({
                title: '保存失败',
                icon: 'none'
            })
        }
    })
}

// 分享海报
const sharePoster = () => {
    uni.vibrateShort()
    if (!posterImageUrl.value) {
        uni.showToast({
            title: '海报生成中，请稍候',
            icon: 'none'
        })
        return
    }

    // #ifdef MP-WEIXIN
    uni.shareAppMessage({
        title: props.shareData.title,
        desc: props.shareData.desc,
        path: props.shareData.path,
        imageUrl: posterImageUrl.value
    })
    // #endif

    closePosterModal()
    handleClose()
}

// 显示举报弹窗
const showReportModal = () => {
    console.log('点击了举报按钮')
    uni.vibrateShort()

    // 设置举报的帖子信息
    const postId = props.shareData.path.split('postId=')[1] || ''
    reportPostInfo.value = {
        id: postId || 'default-id',
        title: props.shareData.title,
        content: props.shareData.desc,
        userId: props.shareData.avatarUrl ? 'unknown' : '' // 可以从其他地方获取用户ID
    }
    console.log('举报的帖子信息:', reportPostInfo.value)
    console.log('设置 showReportDialog 为 true')

    showReportDialog.value = true

    console.log('showReportDialog 当前值:', showReportDialog.value)

    handleClose() // 关闭分享弹窗
}

// 关闭举报弹窗
const closeReportModal = () => {
    showReportDialog.value = false
}

// 举报成功回调
const handleReportSuccess = () => {
    console.log('举报成功')
    // 可以在这里添加额外的成功处理逻辑
}
</script>

<style lang="scss" scoped>
.share-modal {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 9999;
}

.modal-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.6);
}

.modal-content {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: #fff;
    border-radius: 16rpx 16rpx 0 0;
    transform: translateY(100%);
    opacity: 0;
    transition: transform 0.3s ease, opacity 0.3s ease;
    max-height: 80vh;
    overflow-y: auto;

    &.show {
        transform: translateY(0);
        opacity: 1;
    }
}

.modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 32rpx;
    border-bottom: 1px solid #f0f0f0;

    .modal-title {
        font-size: 36rpx;
        font-weight: 600;
        color: #333;
    }

    .close-btn {
        width: 60rpx;
        height: 60rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        background: #f5f5f5;

        .close-icon {
            font-size: 40rpx;
            color: #999;
        }
    }
}

// 分享预览卡片
.share-preview {
    padding: 32rpx;
    border-bottom: 1px solid #f0f0f0;

    .preview-card {
        background: #f8f9fa;
        border-radius: 16rpx;
        overflow: hidden;
        border: 1px solid #e9ecef;

        .preview-image {
            width: 100%;
            height: 300rpx;
            background: #f0f0f0;
        }

        .preview-content {
            padding: 24rpx;

            .preview-title {
                font-size: 32rpx;
                font-weight: 600;
                color: #333;
                line-height: 1.4;
                margin-bottom: 12rpx;
                display: -webkit-box;
                -webkit-box-orient: vertical;
                -webkit-line-clamp: 2;
                overflow: hidden;
            }

            .preview-desc {
                font-size: 28rpx;
                color: #666;
                line-height: 1.4;
                margin-bottom: 20rpx;
                display: -webkit-box;
                -webkit-box-orient: vertical;
                -webkit-line-clamp: 2;
                overflow: hidden;
            }

            .preview-footer {
                display: flex;
                align-items: center;

                .app-logo {
                    width: 32rpx;
                    height: 32rpx;
                    border-radius: 6rpx;
                    margin-right: 12rpx;
                }

                .app-name {
                    font-size: 24rpx;
                    color: #999;
                }
            }
        }
    }
}

.share-options {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20rpx;
    padding: 40rpx 32rpx;
}

.share-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20rpx;
    border-radius: 12rpx;
    transition: background-color 0.2s;

    &:active {
        background: #f8f8f8;
    }
}

.share-button {
    background: transparent;
    border: none;
    padding: 20rpx;
    border-radius: 12rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    transition: background-color 0.2s;

    &::after {
        border: none;
    }

    &:active {
        background: #f8f8f8;
    }
}

.share-icon {
    width: 88rpx;
    height: 88rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16rpx;
    font-size: 40rpx;
}

.share-text {
    font-size: 24rpx;
    color: #333;
    text-align: center;
}


// 图标样式统一
.icon-image {
    width: 70rpx;
    height: 70rpx;
}

// 海报弹窗样式
.poster-modal {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 10000;
}

.poster-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.8);
}

.poster-content {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    background: #fff;
    border-radius: 16rpx;
    padding: 32rpx;
    width: 90%;
    max-width: 600rpx;
    max-height: 90vh;
    overflow-y: auto;

    .poster-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 32rpx;

        .poster-title {
            font-size: 36rpx;
            font-weight: 600;
            color: #333;
        }

        .poster-close {
            width: 60rpx;
            height: 60rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
            background: #f5f5f5;

            .close-icon {
                font-size: 40rpx;
                color: #999;
            }
        }
    }

    .share-canvas {
        width: 100%;
        height: auto;
        border-radius: 12rpx;
        border: 1px solid #e9ecef;
        margin-bottom: 32rpx;
        background: #f8f9fa;
    }

    .poster-actions {
        display: flex;
        gap: 20rpx;

        .poster-btn {
            flex: 1;
            height: 80rpx;
            border-radius: 40rpx;
            font-size: 32rpx;
            border: none;

            &.save-btn {
                background: #f8f9fa;
                color: #333;
            }

            &.share-btn {
                background: #007aff;
                color: #fff;
            }
        }
    }
}
</style>