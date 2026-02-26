package com.lifeos.service;

import com.lifeos.dto.AiParseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LlmRoutingService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private LlmApiService llmApiService;

    public AiParseResult parseText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return createDefaultResult("Empty input");
        }

        // Try to use LLM API first
        try {
            return llmApiService.parseWithLlm(text);
        } catch (Exception e) {
            System.out.println("LLM API failed, falling back to local parsing: " + e.getMessage());
            // Fall back to local rule-based parsing
            return parseWithLocalRules(text);
        }
    }

    private AiParseResult parseWithLocalRules(String text) {
        try {
            List<String> recordTypes = detectRecordTypes(text);
            BigDecimal amount = extractAmount(text);
            List<String> tags = extractTags(text, recordTypes);
            Integer emotionScore = analyzeEmotion(text);
            String recordTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            String summary = generateSummary(text, recordTypes, amount);
            
            return AiParseResult.of(recordTypes, amount, tags, emotionScore, recordTime, summary);
        } catch (Exception e) {
            System.err.println("Error parsing text: " + e.getMessage());
            return createDefaultResult(text);
        }
    }

    private AiParseResult createDefaultResult(String text) {
        return AiParseResult.of(
            Arrays.asList("diary"),
            null,
            Arrays.asList("Uncategorized"),
            0,
            LocalDateTime.now().format(DATE_TIME_FORMATTER),
            text.length() > 50 ? text.substring(0, 50) + "..." : text
        );
    }

    private List<String> detectRecordTypes(String text) {
        String lowerText = text.toLowerCase();
        List<String> types = new ArrayList<>();
        
        String[] expenseKeywords = {"spend", "buy", "cost", "pay", "yuan", "rmb", "dollar", "price", "fee", 
            "shopping", "eat", "taxi", "subway", "bus", "coffee", "milk tea", "delivery", "supermarket", "mall", "taobao", "jd", "花了", "消费", "买", "付款", "价格", "吃饭", "打车", "公交", "地铁"};
        
        String[] moodKeywords = {"happy", "sad", "upset", "excited", "depressed", "disappointed", "anxious", 
            "nervous", "relaxed", "comfortable", "uncomfortable", "angry", "satisfied", "disappointed", "happy", "joyful", "painful",
            "开心", "难过", "伤心", "高兴", "沮丧", "失望", "焦虑", "紧张", "放松", "舒服", "生气", "满意", "郁闷", "郁闷"};
        
        String[] eventKeywords = {"meeting", "date", "party", "activity", "exam", "interview", "travel", "business trip", 
            "move", "renovation", "wedding", "birthday", "holiday", "anniversary", "competition", "performance", "movie", "exhibition",
            "会议", "约会", "聚会", "活动", "考试", "面试", "旅行", "出差", "搬家", "装修", "婚礼", "生日", "节假日", "周年", "比赛"};
        
        if (countKeywords(lowerText, expenseKeywords) > 0 || 
            (extractAmount(text) != null && extractAmount(text).compareTo(BigDecimal.ZERO) > 0)) {
            types.add("expense");
        }
        
        if (countKeywords(lowerText, moodKeywords) > 0) {
            types.add("mood");
        }
        
        if (countKeywords(lowerText, eventKeywords) > 0) {
            types.add("event");
        }
        
        if (types.isEmpty()) {
            types.add("diary");
        }
        
        return types;
    }

    private int countKeywords(String text, String[] keywords) {
        int count = 0;
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                count++;
            }
        }
        return count;
    }

    private BigDecimal extractAmount(String text) {
        try {
            // Match amount patterns: number + yuan/block/dollar
            Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d{1,2})?)\\s*[yuan|rmb|dollar|block|$]");
            Matcher matcher = pattern.matcher(text);
            
            BigDecimal total = BigDecimal.ZERO;
            boolean found = false;
            
            while (matcher.find()) {
                try {
                    BigDecimal amount = new BigDecimal(matcher.group(1));
                    total = total.add(amount);
                    found = true;
                } catch (NumberFormatException e) {
                    // Ignore unparseable amounts
                }
            }
            
            return found ? total : null;
        } catch (Exception e) {
            System.err.println("Error extracting amount: " + e.getMessage());
            return null;
        }
    }

    private List<String> extractTags(String text, List<String> recordTypes) {
        List<String> tags = new ArrayList<>();
        String lowerText = text.toLowerCase();
        
        try {
            // Add base tag based on types
            for (String type : recordTypes) {
                switch (type) {
                    case "expense":
                        if (!tags.contains("Expense")) tags.add("Expense");
                        break;
                    case "mood":
                        if (!tags.contains("Mood")) tags.add("Mood");
                        break;
                    case "event":
                        if (!tags.contains("Event")) tags.add("Event");
                        break;
                    case "diary":
                        if (!tags.contains("Diary")) tags.add("Diary");
                        break;
                }
            }
            
            if (tags.isEmpty()) {
                tags.add("Diary");
            }
            
            // Extract specific tags
            String[][] tagKeywords = {
                {"Food", "eat", "restaurant", "food", "hotpot", "bbq", "sushi", "pizza", "burger", "noodles", "rice", "breakfast", "lunch", "dinner"},
                {"Transport", "taxi", "subway", "bus", "high-speed rail", "plane", "train", "drive", "bike", "walk"},
                {"Shopping", "clothes", "shoes", "bag", "supermarket", "mall", "taobao", "jd", "online shopping"},
                {"Entertainment", "movie", "game", "ktv", "bar", "cafe", "milk tea", "coffee", "ice cream"},
                {"Life", "rent", "utilities", "property", "phone", "internet", "medical", "medicine", "insurance"},
                {"Work", "meeting", "overtime", "business trip", "project", "client", "boss", "colleague"},
            };
            
            for (String[] keywordGroup : tagKeywords) {
                String tagName = keywordGroup[0];
                for (int i = 1; i < keywordGroup.length; i++) {
                    if (lowerText.contains(keywordGroup[i])) {
                        if (!tags.contains(tagName)) {
                            tags.add(tagName);
                        }
                        break;
                    }
                }
            }
            
            return tags;
        } catch (Exception e) {
            System.err.println("Error extracting tags: " + e.getMessage());
            return Arrays.asList("Uncategorized");
        }
    }

    private Integer analyzeEmotion(String text) {
        String lowerText = text.toLowerCase();
        int score = 0;
        
        // Positive emotion keywords
        String[] positiveKeywords = {"happy", "joyful", "excited", "satisfied", "comfortable", "relaxed", "lucky", "wonderful", "great", "awesome"};
        // Negative emotion keywords
        String[] negativeKeywords = {"sad", "upset", "depressed", "disappointed", "angry", "anxious", "nervous", "uncomfortable", "painful", "terrible"};
        
        for (String keyword : positiveKeywords) {
            if (lowerText.contains(keyword)) {
                score += 2;
            }
        }
        
        for (String keyword : negativeKeywords) {
            if (lowerText.contains(keyword)) {
                score -= 2;
            }
        }
        
        // Limit score to -10 to 10
        return Math.max(-10, Math.min(10, score));
    }

    private String generateSummary(String text, List<String> recordTypes, BigDecimal amount) {
        StringBuilder summary = new StringBuilder();
        
        if (recordTypes.contains("expense")) {
            summary.append("消费");
            if (amount != null) {
                summary.append(" ¥").append(amount);
            }
        }
        
        if (recordTypes.contains("mood")) {
            if (summary.length() > 0) summary.append(" | ");
            summary.append("情绪记录");
        }
        
        if (recordTypes.contains("event")) {
            if (summary.length() > 0) summary.append(" | ");
            summary.append("事件");
        }
        
        if (recordTypes.contains("diary") && recordTypes.size() == 1) {
            summary.append(text.length() > 40 ? text.substring(0, 40) + "..." : text);
        } else if (summary.length() == 0) {
            summary.append(text.length() > 40 ? text.substring(0, 40) + "..." : text);
        }
        
        return summary.toString();
    }
}
