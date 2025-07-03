package com.caden.campcircle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caden.campcircle.model.dto.message.PrivateMessageQueryRequest;
import com.caden.campcircle.model.dto.message.PrivateMessageSendRequest;
import com.caden.campcircle.model.entity.PrivateMessage;
import com.caden.campcircle.model.entity.User;
import com.caden.campcircle.model.vo.ChatListVO;
import com.caden.campcircle.model.vo.PrivateMessageVO;

import java.util.List;

/**
 * 私信消息服务
 *
 * @author caden
 */
public interface PrivateMessageService extends IService<PrivateMessage> {

    /**
     * 发送私信
     *
     * @param privateMessageSendRequest 发送请求
     * @param loginUser                 当前用户
     * @return 消息ID
     */
    Long sendMessage(PrivateMessageSendRequest privateMessageSendRequest, User loginUser);

    /**
     * 获取聊天记录
     *
     * @param privateMessageQueryRequest 查询请求
     * @param loginUser                  当前用户
     * @return 聊天记录分页
     */
    Page<PrivateMessageVO> getChatHistory(PrivateMessageQueryRequest privateMessageQueryRequest, User loginUser);

    /**
     * 获取聊天列表
     *
     * @param loginUser 当前用户
     * @return 聊天列表
     */
    List<ChatListVO> getChatList(User loginUser);

    /**
     * 撤回消息
     *
     * @param messageId 消息ID
     * @param loginUser 当前用户
     * @return 是否成功
     */
    Boolean recallMessage(Long messageId, User loginUser);

    /**
     * 标记消息为已读
     *
     * @param fromUserId 发送者ID
     * @param loginUser  当前用户
     * @return 是否成功
     */
    Boolean markAsRead(Long fromUserId, User loginUser);

    /**
     * 获取未读消息数量
     *
     * @param loginUser 当前用户
     * @return 未读消息数量
     */
    Integer getUnreadCount(User loginUser);

    /**
     * 获取私信消息VO
     *
     * @param privateMessage 私信消息
     * @return 私信消息VO
     */
    PrivateMessageVO getPrivateMessageVO(PrivateMessage privateMessage);

    /**
     * 分页获取私信消息VO
     *
     * @param privateMessagePage 私信消息分页
     * @return 私信消息VO分页
     */
    Page<PrivateMessageVO> getPrivateMessageVOPage(Page<PrivateMessage> privateMessagePage);
}
