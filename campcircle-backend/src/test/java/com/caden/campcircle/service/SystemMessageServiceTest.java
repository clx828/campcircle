package com.caden.campcircle.service;

import com.caden.campcircle.constant.MessageConstant;
import com.caden.campcircle.model.entity.SystemMessage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 系统消息服务测试
 */
@SpringBootTest
class SystemMessageServiceTest {

    @Resource
    private SystemMessageService systemMessageService;

    @Test
    void testSendSystemNotification() {
        // 测试发送系统通知
        boolean result = systemMessageService.sendSystemNotification(
                "测试通知", 
                "这是一条测试系统通知", 
                1L
        );
        assertTrue(result);
    }

    @Test
    void testSendThumbNotification() {
        // 测试发送点赞通知
        boolean result = systemMessageService.sendThumbNotification(1L, 2L, 1L);
        assertTrue(result);
    }

    @Test
    void testSendFavourNotification() {
        // 测试发送收藏通知
        boolean result = systemMessageService.sendFavourNotification(1L, 2L, 1L);
        assertTrue(result);
    }

    @Test
    void testSendCommentNotification() {
        // 测试发送评论通知
        boolean result = systemMessageService.sendCommentNotification(1L, 2L, 1L, 1L);
        assertTrue(result);
    }

    @Test
    void testSendFollowNotification() {
        // 测试发送关注通知
        boolean result = systemMessageService.sendFollowNotification(1L, 2L);
        assertTrue(result);
    }

    @Test
    void testGetUnreadCount() {
        // 测试获取未读消息数量
        long count = systemMessageService.getUnreadCount(1L, null);
        assertTrue(count >= 0);
    }

    @Test
    void testMarkAsRead() {
        // 先创建一条消息
        SystemMessage message = new SystemMessage();
        message.setFromUserId(0L);
        message.setToUserId(1L);
        message.setTitle("测试消息");
        message.setContent("测试内容");
        message.setType(0);
        message.setStatus(MessageConstant.MESSAGE_UNREAD); // 未读
        message.setIsGlobal(0);
        
        boolean saveResult = systemMessageService.save(message);
        assertTrue(saveResult);
        
        // 测试标记为已读
        boolean markResult = systemMessageService.markAsRead(message.getId(), 1L);
        assertTrue(markResult);
    }

    @Test
    void testMarkAllAsRead() {
        // 测试批量标记为已读
        int count = systemMessageService.markAllAsRead(1L, null);
        assertTrue(count >= 0);
    }

    @Test
    void testNotificationLogic() {
        // 测试通知逻辑：只有正向操作才发送通知

        // 1. 测试不给自己发送通知
        boolean result1 = systemMessageService.sendThumbNotification(1L, 1L, 1L);
        assertTrue(result1); // 方法执行成功，但实际不会发送通知

        // 2. 测试给其他用户发送通知
        boolean result2 = systemMessageService.sendThumbNotification(1L, 2L, 1L);
        assertTrue(result2); // 应该成功发送通知

        // 3. 测试各种类型的通知
        assertTrue(systemMessageService.sendFavourNotification(1L, 2L, 1L));
        assertTrue(systemMessageService.sendCommentNotification(1L, 2L, 1L, 1L));
        assertTrue(systemMessageService.sendFollowNotification(1L, 2L));
    }

    @Test
    void testGetUnreadCountByTypes() {
        // 测试按类型列表获取未读数量
        List<Integer> likeFavourTypes = Arrays.asList(MessageConstant.SYSTEM_MESSAGE_TYPE_THUMB, MessageConstant.SYSTEM_MESSAGE_TYPE_FAVOUR);
        long count = systemMessageService.getUnreadCountByTypes(1L, likeFavourTypes);
        assertTrue(count >= 0);
    }

    @Test
    void testMarkAllAsReadByTypes() {
        // 测试按类型列表批量标记已读
        List<Integer> likeFavourTypes = Arrays.asList(MessageConstant.SYSTEM_MESSAGE_TYPE_THUMB, MessageConstant.SYSTEM_MESSAGE_TYPE_FAVOUR);
        int count = systemMessageService.markAllAsReadByTypes(1L, likeFavourTypes);
        assertTrue(count >= 0);
    }

    @Test
    void testNewUnreadCountStructure() {
        // 测试新的未读消息统计结构
        Long userId = 1L;

        // 测试总未读数
        long total = systemMessageService.getUnreadCount(userId, null);
        assertTrue(total >= 0);

        // 测试系统通知未读数
        long system = systemMessageService.getUnreadCount(userId, MessageConstant.SYSTEM_MESSAGE_TYPE_SYSTEM);
        assertTrue(system >= 0);

        // 测试点赞和收藏未读数
        long likeFavour = systemMessageService.getUnreadCountByTypes(userId, Arrays.asList(MessageConstant.SYSTEM_MESSAGE_TYPE_THUMB, MessageConstant.SYSTEM_MESSAGE_TYPE_FAVOUR));
        assertTrue(likeFavour >= 0);

        // 测试评论和关注未读数
        long commentFollow = systemMessageService.getUnreadCountByTypes(userId, Arrays.asList(MessageConstant.SYSTEM_MESSAGE_TYPE_COMMENT, MessageConstant.SYSTEM_MESSAGE_TYPE_FOLLOW));
        assertTrue(commentFollow >= 0);

        // 验证总数应该等于各部分之和
        assertEquals(total, system + likeFavour + commentFollow);
    }
}
