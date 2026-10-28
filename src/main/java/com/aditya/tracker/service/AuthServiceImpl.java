package com.aditya.tracker.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aditya.tracker.entity.User;
import com.aditya.tracker.repository.UserRepository;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


 
    public User signup(User user) 
    {
        if (user.getFullName() == null
                || user.getFullName().isBlank()
                || user.getEmail() == null
                || user.getEmail().isBlank()
                || user.getPassword() == null
                || user.getPassword().length() < 8) {
            throw new IllegalArgumentException(
                    "Enter your name, email and a password of at least 8 characters");
        }

        String email = user.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists");
        }

        user.setEmail(email);

        // Never store the plain-text password.
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
        
 
    }
}