package com.caden.campcircle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caden.campcircle.model.entity.PrivateMessage;
import com.caden.campcircle.model.vo.ChatListVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 私信消息数据库操作
 *
 * @author caden
 */
public interface PrivateMessageMapper extends BaseMapper<PrivateMessage> {

    /**
     * 获取用户的聊天列表
     *
     * @param userId 用户ID
     * @return 聊天列表
     */
    List<ChatListVO> getChatList(@Param("userId") Long userId);

    /**
     * 获取两个用户之间的未读消息数量
     *
     * @param fromUserId 发送者ID
     * @param toUserId   接收者ID
     * @return 未读消息数量
     */
    Integer getUnreadCount(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);

    /**
     * 标记消息为已读
     *
     * @param fromUserId 发送者ID
     * @param toUserId   接收者ID
     * @return 更新数量
     */
    Integer markAsRead(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);
}
