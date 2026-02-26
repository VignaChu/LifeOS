package com.lifeos.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class LifeRecord {

    private Long id;
    private Long userId;
    private String content;
    private String recordType;
    private BigDecimal amount;
    private String tags;
    private Integer emotionScore;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    public LifeRecord() {}
    
    public LifeRecord(String content, String recordType, BigDecimal amount,
                      String tags, Integer emotionScore, LocalDateTime recordTime) {
        this.content = content;
        this.recordType = recordType;
        this.amount = amount;
        this.tags = tags;
        this.emotionScore = emotionScore;
        this.recordTime = recordTime;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    // 兼容旧代码的 getter/setter
    public String getOriginalText() { return content; }
    public void setOriginalText(String originalText) { this.content = originalText; }
    
    public String getRecordType() { return recordType; }
    public void setRecordType(String recordType) { this.recordType = recordType; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    
    public Integer getEmotionScore() { return emotionScore; }
    public void setEmotionScore(Integer emotionScore) { this.emotionScore = emotionScore; }
    
    public LocalDateTime getRecordTime() { return recordTime; }
    public void setRecordTime(LocalDateTime recordTime) { this.recordTime = recordTime; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
