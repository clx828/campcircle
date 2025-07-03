package com.caden.campcircle.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 聊天列表视图对象
 *
 * @author caden
 */
@Data
public class ChatListVO implements Serializable {

    /**
     * 聊天对象用户ID
     */
    private Long chatUserId;

    /**
     * 聊天对象用户信息
     */
    private UserVO chatUser;

    /**
     * 最后一条消息内容
     */
    private String lastMessageContent;

    /**
     * 最后一条消息类型:0文本,1图片
     */
    private Integer lastMessageType;

    /**
     * 最后一条消息时间
     */
    private Date lastMessageTime;

    /**
     * 未读消息数量
     */
    private Integer unreadCount;

    private static final long serialVersionUID = 1L;
}
