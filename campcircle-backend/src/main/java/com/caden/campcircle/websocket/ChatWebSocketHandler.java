package com.caden.campcircle.websocket;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Map<Long, WebSocketSession> USER_SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从session中获取用户ID (通过拦截器设置)
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            USER_SESSIONS.put(userId, session);
            log.info("用户 {} WebSocket连接建立", userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            USER_SESSIONS.remove(userId);
            log.info("用户 {} WebSocket连接关闭", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理客户端发送的消息（如心跳包）
        log.info("收到WebSocket消息: {}", message.getPayload());
    }

    /**
     * 发送消息给特定用户
     */
    public boolean sendMessageToUser(Long userId, Object message) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(JSON.toJSONString(message)));
                log.info("向用户 {} 发送WebSocket消息成功", userId);
                return true;
            } catch (Exception e) {
                log.error("WebSocket发送消息失败", e);
                // 移除无效session
                USER_SESSIONS.remove(userId);
            }
        }
        return false;
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return USER_SESSIONS.size();
    }
}