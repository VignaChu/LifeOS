package com.lifeos.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeos.dto.AiParseResult;
import com.lifeos.entity.LifeRecord;
import com.lifeos.mapper.LifeRecordMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackService {

    private final SpringAiService springAiService;
    private final LifeRecordMapper lifeRecordMapper;
    private final CacheService cacheService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public LifeRecord processText(String text) throws Exception {
        return processText(text, 1L); // 默认用户ID为1
    }

    @Transactional
    public LifeRecord processText(String text, Long userId) throws Exception {
        // 先检查缓存
        AiParseResult cachedResult = cacheService.getCachedParseResult(text);
        AiParseResult parseResult;
        
        if (cachedResult != null) {
            log.info("Using cached parse result for text");
            parseResult = cachedResult;
        } else {
            // 使用 Spring AI 解析
            parseResult = springAiService.parseText(text);
            // 缓存结果
            cacheService.cacheParseResult(text, parseResult);
        }
        
        LocalDateTime recordTime = LocalDateTime.now();
        try {
            if (parseResult.getRecordTime() != null) {
                recordTime = LocalDateTime.parse(parseResult.getRecordTime(), formatter);
            }
        } catch (Exception e) {
            recordTime = LocalDateTime.now();
        }
        
        String tagsJson = convertTagsToJson(parseResult.getTags());
        
        // 使用第一个类型作为主类型
        String primaryType = parseResult.getRecordTypes() != null && !parseResult.getRecordTypes().isEmpty()
                ? parseResult.getRecordTypes().get(0)
                : "diary";
        
        LifeRecord record = new LifeRecord(
            text,
            primaryType,
            parseResult.getAmount(),
            tagsJson,
            parseResult.getEmotionScore(),
            recordTime
        );
        record.setUserId(userId);
        
        lifeRecordMapper.insert(record);
        
        checkEmotionAndCare(record);
        
        return record;
    }

    private void checkEmotionAndCare(LifeRecord record) {
        if (record.getEmotionScore() != null && record.getEmotionScore() < -5) {
            log.info("[情感关怀] 检测到用户情绪较低({})，记录ID: {}", record.getEmotionScore(), record.getId());
        }
    }

    public String getCareMessage() {
        return getRandomCareMessage();
    }

    private String getRandomCareMessage() {
        String[] messages = {
            "我注意到你最近可能有些低落。记住，无论发生什么，我都在这里陪着你。",
            "生活中总会有起起落落，这很正常。希望你能好好照顾自己。",
            "如果有什么想说的，随时告诉我，我愿意倾听。",
            "别忘了，你是最棒的！相信一切都会好起来的。",
            "今天辛苦了，记得给自己一个放松的时刻。"
        };
        return messages[(int) (Math.random() * messages.length)];
    }

    private String convertTagsToJson(List<String> tags) {
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (Exception e) {
            return "[]";
        }
    }

    public List<LifeRecord> getAllRecords() {
        return lifeRecordMapper.findAll();
    }

    public List<LifeRecord> getRecordsByUserId(Long userId) {
        return lifeRecordMapper.findByUserId(userId);
    }

    public void deleteRecord(Long id) {
        lifeRecordMapper.deleteById(id, 1L); // 默认用户ID为1
    }

    public void deleteRecord(Long id, Long userId) {
        lifeRecordMapper.deleteById(id, userId);
    }

    public void updateRecord(Long id, LifeRecord record) {
        record.setId(id);
        lifeRecordMapper.update(record);
    }
}
