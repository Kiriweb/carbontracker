package com.carbontracker.controller;

import com.carbontracker.dto.EmissionLogDTO;
import com.carbontracker.dto.QuickEmissionRequest;
import com.carbontracker.model.User;
import com.carbontracker.repository.UserRepository;
import com.carbontracker.service.EmissionLogService;
import com.carbontracker.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/emission-logs")
public class QuickEmissionController {

    private final EmissionLogService emissionLogService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public QuickEmissionController(EmissionLogService emissionLogService,
                                   UserRepository userRepository,
                                   JwtUtil jwtUtil) {
        this.emissionLogService = emissionLogService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/quick")
    public ResponseEntity<?> createQuick(@RequestBody QuickEmissionRequest req,
                                         HttpServletRequest request) {

        String email = emailFromJwtCookie(request);
        if (email == null) {
            return ResponseEntity.status(401).body("Unauthenticated");
        }

        // Verify the user still exists/enabled
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }
        if (!user.isEnabled()) {
            return ResponseEntity.status(403).body("User not approved");
        }

        try {
            EmissionLogDTO dto = emissionLogService.createQuick(email, req);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // --- helpers ---
    private String emailFromJwtCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        Cookie jwtCookie = Arrays.stream(cookies)
                .filter(c -> "jwt".equals(c.getName()))
                .findFirst()
                .orElse(null);
        if (jwtCookie == null) return null;

        // JwtUtil returns the email (or null) from the token
        return jwtUtil.validateToken(jwtCookie.getValue());
    }
}
