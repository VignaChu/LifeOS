package com.lifeos.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lifeos.dto.ApiResponse;
import com.lifeos.entity.LifeRecord;
import com.lifeos.mapper.LifeRecordMapper;

@Service
public class QueryService {

    @Autowired
    private LifeRecordMapper lifeRecordMapper;

    @Autowired
    private Text2SqlService text2SqlService;

    public String executeQuery(String query, Long userId) throws Exception {
        // First try Text2SQL with LLM
        try {
            String result = text2SqlService.executeNaturalLanguageQuery(query, userId);
            if (result != null && !result.isEmpty()) {
                return result;
            }
        } catch (Exception e) {
            System.out.println("Text2SQL failed, falling back to keyword query: " + e.getMessage());
        }

        // Fall back to keyword-based query
        String lowerQuery = query.toLowerCase();

        // Expense query
        if (lowerQuery.contains("花") || lowerQuery.contains("消费") || lowerQuery.contains("支出") || lowerQuery.contains("多少钱")) {
            ApiResponse<String> response = handleExpenseQuery(lowerQuery, query, userId);
            if (response.isSuccess()) {
                return response.getData();
            } else {
                throw new RuntimeException(response.getMessage());
            }
        }

        // Count query
        if (lowerQuery.contains("多少条") || lowerQuery.contains("几条") || lowerQuery.contains("数量")) {
            ApiResponse<String> response = handleCountQuery(lowerQuery, userId);
            if (response.isSuccess()) {
                return response.getData();
            } else {
                throw new RuntimeException(response.getMessage());
            }
        }

        // Emotion query
        if (lowerQuery.contains("情绪") || lowerQuery.contains("心情") || lowerQuery.contains("开心") || lowerQuery.contains("难过")) {
            ApiResponse<String> response = handleEmotionQuery(lowerQuery, userId);
            if (response.isSuccess()) {
                return response.getData();
            } else {
                throw new RuntimeException(response.getMessage());
            }
        }

        // Default help message
        return "I can help you query:\n" +
            "• Expense stats: \"How much did I spend this week?\"\n" +
            "• Record count: \"How many records this month?\"\n" +
            "• Emotion analysis: \"How have I been feeling lately?\"\n" +
            "• Tag analysis: \"What tags do I use most?\"\n" +
            "• Time-based queries: \"What did I do yesterday?\"";
    }

    private ApiResponse<String> handleExpenseQuery(String lowerQuery, String originalQuery, Long userId) {
        List<LifeRecord> records;
        String timeDesc;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime;

        if (lowerQuery.contains("今天") || lowerQuery.contains("今日")) {
            startTime = now.truncatedTo(ChronoUnit.DAYS);
            records = lifeRecordMapper.findByUserIdAndTimeRange(userId, startTime, now);
            timeDesc = "今天";
        } else if (lowerQuery.contains("昨天")) {
            startTime = now.minusDays(1).truncatedTo(ChronoUnit.DAYS);
            LocalDateTime endTime = now.truncatedTo(ChronoUnit.DAYS);
            records = lifeRecordMapper.findByUserIdAndTimeRange(userId, startTime, endTime);
            timeDesc = "昨天";
        } else if (lowerQuery.contains("本周") || lowerQuery.contains("这周") || lowerQuery.contains("星期")) {
            startTime = now.minusDays(7);
            records = lifeRecordMapper.findByUserIdAndTimeRange(userId, startTime, now);
            timeDesc = "本周";
        } else if (lowerQuery.contains("本月") || lowerQuery.contains("这个月") || lowerQuery.contains("这月")) {
            startTime = now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
            records = lifeRecordMapper.findByUserIdAndTimeRange(userId, startTime, now);
            timeDesc = "本月";
        } else if (lowerQuery.contains("今年") || lowerQuery.contains("这一年")) {
            startTime = now.withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);
            records = lifeRecordMapper.findByUserIdAndTimeRange(userId, startTime, now);
            timeDesc = "今年";
        } else {
            records = lifeRecordMapper.findByUserId(userId);
            timeDesc = "总计";
        }

