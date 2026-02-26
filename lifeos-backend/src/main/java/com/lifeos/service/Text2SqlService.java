package com.lifeos.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lifeos.entity.LifeRecord;
import com.lifeos.entity.LlmConfig;
import com.lifeos.mapper.LifeRecordMapper;
import com.lifeos.mapper.LlmConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Text2SqlService {

    @Autowired
    private LlmConfigMapper llmConfigMapper;

    @Autowired
    private LifeRecordMapper lifeRecordMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final RestTemplate restTemplate = new RestTemplate();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String executeNaturalLanguageQuery(String query, Long userId) {
        LlmConfig config = llmConfigMapper.findLatest();
        
        if (config == null || config.getApiKey() == null || config.getApiKey().trim().isEmpty()) {
            // Fall back to keyword-based query if no LLM config
            return executeKeywordQuery(query, userId);
        }

        try {
            // Generate SQL using LLM
            String sql = generateSqlWithLlm(config, query, userId);
            
            // Execute the generated SQL using JdbcTemplate for generic queries
            List<Map<String, Object>> resultMaps = executeGenericQuery(sql);
            
            // Format results using LLM
            return formatResultsWithLlm(config, query, sql, resultMaps);
            
        } catch (Exception e) {
            System.err.println("Text2SQL failed: " + e.getMessage());
            e.printStackTrace();
            // Fall back to keyword-based query
            return executeKeywordQuery(query, userId);
        }
    }

    /**
     * 使用 JdbcTemplate 执行通用 SQL 查询，支持聚合查询
     */
    private List<Map<String, Object>> executeGenericQuery(String sql) {
        return jdbcTemplate.queryForList(sql);
    }

    private List<Map<String, Object>> convertToMapList(List<LifeRecord> records) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (LifeRecord record : records) {
            // 跳过 null 记录
            if (record == null) continue;
            
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("original_text", record.getOriginalText());
            map.put("record_type", record.getRecordType());
            map.put("amount", record.getAmount());
            map.put("tags", record.getTags());
            map.put("emotion_score", record.getEmotionScore());
            // 将 LocalDateTime 格式化为字符串，避免 Jackson 序列化错误
            map.put("record_time", record.getRecordTime() != null ? record.getRecordTime().format(formatter) : null);
            map.put("created_at", record.getCreatedAt() != null ? record.getCreatedAt().format(formatter) : null);
            map.put("updated_at", record.getUpdatedAt() != null ? record.getUpdatedAt().format(formatter) : null);
            result.add(map);
        }
        return result;
    }

    private String generateSqlWithLlm(LlmConfig config, String query, Long userId) throws Exception {
        String apiUrl = config.getApiUrl() != null && !config.getApiUrl().isEmpty()
            ? config.getApiUrl()
            : "https://api.openai.com/v1";
        
        // 确保 URL 以 /chat/completions 结尾
        if (!apiUrl.endsWith("/chat/completions")) {
            if (apiUrl.endsWith("/v1")) {
                apiUrl = apiUrl + "/chat/completions";
            } else if (apiUrl.endsWith("/")) {
                apiUrl = apiUrl + "chat/completions";
            } else {
                apiUrl = apiUrl + "/chat/completions";
            }
        }
        
        String model = config.getModel() != null ? config.getModel() : "gpt-3.5-turbo";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());

        String schema = """
            Table: life_records
            Columns:
            - id: BIGINT, primary key, auto increment
            - user_id: BIGINT, the user's ID, MUST be filtered in WHERE clause
            - content: TEXT, the original user input text
            - record_type: VARCHAR(50), values: 'expense', 'mood', 'event', 'diary'
            - amount: DECIMAL(10,2), monetary amount for expense records
            - tags: TEXT, JSON array of tags
            - emotion_score: INT, -10 to 10, negative for negative emotions
            - record_time: DATETIME, when the event occurred
            - created_at: DATETIME, when record was created
            - updated_at: DATETIME, when record was last updated
            """;

        String prompt = String.format("""
            You are a SQL expert. Convert the following natural language query into a MySQL SQL query.
            
            Database Schema:
            %s
            
            Current User ID: %d
            
            User Query: "%s"
            
            Requirements:
            1. Generate ONLY the SQL query, no explanation
            2. Use standard MySQL syntax
            3. CRITICAL: MUST include "user_id = %d" in the WHERE clause to filter by current user
            4. Use appropriate WHERE clauses for time ranges (today, this week, this month, etc.)
            5. For date comparisons, use: DATE(record_time) = CURDATE() for today, 
               record_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) for last 7 days,
               record_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) for last 30 days
            6. Always include id in SELECT if returning records
            7. Use proper aggregation functions (SUM, COUNT, AVG) when needed
            8. Handle Chinese text properly
            9. Return only valid SQL that can be executed directly
            
            SQL Query:
            """, schema, userId, query, userId);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.1); // Low temperature for consistent SQL
        
        ArrayNode messages = requestBody.putArray("messages");
        ObjectNode systemMessage = messages.addObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a SQL expert. Generate only valid MySQL queries.");
        
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
        String sql = root.path("choices").get(0).path("message").path("content").asText().trim();
        
        // Clean up SQL (remove markdown code blocks if present)
        sql = sql.replaceAll("```sql\\s*", "").replaceAll("```\\s*", "").trim();
        
        System.out.println("Generated SQL: " + sql);
        return sql;
    }

    private String formatResultsWithLlm(LlmConfig config, String originalQuery, String sql, List<Map<String, Object>> results) throws Exception {
        if (results.isEmpty()) {
            return "没有找到相关记录。";
        }

        String apiUrl = config.getApiUrl() != null && !config.getApiUrl().isEmpty()
            ? config.getApiUrl()
            : "https://api.openai.com/v1";
        
        // 确保 URL 以 /chat/completions 结尾
        if (!apiUrl.endsWith("/chat/completions")) {
            if (apiUrl.endsWith("/v1")) {
                apiUrl = apiUrl + "/chat/completions";
            } else if (apiUrl.endsWith("/")) {
                apiUrl = apiUrl + "chat/completions";
            } else {
                apiUrl = apiUrl + "/chat/completions";
            }
        }
        
        String model = config.getModel() != null ? config.getModel() : "gpt-3.5-turbo";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());

        String resultsJson = objectMapper.writeValueAsString(results);
        
        String prompt = String.format("""
            User asked: "%s"
            
            SQL executed: %s
            
            Results (%d rows): %s
            
            Please provide a natural, friendly response in Chinese that answers the user's question based on these results.
            
            Formatting requirements:
            1. Use Markdown syntax for better readability
            2. If showing monetary amounts, format them with ¥ symbol (e.g., ¥10.00)
            3. If showing dates, use the EXACT date values from the results above, do NOT change or hallucinate dates
            4. Format dates as: YYYY年MM月DD日 (e.g., 2026年02月25日) using the exact year, month, day from the data
            5. Use bullet points (• or -) for lists
            6. Use **bold** for important numbers or values
            7. Keep the response concise but informative
            8. CRITICAL: Do not make up, guess, or modify dates - use only the exact dates provided in the data
            
            Response (in Markdown):
            """, originalQuery, sql, results.size(), resultsJson);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.7);
        
        ArrayNode messages = requestBody.putArray("messages");
        ObjectNode systemMessage = messages.addObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a helpful assistant that presents database query results in a natural, friendly way.");
        
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

    private String executeKeywordQuery(String query, Long userId) {
        // Fall back to existing QueryService logic
        String lowerQuery = query.toLowerCase();
        
        if (lowerQuery.contains("花") || lowerQuery.contains("消费") || lowerQuery.contains("支出") || lowerQuery.contains("多少钱")) {
            return handleExpenseQuery(lowerQuery, userId);
        }
        
        if (lowerQuery.contains("多少条") || lowerQuery.contains("几条") || lowerQuery.contains("数量")) {
            return handleCountQuery(lowerQuery, userId);
        }
        
        if (lowerQuery.contains("情绪") || lowerQuery.contains("心情") || lowerQuery.contains("开心") || lowerQuery.contains("难过")) {
            return handleEmotionQuery(lowerQuery, userId);
        }
        
        return "I can help you query:\n" +
            "• Expense stats: \"How much did I spend this week?\"\n" +
            "• Record count: \"How many records this month?\"\n" +
            "• Emotion analysis: \"How have I been feeling lately?\"";
    }

    private String handleExpenseQuery(String lowerQuery, Long userId) {
        List<LifeRecord> records;
        String timeDesc;
        
        LocalDateTime now = LocalDateTime.now();
        
        if (lowerQuery.contains("今天") || lowerQuery.contains("今日")) {
            records = lifeRecordMapper.findByUserIdAndTimeRange(userId,
                now.toLocalDate().atStartOfDay(),
                now
            );
            timeDesc = "今天";
        } else if (lowerQuery.contains("本周") || lowerQuery.contains("这周")) {
            records = lifeRecordMapper.findByUserIdAndTimeRange(userId,
                now.minusDays(7),
                now
            );
            timeDesc = "本周";
        } else if (lowerQuery.contains("本月") || lowerQuery.contains("这个月")) {
            records = lifeRecordMapper.findByUserIdAndTimeRange(userId,
                now.withDayOfMonth(1).toLocalDate().atStartOfDay(),
                now
            );
            timeDesc = "本月";
        } else {
            records = lifeRecordMapper.findByUserId(userId);
            timeDesc = "总计";
        }
        
        BigDecimal total = records.stream()
            .filter(r -> r.getRecordType() != null && r.getRecordType().contains("expense") && r.getAmount() != null)
            .map(LifeRecord::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long count = records.stream()
            .filter(r -> r.getRecordType() != null && r.getRecordType().contains("expense"))
            .count();
        
        return String.format("%s消费统计：\n💰 总支出：¥%.2f\n📝 消费笔数：%d笔", 
            timeDesc, total, count);
    }

    private String handleCountQuery(String lowerQuery, Long userId) {
        List<LifeRecord> records = lifeRecordMapper.findByUserId(userId);
        return String.format("共找到 %d 条记录", records.size());
    }

    private String handleEmotionQuery(String lowerQuery, Long userId) {
        return "请配置 LLM 以获取更详细的情绪分析。";
    }
}
