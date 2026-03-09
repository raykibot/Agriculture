package com.luo.share.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("chat_messages")
public class ChatMessage {

    // 使用 MyBatis-Plus 自带的雪花算法生成分布式唯一 ID
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long fromUserId;

    private Long toUserId;

    private String content;

    // 0-未读，1-已读
    private Integer isRead;

    private Date createTime;
}