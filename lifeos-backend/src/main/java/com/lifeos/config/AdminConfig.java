package com.lifeos.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeos.entity.AdminUser;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class AdminConfig {
    
    private Map<String, AdminUser> adminUsers = new HashMap<>();
    
    @PostConstruct
    public void loadAdminConfig() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ClassPathResource resource = new ClassPathResource("admin-config.json");
            Map<String, Map<String, Object>> config = mapper.readValue(resource.getInputStream(), Map.class);
            
            for (Map.Entry<String, Map<String, Object>> entry : config.entrySet()) {
                Map<String, Object> userData = entry.getValue();
                AdminUser user = new AdminUser();
                user.setUsername((String) userData.get("username"));
                user.setPassword((String) userData.get("password"));
                user.setRole((String) userData.get("role"));
                user.setEnabled((Boolean) userData.get("enabled"));
                adminUsers.put(user.getUsername(), user);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load admin config", e);
        }
    }
    
    public AdminUser getAdminUser(String username) {
        return adminUsers.get(username);
    }
    
    public boolean validateCredentials(String username, String password) {
        AdminUser user = adminUsers.get(username);
        if (user == null || !user.isEnabled()) {
            return false;
        }
        return user.getPassword().equals(password);
    }
}
