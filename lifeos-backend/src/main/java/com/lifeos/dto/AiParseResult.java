package com.lifeos.dto;

import java.math.BigDecimal;
import java.util.List;

public class AiParseResult {
    
    private List<String> recordTypes;
    private BigDecimal amount;
    private List<String> tags;
    private Integer emotionScore;
    private String recordTime;
    private String summary;
    
    public AiParseResult() {}
    
    public AiParseResult(List<String> recordTypes, BigDecimal amount, List<String> tags, 
                         Integer emotionScore, String recordTime, String summary) {
        this.recordTypes = recordTypes;
        this.amount = amount;
        this.tags = tags;
        this.emotionScore = emotionScore;
        this.recordTime = recordTime;
        this.summary = summary;
    }
    
    public static AiParseResult of(List<String> recordTypes, BigDecimal amount, 
                                    List<String> tags, Integer emotionScore, 
                                    String recordTime, String summary) {
        return new AiParseResult(recordTypes, amount, tags, emotionScore, recordTime, summary);
    }
    
    public List<String> getRecordTypes() { return recordTypes; }
    public void setRecordTypes(List<String> recordTypes) { this.recordTypes = recordTypes; }
    
    public String getRecordType() { 
        return recordTypes != null && !recordTypes.isEmpty() ? recordTypes.get(0) : "diary"; 
    }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public Integer getEmotionScore() { return emotionScore; }
    public void setEmotionScore(Integer emotionScore) { this.emotionScore = emotionScore; }
    
    public String getRecordTime() { return recordTime; }
    public void setRecordTime(String recordTime) { this.recordTime = recordTime; }
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
