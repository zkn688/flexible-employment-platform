package com.example.employment.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.employment.common.Result;
import com.example.employment.entity.Resume;
import com.example.employment.service.ResumeService;
import com.example.employment.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/user/resumes")
public class UserResumeController {
    private final ResumeService resumeService;

    public UserResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @GetMapping
    public Result<List<Resume>> list(HttpServletRequest servletRequest) {
        Long userId = TokenUtils.getUserId(servletRequest);
        return Result.success(resumeService.list(new LambdaQueryWrapper<Resume>()
                .eq(Resume::getUserId, userId)
                .eq(Resume::getStatus, 1)
                .orderByDesc(Resume::getCreateTime)));
    }

    @GetMapping("/{id}")
    public Result<Resume> detail(HttpServletRequest servletRequest, @PathVariable Long id) {
        Long userId = TokenUtils.getUserId(servletRequest);
        Resume resume = resumeService.getById(id);
        if (resume == null || !resume.getUserId().equals(userId) || resume.getStatus() == 0) {
            throw new IllegalArgumentException("简历不存在");
        }
        return Result.success(resume);
    }

    @PostMapping
    public Result<Resume> create(HttpServletRequest servletRequest, @RequestBody Resume request) {
        request.setId(null);
        request.setUserId(TokenUtils.getUserId(servletRequest));
        request.setStatus(1);
        request.setAuditStatus(1);
        resumeService.save(request);
        return Result.success("创建成功", request);
    }

    @PutMapping("/{id}")
    public Result<Resume> update(HttpServletRequest servletRequest, @PathVariable Long id, @RequestBody Resume request) {
        Long userId = TokenUtils.getUserId(servletRequest);
        Resume old = resumeService.getById(id);
        if (old == null || !old.getUserId().equals(userId)) {
            throw new IllegalArgumentException("简历不存在");
        }
        request.setId(id);
        request.setUserId(userId);
        resumeService.updateById(request);
        return Result.success("修改成功", resumeService.getById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest servletRequest, @PathVariable Long id) {
        Long userId = TokenUtils.getUserId(servletRequest);
        Resume old = resumeService.getById(id);
        if (old == null || !old.getUserId().equals(userId)) {
            throw new IllegalArgumentException("简历不存在");
        }
        Resume update = new Resume();
        update.setId(id);
        update.setStatus(0);
        resumeService.updateById(update);
        return Result.success("删除成功", null);
    }
}
