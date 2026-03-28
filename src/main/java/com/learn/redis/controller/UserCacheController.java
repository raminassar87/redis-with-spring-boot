package com.learn.redis.controller;

import com.learn.redis.bean.User;
import com.learn.redis.service.UserCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users/cache")
@RequiredArgsConstructor
public class UserCacheController {

    private final UserCacheService userCacheService;

    // POST /api/users/cache
    // Body: { "id": "1", "name": "John", "email": "john@test.com", "role": "ADMIN" }
    @PostMapping
    public ResponseEntity<String> cacheUser(@RequestBody User user) {
        userCacheService.cacheUser(user);
        return ResponseEntity.ok("User cached: " + user.getId());
    }

    // GET /api/users/cache/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<Map<Object, Object>> getUser(
            @PathVariable String userId) {

        Map<Object, Object> user = userCacheService.getUser(userId);
        if (user == null || user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(user);
    }

    // GET /api/users/cache/{userId}/email
    @GetMapping("/{userId}/email")
    public ResponseEntity<Map<String, Object>> getUserEmail(
            @PathVariable String userId) {

        String email = userCacheService.getUserEmail(userId);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found in cache"));
        }
        return ResponseEntity.ok(Map.of("userId", userId, "email", email));
    }

    // PATCH /api/users/cache/{userId}/role
    // Body: { "role": "MANAGER" }
    @PatchMapping("/{userId}/role")
    public ResponseEntity<String> updateRole(
            @PathVariable String userId,
            @RequestBody Map<String, String> body) {

        String newRole = body.get("role");
        if (newRole == null || newRole.isBlank()) {
            return ResponseEntity.badRequest().body("Role must not be empty");
        }
        userCacheService.updateUserRole(userId, newRole);
        return ResponseEntity.ok("Role updated to: " + newRole);
    }

    // DELETE /api/users/cache/{userId}
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> evictUser(@PathVariable String userId) {
        userCacheService.evictUser(userId);
        return ResponseEntity.ok("User evicted from cache: " + userId);
    }
}