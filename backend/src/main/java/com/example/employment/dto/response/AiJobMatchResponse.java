package com.example.employment.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiJobMatchResponse {
    private Integer score;
    private String summary;
    private List<String> reasons = new ArrayList<>();
    private List<String> suggestions = new ArrayList<>();
    private String source;
}
