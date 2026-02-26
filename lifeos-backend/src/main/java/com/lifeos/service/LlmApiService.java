package com.lifeos.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lifeos.dto.AiParseResult;
import com.lifeos.entity.LlmConfig;
import com.lifeos.mapper.LlmConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LlmApiService {

    @Autowired
    private LlmConfigMapper llmConfigMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public AiParseResult parseWithLlm(String text) {
        LlmConfig config = llmConfigMapper.findLatest();
        
        if (config == null || config.getApiKey() == null || config.getApiKey().trim().isEmpty()) {
            throw new RuntimeException("LLM not configured. Please configure in settings.");
        }

        try {
            String provider = config.getProvider() != null ? config.getProvider().toLowerCase() : "openai";
            
            return switch (provider) {
                case "openai", "azure" -> callOpenAiApi(config, text);
                case "claude", "anthropic" -> callClaudeApi(config, text);
                case "gemini", "google" -> callGeminiApi(config, text);
                default -> callOpenAiApi(config, text);
            };
        } catch (Exception e) {
            System.err.println("LLM API call failed: " + e.getMessage());
            throw new RuntimeException("LLM API call failed: " + e.getMessage());
        }
    }

    private AiParseResult callOpenAiApi(LlmConfig config, String text) {
        String apiUrl = config.getApiUrl() != null && !config.getApiUrl().isEmpty() 
            ? config.getApiUrl() 
            : "https://api.openai.com/v1/chat/completions";
        
        // 清理 URL（去除末尾的逗号等非法字符）
        apiUrl = apiUrl.trim();
        while (apiUrl.endsWith(",") || apiUrl.endsWith(" ") || apiUrl.endsWith("\t")) {
            apiUrl = apiUrl.substring(0, apiUrl.length() - 1).trim();
        }
        
        // 如果 URL 不包含 /chat/completions，添加它
        if (!apiUrl.endsWith("/chat/completions")) {
            if (apiUrl.endsWith("/v1")) {
                apiUrl = apiUrl + "/chat/completions";
            } else if (apiUrl.endsWith("/")) {
                apiUrl = apiUrl + "chat/completions";
            } else {
                apiUrl = apiUrl + "/chat/completions";
            }
        }
        
        System.out.println("Calling LLM API at: " + apiUrl);
        
        String model = config.getModel() != null ? config.getModel() : "gpt-3.5-turbo";
        double temperature = config.getTemperature() != null ? config.getTemperature() : 0.7;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        requestBody.put("temperature", temperature);
        
        ArrayNode messages = requestBody.putArray("messages");
        ObjectNode systemMessage = messages.addObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", getSystemPrompt());
        
        ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");
        userMessage.put("content", text);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            apiUrl, 
            HttpMethod.POST, 
            entity, 
            String.class
        );

        try {
            return parseOpenAiResponse(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenAI response: " + e.getMessage(), e);
        }
    }

    private AiParseResult callClaudeApi(LlmConfig config, String text) {
        String apiUrl = config.getApiUrl() != null && !config.getApiUrl().isEmpty()
            ? config.getApiUrl()
            : "https://api.anthropic.com/v1/messages";

        String model = config.getModel() != null ? config.getModel() : "claude-3-sonnet-20240229";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", config.getApiKey());
        headers.set("anthropic-version", "2023-06-01");

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        requestBody.put("max_tokens", 1024);
        
        ArrayNode messages = requestBody.putArray("messages");
        ObjectNode message = messages.addObject();
        message.put("role", "user");
        message.put("content", getSystemPrompt() + "\n\nUser input: " + text);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            apiUrl,
            HttpMethod.POST,
            entity,
            String.class
        );

        try {
            return parseClaudeResponse(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Claude response: " + e.getMessage(), e);
        }
    }

    private AiParseResult callGeminiApi(LlmConfig config, String text) {
        String model = config.getModel() != null ? config.getModel() : "gemini-pro";
        String apiUrl = config.getApiUrl() != null && !config.getApiUrl().isEmpty()
            ? config.getApiUrl()
            : "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", config.getApiKey());

        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode contents = requestBody.putArray("contents");
        ObjectNode content = contents.addObject();
        ObjectNode parts = content.putObject("parts");
        parts.put("text", getSystemPrompt() + "\n\nUser input: " + text);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            apiUrl,
            HttpMethod.POST,
            entity,
            String.class
        );

        try {
            return parseGeminiResponse(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response: " + e.getMessage(), e);
        }
    }

    private String getSystemPrompt() {
        return """ 
            你是一个生活记录解析助手。请解析用户的输入，提取以下信息并以JSON格式返回：
            {
                "recordTypes": ["expense", "mood", "event", "diary"]（可以有多个类型）,
                "amount": 数字（消费金额，如果不是消费则为null）,
                "tags": ["标签1", "标签2"]（相关标签，使用中文）,
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
            - "开心地去看了电影，花了50元" -> recordTypes: ["expense", "mood", "event"]
            
            标签指南（优先从以下标签中选择，也可以添加新标签）：
            - 餐饮类：餐饮、早餐、午餐、晚餐、零食、饮料、咖啡、奶茶
            - 交通类：交通、打车、地铁、公交、火车、飞机、骑行
            - 购物类：购物、衣服、鞋子、电子产品、超市、网购
            - 娱乐类：娱乐、电影、游戏、KTV、酒吧、运动
            - 生活类：生活、房租、水电、医疗、保险、宠物
            - 工作类：工作、会议、加班、出差、项目
            
            情绪分数指南：
            - +8到+10: 极度正面（狂喜、激动）
            - +4到+7: 正面（开心、满意、放松、舒服）
            - +1到+3: 轻微正面（还行、不错）
            - 0: 中性
            - -1到-3: 轻微负面（有点烦、无聊、累）
            - -4到-7: 负面（难过、失望、焦虑、不舒服）
            - -8到-10: 极度负面（沮丧、愤怒、崩溃）
            
            请只返回JSON对象，不要有其他文字。标签必须使用中文。
            """;
    }

    private AiParseResult parseOpenAiResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        String content = root.path("choices").get(0).path("message").path("content").asText();
        return parseLlmJsonResponse(content);
    }

    private AiParseResult parseClaudeResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        String content = root.path("content").get(0).path("text").asText();
        return parseLlmJsonResponse(content);
    }

    private AiParseResult parseGeminiResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        String content = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        return parseLlmJsonResponse(content);
    }

    private AiParseResult parseLlmJsonResponse(String content) throws Exception {
        // Extract JSON from markdown code blocks if present
        Pattern jsonPattern = Pattern.compile("```json\\s*(\\{.*?\\})\\s*```", Pattern.DOTALL);
        Matcher matcher = jsonPattern.matcher(content);
        if (matcher.find()) {
            content = matcher.group(1);
        }

        JsonNode json = objectMapper.readTree(content);
        
        AiParseResult result = new AiParseResult();
        
        // Handle recordTypes array or single recordType
        if (json.has("recordTypes") && json.path("recordTypes").isArray()) {
            List<String> recordTypes = objectMapper.convertValue(json.path("recordTypes"), List.class);
            result.setRecordTypes(recordTypes);
        } else if (json.has("recordType")) {
            String recordType = json.path("recordType").asText("diary");
            result.setRecordTypes(Arrays.asList(recordType));
        } else {
            result.setRecordTypes(Arrays.asList("diary"));
        }
        
        if (json.has("amount") && !json.path("amount").isNull()) {
            JsonNode amountNode = json.path("amount");
            if (amountNode.isNumber()) {
                result.setAmount(BigDecimal.valueOf(amountNode.asDouble()));
            } else {
                String amountStr = amountNode.asText();
                if (!amountStr.isEmpty()) {
                    result.setAmount(new BigDecimal(amountStr));
                }
            }
        }
        
        if (json.has("tags") && json.path("tags").isArray()) {
            List<String> tags = objectMapper.convertValue(json.path("tags"), List.class);
            result.setTags(tags);
        } else {
            result.setTags(Arrays.asList("未分类"));
        }
        
        result.setEmotionScore(json.path("emotionScore").asInt(0));
        
        // 处理日期：如果 LLM 返回的日期不是今天，使用当前日期
        String recordTime = json.path("recordTime").asText("");
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (recordTime.isEmpty() || isDateNotToday(recordTime)) {
            recordTime = currentTime;
        }
        result.setRecordTime(recordTime);
        
        result.setSummary(json.path("summary").asText(""));
        
        return result;
    }
    
    private boolean isDateNotToday(String recordTime) {
        try {
            LocalDateTime recordDate = LocalDateTime.parse(recordTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime now = LocalDateTime.now();
            // 如果日期早于今天，认为是错误的日期
            return recordDate.toLocalDate().isBefore(now.toLocalDate());
        } catch (Exception e) {
            // 解析失败，使用当前时间
            return true;
        }
    }
}
