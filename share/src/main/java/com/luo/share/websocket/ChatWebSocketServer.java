package com.luo.share.websocket;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/chat/{userId}")
public class ChatWebSocketServer {

    private static ChatMessageMapper chatMessageMapper;

    @Autowired
    public void setChatMessageMapper(ChatMessageMapper mapper) {
        ChatWebSocketServer.chatMessageMapper = mapper;
    }

    private static final Map<Long, Session> ONLINE_SESSIONS = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        ONLINE_SESSIONS.put(userId, session);
        log.info("用户上线: ID={}, 当前在线人数: {}", userId, ONLINE_SESSIONS.size());
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") Long userId) {
        try {
            // 1. 将前端传来的 JSON 字符串解析为树形节点
            JsonNode jsonNode = objectMapper.readTree(message);

            // 2. 提取公共字段
            int type = jsonNode.has("type") ? jsonNode.get("type").asInt() : 1; // 默认为 1 (聊天消息)
            Long toUserId = jsonNode.get("toUserId").asLong();

            // 3. 业务分发
            if (type == 1) {
                // 【情况 A】这是正常的聊天消息
                log.info("收到用户 ID={} 的聊天消息, 发给 ID={}", userId, toUserId);

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(IdWorker.getId()); // 雪花算法生成 ID
                chatMessage.setFromUserId(userId);
                chatMessage.setToUserId(toUserId);
                chatMessage.setContent(jsonNode.get("content").asText());
                chatMessage.setIsRead(0); // 默认未读

                // 存入数据库
                chatMessageMapper.insertMessage(chatMessage);

                // 转发给目标用户
                Session targetSession = ONLINE_SESSIONS.get(toUserId);
                if (targetSession != null && targetSession.isOpen()) {
                    // 为了让前端方便处理，我们给发出去的 JSON 也带上 type = 1
                    Map<String, Object> pushMsg = new HashMap<>();
                    pushMsg.put("type", 1);
                    pushMsg.put("id", chatMessage.getId());
                    pushMsg.put("fromUserId", chatMessage.getFromUserId());
                    pushMsg.put("content", chatMessage.getContent());
                    targetSession.getBasicRemote().sendText(objectMapper.writeValueAsString(pushMsg));
                }
            }
            else if (type == 2) {
                // 【情况 B】这是“已读回执”信号
                log.info("收到用户 ID={} 的已读回执, 通知 ID={}", userId, toUserId);

                Session targetSession = ONLINE_SESSIONS.get(toUserId);
                if (targetSession != null && targetSession.isOpen()) {
                    // 直接转发给对方，不需要存数据库
                    Map<String, Object> receiptMsg = new HashMap<>();
                    receiptMsg.put("type", 2);
                    receiptMsg.put("fromUserId", userId); // 告诉对方，是我(userId)读了你的消息
                    targetSession.getBasicRemote().sendText(objectMapper.writeValueAsString(receiptMsg));
                }
            }
        } catch (Exception e) {
            log.error("WebSocket 处理消息失败", e);
        }
    }

    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        ONLINE_SESSIONS.remove(userId);
        log.info("用户下线: ID={}, 当前在线人数: {}", userId, ONLINE_SESSIONS.size());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 发生错误", error);
    }

    public static boolean isOnline(Long userId) {
        return ONLINE_SESSIONS.containsKey(userId);
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