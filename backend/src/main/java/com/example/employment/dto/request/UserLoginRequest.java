package com.example.employment.dto.request;

import lombok.Data;

@Data
public class UserLoginRequest {
    private String username;
    private String password;
    private String captchaId;
    private String captchaCode;
}
