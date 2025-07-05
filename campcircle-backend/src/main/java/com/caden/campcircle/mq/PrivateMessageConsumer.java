//package com.caden.campcircle.mq;
//
//import cn.hutool.json.JSONUtil;
//import com.caden.campcircle.constant.MqConstant;
//import com.caden.campcircle.model.entity.PrivateMessage;
//import com.caden.campcircle.service.PrivateMessageService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
//import org.apache.rocketmq.spring.core.RocketMQListener;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//
///**
// * 私信消息消费者
// *
// * @author caden
// */
//@Component
//@Slf4j
//@RocketMQMessageListener(
//        topic = MqConstant.PRIVATE_MESSAGE_TOPIC,
//        selectorExpression = MqConstant.PRIVATE_MESSAGE_TAG,
//        consumerGroup = "private-message-consumer-group"
//)
//public class PrivateMessageConsumer implements RocketMQListener<String> {
//
//    @Resource
//    private PrivateMessageService privateMessageService;
//
//    @Override
//    public void onMessage(String message) {
//        log.info("接收到私信消息: {}", message);
//        try {
//            // 解析消息
//            PrivateMessage privateMessage = JSONUtil.toBean(message, PrivateMessage.class);
//
//            // 保存消息到数据库
//            boolean result = privateMessageService.save(privateMessage);
//
//            if (result) {
//                log.info("私信消息保存成功: {}", privateMessage.getId());
//
//                // TODO: 这里可以添加实时推送逻辑，比如WebSocket推送
//                // webSocketService.pushMessage(privateMessage.getToUserId(), privateMessage);
//
//            } else {
//                log.error("私信消息保存失败: {}", message);
//            }
//
//        } catch (Exception e) {
//            log.error("处理私信消息失败: {}", message, e);
//            // TODO: 可以考虑重试机制或者死信队列
//        }
//    }
//}
