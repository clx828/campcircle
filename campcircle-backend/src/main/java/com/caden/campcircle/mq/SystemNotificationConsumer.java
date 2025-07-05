//package com.caden.campcircle.mq;
//
//import cn.hutool.json.JSONUtil;
//import com.caden.campcircle.constant.MqConstant;
//import com.caden.campcircle.model.entity.SystemNotification;
//import com.caden.campcircle.service.SystemNotificationService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
//import org.apache.rocketmq.spring.core.RocketMQListener;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//
///**
// * 系统通知消费者
// *
// * @author caden
// */
//@Component
//@Slf4j
//@RocketMQMessageListener(
//        topic = MqConstant.SYSTEM_NOTIFICATION_TOPIC,
//        selectorExpression = MqConstant.LIKE_NOTIFICATION_TAG + " || " +
//                MqConstant.COMMENT_NOTIFICATION_TAG + " || " +
//                MqConstant.FOLLOW_NOTIFICATION_TAG + " || " +
//                MqConstant.SYSTEM_NOTIFICATION_TAG,
//        consumerGroup = "system-notification-consumer-group"
//)
//public class SystemNotificationConsumer implements RocketMQListener<String> {
//
//    @Resource
//    private SystemNotificationService systemNotificationService;
//
//    @Override
//    public void onMessage(String message) {
//        log.info("接收到系统通知: {}", message);
//        try {
//            // 解析消息
//            SystemNotification notification = JSONUtil.toBean(message, SystemNotification.class);
//
//            // 保存通知到数据库
//            boolean result = systemNotificationService.save(notification);
//
//            if (result) {
//                log.info("系统通知保存成功: {}", notification.getId());
//
//                // TODO: 这里可以添加实时推送逻辑，比如WebSocket推送
//                // webSocketService.pushNotification(notification.getTargetUserId(), notification);
//
//            } else {
//                log.error("系统通知保存失败: {}", message);
//            }
//
//        } catch (Exception e) {
//            log.error("处理系统通知失败: {}", message, e);
//            // TODO: 可以考虑重试机制或者死信队列
//        }
//    }
//}
