package com.luo.share.controller;

import com.luo.share.common.api.Result;
import com.luo.share.mapper.ChatMessageMapper;
import com.luo.share.mapper.UserMapper;
import com.luo.share.model.entity.ChatMessage;
import com.luo.share.model.vo.ContactVO;
import com.luo.share.websocket.ChatWebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 1. 获取两个用户之间的历史聊天记录
     */
    @GetMapping("/history")
    public Result<List<ChatMessage>> getHistory(@RequestParam Long userId1, @RequestParam Long userId2) {
        List<ChatMessage> historyList = chatMessageMapper.selectHistory(userId1, userId2);
        return Result.success(historyList);
    }

    /**
     * 2. 查询单个用户的在线状态 (用于聊天面板顶部展示)
     */
    @GetMapping("/online-status")
    public Result<Boolean> getUserOnlineStatus(@RequestParam Long userId) {
        return Result.success(ChatWebSocketServer.isOnline(userId));
    }

    /**
     * 3. 批量查询用户的在线状态 (用于左侧联系人列表渲染)
     * 前端传入一个包含多个 userId 的数组，后端返回 Map<userId, isOnline>
     */
    @PostMapping("/online-status/batch")
    public Result<Map<Long, Boolean>> getBatchOnlineStatus(@RequestBody List<Long> userIds) {
        Map<Long, Boolean> statusMap = new HashMap<>();
        if (userIds != null && !userIds.isEmpty()) {
            for (Long id : userIds) {
                statusMap.put(id, ChatWebSocketServer.isOnline(id));
            }
        }
        return Result.success(statusMap);
    }

    /**
     * 4. 将对方发给我的消息标记为已读
     */
    @PostMapping("/read")
    public Result<String> markAsRead(@RequestParam Long fromUserId, @RequestParam Long toUserId) {
        // fromUserId: 对方的ID
        // toUserId: 当前登录用户的ID
        chatMessageMapper.updateUnreadToRead(fromUserId, toUserId);
        return Result.success("已读状态更新成功");
    }


    /**
     * 5. 获取用户的全局未读消息总数
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(@RequestParam Long userId) {
        int count = chatMessageMapper.countTotalUnread(userId);
        return Result.success(count);
    }

    @GetMapping("/contacts")
    public Result<List<ContactVO>> getContacts(@RequestParam("userId") Long userId) {
        // 这里直接调用 mapper，你也可以写在 Service 层里
        List<ContactVO> list = userMapper.selectChatContacts(userId);
        return Result.success(list);
    }

    /**
     * 获取单个用户的基本信息 (用于发起新聊天)
     */
    @GetMapping("/basicInfo")
    public Result<ContactVO> getUserBasicInfo(@RequestParam("userId") Long userId) {
        ContactVO contact = userMapper.selectUserBasicInfo(userId);
        if (contact != null) {
            contact.setRole("机主"); // 默认给个身份标识
        }
        return Result.success(contact);
    }
}