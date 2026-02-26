package com.lifeos.entity;

import java.time.LocalDateTime;

public class LlmConfig {
    
    private Long id;
    private String provider;
    private String apiKey;
    private String apiUrl;
    private String model;
    private Double temperature;
    private Boolean useLocalRules;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public LlmConfig() {
        this.useLocalRules = false;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    
    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    
    public Boolean getUseLocalRules() { return useLocalRules; }
    public void setUseLocalRules(Boolean useLocalRules) { this.useLocalRules = useLocalRules; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
