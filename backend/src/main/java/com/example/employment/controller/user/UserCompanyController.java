package com.example.employment.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.employment.common.Result;
import com.example.employment.entity.Company;
import com.example.employment.entity.CompanyReview;
import com.example.employment.service.CompanyReviewService;
import com.example.employment.service.CompanyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/companies")
public class UserCompanyController {
    private final CompanyService companyService;
    private final CompanyReviewService companyReviewService;

    public UserCompanyController(CompanyService companyService, CompanyReviewService companyReviewService) {
        this.companyService = companyService;
        this.companyReviewService = companyReviewService;
    }

    @GetMapping("/{id}")
    public Result<Company> detail(@PathVariable Long id) {
        Company company = companyService.getById(id);
        if (company == null || company.getAuditStatus() != 1 || company.getStatus() != 1) {
            throw new IllegalArgumentException("企业不存在或未通过审核");
        }
        company.setPassword(null);
        return Result.success(company);
    }

    @GetMapping("/{id}/reviews")
    public Result<List<CompanyReview>> reviews(@PathVariable Long id) {
        List<CompanyReview> reviews = companyReviewService.list(new LambdaQueryWrapper<CompanyReview>()
                .eq(CompanyReview::getCompanyId, id)
                .eq(CompanyReview::getStatus, 1)
                .orderByDesc(CompanyReview::getCreateTime));
        return Result.success(reviews);
    }
}
