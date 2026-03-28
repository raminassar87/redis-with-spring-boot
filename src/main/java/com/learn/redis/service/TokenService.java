package com.learn.redis.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    private final RedisTemplate<String, Object> redisTemplate;

    public TokenService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Store a value with TTL
    public void saveToken(String userId, String token) {
        redisTemplate.opsForValue()
                .set("token:" + userId, token, Duration.ofHours(1));
    }

    // Retrieve
    public String getToken(String userId) {
        return (String) redisTemplate.opsForValue().get("token:" + userId);
    }

    // Delete manually
    public void deleteToken(String userId) {
        redisTemplate.delete("token:" + userId);
    }

    // Atomic counter (e.g. login attempts)
    public Long incrementLoginAttempts(String userId) {
        return redisTemplate.opsForValue()
                .increment("login:attempts:" + userId);
    }

    // Check if key exists
    public boolean tokenExists(String userId) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey("token:" + userId)
        );
    }
    public long getRemainingTTL(String key) {
        Long seconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return seconds != null ? seconds : -1; // -1 means no TTL set
    }
}