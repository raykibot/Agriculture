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

}
