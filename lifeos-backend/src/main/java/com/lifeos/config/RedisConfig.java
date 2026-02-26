package com.lifeos.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

@Slf4j
@Configuration
public class RedisConfig {

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        try {
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("localhost", 6379);
            
            LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                    .commandTimeout(Duration.ofSeconds(2))
                    .shutdownTimeout(Duration.ZERO)
                    .build();
            
            LettuceConnectionFactory factory = new LettuceConnectionFactory(config, clientConfig);
            factory.setValidateConnection(false);
            return factory;
        } catch (Exception e) {
            log.warn("Failed to create Redis connection factory: {}", e.getMessage());
            return null;
        }
    }

    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        if (connectionFactory == null) {
            log.warn("Redis not available, creating dummy template");
            return new StringRedisTemplate();
        }
        return new StringRedisTemplate(connectionFactory);
    }
}
