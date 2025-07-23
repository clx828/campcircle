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

    // ==================== 系统消息类型常量 ====================

    /**
     * 系统消息类型 - 系统通知
     */
    Integer SYSTEM_MESSAGE_TYPE_SYSTEM = 0;

    /**
     * 系统消息类型 - 点赞通知
     */
    Integer SYSTEM_MESSAGE_TYPE_THUMB = 1;

    /**
     * 系统消息类型 - 收藏通知
     */
    Integer SYSTEM_MESSAGE_TYPE_FAVOUR = 2;

    /**
     * 系统消息类型 - 评论通知
     */
    Integer SYSTEM_MESSAGE_TYPE_COMMENT = 3;

    /**
     * 系统消息类型 - 关注通知
     */
    Integer SYSTEM_MESSAGE_TYPE_FOLLOW = 4;

    // ==================== 系统消息状态常量 ====================

    /**
     * 系统消息状态 - 未读
     */
    Integer SYSTEM_MESSAGE_STATUS_UNREAD = 0;

    /**
     * 系统消息状态 - 已读
     */
    Integer SYSTEM_MESSAGE_STATUS_READ = 1;

    // ==================== 系统消息全局标识常量 ====================

    /**
     * 系统消息 - 非全局消息
     */
    Integer SYSTEM_MESSAGE_NOT_GLOBAL = 0;

    /**
     * 系统消息 - 全局消息
     */
    Integer SYSTEM_MESSAGE_GLOBAL = 1;

    /**
     * 系统消息发送者ID - 系统发送
     */
    Long SYSTEM_MESSAGE_FROM_SYSTEM = 0L;

    // ==================== 系统消息文字常量 ====================

    /**
     * 点赞通知标题
     */
    String THUMB_NOTIFICATION_TITLE = "点赞通知";

    /**
     * 点赞通知内容
     */
    String THUMB_NOTIFICATION_CONTENT = "赞了您的帖子";

    /**
     * 收藏通知标题
     */
    String FAVOUR_NOTIFICATION_TITLE = "收藏通知";

    /**
     * 收藏通知内容
     */
    String FAVOUR_NOTIFICATION_CONTENT = "收藏了您的帖子";

    /**
     * 评论通知标题
     */
    String COMMENT_NOTIFICATION_TITLE = "评论通知";

    /**
     * 评论通知内容
     */
    String COMMENT_NOTIFICATION_CONTENT = "评论了您的帖子";

    /**
     * 关注通知标题
     */
    String FOLLOW_NOTIFICATION_TITLE = "关注通知";

    /**
     * 关注通知内容
     */
    String FOLLOW_NOTIFICATION_CONTENT = "关注了您";
}
