package com.carbontracker.controller;

import com.carbontracker.service.AIAssistantService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIAssistantController {

    private final AIAssistantService aiService;

    public AIAssistantController(AIAssistantService aiService) {
        this.aiService = aiService;
    }

    /** Generate AI suggestions for a specific emission log (current user or admin). */
    @GetMapping("/suggestions/{logId}")
    public ResponseEntity<String> suggestions(@PathVariable Long logId, HttpServletRequest req) {
        try {
            String email = currentUserEmail(req);
            if (email == null) return ResponseEntity.status(401).body("Unauthorized");
            String text = aiService.generateSuggestionsForLog(email, logId);
            return ResponseEntity.ok(text);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("AI suggestion failed: " + e.getMessage());
        }
    }

    /** Admin: get basic info about API key existence / masked preview. */
    @GetMapping("/key")
    public ResponseEntity<Map<String, Object>> getKeyInfo(HttpServletRequest req) {
        if (!isAdmin(req)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(Map.of(
                "hasKey", aiService.hasKey(),
                "masked", aiService.maskedKey()
        ));
    }

    /** Admin: set/replace OpenAI API key (stored in DB). */
    @PutMapping("/key")
    public ResponseEntity<?> putKey(@RequestBody Map<String, String> body, HttpServletRequest req) {
        if (!isAdmin(req)) return ResponseEntity.status(403).build();
        String apiKey = body.get("apiKey");
        if (apiKey == null || apiKey.isBlank()) {
            return ResponseEntity.badRequest().body("Missing apiKey");
        }
        aiService.storeKey(apiKey.trim());
        return ResponseEntity.ok().build();
    }

    /** Admin: delete the stored key. */
    @DeleteMapping("/key")
    public ResponseEntity<?> deleteKey(HttpServletRequest req) {
        if (!isAdmin(req)) return ResponseEntity.status(403).build();
        aiService.deleteKey();
        return ResponseEntity.ok().build();
    }

    // --- helpers ---

    private String currentUserEmail(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        Cookie jwtCookie = Arrays.stream(cookies)
                .filter(c -> "jwt".equals(c.getName()))
                .findFirst().orElse(null);
        if (jwtCookie == null) return null;
        return aiService.emailFromJwt(jwtCookie.getValue());
    }

    private boolean isAdmin(HttpServletRequest req) {
        String email = currentUserEmail(req);
        return "admin@carbontracker.com".equalsIgnoreCase(email);
    }
}
