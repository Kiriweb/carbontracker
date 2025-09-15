package com.carbontracker.service;

import com.carbontracker.dto.UserDTO;
import com.carbontracker.model.User;

import java.util.List;

public interface UserService {
    User register(UserDTO userDto);
    User login(UserDTO userDto);
    User findByEmail(String email);

    List<UserDTO> findPendingUsers();
    void approveUser(Long id);

    // NEW for admin dashboard
    List<UserDTO> findAllUsers();
    void deleteUser(Long id);
}


