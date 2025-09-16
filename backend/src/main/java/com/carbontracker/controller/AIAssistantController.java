package com.carbontracker.controller;

import com.carbontracker.service.AIAssistantService;
import com.carbontracker.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIAssistantController {

    // Match SQL seed and frontend constant
    private static final String ADMIN_EMAIL = "admin@carbontracker.com";

    private final AIAssistantService aiService;
    private final JwtUtil jwtUtil;

    public AIAssistantController(AIAssistantService aiService, JwtUtil jwtUtil) {
        this.aiService = aiService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/suggestions/{logId}")
    public ResponseEntity<String> suggestions(@PathVariable Long logId, HttpServletRequest req) {
        assertAuthenticated(req);
        try {
            String text = aiService.generateSuggestions(logId);
            return ResponseEntity.ok(text);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("AI suggestion failed: " + e.getMessage());
        }
    }

    @GetMapping("/key")
    public ResponseEntity<Map<String, Object>> getKey(HttpServletRequest req) {
        assertAdmin(req);
        String masked = aiService.getMaskedApiKey();
        boolean hasKey = masked != null && !masked.isBlank();
        return ResponseEntity.ok(Map.of("hasKey", hasKey, "masked", hasKey ? masked : ""));
    }

    @PutMapping("/key")
    public ResponseEntity<?> putKey(@RequestBody Map<String, String> body, HttpServletRequest req) {
        assertAdmin(req);
        String apiKey = body.get("apiKey");
        if (apiKey == null || apiKey.isBlank()) {
            return ResponseEntity.badRequest().body("Missing apiKey");
        }
        aiService.setApiKey(apiKey.trim());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/key")
    public ResponseEntity<?> deleteKey(HttpServletRequest req) {
        assertAdmin(req);
        aiService.setApiKey(""); // clear key (or add a dedicated delete method in the service)
        return ResponseEntity.ok().build();
    }

    // --- helpers ---

    private String assertAuthenticated(HttpServletRequest request) {
        String email = emailFromJwtCookie(request);
        if (email == null || email.isBlank()) throw new UnauthorizedException();
        return email;
    }

    private void assertAdmin(HttpServletRequest request) {
        String email = emailFromJwtCookie(request);
        if (!ADMIN_EMAIL.equalsIgnoreCase(email)) throw new ForbiddenException();
    }

    private String emailFromJwtCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                .filter(c -> "jwt".equals(c.getName()))
                .map(c -> jwtUtil.validateToken(c.getValue()))
                .filter(e -> e != null && !e.isBlank())
                .findFirst()
                .orElse(null);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    private static class UnauthorizedException extends RuntimeException {}

    @ResponseStatus(HttpStatus.FORBIDDEN)
    private static class ForbiddenException extends RuntimeException {}
}
