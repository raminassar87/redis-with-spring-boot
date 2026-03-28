package com.learn.redis.controller;

import com.learn.redis.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    // POST /api/tokens/{userId}
    // Body: { "token": "abc123" }
    @PostMapping("/{userId}")
    public ResponseEntity<String> saveToken(
            @PathVariable String userId,
            @RequestBody Map<String, String> body) {

        String token = body.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body("Token must not be empty");
        }
        tokenService.saveToken(userId, token);
        return ResponseEntity.ok("Token saved for user: " + userId);
    }

    // GET /api/tokens/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getToken(
            @PathVariable String userId) {

        String token = tokenService.getToken(userId);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No token found for user: " + userId));
        }
        long ttl = tokenService.getRemainingTTL("token:" + userId);
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "token", token,
                "ttlSeconds", ttl
        ));
    }

    // DELETE /api/tokens/{userId}
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteToken(@PathVariable String userId) {
        tokenService.deleteToken(userId);
        return ResponseEntity.ok("Token deleted for user: " + userId);
    }

    // GET /api/tokens/{userId}/exists
    @GetMapping("/{userId}/exists")
    public ResponseEntity<Map<String, Object>> tokenExists(
            @PathVariable String userId) {

        boolean exists = tokenService.tokenExists(userId);
        return ResponseEntity.ok(Map.of("userId", userId, "exists", exists));
    }

    // POST /api/tokens/{userId}/login-attempt
    @PostMapping("/{userId}/login-attempt")
    public ResponseEntity<Map<String, Object>> incrementLoginAttempts(
            @PathVariable String userId) {

        Long attempts = tokenService.incrementLoginAttempts(userId);
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "loginAttempts", attempts
        ));
    }
}