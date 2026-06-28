package com.example.employment.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplicationResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private Long companyId;
    private String companyName;
    private Long resumeId;
    private String resumeTitle;
    private Integer status;
    private LocalDateTime applyTime;
    private LocalDateTime handleTime;
    private String remark;
}
