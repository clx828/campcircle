//package com.caden.campcircle.mq;
//
//import com.caden.campcircle.constant.MqConstant;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.rocketmq.spring.core.RocketMQTemplate;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//
///**
// * 消息生产者
// *
// * @author caden
// */
//@Component
//@Slf4j
//public class MessageProducer {
//
//    @Resource
//    private RocketMQTemplate rocketMQTemplate;
//
//    /**
//     * 发送私信消息
//     *
//     * @param message 消息内容
//     */
//    public void sendPrivateMessage(String message) {
//        try {
//            rocketMQTemplate.convertAndSend(
//                    MqConstant.PRIVATE_MESSAGE_TOPIC + ":" + MqConstant.PRIVATE_MESSAGE_TAG,
//                    message
//            );
//            log.info("发送私信消息成功: {}", message);
//        } catch (Exception e) {
//            log.error("发送私信消息失败: {}", message, e);
//        }
//    }
//
//    /**
//     * 发送点赞通知
//     *
//     * @param message 消息内容
//     */
//    public void sendLikeNotification(String message) {
//        try {
//            rocketMQTemplate.convertAndSend(
//                    MqConstant.SYSTEM_NOTIFICATION_TOPIC + ":" + MqConstant.LIKE_NOTIFICATION_TAG,
//                    message
//            );
//            log.info("发送点赞通知成功: {}", message);
//        } catch (Exception e) {
//            log.error("发送点赞通知失败: {}", message, e);
//        }
//    }
//
//    /**
//     * 发送评论通知
//     *
//     * @param message 消息内容
//     */
//    public void sendCommentNotification(String message) {
//        try {
//            rocketMQTemplate.convertAndSend(
//                    MqConstant.SYSTEM_NOTIFICATION_TOPIC + ":" + MqConstant.COMMENT_NOTIFICATION_TAG,
//                    message
//            );
//            log.info("发送评论通知成功: {}", message);
//        } catch (Exception e) {
//            log.error("发送评论通知失败: {}", message, e);
//        }
//    }
//
//    /**
//     * 发送关注通知
//     *
//     * @param message 消息内容
//     */
//    public void sendFollowNotification(String message) {
//        try {
//            rocketMQTemplate.convertAndSend(
//                    MqConstant.SYSTEM_NOTIFICATION_TOPIC + ":" + MqConstant.FOLLOW_NOTIFICATION_TAG,
//                    message
//            );
//            log.info("发送关注通知成功: {}", message);
//        } catch (Exception e) {
//            log.error("发送关注通知失败: {}", message, e);
//        }
//    }
//
//    /**
//     * 发送系统通知
//     *
//     * @param message 消息内容
//     */
//    public void sendSystemNotification(String message) {
//        try {
//            rocketMQTemplate.convertAndSend(
//                    MqConstant.SYSTEM_NOTIFICATION_TOPIC + ":" + MqConstant.SYSTEM_NOTIFICATION_TAG,
//                    message
//            );
//            log.info("发送系统通知成功: {}", message);
//        } catch (Exception e) {
//            log.error("发送系统通知失败: {}", message, e);
//        }
//    }
//}
