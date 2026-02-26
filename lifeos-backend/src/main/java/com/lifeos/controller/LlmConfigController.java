package com.lifeos.controller;

import com.lifeos.dto.ApiResponse;
import com.lifeos.entity.LlmConfig;
import com.lifeos.mapper.LlmConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LlmConfigController {

    @Autowired
    private LlmConfigMapper llmConfigMapper;

    @GetMapping("/llm-config")
    public ApiResponse<LlmConfig> getConfig() {
        try {
            LlmConfig config = llmConfigMapper.findLatest();
            if (config == null) {
                // 返回成功但 data 为 null，表示没有配置
                return ApiResponse.success(null);
            }
            // 检查配置是否有效（有 API Key 或启用本地规则）
            boolean hasValidApiKey = config.getApiKey() != null && 
                                     !config.getApiKey().trim().isEmpty() && 
                                     !config.getApiKey().contains("dummy");
            boolean useLocalRules = config.getUseLocalRules() != null && config.getUseLocalRules();
            
            if (!hasValidApiKey && !useLocalRules) {
                // 配置存在但无效，返回 null
                return ApiResponse.success(null);
            }
            return ApiResponse.success(config);
        } catch (Exception e) {
            return ApiResponse.error("获取配置失败: " + e.getMessage());
        }
    }

    @PostMapping("/llm-config")
    public ApiResponse<LlmConfig> saveConfig(@RequestBody LlmConfig config) {
        try {
            LlmConfig existing = llmConfigMapper.findLatest();
            if (existing != null) {
                config.setId(existing.getId());
                llmConfigMapper.update(config);
            } else {
                llmConfigMapper.insert(config);
            }
            return ApiResponse.success(config);
        } catch (Exception e) {
            return ApiResponse.error("保存配置失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/llm-config")
    public ApiResponse<Void> deleteConfig() {
        try {
            llmConfigMapper.deleteAll();
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error("删除配置失败: " + e.getMessage());
        }
    }
}
