package com.example.employment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("company")
public class Company {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String companyName;
    private String creditCode;
    private String legalPerson;
    private String contactName;
    private String contactPhone;
    private String email;
    private String address;
    private String industry;
    private String description;
    private String licenseUrl;
    private Integer auditStatus;
    private String auditRemark;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
