package com.caden.campcircle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caden.campcircle.model.entity.SystemNotification;
import org.apache.ibatis.annotations.Param;

/**
 * 系统通知数据库操作
 *
 * @author caden
 */
public interface SystemNotificationMapper extends BaseMapper<SystemNotification> {

    /**
     * 获取用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    Integer getUnreadNotificationCount(@Param("userId") Long userId);

    /**
     * 标记通知为已读
     *
     * @param userId         用户ID
     * @param notificationId 通知ID
     * @return 更新数量
     */
    Integer markNotificationAsRead(@Param("userId") Long userId, @Param("notificationId") Long notificationId);

    /**
     * 标记所有通知为已读
     *
     * @param userId 用户ID
     * @return 更新数量
     */
    Integer markAllNotificationsAsRead(@Param("userId") Long userId);
}
