package com.example.employment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("labor_contract")
public class LaborContract {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long companyId;
    private Long jobId;
    private String contractNo;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String fileUrl;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
