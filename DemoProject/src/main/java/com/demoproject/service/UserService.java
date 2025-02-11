package com.demoproject.service;

import com.demoproject.entity.User;
import com.demoproject.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserProfile(Long id) {
        return userRepository.findById(id).orElse(null); // Sửa lỗi tại đây
    }

    public void saveUserProfile(User user) {
        userRepository.save(user);
    }
}
