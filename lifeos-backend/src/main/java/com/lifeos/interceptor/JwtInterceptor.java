package com.lifeos.interceptor;

import com.lifeos.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求路径
        String uri = request.getRequestURI();
        
        // 登录接口不需要验证
        if (uri.contains("/api/admin/auth/login")) {
            return true;
        }
        
        // 只有后台管理API需要验证
        if (!uri.startsWith("/api/admin/")) {
            return true;
        }

        // 获取Authorization头
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Missing or invalid authorization header\",\"data\":null}");
            return false;
        }

        String token = authHeader.substring(7);
        
        // 验证Token
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token is invalid or expired\",\"data\":null}");
            return false;
        }

        // 将用户信息存入request属性，供后续使用
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        request.setAttribute("username", username);
        request.setAttribute("role", role);

        return true;
    }
}
