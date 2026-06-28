package com.example.employment.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.employment.common.Result;
import com.example.employment.entity.EmploymentPref;
import com.example.employment.service.EmploymentPrefService;
import com.example.employment.service.RedisCacheService;
import com.example.employment.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user/preference")
public class UserPreferenceController {
    private final EmploymentPrefService employmentPrefService;
    private final RedisCacheService redisCacheService;

    public UserPreferenceController(EmploymentPrefService employmentPrefService, RedisCacheService redisCacheService) {
        this.employmentPrefService = employmentPrefService;
        this.redisCacheService = redisCacheService;
    }

    @GetMapping
    public Result<EmploymentPref> getPreference(HttpServletRequest servletRequest) {
        Long userId = TokenUtils.getUserId(servletRequest);
        EmploymentPref pref = employmentPrefService.getOne(new LambdaQueryWrapper<EmploymentPref>()
                .eq(EmploymentPref::getUserId, userId), false);
        return Result.success(pref);
    }

    @PostMapping
    public Result<EmploymentPref> savePreference(HttpServletRequest servletRequest, @RequestBody EmploymentPref request) {
        Long userId = TokenUtils.getUserId(servletRequest);
        EmploymentPref old = employmentPrefService.getOne(new LambdaQueryWrapper<EmploymentPref>()
                .eq(EmploymentPref::getUserId, userId), false);
        request.setUserId(userId);
        if (old == null) {
            employmentPrefService.save(request);
        } else {
            request.setId(old.getId());
            employmentPrefService.updateById(request);
        }
        redisCacheService.evict("user:recommend:" + userId + ":1:6");
        redisCacheService.evict("user:recommend:" + userId + ":1:10");
        return Result.success("保存成功", request);
    }
}
