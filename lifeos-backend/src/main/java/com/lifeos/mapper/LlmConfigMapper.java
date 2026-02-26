package com.lifeos.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.lifeos.entity.LlmConfig;

@Mapper
public interface LlmConfigMapper {

    @Select("SELECT * FROM llm_config ORDER BY id DESC LIMIT 1")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "provider", column = "provider"),
        @Result(property = "apiKey", column = "api_key"),
        @Result(property = "apiUrl", column = "api_url"),
        @Result(property = "model", column = "model"),
        @Result(property = "temperature", column = "temperature"),
        @Result(property = "useLocalRules", column = "use_local_rules"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    LlmConfig findLatest();

    @Insert("INSERT INTO llm_config (provider, api_key, api_url, model, temperature, use_local_rules, created_at, updated_at) " +
            "VALUES (#{provider}, #{apiKey}, #{apiUrl}, #{model}, #{temperature}, #{useLocalRules}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LlmConfig config);

    @Update("UPDATE llm_config SET provider = #{provider}, api_key = #{apiKey}, api_url = #{apiUrl}, " +
            "model = #{model}, temperature = #{temperature}, use_local_rules = #{useLocalRules}, updated_at = NOW() WHERE id = #{id}")
    int update(LlmConfig config);

    @Delete("DELETE FROM llm_config")
    int deleteAll();
}
