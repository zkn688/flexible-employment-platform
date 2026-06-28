package com.example.employment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("resume")
public class Resume {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String realName;
    private Integer gender;
    private LocalDate birthday;
    private String education;
    private Integer workYears;
    private String phone;
    private String email;
    private String expectedPosition;
    private String expectedCity;
    private String selfIntro;
    private String attachmentUrl;
    private Integer auditStatus;
    private String auditRemark;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
