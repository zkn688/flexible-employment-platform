package com.example.employment.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.employment.common.Result;
import com.example.employment.dto.response.ApplicationResponse;
import com.example.employment.entity.Application;
import com.example.employment.entity.Company;
import com.example.employment.entity.Job;
import com.example.employment.entity.Resume;
import com.example.employment.service.ApplicationService;
import com.example.employment.service.CompanyService;
import com.example.employment.service.JobService;
import com.example.employment.service.ResumeService;
import com.example.employment.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user/applications")
public class UserApplicationController {
    private final ApplicationService applicationService;
    private final JobService jobService;
    private final ResumeService resumeService;
    private final CompanyService companyService;

    public UserApplicationController(ApplicationService applicationService, JobService jobService,
                                     ResumeService resumeService, CompanyService companyService) {
        this.applicationService = applicationService;
        this.jobService = jobService;
        this.resumeService = resumeService;
        this.companyService = companyService;
    }

    @PostMapping
    public Result<Application> create(HttpServletRequest servletRequest, @RequestBody Application request) {
        Long userId = TokenUtils.getUserId(servletRequest);
        Job job = jobService.getById(request.getJobId());
        if (job == null || job.getAuditStatus() != 1 || job.getStatus() != 1) {
            throw new IllegalArgumentException("岗位不存在或未上架");
        }
        Resume resume = resumeService.getById(request.getResumeId());
        if (resume == null || !resume.getUserId().equals(userId) || resume.getStatus() == 0) {
            throw new IllegalArgumentException("请选择有效的本人简历");
        }
        Long count = applicationService.count(new LambdaQueryWrapper<Application>()
                .eq(Application::getUserId, userId)
                .eq(Application::getJobId, request.getJobId()));
        if (count > 0) {
            throw new IllegalArgumentException("该岗位已投递，请勿重复投递");
        }
        request.setId(null);
        request.setUserId(userId);
        request.setCompanyId(job.getCompanyId());
        request.setStatus(0);
        request.setApplyTime(LocalDateTime.now());
        request.setRemark("用户已投递，等待企业处理");
        applicationService.save(request);
        return Result.success("投递成功", request);
    }

    @GetMapping
    public Result<List<ApplicationResponse>> list(HttpServletRequest servletRequest) {
        Long userId = TokenUtils.getUserId(servletRequest);
        List<Application> applications = applicationService.list(new LambdaQueryWrapper<Application>()
                .eq(Application::getUserId, userId)
                .orderByDesc(Application::getApplyTime));
        List<ApplicationResponse> responses = applications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return Result.success(responses);
    }

    @GetMapping("/{id}")
    public Result<ApplicationResponse> detail(HttpServletRequest servletRequest, @PathVariable Long id) {
        Long userId = TokenUtils.getUserId(servletRequest);
        Application application = applicationService.getById(id);
        if (application == null || !application.getUserId().equals(userId)) {
            throw new IllegalArgumentException("投递记录不存在");
        }
        return Result.success(toResponse(application));
    }

    @PutMapping("/{id}/withdraw")
    public Result<Void> withdraw(HttpServletRequest servletRequest, @PathVariable Long id) {
        Long userId = TokenUtils.getUserId(servletRequest);
        Application application = applicationService.getById(id);
        if (application == null || !application.getUserId().equals(userId)) {
            throw new IllegalArgumentException("投递记录不存在");
        }
        if (application.getStatus() != 0 && application.getStatus() != 1) {
            throw new IllegalArgumentException("当前状态不可撤回");
        }
        Application update = new Application();
        update.setId(id);
        update.setStatus(4);
        update.setHandleTime(LocalDateTime.now());
        update.setRemark("用户已撤回投递");
        applicationService.updateById(update);
        return Result.success("撤回成功", null);
    }

    private ApplicationResponse toResponse(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setJobId(application.getJobId());
        response.setCompanyId(application.getCompanyId());
        response.setResumeId(application.getResumeId());
        response.setStatus(application.getStatus());
        response.setApplyTime(application.getApplyTime());
        response.setHandleTime(application.getHandleTime());
        response.setRemark(application.getRemark());

        Job job = jobService.getById(application.getJobId());
        if (job != null) {
            response.setJobTitle(job.getTitle());
        }
        Company company = companyService.getById(application.getCompanyId());
        if (company != null) {
            response.setCompanyName(company.getCompanyName());
        }
        Resume resume = resumeService.getById(application.getResumeId());
        if (resume != null) {
            response.setResumeTitle(resume.getTitle());
        }
        return response;
    }
}
