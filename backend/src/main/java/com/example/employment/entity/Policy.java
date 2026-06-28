package com.example.employment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("policy")
public class Policy {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String applyCondition;
    private String materialRequired;
    private Integer status;
    private Long createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
