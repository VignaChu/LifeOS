package com.lifeos.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lifeos.entity.LifeRecord;
import com.lifeos.entity.LlmConfig;
import com.lifeos.mapper.LifeRecordMapper;
import com.lifeos.mapper.LlmConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private LifeRecordMapper lifeRecordMapper;

    @Autowired
    private LlmConfigMapper llmConfigMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public String generateWeeklyReport(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);
        List<LifeRecord> records = lifeRecordMapper.findByUserIdAndTimeRange(userId, weekAgo, now);
        
        return generateReport(records, "本周", weekAgo, now);
    }

    public String generateMonthlyReport(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthAgo = now.minusDays(30);
        List<LifeRecord> records = lifeRecordMapper.findByUserIdAndTimeRange(userId, monthAgo, now);
        
        return generateReport(records, "本月", monthAgo, now);
    }

    private String generateReport(List<LifeRecord> records, String periodName, LocalDateTime start, LocalDateTime end) {
        if (records.isEmpty()) {
            return periodName + "还没有任何记录哦，快开始记录你的生活吧！";
        }

        LlmConfig config = llmConfigMapper.findLatest();
        
        if (config != null && config.getApiKey() != null && !config.getApiKey().trim().isEmpty()) {
            try {
                return generateAiReport(config, records, periodName);
            } catch (Exception e) {
                System.err.println("AI report generation failed: " + e.getMessage());
            }
        }

        return generateSimpleReport(records, periodName);
    }

    private String generateAiReport(LlmConfig config, List<LifeRecord> records, String periodName) throws Exception {
        String apiUrl = config.getApiUrl() != null && !config.getApiUrl().isEmpty()
            ? config.getApiUrl()
            : "https://api.openai.com/v1/chat/completions";
        
        String model = config.getModel() != null ? config.getModel() : "gpt-3.5-turbo";

        Map<String, Object> stats = calculateStats(records);
        String statsJson = objectMapper.writeValueAsString(stats);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());

        String prompt = String.format("""
            你是一个贴心的生活管家。请根据以下用户的生活记录数据，生成一份温馨、有洞察力的%s生活总结报告。
            
            数据统计：
            %s
            
            请用温暖友好的语气，包含以下内容：
            1. 整体概述 - 用一两句话概括这个月的整体感受
            2. 消费分析 - 总消费、是否有异常消费
            3. 情绪分析 - 情绪状态如何，有什么值得注意的
            4. 高光时刻 - 记录中值得回味的事情
            5. 小建议 - 基于数据分析给出1-2条生活建议
            
            用中文回复，语气亲切自然，像朋友聊天一样。
            """, periodName, statsJson);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.7);
        
        ArrayNode messages = requestBody.putArray("messages");
        ObjectNode systemMessage = messages.addObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a warm and thoughtful life coach. Generate friendly and insightful life reports.");
        
        ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            apiUrl,
            HttpMethod.POST,
            entity,
            String.class
        );

        JsonNode root = objectMapper.readTree(response.getBody());
        return root.path("choices").get(0).path("message").path("content").asText().trim();
    }

    private String generateSimpleReport(List<LifeRecord> records, String periodName) {
        Map<String, Object> stats = calculateStats(records);
        
        BigDecimal totalExpense = (BigDecimal) stats.getOrDefault("totalExpense", BigDecimal.ZERO);
        double avgEmotion = (double) stats.getOrDefault("avgEmotion", 0.0);
        int recordCount = (int) stats.getOrDefault("recordCount", 0);
        Map<String, Long> typeBreakdown = (Map<String, Long>) stats.get("typeBreakdown");
        
        StringBuilder report = new StringBuilder();
        report.append(periodName).append("生活报告\n\n");
        report.append("📊 总体概览\n");
        report.append("共记录 ").append(recordCount).append(" 条生活轨迹\n\n");
        
        report.append("💰 消费统计\n");
        report.append(String.format("总支出：¥%.2f\n", totalExpense));
        
        if (typeBreakdown.containsKey("expense")) {
            report.append("消费记录：").append(typeBreakdown.get("expense")).append("条\n");
        }
        report.append("\n");
        
        report.append("😊 情绪分析\n");
        if (avgEmotion > 0) {
            report.append(String.format("平均情绪：%.1f（正向）\n", avgEmotion));
        } else if (avgEmotion < 0) {
            report.append(String.format("平均情绪：%.1f（偏低）\n", avgEmotion));
        } else {
            report.append("情绪平稳\n");
        }
        
        return report.toString();
    }

    private Map<String, Object> calculateStats(List<LifeRecord> records) {
        Map<String, Object> stats = new HashMap<>();
        
        BigDecimal totalExpense = records.stream()
            .filter(r -> r.getRecordType() != null && r.getRecordType().contains("expense") && r.getAmount() != null)
            .map(LifeRecord::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalExpense", totalExpense);
        
        List<LifeRecord> moodRecords = records.stream()
            .filter(r -> r.getEmotionScore() != null)
            .toList();
        
        double avgEmotion = 0;
        if (!moodRecords.isEmpty()) {
            avgEmotion = moodRecords.stream()
                .mapToInt(LifeRecord::getEmotionScore)
                .average()
                .orElse(0);
        }
        stats.put("avgEmotion", avgEmotion);
        
        Map<String, Long> typeBreakdown = records.stream()
            .collect(Collectors.groupingBy(
                r -> r.getRecordType() != null ? r.getRecordType() : "unknown",
                Collectors.counting()
            ));
        stats.put("typeBreakdown", typeBreakdown);
        
        stats.put("recordCount", records.size());
        
        List<String> allTags = new ArrayList<>();
        for (LifeRecord r : records) {
            if (r.getTags() != null) {
                try {
                    if (r.getTags() instanceof String) {
                        List<String> parsed = objectMapper.readValue((String) r.getTags(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                        allTags.addAll(parsed);
                    }
                } catch (Exception e) {
                    // skip
                }
            }
        }
        stats.put("topTags", allTags.stream()
            .collect(Collectors.groupingBy(t -> t, Collectors.counting()))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList()));
        
        return stats;
    }
}
