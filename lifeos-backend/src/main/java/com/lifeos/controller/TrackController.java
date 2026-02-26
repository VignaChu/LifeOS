package com.lifeos.controller;

import com.lifeos.dto.ApiResponse;
import com.lifeos.dto.TrackRequest;
import com.lifeos.entity.LifeRecord;
import com.lifeos.service.TrackService;
import com.lifeos.service.QueryService;
import com.lifeos.service.ReportService;
import com.lifeos.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TrackController {

    @Autowired
    private TrackService trackService;

    @Autowired
    private QueryService queryService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserIdFromHeader(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return null;
        }
        try {
            String token = authHeader.replace("Bearer ", "");
            if (jwtUtil.validateToken(token)) {
                return jwtUtil.getUserIdFromToken(token);
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("ok");
    }

    @GetMapping("/test")
    public ApiResponse<String> test() {
        return ApiResponse.success("LifeOS Backend is running!");
    }

    @PostMapping("/track")
    public ApiResponse<LifeRecord> track(@RequestBody TrackRequest request,
                                         @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return ApiResponse.error("请先登录");
            }
            LifeRecord record = trackService.processText(request.getText(), userId);
            return ApiResponse.success(record);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/records")
    public ApiResponse<List<LifeRecord>> getRecords(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return ApiResponse.error("请先登录");
            }
            List<LifeRecord> records = trackService.getRecordsByUserId(userId);
            return ApiResponse.success(records);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // Admin API - get all records
    @GetMapping("/admin/records")
    public ApiResponse<List<LifeRecord>> getAllRecords(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // 验证管理员token（只需要验证token有效，不需要userId）
            if (authHeader == null || authHeader.isEmpty()) {
                return ApiResponse.error("请先登录");
            }
            String token = authHeader.replace("Bearer ", "");
            if (!jwtUtil.validateToken(token)) {
                return ApiResponse.error("登录已过期，请重新登录");
            }
            // 返回所有记录给管理员
            List<LifeRecord> records = trackService.getAllRecords();
            return ApiResponse.success(records);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/records/{id}")
    public ApiResponse<Void> deleteRecord(@PathVariable Long id,
                                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return ApiResponse.error("请先登录");
            }
            trackService.deleteRecord(id, userId);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/records/{id}")
    public ApiResponse<Void> updateRecord(@PathVariable Long id, @RequestBody LifeRecord record,
                                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return ApiResponse.error("请先登录");
            }
            record.setUserId(userId);
            trackService.updateRecord(id, record);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/query")
    public ApiResponse<String> query(@RequestBody TrackRequest request,
                                     @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return ApiResponse.error("请先登录");
            }
            String result = queryService.executeQuery(request.getText(), userId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/report/weekly")
    public ApiResponse<String> getWeeklyReport(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return ApiResponse.error("请先登录");
            }
            String report = reportService.generateWeeklyReport(userId);
            return ApiResponse.success(report);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/report/monthly")
    public ApiResponse<String> getMonthlyReport(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return ApiResponse.error("请先登录");
            }
            String report = reportService.generateMonthlyReport(userId);
            return ApiResponse.success(report);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/care")
    public ApiResponse<String> getCareMessage() {
        try {
            String message = trackService.getCareMessage();
            return ApiResponse.success(message);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