        BigDecimal totalExpense = records.stream()
            .filter(r -> r.getRecordType() != null && r.getRecordType().contains("expense") && r.getAmount() != null)
            .map(LifeRecord::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder categoryBreakdown = new StringBuilder();
        if (lowerQuery.contains("吃饭") || lowerQuery.contains("餐饮") || lowerQuery.contains("吃")) {
            BigDecimal foodExpense = records.stream()
                .filter(r -> r.getRecordType() != null && r.getRecordType().contains("expense") && r.getAmount() != null)
                .filter(r -> r.getTags() != null && r.getTags().toLowerCase().contains("food"))
                .map(LifeRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            categoryBreakdown.append(String.format("，其中餐饮消费 ¥%.2f", foodExpense));
        }

        int expenseCount = (int) records.stream()
            .filter(r -> r.getRecordType() != null && r.getRecordType().contains("expense"))
            .count();

        String response = String.format("%s消费统计：\n" +
            "💰 总支出：¥%.2f\n" +
            "📝 消费笔数：%d笔%s",
            timeDesc, totalExpense, expenseCount, categoryBreakdown.toString());

        return ApiResponse.success(response);
    }

    private ApiResponse<String> handleCountQuery(String lowerQuery, Long userId) {
        List<LifeRecord> records;
        String timeDesc;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime;

        if (lowerQuery.contains("今天") || lowerQuery.contains("今日")) {
            startTime = now.truncatedTo(ChronoUnit.DAYS);
            records = lifeRecordMapper.findByUserIdAndTimeRange(userId, startTime, now);
            timeDesc = "今天";
        } else if (lowerQuery.contains("本周") || lowerQuery.contains("这周")) {
            startTime = now.minusDays(7);
            records = lifeRecordMapper.findByUserIdAndTimeRange(userId, startTime, now);
            timeDesc = "本周";
        } else if (lowerQuery.contains("本月") || lowerQuery.contains("这个月")) {
            startTime = now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
            records = lifeRecordMapper.findByUserIdAndTimeRange(userId, startTime, now);
            timeDesc = "本月";
        } else {
            records = lifeRecordMapper.findByUserId(userId);
            timeDesc = "总共";
        }

        long totalCount = records.size();
        long expenseCount = records.stream().filter(r -> r.getRecordType() != null && r.getRecordType().contains("expense")).count();
        long diaryCount = records.stream().filter(r -> r.getRecordType() != null && r.getRecordType().contains("diary")).count();
        long eventCount = records.stream().filter(r -> r.getRecordType() != null && r.getRecordType().contains("event")).count();
        long moodCount = records.stream().filter(r -> r.getRecordType() != null && r.getRecordType().contains("mood")).count();

        String response = String.format("%s记录了 %d 条生活轨迹：\n" +
            "💰 消费记录：%d条\n" +
            "📔 日记记录：%d条\n" +
            "📅 事件记录：%d条\n" +
            "😊 情绪记录：%d条",
            timeDesc, totalCount, expenseCount, diaryCount, eventCount, moodCount);

        return ApiResponse.success(response);
    }

    private ApiResponse<String> handleEmotionQuery(String lowerQuery, Long userId) {
        List<LifeRecord> records;
        String timeDesc;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime;

        if (lowerQuery.contains("今天") || lowerQuery.contains("今日")) {
            startTime = now.truncatedTo(ChronoUnit.DAYS);
            records = lifeRecordMapper.findByUserIdAndTimeRange(userId, startTime, now);
            timeDesc = "今天";
        } else if (lowerQuery.contains("本周") || lowerQuery.contains("这周") || lowerQuery.contains("最近")) {
            startTime = now.minusDays(7);
            records = lifeRecordMapper.findByUserIdAndTimeRange(userId, startTime, now);
            timeDesc = "最近一周";
        } else if (lowerQuery.contains("本月") || lowerQuery.contains("这个月")) {
            startTime = now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
            records = lifeRecordMapper.findByUserIdAndTimeRange(userId, startTime, now);
            timeDesc = "本月";
        } else {
            records = lifeRecordMapper.findByUserId(userId);
            timeDesc = "总体";
        }

        List<LifeRecord> moodRecords = records.stream()
            .filter(r -> r.getEmotionScore() != null)
            .toList();

        if (moodRecords.isEmpty()) {
            return ApiResponse.success(timeDesc + "还没有情绪记录哦，试着记录一下你的心情吧！");
        }

        double avgEmotion = moodRecords.stream()
            .mapToInt(LifeRecord::getEmotionScore)
            .average()
            .orElse(0);

        String emotionDesc;
        if (avgEmotion >= 7) {
            emotionDesc = "非常积极 😄";
        } else if (avgEmotion >= 3) {
            emotionDesc = "比较开心 🙂";
        } else if (avgEmotion >= 0) {
            emotionDesc = "情绪平稳 😐";
        } else if (avgEmotion >= -3) {
            emotionDesc = "略显低落 😕";
        } else if (avgEmotion >= -7) {
            emotionDesc = "比较消极 😔";
        } else {
            emotionDesc = "情绪低落 😢";
        }

        String response = String.format("%s情绪分析：\n" +
            "📊 平均情绪分数：%.1f\n" +
            "😊 情绪状态：%s\n" +
            "📝 情绪记录数：%d条",
            timeDesc, avgEmotion, emotionDesc, moodRecords.size());

        return ApiResponse.success(response);
    }
}
