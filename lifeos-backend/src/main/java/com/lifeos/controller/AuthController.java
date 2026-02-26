package com.lifeos.controller;

import com.lifeos.config.AdminConfig;
import com.lifeos.dto.ApiResponse;
import com.lifeos.dto.LoginRequest;
import com.lifeos.dto.LoginResponse;
import com.lifeos.entity.AdminUser;
import com.lifeos.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AdminConfig adminConfig;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return ApiResponse.error("Username and password are required");
        }

        // 验证用户名密码
        if (!adminConfig.validateCredentials(request.getUsername(), request.getPassword())) {
            return ApiResponse.error("Invalid username or password");
        }

        AdminUser user = adminConfig.getAdminUser(request.getUsername());
        if (user == null || !user.isEnabled()) {
            return ApiResponse.error("User is disabled");
        }

        // 生成JWT Token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        
        LoginResponse response = new LoginResponse(
                token,
                user.getUsername(),
                user.getRole(),
                86400000 // 24小时
        );

        return ApiResponse.success("Login successful", response);
    }

    /**
     * 验证Token是否有效
     */
    @GetMapping("/verify")
    public ApiResponse<Boolean> verifyToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ApiResponse.error("Invalid authorization header");
        }

        String token = authHeader.substring(7);
        boolean isValid = jwtUtil.validateToken(token);
        
        if (isValid) {
            return ApiResponse.success("Token is valid", true);
        } else {
            return ApiResponse.error("Token is invalid or expired");
        }
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    public ApiResponse<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ApiResponse.error("Invalid authorization header");
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return ApiResponse.error("Token is invalid or expired");
        }

        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        
        AdminUser user = adminConfig.getAdminUser(username);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        // 返回用户信息（不包含密码）
        return ApiResponse.success("User info retrieved", new Object() {
            public final String username = user.getUsername();
            public final String role = user.getRole();
            public final boolean enabled = user.isEnabled();
        });
    }

    /**
     * 退出登录（前端清除token即可，后端可以记录日志）
     */
    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestHeader("Authorization") String authHeader) {
        // 实际项目中可以在这里将token加入黑名单
        // 或者记录用户登出日志
        return ApiResponse.success("Logout successful", null);
    }
}
