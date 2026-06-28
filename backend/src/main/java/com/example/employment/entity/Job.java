package com.example.employment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("job")
public class Job {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long companyId;
    private String title;
    private String industry;
    private String jobType;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryUnit;
    private String workCity;
    private String workAddress;
    private String description;
    private String requirement;
    private Integer recruitCount;
    private Integer viewCount;
    private Integer auditStatus;
    private String auditRemark;
    private Integer status;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
