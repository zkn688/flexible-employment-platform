package com.example.employment.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.employment.common.Result;
import com.example.employment.entity.LaborContract;
import com.example.employment.service.LaborContractService;
import com.example.employment.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/user/contracts")
public class UserContractController {
    private final LaborContractService laborContractService;

    public UserContractController(LaborContractService laborContractService) {
        this.laborContractService = laborContractService;
    }

    @GetMapping
    public Result<List<LaborContract>> list(HttpServletRequest servletRequest) {
        Long userId = TokenUtils.getUserId(servletRequest);
        return Result.success(laborContractService.list(new LambdaQueryWrapper<LaborContract>()
                .eq(LaborContract::getUserId, userId)
                .orderByDesc(LaborContract::getCreateTime)));
    }

    @GetMapping("/{id}")
    public Result<LaborContract> detail(HttpServletRequest servletRequest, @PathVariable Long id) {
        Long userId = TokenUtils.getUserId(servletRequest);
        LaborContract contract = laborContractService.getById(id);
        if (contract == null || !contract.getUserId().equals(userId)) {
            throw new IllegalArgumentException("合同不存在");
        }
        return Result.success(contract);
    }
}
