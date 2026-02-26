package com.lifeos.controller;

import com.lifeos.dto.ApiResponse;
import com.lifeos.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/database")
@CrossOrigin(origins = "*")
public class DatabaseController {

    @Autowired
    private DatabaseService databaseService;

    /**
     * 获取所有表信息
     */
    @GetMapping("/tables")
    public ApiResponse<List<Map<String, Object>>> getTables() {
        try {
            List<Map<String, Object>> tables = databaseService.getAllTables();
            return ApiResponse.success(tables);
        } catch (Exception e) {
            return ApiResponse.error("获取表列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取表结构
     */
    @GetMapping("/tables/{tableName}/structure")
    public ApiResponse<List<Map<String, Object>>> getTableStructure(@PathVariable String tableName) {
        try {
            List<Map<String, Object>> structure = databaseService.getTableStructure(tableName);
            return ApiResponse.success(structure);
        } catch (Exception e) {
            return ApiResponse.error("获取表结构失败: " + e.getMessage());
        }
    }

    /**
     * 获取表数据
     */
    @GetMapping("/tables/{tableName}/data")
    public ApiResponse<Map<String, Object>> getTableData(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        try {
            Map<String, Object> result = databaseService.getTableData(tableName, page, size, sortField, sortOrder);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("获取表数据失败: " + e.getMessage());
        }
    }

    /**
     * 插入数据
     */
    @PostMapping("/tables/{tableName}/data")
    public ApiResponse<Integer> insertData(@PathVariable String tableName, @RequestBody Map<String, Object> data) {
        try {
            int rows = databaseService.insertData(tableName, data);
            return ApiResponse.success("插入成功", rows);
        } catch (Exception e) {
            return ApiResponse.error("插入数据失败: " + e.getMessage());
        }
    }

    /**
     * 更新数据
     */
    @PutMapping("/tables/{tableName}/data/{id}")
    public ApiResponse<Integer> updateData(
            @PathVariable String tableName,
            @PathVariable Long id,
            @RequestBody Map<String, Object> data) {
        try {
            int rows = databaseService.updateData(tableName, id, data);
            return ApiResponse.success("更新成功", rows);
        } catch (Exception e) {
            return ApiResponse.error("更新数据失败: " + e.getMessage());
        }
    }

    /**
     * 删除数据
     */
    @DeleteMapping("/tables/{tableName}/data/{id}")
    public ApiResponse<Integer> deleteData(@PathVariable String tableName, @PathVariable Long id) {
        try {
            int rows = databaseService.deleteData(tableName, id);
            return ApiResponse.success("删除成功", rows);
        } catch (Exception e) {
            return ApiResponse.error("删除数据失败: " + e.getMessage());
        }
    }

    /**
     * 执行SQL查询
     */
    @PostMapping("/query")
    public ApiResponse<List<Map<String, Object>>> executeQuery(@RequestBody Map<String, String> request) {
        try {
            String sql = request.get("sql");
            if (sql == null || sql.trim().isEmpty()) {
                return ApiResponse.error("SQL语句不能为空");
            }
            
            // 只允许SELECT语句
            String upperSql = sql.trim().toUpperCase();
            if (!upperSql.startsWith("SELECT")) {
                return ApiResponse.error("只允许执行SELECT查询语句");
            }
            
            List<Map<String, Object>> result = databaseService.executeQuery(sql);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("执行查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据库状态
     */
    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> getDatabaseStatus() {
        try {
            Map<String, Object> status = databaseService.getDatabaseStatus();
            return ApiResponse.success(status);
        } catch (Exception e) {
            return ApiResponse.error("获取数据库状态失败: " + e.getMessage());
        }
    }
}
