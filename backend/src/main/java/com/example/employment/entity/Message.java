package com.example.employment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer senderType;
    private Long senderId;
    private Integer receiverType;
    private Long receiverId;
    private String title;
    private String content;
    private Integer isRead;
    private Integer status;
    private LocalDateTime createTime;
}
