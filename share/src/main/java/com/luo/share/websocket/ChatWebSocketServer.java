package com.luo.share.websocket;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luo.share.mapper.ChatMessageMapper;
import com.luo.share.model.entity.ChatMessage;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
// 客户端连接的 URL 格式: ws://localhost:9191/ws/chat/{userId}
@ServerEndpoint("/ws/chat/{userId}")
public class ChatWebSocketServer {

    // 解决 WebSocket 无法直接注入 Mapper 的天坑
    private static ChatMessageMapper chatMessageMapper;

    @Autowired
    public void setChatMessageMapper(ChatMessageMapper mapper) {
        ChatWebSocketServer.chatMessageMapper = mapper;
    }

    // 存放所有在线用户的 WebSocket Session
    // 必须使用 ConcurrentHashMap 保证多线程并发安全！
    private static final Map<Long, Session> ONLINE_SESSIONS = new ConcurrentHashMap<>();

    // 用于 JSON 和 Java 对象相互转换
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 1. 建立连接时触发 (用户上线)
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        ONLINE_SESSIONS.put(userId, session);
        log.info("用户上线: ID={}, 当前在线人数: {}", userId, ONLINE_SESSIONS.size());
    }

    /**
     * 2. 收到客户端消息时触发 (转发并存库)
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") Long userId) {
        log.info("收到用户 ID={} 的消息: {}", userId, message);
        try {
            // 解析前端传来的 JSON 字符串为对象
            // 假设前端传的格式为: {"toUserId": 2, "content": "老板，这台拖拉机能便宜点吗？"}
            ChatMessage chatMessage = objectMapper.readValue(message, ChatMessage.class);
            chatMessage.setFromUserId(userId);
            chatMessage.setIsRead(0); // 默认未读

            chatMessage.setId(IdWorker.getId());

            // 存入数据库 (持久化)
            chatMessageMapper.insertMessage(chatMessage);

            // 核心逻辑：转发给目标用户
            Session targetSession = ONLINE_SESSIONS.get(chatMessage.getToUserId());
            if (targetSession != null && targetSession.isOpen()) {
                // 对方在线，直接把 JSON 消息推过去
                String jsonMessage = objectMapper.writeValueAsString(chatMessage);
                targetSession.getBasicRemote().sendText(jsonMessage);
            } else {
                // 对方不在线，消息已经存入数据库了，对方下次上线时去查历史记录即可
                log.info("用户 ID={} 不在线，消息已转为离线存储", chatMessage.getToUserId());
            }
        } catch (Exception e) {
            log.error("处理消息失败", e);
        }
    }

    /**
     * 3. 连接关闭时触发 (用户下线)
     */
    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        ONLINE_SESSIONS.remove(userId);
        log.info("用户下线: ID={}, 当前在线人数: {}", userId, ONLINE_SESSIONS.size());
    }

    /**
     * 4. 发生错误时触发
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 发生错误", error);
    }

    /**
     * 暴露给外部调用的强制下线方法
     */
    public static void forceDisconnect(Long userId) {
        Session session = ONLINE_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "用户已退出登录"));
                log.info("已强制断开用户 ID:{} 的 WebSocket 连接", userId);
            } catch (IOException e) {
                log.error("强制断开 WebSocket 失败", e);
            }
        }
    }
}