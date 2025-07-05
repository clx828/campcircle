package com.caden.campcircle.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 聊天列表视图对象
 *
 * @author caden
 */
@Data
public class ChatVO implements Serializable {

    /**
     * 聊天对象用户ID
     */
    private Long chatUserId;

    /**
     * 聊天对象用户信息
     */
    private UserVO chatUser;

    private LastMessage lastMessage;

    /**
     * 未读消息数量
     */
    private Long unreadCount;

    private static final long serialVersionUID = 1L;
}


