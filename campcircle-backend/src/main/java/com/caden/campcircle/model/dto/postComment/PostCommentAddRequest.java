package com.caden.campcircle.model.dto.postComment;

import lombok.Data;

import java.io.Serializable;

@Data
public class PostCommentAddRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 帖子 id
     */
    private Long postId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论 id，为空则是一级评论
     */
    private Long parentId;

    /**
     * 回复的用户 id
     */
    private Long replyUserId;

}
