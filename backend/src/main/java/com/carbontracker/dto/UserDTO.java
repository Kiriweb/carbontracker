package com.carbontracker.dto;

public class UserDTO {
    private Long id;
    private String email;
    private String password; // used only on register/login
    private boolean enabled;

    public UserDTO() {}

    public UserDTO(Long id, String email) {
        this.id = id;
        this.email = email;
    }

    public UserDTO(Long id, String email, boolean enabled) {
        this.id = id;
        this.email = email;
        this.enabled = enabled;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
