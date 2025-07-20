<template>
    <scroll-view class="comment-content" scroll-y>
        <view v-if="commentList.length === 0" class="empty-tip">
          <EmptyState text="暂无评论" />

        </view>
        <view v-else class="comment-list">
            <view v-for="comment in commentList" :key="comment.id" class="comment-item">
                <view class="comment-user">
                    <image :src="comment.user.userAvatar" class="avatar" @click="goToUserProfile(comment.user.id)" />
                    <view class="user-info" @click="goToUserProfile(comment.user.id)">
                        <text class="username">{{ comment.user.userName }}</text>
                        <text class="time">{{ formatTime(comment.createTime) }}</text>
                    </view>
                </view>
                <view class="comment-content" @click="handleReply(comment)">{{ comment.content }}</view>
                <view v-if="comment.children && comment.children.length > 0" class="reply-list">
                    <view v-for="reply in comment.children" :key="reply.id" class="reply-item">
                        <view class="reply-user">
                            <image :src="reply.user.userAvatar" class="avatar" @click="goToUserProfile(reply.user.id)" />
                            <view class="user-info" @click="goToUserProfile(reply.user.id)">
                                <text class="username">{{ reply.user.userName }}</text>
                                <text v-if="reply.replyUser" class="reply-to">回复</text>
                                <text v-if="reply.replyUser" class="username" @click.stop="goToUserProfile(reply.replyUser.id)">{{ reply.replyUser.userName }}</text>
                                <text class="time">{{ formatTime(reply.createTime) }}</text>
                            </view>
                        </view>
                        <view class="reply-content" @click="handleReply(comment, reply)">{{ reply.content }}</view>
                    </view>
                </view>
            </view>
            <view class="end-tip">
                <text class="end-text">—— THE END ——</text>
            </view>
        </view>
    </scroll-view>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue'
import { formatTime } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

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
    commentList: Comment[]
}>()

const emit = defineEmits<{
    (e: 'reply', comment: Comment, replyTo?: Comment): void
}>()

// 处理回复
function handleReply(comment: Comment, replyTo?: Comment) {
    // 如果replyTo存在，说明是回复二级评论，此时comment是一级评论
    // 如果replyTo不存在，说明是回复一级评论，此时comment就是一级评论
    emit('reply', comment, replyTo)
}

// 跳转到用户主页
function goToUserProfile(userId: string) {
    uni.vibrateShort()
    uni.navigateTo({
        url: `/pages/userProfile/userProfile?id=${userId}`
    })
}
</script>

<style lang="scss" scoped>
.comment-content {
    height: 100%;
    box-sizing: border-box;
}

.comment-list {
    padding: 20rpx;
    padding-bottom: 120rpx; // 为底部输入框留出空间
}

.comment-item {
    margin-bottom: 30rpx;

    .comment-user {
        display: flex;
        align-items: center;
        margin-bottom: 16rpx;

        .avatar {
            width: 64rpx;
            height: 64rpx;
            border-radius: 50%;
            margin-right: 16rpx;
        }

        .user-info {
            flex: 1;

            .username {
                font-size: 28rpx;
                color: #333;
                font-weight: 500;
            }

            .time {
                font-size: 24rpx;
                color: #999;
                margin-left: 16rpx;
            }
        }
    }

    .comment-content {
        font-size: 28rpx;
        color: #333;
        line-height: 1.5;
        margin-left: 80rpx;
    }

    .reply-list {
        margin-left: 80rpx;
        margin-top: 16rpx;
        background: #f8f9fa;
        border-radius: 12rpx;
        padding: 16rpx;

        .reply-item {
            margin-bottom: 16rpx;

            &:last-child {
                margin-bottom: 0;
            }

            .reply-user {
                display: flex;
                align-items: center;
                margin-bottom: 8rpx;

                .avatar {
                    width: 48rpx;
                    height: 48rpx;
                    border-radius: 50%;
                    margin-right: 12rpx;
                }

                .user-info {
                    flex: 1;
                    display: flex;
                    align-items: center;
                    flex-wrap: wrap;

                    .username {
                        font-size: 26rpx;
                        color: #333;
                    }

                    .reply-to {
                        font-size: 26rpx;
                        color: #999;
                        margin: 0 8rpx;
                    }

                    .time {
                        font-size: 24rpx;
                        color: #999;
                        margin-left: 16rpx;
                    }
                }
            }

            .reply-content {
                font-size: 26rpx;
                color: #333;
                line-height: 1.5;
                margin-left: 60rpx;
            }
        }
    }
}

.end-tip {
    text-align: center;
    padding: 30rpx 0;
    color: #999;

    .end-text {
        font-size: 24rpx;
        letter-spacing: 2rpx;
    }
}
</style>