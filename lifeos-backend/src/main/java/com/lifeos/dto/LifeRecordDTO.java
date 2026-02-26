package com.lifeos.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class LifeRecordDTO {
    private Long id;
    private String content;
    private String recordType;
    private Double amount;
    private List<String> tags;
    private Integer emotionScore;
    private LocalDateTime recordTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
