<template>
    <wd-popup :z-index="9999" v-model="show" position="bottom" closable custom-style="height: 500px;"
        @close="handleClose">
        <view class="comment-popup">
            <view class="comment-header">
                <text class="title">评论</text>
            </view>
            <view class="comment-container">
                <CommentList :comment-list="commentList" @reply="handleReply" />
            </view>
            <view class="comment-input-wrapper">
                <CommentInput :placeholder="replyTo ? `回复 ${replyTo.userName}` : '说点什么...'" :reply-to="replyTo"
                    :show="showCommentInput" @update:show="showCommentInput = $event" @submit="handleSubmit"
                    @close="handleClose" />
            </view>
        </view>
    </wd-popup>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import CommentList from './CommentList.vue'
import CommentInput from './CommentInput.vue'
import { postCommentApi } from '@/api/postComment'

interface UserVO {
    id: string
    userName: string
    userAvatar: string
    userProfile: string | null
    userRole: string
    createTime: string
}

interface Comment {
    id: string
    postId: string
    userId: string
    content: string
    parentId: string
    replyUserId: string
    createTime: string
    updateTime: string
    user: UserVO
    replyUser: UserVO | null
    children: Comment[] | null
}

const props = defineProps<{
    show: boolean
    postId: string
}>()

const emit = defineEmits<{
    (e: 'update:show', value: boolean): void
    (e: 'close'): void
    (e: 'commentSuccess'): void
}>()

// 评论相关
const commentList = ref<Comment[]>([])
const commentCurrent = ref(1)
const commentPageSize = ref(10)
const commentTotal = ref(0)
const replyTo = ref<{ userName: string } | null>(null)
const parentComment = ref<Comment | null>(null)
const showCommentInput = ref(false)

// 监听show属性变化
watch(() => props.show, (newVal) => {
    if (newVal) {
        loadComments()
    }
})

// 加载评论列表
async function loadComments() {
    if (!props.postId) return
    try {
        const res = await postCommentApi.getPostCommentByPage({
            postId: props.postId,
            current: commentCurrent.value,
            pageSize: commentPageSize.value
        })
        if (res.code === 0) {
            commentList.value = res.data.records
            commentTotal.value = res.data.total
        }
    } catch (error) {
        console.error('加载评论失败:', error)
    }
}

// 处理回复
function handleReply(comment: Comment, replyToComment?: Comment) {
    // 如果replyToComment存在，说明是回复二级评论
    // 此时comment是一级评论，replyToComment是被回复的二级评论
    replyTo.value = {
        userName: replyToComment ? replyToComment.user.userName : comment.user.userName
    }
    // 设置父评论（一级评论）
    parentComment.value = comment
    showCommentInput.value = true
}

async function handleSubmit(data: { content: string; isAnonymous: boolean }) {
    if (!props.postId) return
    try {
        const res = await postCommentApi.addComment({
            postId: props.postId,
            content: data.content,
            // 如果parentComment存在，说明是回复评论，使用一级评论的id
            parentId: parentComment.value ? parentComment.value.id : undefined,
            // 如果parentComment存在，说明是回复评论，使用被回复评论的userId
            replyUserId: parentComment.value ? parentComment.value.userId : undefined
        })
        if (res.code === 0) {
            // 重新加载评论列表
            loadComments()
            // 重置回复状态
            replyTo.value = null
            parentComment.value = null
            showCommentInput.value = false
            // 通知父组件评论成功
            emit('commentSuccess')
        }
    } catch (error) {
        console.error('提交评论失败:', error)
    }
}

function handleClose() {
    emit('update:show', false)
    emit('close')
    // 重置状态
    commentList.value = []
    commentCurrent.value = 1
    replyTo.value = null
    parentComment.value = null
    // showCommentInput.value = false
}
</script>

<style lang="scss" scoped>
.comment-popup {
    display: flex;
    flex-direction: column;
    height: 100%;
    background: #fff;
    border-top-left-radius: 20rpx;
    border-top-right-radius: 20rpx;
    position: relative;
}

.comment-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 30rpx;
    border-bottom: 1px solid #eee;
    background: #fff;
    flex-shrink: 0;
    z-index: 1;

    .title {
        font-size: 32rpx;
        font-weight: bold;
    }
}

.comment-container {
    flex: 1;
    overflow: hidden;
    position: relative;
}

.comment-input-wrapper {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    background: #fff;
    border-top: 1px solid #eee;
    padding: 20rpx;
    z-index: 1;
}
</style>