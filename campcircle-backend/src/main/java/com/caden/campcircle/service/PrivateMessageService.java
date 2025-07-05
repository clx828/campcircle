package com.caden.campcircle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caden.campcircle.model.dto.message.PrivateMessageQueryRequest;
import com.caden.campcircle.model.dto.message.PrivateMessageSendRequest;
import com.caden.campcircle.model.entity.PrivateMessage;
import com.caden.campcircle.model.entity.User;
import com.caden.campcircle.model.vo.ChatVO;
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
     * 分页获取私信消息VO
     *
     * @param loginUser 当前用户
     * @return 私信消息VO分页
     */
    List<ChatVO> getChatVOList(User loginUser);

    /**
     * 标记消息已读
     * @param Id 消息 ID
     * @param loginUserId 当前用户ID
     */
    boolean markAsRead(Long Id, Long loginUserId);

    /**
     * 撤回消息
     * @param Id 消息 ID
     * @param loginUserId 当前用户ID
     */
    boolean recallMessage(Long Id, Long loginUserId);

    /**
     * 获取未读消息数量
     * @param loginUserId 当前用户ID
     * @return 未读消息数量
     */
    Long getUnreadCount(Long loginUserId);

    /**
     * 获取聊天记录分页
     * @param privateMessageQueryRequest 查询请求
     * @param loginUser 当前用户
     * @return 聊天记录分页
     */
    Page<PrivateMessageVO> getChatHistory(PrivateMessageQueryRequest privateMessageQueryRequest, User loginUser);


}
