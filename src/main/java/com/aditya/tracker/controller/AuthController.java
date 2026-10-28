package com.aditya.tracker.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.aditya.tracker.repository.UserRepository;

import com.aditya.tracker.entity.User;
import com.aditya.tracker.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController 
{

	private final AuthService authService;
	private final UserRepository userRepository;

	public AuthController(
	        AuthService authService,
	        UserRepository userRepository) {

	    this.authService = authService;
	    this.userRepository = userRepository;
	}

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) 
    {
        return Map.of("token", token.getToken());
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(
            @RequestBody User user) {

        authService.signup(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message",
                        "Account created successfully. Please log in."));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logoutInfo() 
    {
        return ResponseEntity.ok(
                Map.of("message", "You have been logged out."));
    }
    

@GetMapping("/me")
public Map<String, String> getCurrentUser(
        java.security.Principal principal) {

    if (principal == null) {
        throw new org.springframework.security.access.AccessDeniedException(
                "Please log in first");
    }

    User user = userRepository.findByEmail(
            principal.getName().trim().toLowerCase()
    ).orElseThrow(() ->
            new org.springframework.security.access.AccessDeniedException(
                    "User not found"));

    return Map.of(
            "fullName", user.getFullName(),
            "email", user.getEmail()
    );
}

}