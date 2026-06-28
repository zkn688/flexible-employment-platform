package com.example.employment.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.employment.common.Result;
import com.example.employment.dto.request.AiInterviewFeedbackRequest;
import com.example.employment.dto.request.AiInterviewQuestionRequest;
import com.example.employment.dto.response.AiJobMatchResponse;
import com.example.employment.dto.response.AiInterviewFeedbackResponse;
import com.example.employment.dto.response.AiInterviewQuestionResponse;
import com.example.employment.dto.response.AiResumeAdviceResponse;
import com.example.employment.entity.EmploymentPref;
import com.example.employment.entity.Job;
import com.example.employment.entity.Resume;
import com.example.employment.service.DeepSeekAiService;
import com.example.employment.service.EmploymentPrefService;
import com.example.employment.service.JobService;
import com.example.employment.service.ResumeService;
import com.example.employment.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user/ai")
public class UserAiController {
    private final DeepSeekAiService deepSeekAiService;
    private final JobService jobService;
    private final EmploymentPrefService employmentPrefService;
    private final ResumeService resumeService;

    public UserAiController(DeepSeekAiService deepSeekAiService, JobService jobService,
                            EmploymentPrefService employmentPrefService, ResumeService resumeService) {
        this.deepSeekAiService = deepSeekAiService;
        this.jobService = jobService;
        this.employmentPrefService = employmentPrefService;
        this.resumeService = resumeService;
    }

    @PostMapping("/job-match/{jobId}")
    public Result<AiJobMatchResponse> analyzeJobMatch(HttpServletRequest request, @PathVariable Long jobId) {
        Long userId = TokenUtils.getUserId(request);
        Job job = jobService.getById(jobId);
        if (job == null || job.getAuditStatus() != 1 || job.getStatus() != 1) {
            throw new IllegalArgumentException("岗位不存在或未上架");
        }

        EmploymentPref pref = employmentPrefService.getOne(new LambdaQueryWrapper<EmploymentPref>()
                .eq(EmploymentPref::getUserId, userId), false);
        Resume resume = resumeService.getOne(new LambdaQueryWrapper<Resume>()
                .eq(Resume::getUserId, userId)
                .eq(Resume::getStatus, 1)
                .orderByDesc(Resume::getUpdateTime)
                .orderByDesc(Resume::getCreateTime), false);

        return Result.success("AI 匹配分析完成", deepSeekAiService.analyzeJobMatch(job, pref, resume));
    }

    @PostMapping("/resume-advice/{resumeId}")
    public Result<AiResumeAdviceResponse> analyzeResumeAdvice(HttpServletRequest request, @PathVariable Long resumeId) {
        Long userId = TokenUtils.getUserId(request);
        Resume resume = resumeService.getById(resumeId);
        if (resume == null || !userId.equals(resume.getUserId()) || resume.getStatus() == 0) {
            throw new IllegalArgumentException("简历不存在");
        }

        EmploymentPref pref = employmentPrefService.getOne(new LambdaQueryWrapper<EmploymentPref>()
                .eq(EmploymentPref::getUserId, userId), false);

        return Result.success("AI 简历优化建议生成成功", deepSeekAiService.analyzeResumeAdvice(resume, pref));
    }

    @PostMapping("/interview/questions")
    public Result<AiInterviewQuestionResponse> generateInterviewQuestions(HttpServletRequest request,
                                                                          @RequestBody AiInterviewQuestionRequest body) {
        TokenUtils.getUserId(request);
        return Result.success("AI 面试题生成成功",
                deepSeekAiService.generateInterviewQuestions(body.getPosition(), body.getDifficulty()));
    }

    @PostMapping("/interview/feedback")
    public Result<AiInterviewFeedbackResponse> analyzeInterviewAnswer(HttpServletRequest request,
                                                                      @RequestBody AiInterviewFeedbackRequest body) {
        TokenUtils.getUserId(request);
        return Result.success("AI 面试反馈生成成功",
                deepSeekAiService.analyzeInterviewAnswer(body.getPosition(), body.getDifficulty(),
                        body.getQuestion(), body.getAnswer()));
    }
}
