package com.example.employment.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.employment.common.Result;
import com.example.employment.entity.Notice;
import com.example.employment.entity.Policy;
import com.example.employment.entity.PolicyApply;
import com.example.employment.service.NoticeService;
import com.example.employment.service.PolicyApplyService;
import com.example.employment.service.PolicyService;
import com.example.employment.service.RedisCacheService;
import com.example.employment.utils.TokenUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserContentController {
    private final NoticeService noticeService;
    private final PolicyService policyService;
    private final PolicyApplyService policyApplyService;
    private final RedisCacheService redisCacheService;

    public UserContentController(NoticeService noticeService, PolicyService policyService,
                                 PolicyApplyService policyApplyService,
                                 RedisCacheService redisCacheService) {
        this.noticeService = noticeService;
        this.policyService = policyService;
        this.policyApplyService = policyApplyService;
        this.redisCacheService = redisCacheService;
    }

    @GetMapping("/notices")
    public Result<List<Notice>> notices() {
        List<Notice> notices = redisCacheService.getOrLoad("content:notices:enabled",
                new TypeReference<List<Notice>>() {
                },
                Duration.ofMinutes(5),
                () -> noticeService.list(new LambdaQueryWrapper<Notice>()
                        .eq(Notice::getStatus, 1)
                        .orderByDesc(Notice::getIsTop)
                        .orderByDesc(Notice::getCreateTime)));
        return Result.success(notices);
    }

    @GetMapping("/notices/{id}")
    public Result<Notice> noticeDetail(@PathVariable Long id) {
        Notice notice = noticeService.getById(id);
        if (notice == null || notice.getStatus() == 0) {
            throw new IllegalArgumentException("公告不存在");
        }
        return Result.success(notice);
    }

    @GetMapping("/policies")
    public Result<List<Policy>> policies() {
        List<Policy> policies = redisCacheService.getOrLoad("content:policies:enabled",
                new TypeReference<List<Policy>>() {
                },
                Duration.ofMinutes(5),
                () -> policyService.list(new LambdaQueryWrapper<Policy>()
                        .eq(Policy::getStatus, 1)
                        .orderByDesc(Policy::getCreateTime)));
        return Result.success(policies);
    }

    @GetMapping("/policies/{id}")
    public Result<Policy> policyDetail(@PathVariable Long id) {
        Policy policy = policyService.getById(id);
        if (policy == null || policy.getStatus() == 0) {
            throw new IllegalArgumentException("政策不存在");
        }
        return Result.success(policy);
    }

    @PostMapping("/policy-applications")
    public Result<PolicyApply> applyPolicy(HttpServletRequest servletRequest, @RequestBody PolicyApply request) {
        Long userId = TokenUtils.getUserId(servletRequest);
        Policy policy = policyService.getById(request.getPolicyId());
        if (policy == null || policy.getStatus() == 0) {
            throw new IllegalArgumentException("政策不存在或已下架");
        }
        request.setId(null);
        request.setUserId(userId);
        request.setStatus(0);
        request.setApplyTime(LocalDateTime.now());
        request.setAuditRemark("等待管理员审核");
        policyApplyService.save(request);
        return Result.success("申报成功", request);
    }

    @GetMapping("/policy-applications")
    public Result<List<PolicyApply>> myPolicyApplications(HttpServletRequest servletRequest) {
        Long userId = TokenUtils.getUserId(servletRequest);
        return Result.success(policyApplyService.list(new LambdaQueryWrapper<PolicyApply>()
                .eq(PolicyApply::getUserId, userId)
                .orderByDesc(PolicyApply::getApplyTime)));
    }
}
