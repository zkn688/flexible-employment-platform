package com.example.employment.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiInterviewQuestionResponse {
    private String summary;
    private List<String> questions = new ArrayList<>();
    private List<String> tips = new ArrayList<>();
    private String source;
}
