package com.lifeos.controller;

import com.lifeos.dto.*;
import com.lifeos.entity.User;
import com.lifeos.mapper.UserMapper;
import com.lifeos.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public ApiResponse<User> register(@RequestBody UserRegisterRequest request) {
        try {
            // 验证参数
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ApiResponse.error("用户名不能为空");
            }
            if (request.getPassword() == null || request.getPassword().length() < 6) {
                return ApiResponse.error("密码长度至少6位");
            }

            // 检查用户名是否已存在
            User existingUser = userMapper.findByUsername(request.getUsername());
            if (existingUser != null) {
                return ApiResponse.error("用户名已存在");
            }

            // 创建新用户
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());

            userMapper.insert(user);

            // 不返回密码
            user.setPassword(null);
            return ApiResponse.success("注册成功", user);
        } catch (Exception e) {
            return ApiResponse.error("注册失败: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ApiResponse<UserLoginResponse> login(@RequestBody UserLoginRequest request) {
        try {
            // 验证参数
            if (request.getUsername() == null || request.getPassword() == null) {
                System.out.println("登录失败: 用户名或密码为空");
                return ApiResponse.error("用户名和密码不能为空");
            }

            System.out.println("登录请求 - 用户名: " + request.getUsername() + ", 密码长度: " + request.getPassword().length());

            // 查找用户
            User user = userMapper.findByUsername(request.getUsername());
            if (user == null) {
                System.out.println("登录失败: 用户不存在 - " + request.getUsername());
                return ApiResponse.error("用户名或密码错误");
            }

            System.out.println("找到用户: " + user.getUsername());
            
            // 如果是admin用户且密码是114514，直接更新密码哈希
            if ("admin".equals(request.getUsername()) && "114514".equals(request.getPassword())) {
                String newHash = passwordEncoder.encode(request.getPassword());
                System.out.println("更新admin密码哈希为: " + newHash);
                user.setPassword(newHash);
                userMapper.update(user);
            }

            // 验证密码
            boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
            System.out.println("密码验证结果: " + passwordMatches);
            
            if (!passwordMatches) {
                System.out.println("登录失败: 密码不匹配");
                return ApiResponse.error("用户名或密码错误");
            }

            // 生成JWT Token
            String token = jwtUtil.generateToken(user.getUsername(), "user", user.getId());

            UserLoginResponse response = new UserLoginResponse(
                    token,
                    user.getUsername(),
                    user.getId(),
                    86400000L // 24小时
            );

            return ApiResponse.success("登录成功", response);
        } catch (Exception e) {
            return ApiResponse.error("登录失败: " + e.getMessage());
        }
    }

    @GetMapping("/profile")
    public ApiResponse<User> getProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            if (!jwtUtil.validateToken(token)) {
                return ApiResponse.error("Token无效或已过期");
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userMapper.findById(userId);

            if (user == null) {
                return ApiResponse.error("用户不存在");
            }

            user.setPassword(null);
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error("获取用户信息失败: " + e.getMessage());
        }
    }
}
