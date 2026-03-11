package com.luo.share.mapper;

import com.luo.share.model.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {


    /**
     * 插入一条聊天记录
     */
    int insertMessage(ChatMessage chatMessage);

    /**
     * 查询两个用户之间的聊天记录（按时间升序，也就是从旧到新排列）
     */
    List<ChatMessage> selectHistory(@Param("userId1") Long userId1,
                                    @Param("userId2") Long userId2);

    /**
     * 将某个用户发给我的所有未读消息标记为已读
     * @param fromUserId 发送方 (对方的ID)
     * @param toUserId   接收方 (我自己的ID)
     */
    void updateUnreadToRead(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);

    /**
     * 统计某个用户所有的未读消息总数
     */
    int countTotalUnread(@Param("userId") Long userId);

}
