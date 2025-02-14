package com.demoproject.service;

import com.demoproject.entity.Users;
import com.demoproject.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public Optional<Users> getUserProfile(Long id) {
        return userRepository.findById(id);

    }

    public void saveUserProfile(Users user) {
        userRepository.save(user);
    }


}