package com.learnredis.learnredis.service;

import com.learnredis.learnredis.bean.User;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public UserCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Store full user as hash fields
    public void cacheUser(User user) {
        String key = "user:" + user.getId();
        Map<String, Object> fields = new HashMap<>();
        fields.put("name", user.getName());
        fields.put("email", user.getEmail());
        fields.put("role", user.getRole());

        redisTemplate.opsForHash().putAll(key, fields);
        redisTemplate.expire(key, Duration.ofMinutes(30)); // set TTL
    }

    // Get a single field — no need to deserialize the whole object
    public String getUserEmail(String userId) {
        return (String) redisTemplate.opsForHash()
                .get("user:" + userId, "email");
    }

    // Update just one field
    public void updateUserRole(String userId, String newRole) {
        redisTemplate.opsForHash()
                .put("user:" + userId, "role", newRole);
    }

    // Get all fields
    public Map<Object, Object> getUser(String userId) {
        return redisTemplate.opsForHash().entries("user:" + userId);
    }

    public void evictUser(String userId) {
        redisTemplate.delete("user:" + userId);
    }
}