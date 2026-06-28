package com.example.employment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("employment_pref")
public class EmploymentPref {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String industry;
    private String position;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String workCity;
    private String jobType;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
