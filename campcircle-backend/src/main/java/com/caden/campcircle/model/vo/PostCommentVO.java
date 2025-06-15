package com.caden.campcircle.model.vo;

import com.caden.campcircle.model.entity.PostComment;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 评论视图
 *
 */
@Data
public class PostCommentVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 帖子 id
     */
    private Long postId;

    /**
     * 评论用户 id
     */
    private Long userId;

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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 评论用户信息
     */
    private UserVO user;

    /**
     * 回复用户信息
     */
    private UserVO replyUser;

    /**
     * 子评论列表
     */
    private List<PostCommentVO> children;
}