package com.lifeos.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeos.dto.AiParseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "lifeos:";
    private static final long CACHE_TTL_MINUTES = 30;

    /**
     * 检查 Redis 是否可用
     */
    private boolean isRedisAvailable() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 缓存 AI 解析结果
     */
    public void cacheParseResult(String text, AiParseResult result) {
        if (!isRedisAvailable()) {
            log.debug("Redis not available, skipping cache");
            return;
        }
        try {
            String key = CACHE_PREFIX + "parse:" + hashText(text);
            String value = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(key, value, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("Cached parse result for text hash: {}", hashText(text));
        } catch (JsonProcessingException e) {
            log.error("Failed to cache parse result: {}", e.getMessage());
        }
    }

    /**
     * 获取缓存的 AI 解析结果
     */
    public AiParseResult getCachedParseResult(String text) {
        if (!isRedisAvailable()) {
            return null;
        }
        try {
            String key = CACHE_PREFIX + "parse:" + hashText(text);
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("Cache hit for text hash: {}", hashText(text));
                return objectMapper.readValue(value, AiParseResult.class);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize cached result: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 缓存查询结果
     */
    public void cacheQueryResult(String query, String result) {
        if (!isRedisAvailable()) {
            return;
        }
        String key = CACHE_PREFIX + "query:" + hashText(query);
        redisTemplate.opsForValue().set(key, result, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 获取缓存的查询结果
     */
    public String getCachedQueryResult(String query) {
        if (!isRedisAvailable()) {
            return null;
        }
        String key = CACHE_PREFIX + "query:" + hashText(query);
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 清除所有缓存
     */
    public void clearAllCache() {
        if (!isRedisAvailable()) {
            log.warn("Redis not available, cannot clear cache");
            return;
        }
        var keys = redisTemplate.keys(CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Cleared {} cache entries", keys.size());
        }
    }

    /**
     * 生成文本的简单哈希
     */
    private String hashText(String text) {
        return String.valueOf(text.trim().toLowerCase().hashCode());
    }
}
