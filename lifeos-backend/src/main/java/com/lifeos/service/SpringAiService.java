package com.lifeos.service;

import com.lifeos.dto.AiParseResult;
import com.lifeos.entity.LlmConfig;
import com.lifeos.mapper.LlmConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpringAiService {

    private final ChatClient chatClient;
    private final LlmConfigMapper llmConfigMapper;
    private final LlmRoutingService llmRoutingService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LlmApiService llmApiService;

    public AiParseResult parseText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return createDefaultResult("Empty input");
        }

        // 获取配置
        LlmConfig config = llmConfigMapper.findLatest();
        
        // 检查是否强制使用本地规则
        if (config != null && config.getUseLocalRules() != null && config.getUseLocalRules()) {
            log.info("Local rules enabled, skipping LLM API");
            return llmRoutingService.parseText(text);
        }

        // 尝试使用 LLM API
        try {
            if (config != null && config.getApiKey() != null && !config.getApiKey().trim().isEmpty() && !config.getApiKey().contains("dummy")) {
                log.info("Using LLM API for parsing");
                return llmApiService.parseWithLlm(text);
            }
        } catch (Exception e) {
            log.warn("LLM API parsing failed, falling back to local rules: {}", e.getMessage());
        }

        // 回退到本地规则
        return llmRoutingService.parseText(text);
    }

    private AiParseResult parseWithSpringAi(String text, LlmConfig config) {
        String promptText = """
            你是一个生活记录解析助手。请解析用户的输入，提取以下信息并以JSON格式返回：
            
            {
                "recordTypes": ["expense", "mood", "event", "diary"]（可以有多个类型）,
                "amount": 数字（消费金额，如果不是消费则为null）,
                "tags": ["标签1", "标签2"]（相关标签列表）,
                "emotionScore": -10到10之间的整数（情绪分数，负面为负，正面为正）,
                "recordTime": "yyyy-MM-dd HH:mm:ss"（记录时间，如果没有指定则使用当前时间）,
                "summary": "简短摘要"
            }
            
            类型定义：
            - expense: 消费、购买、花费金钱
            - mood: 情绪、心情、感受
            - event: 事件、活动、约会
            - diary: 一般日记记录
            
            重要：一条记录可以有多个类型。例如：
            - "花了30元买饭，但是好难吃" -> recordTypes: ["expense", "mood"]
            
            情绪分数指南：
            - +8到+10: 极度正面（狂喜、激动）
            - +4到+7: 正面（开心、满意）
            - +1到+3: 轻微正面（还行、不错）
            - 0: 中性
            - -1到-3: 轻微负面（有点烦、无聊）
            - -4到-7: 负面（难过、失望）
            - -8到-10: 极度负面（沮丧、愤怒）
            
            用户输入：{text}
            
            请只返回JSON对象，不要有其他文字。
            """;

        PromptTemplate promptTemplate = new PromptTemplate(promptText);
        Prompt prompt = promptTemplate.create(Map.of("text", text));

        String response = chatClient.prompt(prompt)
                .call()
                .content();

        return parseResponse(response, text);
    }

    private AiParseResult parseResponse(String response, String originalText) {
        try {
            // 提取 JSON
            String json = response;
            if (response.contains("```json")) {
                json = response.substring(response.indexOf("```json") + 7, response.indexOf("```", response.indexOf("```json") + 7)).trim();
            } else if (response.contains("```")) {
                json = response.substring(response.indexOf("```") + 3, response.indexOf("```", response.indexOf("```") + 3)).trim();
            }

            // 简单解析（实际项目中建议使用 Jackson）
            List<String> recordTypes = extractArray(json, "recordTypes");
            if (recordTypes.isEmpty()) {
                String recordType = extractString(json, "recordType");
                if (!recordType.isEmpty()) {
                    recordTypes = Arrays.asList(recordType);
                } else {
                    recordTypes = Arrays.asList("diary");
                }
            }

            BigDecimal amount = extractBigDecimal(json, "amount");
            List<String> tags = extractArray(json, "tags");
            Integer emotionScore = extractInt(json, "emotionScore", 0);
            String recordTime = extractString(json, "recordTime");
            if (recordTime.isEmpty()) {
                recordTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            }
            String summary = extractString(json, "summary");
            if (summary.isEmpty()) {
                summary = originalText.length() > 50 ? originalText.substring(0, 50) + "..." : originalText;
            }

            return AiParseResult.of(recordTypes, amount, tags, emotionScore, recordTime, summary);
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", e.getMessage());
            return createDefaultResult(originalText);
        }
    }

    private List<String> extractArray(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\\[(.*?)\\]";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher m = r.matcher(json);
        if (m.find()) {
            String content = m.group(1);
            return Arrays.stream(content.split(","))
                    .map(s -> s.trim().replace("\"", ""))
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
        return Arrays.asList();
    }

    private String extractString(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    private BigDecimal extractBigDecimal(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*(null|\\d+(?:\\.\\d+)?)";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(json);
        if (m.find()) {
            String value = m.group(1);
            if (!"null".equals(value)) {
                return new BigDecimal(value);
            }
        }
        return null;
    }

    private Integer extractInt(String json, String key, int defaultValue) {
        String pattern = "\"" + key + "\"\\s*:\\s*(-?\\d+)";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(json);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return defaultValue;
    }

    private AiParseResult createDefaultResult(String text) {
        return AiParseResult.of(
                Arrays.asList("diary"),
                null,
                Arrays.asList("未分类"),
                0,
                LocalDateTime.now().format(DATE_TIME_FORMATTER),
                text.length() > 50 ? text.substring(0, 50) + "..." : text
        );
    }
}
