package com.lifeos.mapper;

import com.lifeos.dto.LifeRecordDTO;
import com.lifeos.entity.LifeRecord;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface LifeRecordMapper {

    @Insert("INSERT INTO life_records (user_id, content, record_type, amount, tags, emotion_score, record_time, created_at, updated_at) " +
            "VALUES (#{userId}, #{content}, #{recordType}, #{amount}, #{tags}, #{emotionScore}, #{recordTime}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(LifeRecord record);

    @Select("SELECT * FROM life_records ORDER BY record_time DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "recordType", column = "record_type"),
        @Result(property = "amount", column = "amount"),
        @Result(property = "tags", column = "tags"),
        @Result(property = "emotionScore", column = "emotion_score"),
        @Result(property = "recordTime", column = "record_time"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<LifeRecord> findAll();

    @Select("SELECT * FROM life_records WHERE user_id = #{userId} ORDER BY record_time DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "recordType", column = "record_type"),
        @Result(property = "amount", column = "amount"),
        @Result(property = "tags", column = "tags"),
        @Result(property = "emotionScore", column = "emotion_score"),
        @Result(property = "recordTime", column = "record_time"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<LifeRecord> findByUserId(Long userId);

    @Select("SELECT * FROM life_records WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "recordType", column = "record_type"),
        @Result(property = "amount", column = "amount"),
        @Result(property = "tags", column = "tags"),
        @Result(property = "emotionScore", column = "emotion_score"),
        @Result(property = "recordTime", column = "record_time"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    LifeRecord findById(Long id);

    @Update("UPDATE life_records SET " +
            "content = #{content}, " +
            "record_type = #{recordType}, " +
            "amount = #{amount}, " +
            "tags = #{tags}, " +
            "emotion_score = #{emotionScore}, " +
            "record_time = #{recordTime}, " +
            "updated_at = NOW() " +
            "WHERE id = #{id} AND user_id = #{userId}")
    void update(LifeRecord record);

    @Delete("DELETE FROM life_records WHERE id = #{id} AND user_id = #{userId}")
    void deleteById(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM life_records")
    long count();

    @Select("SELECT COUNT(*) FROM life_records WHERE user_id = #{userId}")
    long countByUserId(Long userId);

    @Select("SELECT COUNT(*) FROM life_records WHERE record_type = #{type}")
    long countByType(String type);

    @Select("SELECT COUNT(*) FROM life_records WHERE user_id = #{userId} AND record_type = #{type}")
    long countByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);

    @Select("SELECT * FROM life_records WHERE record_time >= #{startTime} AND record_time <= #{endTime} ORDER BY record_time DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "recordType", column = "record_type"),
        @Result(property = "amount", column = "amount"),
        @Result(property = "tags", column = "tags"),
        @Result(property = "emotionScore", column = "emotion_score"),
        @Result(property = "recordTime", column = "record_time"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<LifeRecord> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    @Select("SELECT * FROM life_records WHERE user_id = #{userId} AND record_time >= #{startTime} AND record_time <= #{endTime} ORDER BY record_time DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "recordType", column = "record_type"),
        @Result(property = "amount", column = "amount"),
        @Result(property = "tags", column = "tags"),
        @Result(property = "emotionScore", column = "emotion_score"),
        @Result(property = "recordTime", column = "record_time"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<LifeRecord> findByUserIdAndTimeRange(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("SELECT * FROM life_records WHERE tags LIKE CONCAT('%', #{tag}, '%') ORDER BY record_time DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "recordType", column = "record_type"),
        @Result(property = "amount", column = "amount"),
        @Result(property = "tags", column = "tags"),
        @Result(property = "emotionScore", column = "emotion_score"),
        @Result(property = "recordTime", column = "record_time"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<LifeRecord> findByTag(String tag);

    // 动态查询方法 - 后台使用，查询全部
    @Select("${sql}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "recordType", column = "record_type"),
        @Result(property = "amount", column = "amount"),
        @Result(property = "tags", column = "tags"),
        @Result(property = "emotionScore", column = "emotion_score"),
        @Result(property = "recordTime", column = "record_time"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<LifeRecord> executeDynamicQuery(@Param("sql") String sql);

    @Select("SELECT * FROM life_records WHERE record_time >= #{startTime} AND record_time <= #{endTime} ORDER BY record_time DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "recordType", column = "record_type"),
        @Result(property = "amount", column = "amount"),
        @Result(property = "tags", column = "tags"),
        @Result(property = "emotionScore", column = "emotion_score"),
        @Result(property = "recordTime", column = "record_time"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<LifeRecord> findByTimeRangeDynamic(LocalDateTime startTime, LocalDateTime endTime);

    @Select("SELECT * FROM life_records ORDER BY record_time DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "recordType", column = "record_type"),
        @Result(property = "amount", column = "amount"),
        @Result(property = "tags", column = "tags"),
        @Result(property = "emotionScore", column = "emotion_score"),
        @Result(property = "recordTime", column = "record_time"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<LifeRecord> findAllDynamic();
}
