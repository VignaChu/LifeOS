package com.lifeos.entity;

import java.time.LocalDateTime;

/**
 * 音乐播放列表配置实体
 * 存储网易云音乐歌单配置，根据情绪值播放不同歌单
 */
public class MusicPlaylist {
    
    private Long id;
    private String name;
    private String description;
    private String neteasePlaylistId;
    private Integer emotionMin;
    private Integer emotionMax;
    private Boolean isDefault;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 默认构造函数
    public MusicPlaylist() {}
    
    // 全参构造函数
    public MusicPlaylist(Long id, String name, String description, String neteasePlaylistId,
                         Integer emotionMin, Integer emotionMax, Boolean isDefault,
                         Integer sortOrder, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.neteasePlaylistId = neteasePlaylistId;
        this.emotionMin = emotionMin;
        this.emotionMax = emotionMax;
        this.isDefault = isDefault;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getNeteasePlaylistId() {
        return neteasePlaylistId;
    }
    
    public void setNeteasePlaylistId(String neteasePlaylistId) {
        this.neteasePlaylistId = neteasePlaylistId;
    }
    
    public Integer getEmotionMin() {
        return emotionMin;
    }
    
    public void setEmotionMin(Integer emotionMin) {
        this.emotionMin = emotionMin;
    }
    
    public Integer getEmotionMax() {
        return emotionMax;
    }
    
    public void setEmotionMax(Integer emotionMax) {
        this.emotionMax = emotionMax;
    }
    
    public Boolean getIsDefault() {
        return isDefault;
    }
    
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * 检查给定的情绪值是否适用于此歌单
     */
    public boolean matchesEmotion(Integer emotionScore) {
        if (emotionScore == null) {
            return isDefault != null && isDefault;
        }
        return emotionScore >= emotionMin && emotionScore <= emotionMax;
    }
    
    @Override
    public String toString() {
        return "MusicPlaylist{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", neteasePlaylistId='" + neteasePlaylistId + '\'' +
                ", emotionMin=" + emotionMin +
                ", emotionMax=" + emotionMax +
                ", isDefault=" + isDefault +
                '}';
    }
}
