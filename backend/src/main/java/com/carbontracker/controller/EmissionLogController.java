package com.carbontracker.controller;

import com.carbontracker.dto.EmissionLogDTO;
import com.carbontracker.model.User;
import com.carbontracker.repository.UserRepository;
import com.carbontracker.service.EmissionLogService;
import com.carbontracker.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class EmissionLogController {

    private final EmissionLogService emissionLogService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public EmissionLogController(EmissionLogService emissionLogService,
                                 UserRepository userRepository,
                                 JwtUtil jwtUtil) {
        this.emissionLogService = emissionLogService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Returns the authenticated user's emission logs as DTOs.
     */
    @GetMapping
    public ResponseEntity<List<EmissionLogDTO>> myLogs(HttpServletRequest request) {
        String email = emailFromJwtCookie(request);
        if (email == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        if (!user.isEnabled()) {
            return ResponseEntity.status(403).build();
        }

        List<EmissionLogDTO> dto = emissionLogService.getMyLogs(email);
        return ResponseEntity.ok(dto);
    }

    /**
     * Creates an emission log from a full DTO payload for the authenticated user.
     * (This is separate from the quick-entry flow.)
     */
    @PostMapping
    public ResponseEntity<EmissionLogDTO> create(HttpServletRequest request,
                                                 @RequestBody EmissionLogDTO payload) {
        String email = emailFromJwtCookie(request);
        if (email == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        if (!user.isEnabled()) {
            return ResponseEntity.status(403).build();
        }

        EmissionLogDTO saved = emissionLogService.create(email, payload);
        return ResponseEntity.ok(saved);
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

        // Your JwtUtil returns the email (or null) when validating the token
        return jwtUtil.validateToken(jwtCookie.getValue());
    }
}
