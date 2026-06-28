package com.example.employment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("company_review")
public class CompanyReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long companyId;
    private Long jobId;
    private Integer score;
    private String content;
    private Integer status;
    private LocalDateTime createTime;
}
