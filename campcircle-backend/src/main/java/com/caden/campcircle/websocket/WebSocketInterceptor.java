package com.caden.campcircle.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
public class WebSocketInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        // 从请求参数中获取用户ID
        String query = request.getURI().getQuery();
        if (query != null && query.contains("userId=")) {
            String userId = query.split("userId=")[1].split("&")[0];
            try {
                attributes.put("userId", Long.parseLong(userId));
                log.info("WebSocket握手成功，用户ID: {}", userId);
                return true;
            } catch (NumberFormatException e) {
                log.error("用户ID格式错误: {}", userId);
            }
        }

        log.warn("WebSocket握手失败，缺少用户ID");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手完成后的处理
    }
}