package com.example.employment.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiInterviewFeedbackResponse {
    private Integer score;
    private String summary;
    private List<String> strengths = new ArrayList<>();
    private List<String> improvements = new ArrayList<>();
    private String sampleAnswer;
    private String source;
}
