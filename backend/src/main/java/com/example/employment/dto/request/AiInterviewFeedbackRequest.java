package com.example.employment.dto.request;

import lombok.Data;

@Data
public class AiInterviewFeedbackRequest {
    private String position;
    private String difficulty;
    private String question;
    private String answer;
}
