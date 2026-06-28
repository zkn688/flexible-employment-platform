package com.example.employment.dto.request;

import lombok.Data;

@Data
public class UserRegisterRequest {
    private String username;
    private String password;
    private String realName;
    private Integer gender;
    private String phone;
    private String email;
    private String captchaId;
    private String captchaCode;
}
