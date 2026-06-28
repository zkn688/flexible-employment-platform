package com.example.employment.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class RecommendJobResponse {
    private Long id;
    private Long companyId;
    private String title;
    private String industry;
    private String jobType;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryUnit;
    private String workCity;
    private String workAddress;
    private String description;
    private String requirement;
    private Integer recruitCount;
    private Integer viewCount;
    private LocalDateTime publishTime;
    private Integer matchScore;
    private List<String> matchReasons = new ArrayList<>();
}
