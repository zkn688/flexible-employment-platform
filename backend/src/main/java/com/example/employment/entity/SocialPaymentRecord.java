package com.example.employment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("social_payment_record")
public class SocialPaymentRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String billNo;
    private String paymentMonth;
    private BigDecimal amount;
    private Integer status;
    private LocalDateTime payTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
