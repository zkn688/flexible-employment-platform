package com.example.employment.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.employment.common.Result;
import com.example.employment.entity.SocialPaymentRecord;
import com.example.employment.entity.SocialSecurityApply;
import com.example.employment.service.SocialPaymentRecordService;
import com.example.employment.service.SocialSecurityApplyService;
import com.example.employment.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/user/social-security")
public class UserSocialSecurityController {
    private final SocialSecurityApplyService socialSecurityApplyService;
    private final SocialPaymentRecordService socialPaymentRecordService;

    public UserSocialSecurityController(SocialSecurityApplyService socialSecurityApplyService,
                                        SocialPaymentRecordService socialPaymentRecordService) {
        this.socialSecurityApplyService = socialSecurityApplyService;
        this.socialPaymentRecordService = socialPaymentRecordService;
    }

    @PostMapping("/apply")
    public Result<SocialSecurityApply> apply(HttpServletRequest servletRequest,
                                             @RequestBody SocialSecurityApply request) {
        request.setId(null);
        request.setUserId(TokenUtils.getUserId(servletRequest));
        request.setStatus(0);
        request.setApplyTime(LocalDateTime.now());
        request.setAuditRemark("等待审核");
        socialSecurityApplyService.save(request);
        return Result.success("申请提交成功", request);
    }

    @GetMapping("/applications")
    public Result<List<SocialSecurityApply>> applications(HttpServletRequest servletRequest) {
        Long userId = TokenUtils.getUserId(servletRequest);
        return Result.success(socialSecurityApplyService.list(new LambdaQueryWrapper<SocialSecurityApply>()
                .eq(SocialSecurityApply::getUserId, userId)
                .orderByDesc(SocialSecurityApply::getApplyTime)));
    }

    @GetMapping("/payments")
    public Result<List<SocialPaymentRecord>> payments(HttpServletRequest servletRequest) {
        Long userId = TokenUtils.getUserId(servletRequest);
        return Result.success(socialPaymentRecordService.list(new LambdaQueryWrapper<SocialPaymentRecord>()
                .eq(SocialPaymentRecord::getUserId, userId)
                .orderByDesc(SocialPaymentRecord::getCreateTime)));
    }

    @PutMapping("/payments/{id}/pay")
    public Result<Void> pay(HttpServletRequest servletRequest, @PathVariable Long id) {
        Long userId = TokenUtils.getUserId(servletRequest);
        SocialPaymentRecord record = socialPaymentRecordService.getById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new IllegalArgumentException("缴费记录不存在");
        }
        if (record.getStatus() == 1) {
            throw new IllegalArgumentException("该账单已缴费");
        }
        SocialPaymentRecord update = new SocialPaymentRecord();
        update.setId(id);
        update.setStatus(1);
        update.setPayTime(LocalDateTime.now());
        socialPaymentRecordService.updateById(update);
        return Result.success("缴费成功", null);
    }
}
