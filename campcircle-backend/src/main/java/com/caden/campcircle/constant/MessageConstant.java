package com.caden.campcircle.constant;

/**
 * 消息常量
 *
 * @author caden
 */
public interface MessageConstant {

    /**
     * 消息类型 - 文本
     */
    Integer MESSAGE_TYPE_TEXT = 0;

    /**
     * 消息类型 - 图片
     */
    Integer MESSAGE_TYPE_IMAGE = 1;

    /**
     * 消息已读状态 - 未读
     */
    Integer MESSAGE_UNREAD = 0;

    /**
     * 消息已读状态 - 已读
     */
    Integer MESSAGE_READ = 1;

    /**
     * 消息撤回状态 - 未撤回
     */
    Integer MESSAGE_NOT_RECALLED = 0;

    /**
     * 消息撤回状态 - 已撤回
     */
    Integer MESSAGE_RECALLED = 1;

    /**
     * 通知类型 - 点赞
     */
    Integer NOTIFICATION_TYPE_LIKE = 1;

    /**
     * 通知类型 - 评论
     */
    Integer NOTIFICATION_TYPE_COMMENT = 2;

    /**
     * 通知类型 - 关注
     */
    Integer NOTIFICATION_TYPE_FOLLOW = 3;

    /**
     * 通知类型 - 系统
     */
    Integer NOTIFICATION_TYPE_SYSTEM = 4;

    /**
     * 通知已读状态 - 未读
     */
    Integer NOTIFICATION_UNREAD = 0;

    /**
     * 通知已读状态 - 已读
     */
    Integer NOTIFICATION_READ = 1;

    /**
     * 全体用户通知的目标用户ID
     */
    Long ALL_USERS_TARGET_ID = 0L;
}
