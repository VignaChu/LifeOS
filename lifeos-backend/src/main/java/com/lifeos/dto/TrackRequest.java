package com.lifeos.dto;

public class TrackRequest {
    
    private String text;
    private String recordTime;
    
    public TrackRequest() {}
    
    public TrackRequest(String text, String recordTime) {
        this.text = text;
        this.recordTime = recordTime;
    }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public String getRecordTime() { return recordTime; }
    public void setRecordTime(String recordTime) { this.recordTime = recordTime; }
}
