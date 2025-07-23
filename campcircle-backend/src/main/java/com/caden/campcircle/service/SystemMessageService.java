package com.caden.campcircle.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caden.campcircle.model.dto.systemmessage.SystemMessageQueryRequest;
import com.caden.campcircle.model.entity.SystemMessage;
import com.caden.campcircle.model.vo.SystemMessageVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 系统消息服务
 *
 */
public interface SystemMessageService extends IService<SystemMessage> {

    /**
     * 校验系统消息
     *
     * @param systemMessage
     * @param add
     */
    void validSystemMessage(SystemMessage systemMessage, boolean add);

    /**
     * 获取查询条件
     *
     * @param systemMessageQueryRequest
     * @return
     */
    QueryWrapper<SystemMessage> getQueryWrapper(SystemMessageQueryRequest systemMessageQueryRequest);

    /**
     * 获取系统消息封装
     *
     * @param systemMessage
     * @param request
     * @return
     */
    SystemMessageVO getSystemMessageVO(SystemMessage systemMessage, HttpServletRequest request);

    /**
     * 分页获取系统消息封装
     *
     * @param systemMessagePage
     * @param request
     * @return
     */
    Page<SystemMessageVO> getSystemMessageVOPage(Page<SystemMessage> systemMessagePage, HttpServletRequest request);

    /**
     * 发送系统通知（管理员使用）
     *
     * @param title 消息标题
     * @param content 消息内容
     * @param toUserId 接收用户ID，为null时发送给所有用户
     * @return 是否发送成功
     */
    boolean sendSystemNotification(String title, String content, Long toUserId);

    /**
     * 发送点赞通知
     *
     * @param fromUserId 点赞用户ID
     * @param toUserId 被点赞用户ID
     * @param postId 帖子ID
     * @return 是否发送成功
     */
    boolean sendThumbNotification(Long fromUserId, Long toUserId, Long postId);

    /**
     * 发送收藏通知
     *
     * @param fromUserId 收藏用户ID
     * @param toUserId 被收藏用户ID
     * @param postId 帖子ID
     * @return 是否发送成功
     */
    boolean sendFavourNotification(Long fromUserId, Long toUserId, Long postId);

    /**
     * 发送评论通知
     *
     * @param fromUserId 评论用户ID
     * @param toUserId 被评论用户ID
     * @param postId 帖子ID
     * @param commentId 评论ID
     * @return 是否发送成功
     */
    boolean sendCommentNotification(Long fromUserId, Long toUserId, Long postId, Long commentId);

    /**
     * 发送关注通知
     *
     * @param fromUserId 关注用户ID
     * @param toUserId 被关注用户ID
     * @return 是否发送成功
     */
    boolean sendFollowNotification(Long fromUserId, Long toUserId);

    /**
     * 标记消息为已读
     *
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 是否标记成功
     */
    boolean markAsRead(Long messageId, Long userId);

    /**
     * 批量标记消息为已读
     *
     * @param userId 用户ID
     * @param type 消息类型，为null时标记所有类型
     * @return 标记成功的数量
     */
    int markAllAsRead(Long userId, Integer type);

    /**
     * 获取用户未读消息数量
     *
     * @param userId 用户ID
     * @param type 消息类型，为null时统计所有类型
     * @return 未读消息数量
     */
    long getUnreadCount(Long userId, Integer type);

    /**
     * 获取用户指定类型列表的未读消息数量
     *
     * @param userId 用户ID
     * @param types 消息类型列表
     * @return 未读消息数量
     */
    long getUnreadCountByTypes(Long userId, List<Integer> types);

    /**
     * 批量标记指定类型列表的消息为已读
     *
     * @param userId 用户ID
     * @param types 消息类型列表
     * @return 标记成功的数量
     */
    int markAllAsReadByTypes(Long userId, List<Integer> types);
}
