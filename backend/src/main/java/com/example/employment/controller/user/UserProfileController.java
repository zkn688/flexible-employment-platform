package com.example.employment.controller.user;

import com.example.employment.common.Result;
import com.example.employment.entity.User;
import com.example.employment.service.UserService;
import com.example.employment.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/user/profile")
public class UserProfileController {
    private final UserService userService;

    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Result<User> profile(HttpServletRequest servletRequest) {
        User user = userService.getById(TokenUtils.getUserId(servletRequest));
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping
    public Result<User> updateProfile(HttpServletRequest servletRequest, @RequestBody User request) {
        Long userId = TokenUtils.getUserId(servletRequest);
        request.setId(userId);
        request.setUsername(null);
        request.setPassword(null);
        userService.updateById(request);
        User user = userService.getById(userId);
        user.setPassword(null);
        return Result.success("修改成功", user);
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(HttpServletRequest servletRequest, @RequestBody Map<String, String> request) {
        Long userId = TokenUtils.getUserId(servletRequest);
        User user = userService.getById(userId);
        if (user == null || !user.getPassword().equals(request.get("oldPassword"))) {
            throw new IllegalArgumentException("原密码错误");
        }
        User update = new User();
        update.setId(userId);
        update.setPassword(request.get("newPassword"));
        userService.updateById(update);
        return Result.success("密码修改成功", null);
    }
}
