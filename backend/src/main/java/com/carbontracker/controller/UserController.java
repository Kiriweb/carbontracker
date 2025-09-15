package com.carbontracker.controller;

import com.carbontracker.dto.UserDTO;
import com.carbontracker.model.User;
import com.carbontracker.service.UserService;
import com.carbontracker.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final String ADMIN_EMAIL = "admin@carbontrackerapp.com";

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public User register(@RequestBody UserDTO userDto) {
        return userService.register(userDto);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO userDto, HttpServletResponse response) {
        User user = userService.login(userDto);

        if (!user.isEnabled()) {
            return ResponseEntity.status(403).body("Account not yet approved by admin.");
        }

        String token = jwtUtil.generateToken(user);

        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .path("/")
                .maxAge(3600)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
    public User getCurrentUser(HttpServletRequest request) {
        String email = emailFromJwtCookie(request);
        if (email == null) throw new RuntimeException("Unauthenticated");
        return userService.findByEmail(email);
    }

    // -------- Admin-only: pending list / approve --------
    @GetMapping("/pending")
    public List<UserDTO> getPendingUsers(HttpServletRequest request) {
        assertAdmin(request);
        return userService.findPendingUsers();
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveUser(@PathVariable Long id, HttpServletRequest request) {
        assertAdmin(request);
        userService.approveUser(id);
        return ResponseEntity.ok().build();
    }

    // -------- NEW Admin-only: list all users / delete user --------
    @GetMapping
    public List<UserDTO> getAllUsers(HttpServletRequest request) {
        assertAdmin(request);
        return userService.findAllUsers();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        assertAdmin(request);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // -------- helpers --------
    private void assertAdmin(HttpServletRequest request) {
        String email = emailFromJwtCookie(request);
        if (!ADMIN_EMAIL.equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }
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
}
