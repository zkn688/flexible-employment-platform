package com.example.employment.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiResumeAdviceResponse {
    private Integer score;
    private String summary;
    private List<String> advantages = new ArrayList<>();
    private List<String> suggestions = new ArrayList<>();
    private List<String> keywords = new ArrayList<>();
    private String source;
}
