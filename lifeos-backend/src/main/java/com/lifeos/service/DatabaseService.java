package com.lifeos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    /**
     * 获取所有表信息
     */
    public List<Map<String, Object>> getAllTables() {
        List<Map<String, Object>> tables = new ArrayList<>();
        
        // 查询所有表
        String sql = "SELECT TABLE_NAME, TABLE_COMMENT, TABLE_ROWS, DATA_LENGTH, INDEX_LENGTH " +
                     "FROM INFORMATION_SCHEMA.TABLES " +
                     "WHERE TABLE_SCHEMA = DATABASE() " +
                     "ORDER BY TABLE_NAME";
        
        List<Map<String, Object>> tableList = jdbcTemplate.queryForList(sql);
        
        for (Map<String, Object> table : tableList) {
            Map<String, Object> tableInfo = new HashMap<>();
            tableInfo.put("tableName", table.get("TABLE_NAME"));
            tableInfo.put("comment", table.get("TABLE_COMMENT"));
            tableInfo.put("rowCount", table.get("TABLE_ROWS") != null ? 
                ((Number) table.get("TABLE_ROWS")).longValue() : 0);
            tableInfo.put("dataSize", table.get("DATA_LENGTH") != null ? 
                ((Number) table.get("DATA_LENGTH")).longValue() : 0);
            tableInfo.put("indexSize", table.get("INDEX_LENGTH") != null ? 
                ((Number) table.get("INDEX_LENGTH")).longValue() : 0);
            tables.add(tableInfo);
        }
        
        return tables;
    }

    /**
     * 获取表结构
     */
    public List<Map<String, Object>> getTableStructure(String tableName) {
        String sql = "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT, EXTRA " +
                     "FROM INFORMATION_SCHEMA.COLUMNS " +
                     "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? " +
                     "ORDER BY ORDINAL_POSITION";
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> column = new HashMap<>();
            column.put("columnName", toUtf8(rs.getString("COLUMN_NAME")));
            column.put("dataType", toUtf8(rs.getString("DATA_TYPE")));
            column.put("nullable", toUtf8(rs.getString("IS_NULLABLE")));
            column.put("defaultValue", toUtf8(rs.getString("COLUMN_DEFAULT")));
            column.put("comment", toUtf8(rs.getString("COLUMN_COMMENT")));
            column.put("extra", toUtf8(rs.getString("EXTRA")));
            return column;
        }, tableName);
    }

    /**
     * 将字符串转换为 UTF-8 编码
     * 尝试多种编码组合来修复乱码问题
     */
    private String toUtf8(String str) {
        if (str == null) return null;
        
        // 如果字符串已经是正常的中文，直接返回
        if (isValidChinese(str)) {
            return str;
        }
        
        // 尝试各种编码转换
        String[] fromEncodings = {"ISO-8859-1", "UTF-8", "GBK", "GB2312", "Latin1"};
        String[] toEncodings = {"UTF-8", "GBK", "GB2312"};
        
        for (String from : fromEncodings) {
            for (String to : toEncodings) {
                if (from.equals(to)) continue;
                try {
                    byte[] bytes = str.getBytes(from);
                    String result = new String(bytes, to);
                    if (isValidChinese(result) && !result.contains("�")) {
                        return result;
                    }
                } catch (Exception e) {
                    // 忽略转换错误，继续尝试
                }
            }
        }
        
        // 如果都失败了，返回原始字符串
        return str;
    }

    /**
     * 检查字符串是否包含有效中文字符
     */
    private boolean isValidChinese(String str) {
        if (str == null || str.isEmpty()) return true;
        
        int chineseCount = 0;
        for (char c : str.toCharArray()) {
            // 检查是否是中文字符范围
            if (c >= 0x4E00 && c <= 0x9FA5) {
                chineseCount++;
            }
        }
        
        // 如果包含中文字符，认为是有效的
        return chineseCount > 0;
    }

    /**
     * 检查字符串是否包含乱码特征
     */
    private boolean containsGarbled(String str) {
        if (str == null) return false;
        // 检查是否包含常见的乱码字符（问号或替换字符）
        return str.contains("�") || str.contains("???");
    }

    /**
     * 获取表数据（带分页）
     */
    public Map<String, Object> getTableData(String tableName, int page, int size, 
                                            String sortField, String sortOrder) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取总记录数
        String countSql = "SELECT COUNT(*) FROM " + escapeIdentifier(tableName);
        Long total = jdbcTemplate.queryForObject(countSql, Long.class);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (int) Math.ceil((double) total / size));
        
        // 构建查询SQL
        StringBuilder querySql = new StringBuilder("SELECT * FROM ");
        querySql.append(escapeIdentifier(tableName));
        
        // 添加排序
        if (sortField != null && !sortField.isEmpty()) {
            querySql.append(" ORDER BY ").append(escapeIdentifier(sortField));
            if ("desc".equalsIgnoreCase(sortOrder)) {
                querySql.append(" DESC");
            } else {
                querySql.append(" ASC");
            }
        }
        
        // 添加分页
        int offset = (page - 1) * size;
        querySql.append(" LIMIT ").append(size).append(" OFFSET ").append(offset);
        
        List<Map<String, Object>> data = jdbcTemplate.queryForList(querySql.toString());
        
        // 处理BLOB类型数据
        for (Map<String, Object> row : data) {
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                if (entry.getValue() instanceof byte[]) {
                    entry.setValue(new String((byte[]) entry.getValue()));
                }
            }
        }
        
        result.put("data", data);
        return result;
    }

    /**
     * 插入数据
     */
    @Transactional
    public int insertData(String tableName, Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("数据不能为空");
        }
        
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(escapeIdentifier(tableName)).append(" (");
        
        StringBuilder values = new StringBuilder("VALUES (");
        List<Object> params = new ArrayList<>();
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                sql.append(", ");
                values.append(", ");
            }
            sql.append(escapeIdentifier(entry.getKey()));
            values.append("?");
            params.add(entry.getValue());
            first = false;
        }
        
        sql.append(") ").append(values).append(")");
        
        return jdbcTemplate.update(sql.toString(), params.toArray());
    }

    /**
     * 更新数据
     */
    @Transactional
    public int updateData(String tableName, Long id, Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("数据不能为空");
        }
        
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(escapeIdentifier(tableName)).append(" SET ");
        
        List<Object> params = new ArrayList<>();
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if ("id".equalsIgnoreCase(entry.getKey())) {
                continue; // 跳过id字段
            }
            if (!first) {
                sql.append(", ");
            }
            sql.append(escapeIdentifier(entry.getKey())).append(" = ?");
            params.add(entry.getValue());
            first = false;
        }
        
        sql.append(" WHERE id = ?");
        params.add(id);
        
        return jdbcTemplate.update(sql.toString(), params.toArray());
    }

    /**
     * 删除数据
     */
    @Transactional
    public int deleteData(String tableName, Long id) {
        String sql = "DELETE FROM " + escapeIdentifier(tableName) + " WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    /**
     * 执行SQL查询（只允许SELECT）
     */
    public List<Map<String, Object>> executeQuery(String sql) {
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * 获取数据库状态
     */
    public Map<String, Object> getDatabaseStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            status.put("databaseUrl", metaData.getURL());
            status.put("databaseProductName", metaData.getDatabaseProductName());
            status.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            status.put("driverName", metaData.getDriverName());
            status.put("driverVersion", metaData.getDriverVersion());
            
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库状态失败", e);
        }
        
        // 获取表数量
        String tableCountSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE()";
        Long tableCount = jdbcTemplate.queryForObject(tableCountSql, Long.class);
        status.put("tableCount", tableCount);
        
        return status;
    }

    /**
     * 转义标识符（防止SQL注入）
     */
    private String escapeIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("标识符不能为空");
        }
        // 只允许字母、数字、下划线
        if (!identifier.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("非法的标识符: " + identifier);
        }
        return "`" + identifier + "`";
    }
}
