package com.example.employment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("policy_apply")
public class PolicyApply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long policyId;
    private String applicantName;
    private String phone;
    private String materialUrl;
    private Integer status;
    private String auditRemark;
    private LocalDateTime applyTime;
    private LocalDateTime auditTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
