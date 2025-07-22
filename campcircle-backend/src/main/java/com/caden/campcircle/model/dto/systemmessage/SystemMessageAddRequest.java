package com.caden.campcircle.model.dto.systemmessage;

import java.io.Serializable;
import lombok.Data;

/**
 * 系统消息创建请求
 *
 */
@Data
public class SystemMessageAddRequest implements Serializable {

    /**
     * 接收用户 id（如果是全局消息则为空）
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
     * 关联的帖子ID（如果是帖子相关的通知）
     */
    private Long postId;

    /**
     * 关联的评论ID（如果是评论相关的通知）
     */
    private Long commentId;

    /**
     * 是否为全局消息：0-否，1-是（发送给所有用户）
     */
    private Integer isGlobal;

    private static final long serialVersionUID = 1L;
}
