package com.caden.campcircle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caden.campcircle.model.dto.notification.SystemNotificationQueryRequest;
import com.caden.campcircle.model.entity.SystemNotification;
import com.caden.campcircle.model.entity.User;
import com.caden.campcircle.model.vo.SystemNotificationVO;

/**
 * 系统通知服务
 *
 * @author caden
 */
public interface SystemNotificationService extends IService<SystemNotification> {

    /**
     * 创建点赞通知
     *
     * @param postId     帖子ID
     * @param postUserId 帖子作者ID
     * @param likeUserId 点赞用户ID
     */
    void createLikeNotification(Long postId, Long postUserId, Long likeUserId);

    /**
     * 创建评论通知
     *
     * @param postId         帖子ID
     * @param postUserId     帖子作者ID
     * @param commentUserId  评论用户ID
     * @param commentContent 评论内容
     */
    void createCommentNotification(Long postId, Long postUserId, Long commentUserId, String commentContent);

    /**
     * 创建关注通知
     *
     * @param followedUserId 被关注用户ID
     * @param followerUserId 关注者ID
     */
    void createFollowNotification(Long followedUserId, Long followerUserId);

    /**
     * 创建系统通知
     *
     * @param title        通知标题
     * @param content      通知内容
     * @param targetUserId 目标用户ID（0表示全体用户）
     */
    void createSystemNotification(String title, String content, Long targetUserId);

    /**
     * 获取用户通知列表
     *
     * @param systemNotificationQueryRequest 查询请求
     * @param loginUser                      当前用户
     * @return 通知列表分页
     */
    Page<SystemNotificationVO> getUserNotifications(SystemNotificationQueryRequest systemNotificationQueryRequest, User loginUser);

    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     * @param loginUser      当前用户
     * @return 是否成功
     */
    Boolean markAsRead(Long notificationId, User loginUser);

    /**
     * 标记所有通知为已读
     *
     * @param loginUser 当前用户
     * @return 是否成功
     */
    Boolean markAllAsRead(User loginUser);

    /**
     * 获取未读通知数量
     *
     * @param loginUser 当前用户
     * @return 未读通知数量
     */
    Integer getUnreadCount(User loginUser);

    /**
     * 获取系统通知VO
     *
     * @param systemNotification 系统通知
     * @return 系统通知VO
     */
    SystemNotificationVO getSystemNotificationVO(SystemNotification systemNotification);

    /**
     * 分页获取系统通知VO
     *
     * @param systemNotificationPage 系统通知分页
     * @return 系统通知VO分页
     */
    Page<SystemNotificationVO> getSystemNotificationVOPage(Page<SystemNotification> systemNotificationPage);
}
