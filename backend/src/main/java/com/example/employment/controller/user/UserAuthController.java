package com.example.employment.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.employment.common.Result;
import com.example.employment.dto.request.UserLoginRequest;
import com.example.employment.dto.request.UserRegisterRequest;
import com.example.employment.dto.response.CaptchaResponse;
import com.example.employment.entity.User;
import com.example.employment.service.CaptchaService;
import com.example.employment.service.UserService;
import com.example.employment.utils.TokenUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserAuthController {
    private final UserService userService;
    private final CaptchaService captchaService;

    public UserAuthController(UserService userService, CaptchaService captchaService) {
        this.userService = userService;
        this.captchaService = captchaService;
    }

    @GetMapping("/captcha")
    public Result<CaptchaResponse> captcha(@RequestParam(defaultValue = "login") String scene) {
        return Result.success(captchaService.generate(scene));
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody UserRegisterRequest request) {
        captchaService.validate("register", request.getCaptchaId(), request.getCaptchaCode());
        if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        if (!StringUtils.hasText(request.getRealName()) || !StringUtils.hasText(request.getPhone())) {
            throw new IllegalArgumentException("真实姓名和手机号不能为空");
        }
        Long count = userService.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setRealName(request.getRealName());
        user.setGender(request.getGender());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(1);
        userService.save(user);
        user.setPassword(null);
        return Result.success("注册成功", user);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody UserLoginRequest request) {
        captchaService.validate("login", request.getCaptchaId(), request.getCaptchaCode());
        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .eq(User::getPassword, request.getPassword())
                .eq(User::getStatus, 1), false);
        if (user == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("phone", user.getPhone());
        data.put("token", TokenUtils.createUserToken(user.getId()));
        return Result.success("登录成功", data);
    }
}
