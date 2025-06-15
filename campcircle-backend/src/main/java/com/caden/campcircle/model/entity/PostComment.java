package com.caden.campcircle.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 评论
 *
 */
@TableName(value = "post_comment")
@Data
public class PostComment implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
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
     * 评论级别
     */
    private Integer level;

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
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}