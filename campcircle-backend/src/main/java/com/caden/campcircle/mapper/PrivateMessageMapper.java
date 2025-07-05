package com.caden.campcircle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caden.campcircle.model.entity.PrivateMessage;
import com.caden.campcircle.model.vo.LastMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 私信消息数据库操作
 *
 * @author caden
 */
public interface PrivateMessageMapper extends BaseMapper<PrivateMessage> {

    /**
     * 获取两个用户之间的未读消息数量
     *
     * @param fromUserId 发送者ID
     * @param toUserId   接收者ID
     * @return 未读消息数量
     */
    Long getUnreadCountByUser(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);

    /**
     * 获取用户未读消息数量
     *
     * @param userId 用户ID
     * @return 未读消息数量
     */
    Long getAllUnreadCount(@Param("userId") Long userId);
    /**
     * 标记消息为已读
     *
     * @param fromUserId 发送者ID
     * @param toUserId   接收者ID
     * @return 更新数量
     */
    Integer markAsRead(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);

    /**
     * 获取和我有聊天记录的用户ID列表
     * @param userId 用户ID
     * @return 用户ID列表
     */
    List<Long> getChatUserIdList(@Param("userId") Long userId);

    /**
     * 获取当前用户和指定用户之间的最新一条聊天记录
     */
    LastMessage getLatestMessage(@Param("currentUserId") Long currentUserId, @Param("chatUserId") Long chatUserId);
}
