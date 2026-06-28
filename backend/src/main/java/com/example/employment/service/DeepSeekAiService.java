package com.example.employment.service;

import com.example.employment.dto.response.AiJobMatchResponse;
import com.example.employment.dto.response.AiInterviewFeedbackResponse;
import com.example.employment.dto.response.AiInterviewQuestionResponse;
import com.example.employment.dto.response.AiResumeAdviceResponse;
import com.example.employment.entity.EmploymentPref;
import com.example.employment.entity.Job;
import com.example.employment.entity.Resume;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeepSeekAiService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    @Value("${deepseek.api-key:}")
    private String apiKey;

    public AiJobMatchResponse analyzeJobMatch(Job job, EmploymentPref pref, Resume resume) {
        AiJobMatchResponse fallback = buildRuleBasedResult(job, pref, resume);
        if (!StringUtils.hasText(apiKey)) {
            fallback.setSource("rule");
            fallback.setSummary(fallback.getSummary() + "（未配置 DeepSeek API Key，当前展示规则匹配结果）");
            return fallback;
        }

        try {
            AiJobMatchResponse aiResult = callDeepSeek(job, pref, resume);
            aiResult.setSource("deepseek");
            return aiResult;
        } catch (Exception ex) {
            fallback.setSource("rule");
            fallback.setSummary(fallback.getSummary() + "（AI 接口暂不可用，已切换为规则匹配结果）");
            return fallback;
        }
    }

    public AiResumeAdviceResponse analyzeResumeAdvice(Resume resume, EmploymentPref pref) {
        AiResumeAdviceResponse fallback = buildRuleBasedResumeAdvice(resume, pref);
        if (!StringUtils.hasText(apiKey)) {
            fallback.setSource("rule");
            fallback.setSummary(fallback.getSummary() + "（未配置 DeepSeek API Key，当前展示规则优化建议）");
            return fallback;
        }

        try {
            AiResumeAdviceResponse aiResult = callDeepSeekForResume(resume, pref);
            aiResult.setSource("deepseek");
            return aiResult;
        } catch (Exception ex) {
            fallback.setSource("rule");
            fallback.setSummary(fallback.getSummary() + "（AI 接口暂不可用，已切换为规则优化建议）");
            return fallback;
        }
    }

    public AiInterviewQuestionResponse generateInterviewQuestions(String position, String difficulty) {
        AiInterviewQuestionResponse fallback = buildRuleBasedInterviewQuestions(position, difficulty);
        if (!StringUtils.hasText(apiKey)) {
            fallback.setSource("rule");
            fallback.setSummary(fallback.getSummary() + "（未配置 DeepSeek API Key，当前展示规则面试题）");
            return fallback;
        }

        try {
            AiInterviewQuestionResponse aiResult = callDeepSeekForInterviewQuestions(position, difficulty);
            aiResult.setSource("deepseek");
            return aiResult;
        } catch (Exception ex) {
            fallback.setSource("rule");
            fallback.setSummary(fallback.getSummary() + "（AI 接口暂不可用，已切换为规则面试题）");
            return fallback;
        }
    }

    public AiInterviewFeedbackResponse analyzeInterviewAnswer(String position, String difficulty,
                                                              String question, String answer) {
        AiInterviewFeedbackResponse fallback = buildRuleBasedInterviewFeedback(question, answer);
        if (!StringUtils.hasText(apiKey)) {
            fallback.setSource("rule");
            fallback.setSummary(fallback.getSummary() + "（未配置 DeepSeek API Key，当前展示规则反馈）");
            return fallback;
        }

        try {
            AiInterviewFeedbackResponse aiResult = callDeepSeekForInterviewFeedback(position, difficulty, question, answer);
            aiResult.setSource("deepseek");
            return aiResult;
        } catch (Exception ex) {
            fallback.setSource("rule");
            fallback.setSummary(fallback.getSummary() + "（AI 接口暂不可用，已切换为规则反馈）");
            return fallback;
        }
    }

    private AiJobMatchResponse callDeepSeek(Job job, EmploymentPref pref, Resume resume) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("temperature", 0.3);
        request.put("messages", buildMessages(job, pref, resume));

        String responseText = restTemplate.postForObject(baseUrl + "/chat/completions",
                new HttpEntity<>(request, headers), String.class);
        JsonNode root = objectMapper.readTree(responseText);
        String content = root.path("choices").path(0).path("message").path("content").asText();
        return parseAiContent(content);
    }

    private AiResumeAdviceResponse callDeepSeekForResume(Resume resume, EmploymentPref pref) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("temperature", 0.3);
        request.put("messages", buildResumeMessages(resume, pref));

        String responseText = restTemplate.postForObject(baseUrl + "/chat/completions",
                new HttpEntity<>(request, headers), String.class);
        JsonNode root = objectMapper.readTree(responseText);
        String content = root.path("choices").path(0).path("message").path("content").asText();
        return parseResumeContent(content);
    }

    private AiInterviewQuestionResponse callDeepSeekForInterviewQuestions(String position, String difficulty) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("temperature", 0.4);
        request.put("messages", buildInterviewQuestionMessages(position, difficulty));

        String responseText = restTemplate.postForObject(baseUrl + "/chat/completions",
                new HttpEntity<>(request, headers), String.class);
        JsonNode root = objectMapper.readTree(responseText);
        String content = root.path("choices").path(0).path("message").path("content").asText();
        return parseInterviewQuestionsContent(content);
    }

    private AiInterviewFeedbackResponse callDeepSeekForInterviewFeedback(String position, String difficulty,
                                                                         String question, String answer) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("temperature", 0.3);
        request.put("messages", buildInterviewFeedbackMessages(position, difficulty, question, answer));

        String responseText = restTemplate.postForObject(baseUrl + "/chat/completions",
                new HttpEntity<>(request, headers), String.class);
        JsonNode root = objectMapper.readTree(responseText);
        String content = root.path("choices").path(0).path("message").path("content").asText();
        return parseInterviewFeedbackContent(content);
    }

    private List<Map<String, String>> buildMessages(Job job, EmploymentPref pref, Resume resume) {
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> system = new HashMap<>();
        system.put("role", "system");
        system.put("content", "你是灵活就业服务平台的岗位匹配助手。请只返回 JSON，不要返回 Markdown。");
        messages.add(system);

        Map<String, String> user = new HashMap<>();
        user.put("role", "user");
        user.put("content", buildPrompt(job, pref, resume));
        messages.add(user);
        return messages;
    }

    private List<Map<String, String>> buildResumeMessages(Resume resume, EmploymentPref pref) {
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> system = new HashMap<>();
        system.put("role", "system");
        system.put("content", "你是灵活就业服务平台的简历优化助手。请只返回 JSON，不要返回 Markdown。");
        messages.add(system);

        Map<String, String> user = new HashMap<>();
        user.put("role", "user");
        user.put("content", buildResumePrompt(resume, pref));
        messages.add(user);
        return messages;
    }

    private List<Map<String, String>> buildInterviewQuestionMessages(String position, String difficulty) {
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> system = new HashMap<>();
        system.put("role", "system");
        system.put("content", "你是灵活就业服务平台的 AI 模拟面试官。请只返回 JSON，不要返回 Markdown。");
        messages.add(system);

        Map<String, String> user = new HashMap<>();
        user.put("role", "user");
        user.put("content", buildInterviewQuestionPrompt(position, difficulty));
        messages.add(user);
        return messages;
    }

    private List<Map<String, String>> buildInterviewFeedbackMessages(String position, String difficulty,
                                                                     String question, String answer) {
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> system = new HashMap<>();
        system.put("role", "system");
        system.put("content", "你是灵活就业服务平台的 AI 面试评价助手。请只返回 JSON，不要返回 Markdown。");
        messages.add(system);

        Map<String, String> user = new HashMap<>();
        user.put("role", "user");
        user.put("content", buildInterviewFeedbackPrompt(position, difficulty, question, answer));
        messages.add(user);
        return messages;
    }

    private String buildPrompt(Job job, EmploymentPref pref, Resume resume) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据用户求职偏好、简历信息和岗位信息，分析岗位匹配度。");
        prompt.append("要求返回 JSON：{\"score\":85,\"summary\":\"一句话总结\",\"reasons\":[\"原因1\"],\"suggestions\":[\"建议1\"]}。");
        prompt.append("score 必须是 0 到 100 的整数，reasons 给 3 条，suggestions 给 2 条。\n\n");
        prompt.append("【岗位信息】\n");
        prompt.append("岗位名称：").append(nullToDash(job.getTitle())).append("\n");
        prompt.append("行业：").append(nullToDash(job.getIndustry())).append("\n");
        prompt.append("岗位类型：").append(nullToDash(job.getJobType())).append("\n");
        prompt.append("城市：").append(nullToDash(job.getWorkCity())).append("\n");
        prompt.append("薪资：").append(job.getSalaryMin()).append("-").append(job.getSalaryMax()).append("/").append(nullToDash(job.getSalaryUnit())).append("\n");
        prompt.append("描述：").append(nullToDash(job.getDescription())).append("\n");
        prompt.append("要求：").append(nullToDash(job.getRequirement())).append("\n\n");
        prompt.append("【求职偏好】\n");
        if (pref == null) {
            prompt.append("用户暂未填写求职偏好。\n\n");
        } else {
            prompt.append("期望行业：").append(nullToDash(pref.getIndustry())).append("\n");
            prompt.append("期望岗位：").append(nullToDash(pref.getPosition())).append("\n");
            prompt.append("期望城市：").append(nullToDash(pref.getWorkCity())).append("\n");
            prompt.append("岗位类型：").append(nullToDash(pref.getJobType())).append("\n");
            prompt.append("期望薪资：").append(pref.getSalaryMin()).append("-").append(pref.getSalaryMax()).append("\n\n");
        }
        prompt.append("【简历信息】\n");
        if (resume == null) {
            prompt.append("用户暂未维护简历。");
        } else {
            prompt.append("简历标题：").append(nullToDash(resume.getTitle())).append("\n");
            prompt.append("姓名：").append(nullToDash(resume.getRealName())).append("\n");
            prompt.append("学历：").append(nullToDash(resume.getEducation())).append("\n");
            prompt.append("工作年限：").append(resume.getWorkYears()).append("\n");
            prompt.append("期望岗位：").append(nullToDash(resume.getExpectedPosition())).append("\n");
            prompt.append("期望城市：").append(nullToDash(resume.getExpectedCity())).append("\n");
            prompt.append("自我介绍：").append(nullToDash(resume.getSelfIntro()));
        }
        return prompt.toString();
    }

    private String buildResumePrompt(Resume resume, EmploymentPref pref) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据用户简历和求职偏好，给出简历优化建议。");
        prompt.append("要求返回 JSON：{\"score\":78,\"summary\":\"一句话总结\",\"advantages\":[\"优势1\"],\"suggestions\":[\"建议1\"],\"keywords\":[\"关键词1\"]}。");
        prompt.append("score 必须是 0 到 100 的整数，advantages 给 2-3 条，suggestions 给 3 条，keywords 给 5 个。\n\n");
        prompt.append("【简历信息】\n");
        prompt.append("简历标题：").append(nullToDash(resume.getTitle())).append("\n");
        prompt.append("姓名：").append(nullToDash(resume.getRealName())).append("\n");
        prompt.append("学历：").append(nullToDash(resume.getEducation())).append("\n");
        prompt.append("工作年限：").append(resume.getWorkYears()).append("\n");
        prompt.append("电话：").append(nullToDash(resume.getPhone())).append("\n");
        prompt.append("邮箱：").append(nullToDash(resume.getEmail())).append("\n");
        prompt.append("期望岗位：").append(nullToDash(resume.getExpectedPosition())).append("\n");
        prompt.append("期望城市：").append(nullToDash(resume.getExpectedCity())).append("\n");
        prompt.append("自我介绍：").append(nullToDash(resume.getSelfIntro())).append("\n\n");
        prompt.append("【求职偏好】\n");
        if (pref == null) {
            prompt.append("用户暂未填写求职偏好。");
        } else {
            prompt.append("期望行业：").append(nullToDash(pref.getIndustry())).append("\n");
            prompt.append("期望岗位：").append(nullToDash(pref.getPosition())).append("\n");
            prompt.append("期望城市：").append(nullToDash(pref.getWorkCity())).append("\n");
            prompt.append("岗位类型：").append(nullToDash(pref.getJobType())).append("\n");
            prompt.append("期望薪资：").append(pref.getSalaryMin()).append("-").append(pref.getSalaryMax());
        }
        return prompt.toString();
    }

    private String buildInterviewQuestionPrompt(String position, String difficulty) {
        String safePosition = StringUtils.hasText(position) ? position : "Java开发工程师";
        String safeDifficulty = StringUtils.hasText(difficulty) ? difficulty : "基础";
        return "请为【" + safePosition + "】岗位生成【" + safeDifficulty + "】难度模拟面试题。"
                + "要求返回 JSON：{\"summary\":\"一句话说明\",\"questions\":[\"问题1\"],\"tips\":[\"答题建议1\"]}。"
                + "questions 给 5 道题，包含基础知识、项目经验、问题分析和职业素养；tips 给 3 条。";
    }

    private String buildInterviewFeedbackPrompt(String position, String difficulty, String question, String answer) {
        String safePosition = StringUtils.hasText(position) ? position : "软件开发工程师";
        String safeDifficulty = StringUtils.hasText(difficulty) ? difficulty : "基础";
        return "请评价用户对模拟面试题的回答。"
                + "要求返回 JSON：{\"score\":80,\"summary\":\"一句话评价\",\"strengths\":[\"优点1\"],"
                + "\"improvements\":[\"改进建议1\"],\"sampleAnswer\":\"参考回答\"}。"
                + "score 必须是 0 到 100 的整数，strengths 给 2 条，improvements 给 3 条。"
                + "\n\n岗位：" + safePosition
                + "\n难度：" + safeDifficulty
                + "\n面试题：" + nullToDash(question)
                + "\n用户回答：" + nullToDash(answer);
    }

    private AiJobMatchResponse parseAiContent(String content) throws Exception {
        String json = content.trim();
        if (json.startsWith("```")) {
            json = json.replaceFirst("```json", "").replaceFirst("```", "");
            int end = json.lastIndexOf("```");
            if (end >= 0) {
                json = json.substring(0, end);
            }
        }
        JsonNode node = objectMapper.readTree(json.trim());
        AiJobMatchResponse response = new AiJobMatchResponse();
        response.setScore(clampScore(node.path("score").asInt(70)));
        response.setSummary(node.path("summary").asText("该岗位与当前简历和求职偏好具有一定匹配度。"));
        response.setReasons(toStringList(node.path("reasons"), "岗位信息与用户求职方向存在匹配点"));
        response.setSuggestions(toStringList(node.path("suggestions"), "建议继续完善简历中的技能和项目经历"));
        return response;
    }

    private AiResumeAdviceResponse parseResumeContent(String content) throws Exception {
        String json = content.trim();
        if (json.startsWith("```")) {
            json = json.replaceFirst("```json", "").replaceFirst("```", "");
            int end = json.lastIndexOf("```");
            if (end >= 0) {
                json = json.substring(0, end);
            }
        }
        JsonNode node = objectMapper.readTree(json.trim());
        AiResumeAdviceResponse response = new AiResumeAdviceResponse();
        response.setScore(clampScore(node.path("score").asInt(70)));
        response.setSummary(node.path("summary").asText("简历基础信息较完整，但仍可继续补充技能和项目经历。"));
        response.setAdvantages(toStringList(node.path("advantages"), "求职方向较明确"));
        response.setSuggestions(toStringList(node.path("suggestions"), "建议补充项目经历和技能关键词"));
        response.setKeywords(toStringList(node.path("keywords"), "项目经历"));
        return response;
    }

    private AiInterviewQuestionResponse parseInterviewQuestionsContent(String content) throws Exception {
        String json = cleanJsonContent(content);
        JsonNode node = objectMapper.readTree(json);
        AiInterviewQuestionResponse response = new AiInterviewQuestionResponse();
        response.setSummary(node.path("summary").asText("已根据岗位方向生成模拟面试题。"));
        response.setQuestions(toStringList(node.path("questions"), "请介绍一个你参与过的项目，并说明你的职责。"));
        response.setTips(toStringList(node.path("tips"), "回答时建议结合项目背景、技术方案和结果进行说明"));
        return response;
    }

    private AiInterviewFeedbackResponse parseInterviewFeedbackContent(String content) throws Exception {
        String json = cleanJsonContent(content);
        JsonNode node = objectMapper.readTree(json);
        AiInterviewFeedbackResponse response = new AiInterviewFeedbackResponse();
        response.setScore(clampScore(node.path("score").asInt(70)));
        response.setSummary(node.path("summary").asText("回答具备一定基础，但还可以补充更多项目细节。"));
        response.setStrengths(toStringList(node.path("strengths"), "能够围绕问题给出基本回答"));
        response.setImprovements(toStringList(node.path("improvements"), "建议补充具体项目场景、技术细节和最终结果"));
        response.setSampleAnswer(node.path("sampleAnswer").asText("可以按照“背景、方案、行动、结果”的结构组织回答。"));
        return response;
    }

    private AiJobMatchResponse buildRuleBasedResult(Job job, EmploymentPref pref, Resume resume) {
        int score = 35;
        List<String> reasons = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        if (pref != null) {
            if (equalsText(pref.getWorkCity(), job.getWorkCity())) {
                score += 15;
                reasons.add("期望城市与岗位工作城市一致");
            }
            if (equalsText(pref.getIndustry(), job.getIndustry())) {
                score += 15;
                reasons.add("期望行业与岗位所属行业一致");
            }
            if (equalsText(pref.getJobType(), job.getJobType())) {
                score += 15;
                reasons.add("期望岗位类型与岗位类型一致");
            }
            if (pref.getPosition() != null && job.getTitle() != null && job.getTitle().contains(pref.getPosition())) {
                score += 10;
                reasons.add("期望岗位关键词与岗位名称匹配");
            }
            if (pref.getSalaryMin() != null && pref.getSalaryMax() != null
                    && job.getSalaryMax() != null && job.getSalaryMin() != null
                    && job.getSalaryMax().compareTo(pref.getSalaryMin()) >= 0
                    && job.getSalaryMin().compareTo(pref.getSalaryMax()) <= 0) {
                score += 10;
                reasons.add("岗位薪资范围与期望薪资存在重合");
            }
        }

        if (resume != null) {
            if (StringUtils.hasText(resume.getExpectedPosition()) && job.getTitle() != null
                    && job.getTitle().contains(resume.getExpectedPosition())) {
                score += 10;
                reasons.add("简历期望岗位与当前岗位名称匹配");
            }
            if (StringUtils.hasText(resume.getSelfIntro())) {
                score += 5;
                reasons.add("简历已填写个人优势，便于企业了解求职者能力");
            }
        }

        if (reasons.isEmpty()) {
            reasons.add("岗位处于招聘中状态，可以作为求职备选");
            reasons.add("建议结合岗位要求进一步完善求职偏好和简历");
        }
        while (reasons.size() < 3) {
            reasons.add("岗位信息完整，便于投递前进行综合判断");
        }

        suggestions.add("投递前建议检查简历中的期望岗位、城市和联系方式是否完整");
        suggestions.add("可以在自我介绍中补充与该岗位相关的课程项目、实习经历或技能关键词");

        AiJobMatchResponse response = new AiJobMatchResponse();
        response.setScore(clampScore(score));
        response.setSummary("根据求职偏好和简历信息，该岗位匹配度为 " + response.getScore() + " 分。");
        response.setReasons(reasons.size() > 3 ? reasons.subList(0, 3) : reasons);
        response.setSuggestions(suggestions);
        response.setSource("rule");
        return response;
    }

    private AiResumeAdviceResponse buildRuleBasedResumeAdvice(Resume resume, EmploymentPref pref) {
        int score = 30;
        List<String> advantages = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        List<String> keywords = new ArrayList<>();

        if (StringUtils.hasText(resume.getTitle())) {
            score += 8;
        }
        if (StringUtils.hasText(resume.getRealName()) && StringUtils.hasText(resume.getPhone())) {
            score += 12;
            advantages.add("姓名和联系电话完整，便于企业联系");
        }
        if (StringUtils.hasText(resume.getEmail())) {
            score += 8;
        } else {
            suggestions.add("建议补充邮箱，方便企业发送面试或合同相关信息");
        }
        if (StringUtils.hasText(resume.getEducation())) {
            score += 10;
            advantages.add("已填写学历信息，基础信息较完整");
        } else {
            suggestions.add("建议补充学历信息，提高简历可信度");
        }
        if (StringUtils.hasText(resume.getExpectedPosition())) {
            score += 12;
            advantages.add("期望岗位明确，有利于系统推荐合适岗位");
            keywords.add(resume.getExpectedPosition());
        } else {
            suggestions.add("建议填写期望岗位，让企业更快判断求职方向");
        }
        if (StringUtils.hasText(resume.getExpectedCity())) {
            score += 8;
        } else {
            suggestions.add("建议填写期望城市，提升本地岗位匹配效果");
        }
        if (StringUtils.hasText(resume.getSelfIntro()) && resume.getSelfIntro().length() >= 30) {
            score += 12;
            advantages.add("自我介绍已有一定内容，可以体现个人优势");
        } else {
            suggestions.add("建议在自我介绍中补充技能、课程项目、实习经历或获奖情况");
        }

        if (pref != null) {
            if (StringUtils.hasText(pref.getIndustry())) {
                keywords.add(pref.getIndustry());
            }
            if (StringUtils.hasText(pref.getPosition())) {
                keywords.add(pref.getPosition());
            }
        }

        keywords.add("项目经历");
        keywords.add("技能关键词");
        keywords.add("实习经历");
        keywords.add("沟通能力");

        while (advantages.size() < 2) {
            advantages.add("简历已具备基础投递信息，可以继续完善细节");
        }
        while (suggestions.size() < 3) {
            suggestions.add("建议量化描述个人经历，例如项目成果、负责模块和使用技术");
        }

        AiResumeAdviceResponse response = new AiResumeAdviceResponse();
        response.setScore(clampScore(score));
        response.setSummary("根据简历完整度和求职方向，当前简历优化评分为 " + response.getScore() + " 分。");
        response.setAdvantages(advantages.size() > 3 ? advantages.subList(0, 3) : advantages);
        response.setSuggestions(suggestions.size() > 3 ? suggestions.subList(0, 3) : suggestions);
        response.setKeywords(keywords.size() > 5 ? keywords.subList(0, 5) : keywords);
        response.setSource("rule");
        return response;
    }

    private AiInterviewQuestionResponse buildRuleBasedInterviewQuestions(String position, String difficulty) {
        String safePosition = StringUtils.hasText(position) ? position : "软件开发工程师";
        List<String> questions = new ArrayList<>();
        questions.add("请做一个简短的自我介绍，并说明你为什么想应聘" + safePosition + "。");
        questions.add("请介绍一个你做过的项目，包括项目背景、技术栈、你负责的模块和最终效果。");
        questions.add("如果线上接口响应变慢，你会从哪些方面定位问题？");
        questions.add("请说明你对数据库索引、事务或缓存的理解，并举一个项目中的使用场景。");
        questions.add("如果团队成员对技术方案有不同意见，你会如何沟通和推进？");

        List<String> tips = new ArrayList<>();
        tips.add("回答技术题时建议结合具体项目，而不是只背概念。");
        tips.add("可以使用“背景-任务-行动-结果”的结构组织答案。");
        tips.add("如果遇到不会的问题，可以说明自己的分析思路和后续学习计划。");

        AiInterviewQuestionResponse response = new AiInterviewQuestionResponse();
        response.setSummary("已为 " + safePosition + " 生成 " + nullToDash(difficulty) + " 难度模拟面试题。");
        response.setQuestions(questions);
        response.setTips(tips);
        response.setSource("rule");
        return response;
    }

    private AiInterviewFeedbackResponse buildRuleBasedInterviewFeedback(String question, String answer) {
        int score = 45;
        List<String> strengths = new ArrayList<>();
        List<String> improvements = new ArrayList<>();

        if (StringUtils.hasText(answer)) {
            score += Math.min(30, answer.length() / 8);
            strengths.add("回答已经围绕题目展开，具备基本表达。");
        }
        if (StringUtils.hasText(answer) && answer.length() >= 80) {
            score += 10;
            strengths.add("回答内容相对完整，有一定细节。");
        }
        if (StringUtils.hasText(answer) && (answer.contains("项目") || answer.contains("技术") || answer.contains("实现"))) {
            score += 8;
            strengths.add("回答中包含项目或技术相关描述。");
        }

        if (!StringUtils.hasText(answer) || answer.length() < 60) {
            improvements.add("回答略短，建议补充项目背景、个人职责和实现结果。");
        }
        improvements.add("建议使用“问题背景、解决方案、个人行动、最终结果”的结构。");
        improvements.add("可以补充具体技术点，例如框架、数据库、接口设计、性能优化等。");
        improvements.add("结尾可以说明自己从该经历中获得的经验。");

        while (strengths.size() < 2) {
            strengths.add("能够根据题目进行基本思考，可以继续加强结构化表达。");
        }

        AiInterviewFeedbackResponse response = new AiInterviewFeedbackResponse();
        response.setScore(clampScore(score));
        response.setSummary("本次回答评分为 " + response.getScore() + " 分，建议继续补充项目细节和量化结果。");
        response.setStrengths(strengths.size() > 2 ? strengths.subList(0, 2) : strengths);
        response.setImprovements(improvements.size() > 3 ? improvements.subList(0, 3) : improvements);
        response.setSampleAnswer("可以这样组织：首先说明题目相关的项目背景，然后描述你负责的模块，接着说明使用的技术方案和遇到的问题，最后给出结果，例如性能提升、功能上线或用户反馈。");
        response.setSource("rule");
        return response;
    }

    private List<String> toStringList(JsonNode node, String defaultValue) {
        List<String> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : node) {
                if (StringUtils.hasText(item.asText())) {
                    list.add(item.asText());
                }
            }
        }
        if (list.isEmpty()) {
            list.add(defaultValue);
        }
        return list;
    }

    private String cleanJsonContent(String content) {
        String json = content.trim();
        if (json.startsWith("```")) {
            json = json.replaceFirst("```json", "").replaceFirst("```", "");
            int end = json.lastIndexOf("```");
            if (end >= 0) {
                json = json.substring(0, end);
            }
        }
        return json.trim();
    }

    private boolean equalsText(String a, String b) {
        return StringUtils.hasText(a) && StringUtils.hasText(b) && a.trim().equals(b.trim());
    }

    private int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }

    private String nullToDash(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }
}
