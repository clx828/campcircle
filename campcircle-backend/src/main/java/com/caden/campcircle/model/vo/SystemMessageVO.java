package com.caden.campcircle.model.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 系统消息视图
 *
 */
@Data
public class SystemMessageVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 发送用户 id
     */
    private Long fromUserId;

    /**
     * 接收用户 id
     */
    private Long toUserId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：0-系统通知，1-点赞通知，2-收藏通知，3-评论通知，4-关注通知
     */
    private Integer type;

    /**
     * 消息类型描述
     */
    private String typeDesc;

    /**
     * 关联的帖子ID
     */
    private Long postId;

    /**
     * 关联的评论ID
     */
    private Long commentId;

    /**
     * 状态：1-未读，0-已读
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 是否为全局消息：0-否，1-是
     */
    private Integer isGlobal;

    /**
     * 发送用户信息
     */
    private UserVO fromUser;

    /**
     * 接收用户信息
     */
    private UserVO toUser;

    /**
     * 关联的帖子信息
     */
    private PostVO post;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
