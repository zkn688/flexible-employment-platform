package com.example.employment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("social_security_apply")
public class SocialSecurityApply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String applicantName;
    private String idCard;
    private String phone;
    private String insuranceType;
    private String materialUrl;
    private Integer status;
    private String auditRemark;
    private LocalDateTime applyTime;
    private LocalDateTime auditTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
