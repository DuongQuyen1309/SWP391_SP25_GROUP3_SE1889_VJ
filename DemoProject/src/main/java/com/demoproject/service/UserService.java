package com.demoproject.service;

import com.demoproject.entity.Users;
import com.demoproject.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public boolean isUserProfileComplete(Users user) {
        return user.getName() != null &&
                user.getPhone() != null &&
                user.getGender() != null &&
                user.getDateOfBirth() != null;
    }

    public boolean existsByPhoneExcludingUser(String phone, Long userId) {
        return userRepository.existsByPhoneAndIdNot(phone, userId);
    }

    public Optional<Users> getUserProfileByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    public List<Long> getStaffID(Long ownerID){
        return userRepository.getStaffID(ownerID);
    }
    public Long getOwnerID(Long staffID){
        return userRepository.getOwnerID(staffID);
    }
    public List<Users> getUsers(List<Long> relateList){
        return userRepository.getUser(relateList);
    }

    public List<Long> getStaffID1(Long ownerID){
        return userRepository.getStaffID1(ownerID);
    }
}