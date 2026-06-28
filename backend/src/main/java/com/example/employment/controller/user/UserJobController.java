package com.example.employment.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.employment.common.PageResult;
import com.example.employment.common.Result;
import com.example.employment.dto.response.RecommendJobResponse;
import com.example.employment.entity.EmploymentPref;
import com.example.employment.entity.FavoriteJob;
import com.example.employment.entity.Job;
import com.example.employment.service.EmploymentPrefService;
import com.example.employment.service.FavoriteJobService;
import com.example.employment.service.JobService;
import com.example.employment.service.RedisCacheService;
import com.example.employment.utils.TokenUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user/jobs")
public class UserJobController {
    private final JobService jobService;
    private final FavoriteJobService favoriteJobService;
    private final EmploymentPrefService employmentPrefService;
    private final RedisCacheService redisCacheService;

    public UserJobController(JobService jobService, FavoriteJobService favoriteJobService,
                             EmploymentPrefService employmentPrefService,
                             RedisCacheService redisCacheService) {
        this.jobService = jobService;
        this.favoriteJobService = favoriteJobService;
        this.employmentPrefService = employmentPrefService;
        this.redisCacheService = redisCacheService;
    }

    @GetMapping
    public Result<PageResult<Job>> list(@RequestParam(defaultValue = "1") Long pageNum,
                                        @RequestParam(defaultValue = "10") Long pageSize,
                                        String keyword,
                                        String industry,
                                        String jobType,
                                        String workCity,
                                        BigDecimal salaryMin,
                                        BigDecimal salaryMax) {
        LambdaQueryWrapper<Job> wrapper = buildAvailableJobWrapper(keyword, industry, jobType, workCity, salaryMin, salaryMax);
        Page<Job> page = jobService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(new PageResult<>(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Job> detail(@PathVariable Long id) {
        Job job = jobService.getById(id);
        if (job == null || job.getAuditStatus() != 1 || job.getStatus() != 1) {
            throw new IllegalArgumentException("岗位不存在或未上架");
        }
        Job update = new Job();
        update.setId(id);
        update.setViewCount(job.getViewCount() == null ? 1 : job.getViewCount() + 1);
        jobService.updateById(update);
        job.setViewCount(update.getViewCount());
        return Result.success(job);
    }

    @GetMapping("/recommend")
    public Result<PageResult<RecommendJobResponse>> recommend(HttpServletRequest servletRequest,
                                                              @RequestParam(defaultValue = "1") Long pageNum,
                                                              @RequestParam(defaultValue = "10") Long pageSize) {
        Long userId = TokenUtils.getUserId(servletRequest);
        long safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        long safePageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        String cacheKey = "user:recommend:" + userId + ":" + safePageNum + ":" + safePageSize;
        PageResult<RecommendJobResponse> result = redisCacheService.getOrLoad(cacheKey,
                new TypeReference<PageResult<RecommendJobResponse>>() {
                },
                Duration.ofMinutes(10),
                () -> buildRecommendPage(userId, safePageNum, safePageSize));
        return Result.success(result);
    }

    private PageResult<RecommendJobResponse> buildRecommendPage(Long userId, long safePageNum, long safePageSize) {
        EmploymentPref pref = employmentPrefService.getOne(new LambdaQueryWrapper<EmploymentPref>()
                .eq(EmploymentPref::getUserId, userId), false);
        List<Job> jobs = jobService.list(buildAvailableJobWrapper(null, null, null, null, null, null));
        List<RecommendJobResponse> scoredJobs = jobs.stream()
                .map(job -> buildRecommendJob(job, pref))
                .sorted(Comparator.comparing(RecommendJobResponse::getMatchScore).reversed()
                        .thenComparing(RecommendJobResponse::getPublishTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        int fromIndex = (int) Math.min((safePageNum - 1) * safePageSize, scoredJobs.size());
        int toIndex = (int) Math.min(fromIndex + safePageSize, scoredJobs.size());
        List<RecommendJobResponse> records = scoredJobs.subList(fromIndex, toIndex);
        return new PageResult<>(records, (long) scoredJobs.size(), safePageNum, safePageSize);
    }

    @PostMapping("/{id}/favorite")
    public Result<Void> favorite(HttpServletRequest servletRequest, @PathVariable Long id) {
        Long userId = TokenUtils.getUserId(servletRequest);
        Long count = favoriteJobService.count(new LambdaQueryWrapper<FavoriteJob>()
                .eq(FavoriteJob::getUserId, userId)
                .eq(FavoriteJob::getJobId, id));
        if (count == 0) {
            FavoriteJob favoriteJob = new FavoriteJob();
            favoriteJob.setUserId(userId);
            favoriteJob.setJobId(id);
            favoriteJobService.save(favoriteJob);
        }
        return Result.success("收藏成功", null);
    }

    @DeleteMapping("/{id}/favorite")
    public Result<Void> cancelFavorite(HttpServletRequest servletRequest, @PathVariable Long id) {
        Long userId = TokenUtils.getUserId(servletRequest);
        favoriteJobService.remove(new LambdaQueryWrapper<FavoriteJob>()
                .eq(FavoriteJob::getUserId, userId)
                .eq(FavoriteJob::getJobId, id));
        return Result.success("取消收藏成功", null);
    }

    @GetMapping("/favorites")
    public Result<PageResult<Job>> favorites(HttpServletRequest servletRequest,
                                             @RequestParam(defaultValue = "1") Long pageNum,
                                             @RequestParam(defaultValue = "10") Long pageSize) {
        Long userId = TokenUtils.getUserId(servletRequest);
        Page<FavoriteJob> favoritePage = favoriteJobService.page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<FavoriteJob>().eq(FavoriteJob::getUserId, userId)
                        .orderByDesc(FavoriteJob::getCreateTime));
        java.util.List<Job> jobs = favoritePage.getRecords().stream()
                .map(item -> jobService.getById(item.getJobId()))
                .filter(job -> job != null && job.getAuditStatus() == 1 && job.getStatus() == 1)
                .collect(java.util.stream.Collectors.toList());
        return Result.success(new PageResult<>(jobs, favoritePage.getTotal(), pageNum, pageSize));
    }

    private LambdaQueryWrapper<Job> buildAvailableJobWrapper(String keyword, String industry, String jobType,
                                                             String workCity, BigDecimal salaryMin,
                                                             BigDecimal salaryMax) {
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<Job>()
                .eq(Job::getAuditStatus, 1)
                .eq(Job::getStatus, 1)
                .orderByDesc(Job::getPublishTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Job::getTitle, keyword).or().like(Job::getDescription, keyword));
        }
        if (StringUtils.hasText(industry)) {
            wrapper.eq(Job::getIndustry, industry);
        }
        if (StringUtils.hasText(jobType)) {
            wrapper.eq(Job::getJobType, jobType);
        }
        if (StringUtils.hasText(workCity)) {
            wrapper.eq(Job::getWorkCity, workCity);
        }
        if (salaryMin != null) {
            wrapper.ge(Job::getSalaryMax, salaryMin);
        }
        if (salaryMax != null) {
            wrapper.le(Job::getSalaryMin, salaryMax);
        }
        return wrapper;
    }

    private RecommendJobResponse buildRecommendJob(Job job, EmploymentPref pref) {
        RecommendJobResponse response = new RecommendJobResponse();
        response.setId(job.getId());
        response.setCompanyId(job.getCompanyId());
        response.setTitle(job.getTitle());
        response.setIndustry(job.getIndustry());
        response.setJobType(job.getJobType());
        response.setSalaryMin(job.getSalaryMin());
        response.setSalaryMax(job.getSalaryMax());
        response.setSalaryUnit(job.getSalaryUnit());
        response.setWorkCity(job.getWorkCity());
        response.setWorkAddress(job.getWorkAddress());
        response.setDescription(job.getDescription());
        response.setRequirement(job.getRequirement());
        response.setRecruitCount(job.getRecruitCount());
        response.setViewCount(job.getViewCount());
        response.setPublishTime(job.getPublishTime());

        List<String> reasons = new ArrayList<>();
        int score = calculateMatchScore(job, pref, reasons);
        response.setMatchScore(score);
        response.setMatchReasons(reasons);
        return response;
    }

    private int calculateMatchScore(Job job, EmploymentPref pref, List<String> reasons) {
        if (pref == null) {
            reasons.add("完善求职偏好后可获得更精准的匹配度");
            return 45;
        }

        int score = 0;
        if (containsText(job.getTitle(), pref.getPosition()) || containsText(job.getDescription(), pref.getPosition())
                || containsText(job.getRequirement(), pref.getPosition())) {
            score += 25;
            reasons.add("岗位关键词与期望岗位匹配");
        }
        if (equalsText(job.getIndustry(), pref.getIndustry())) {
            score += 20;
            reasons.add("行业符合求职偏好");
        }
        if (equalsText(job.getWorkCity(), pref.getWorkCity())) {
            score += 20;
            reasons.add("工作城市符合求职偏好");
        }
        if (equalsText(job.getJobType(), pref.getJobType())) {
            score += 15;
            reasons.add("岗位类型符合求职偏好");
        }
        if (salaryMatched(job, pref)) {
            score += 10;
            reasons.add("薪资范围与期望薪资有重合");
        }
        if (job.getRecruitCount() != null && job.getRecruitCount() > 0) {
            score += 5;
        }
        if (StringUtils.hasText(job.getDescription()) && StringUtils.hasText(job.getRequirement())) {
            score += 5;
        }

        if (reasons.isEmpty()) {
            reasons.add("岗位已上架，可作为求职备选");
        }
        if (score == 0) {
            return 35;
        }
        return Math.max(35, Math.min(98, score));
    }

    private boolean salaryMatched(Job job, EmploymentPref pref) {
        return pref.getSalaryMin() != null && pref.getSalaryMax() != null
                && job.getSalaryMin() != null && job.getSalaryMax() != null
                && job.getSalaryMax().compareTo(pref.getSalaryMin()) >= 0
                && job.getSalaryMin().compareTo(pref.getSalaryMax()) <= 0;
    }

    private boolean containsText(String source, String target) {
        return StringUtils.hasText(source) && StringUtils.hasText(target)
                && source.toLowerCase().contains(target.toLowerCase());
    }

    private boolean equalsText(String a, String b) {
        return StringUtils.hasText(a) && StringUtils.hasText(b) && a.trim().equals(b.trim());
    }
}
