package com.example.employment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("application")
public class Application {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long jobId;
    private Long companyId;
    private Long resumeId;
    private Integer status;
    private LocalDateTime applyTime;
    private LocalDateTime handleTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
