package com.bookexchange.controller;

import com.bookexchange.dto.LoginRequest;
import com.bookexchange.dto.RegisterRequest;
import com.bookexchange.entity.User;
import com.bookexchange.service.JwtService;
import com.bookexchange.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtService jwtService;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture) {
        try {
            User user = userService.registerUser(username, email, password);
            
            if (profilePicture != null && !profilePicture.isEmpty()) {
                String fileName = userService.saveProfilePicture(profilePicture, user.getId());
                user.setProfilePicture(fileName);
                userService.saveUser(user);
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "User registered successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Backend is working!");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        System.out.println("=== LOGIN DEBUG ===");
        System.out.println("Email: " + request.getEmail());
        System.out.println("Password: " + request.getPassword());
        
        try {
            Optional<User> userOpt = userService.findByEmail(request.getEmail());
            System.out.println("User found: " + userOpt.isPresent());
            
            if (userOpt.isEmpty()) {
                System.out.println("User not found in database");
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }
            
            User user = userOpt.get();
            System.out.println("Found user: " + user.getUsername());
            System.out.println("Stored password hash: " + user.getPassword());
            
            boolean passwordMatch = userService.checkPassword(request.getPassword(), user.getPassword());
            System.out.println("Password match: " + passwordMatch);
            
            if (!passwordMatch) {
                System.out.println("Password verification failed");
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid password");
                return ResponseEntity.badRequest().body(error);
            }
            
            String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getUsername(), user.getRole());
            System.out.println("Generated token: " + token.substring(0, 20) + "...");
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("profilePicture", user.getProfilePicture());
            userInfo.put("role", user.getRole());
            response.put("user", userInfo);
            
            System.out.println("Login successful for: " + user.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Login exception: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Login failed: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtService.extractEmail(token);
            
            Optional<User> userOpt = userService.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }
            
            User user = userOpt.get();
            
            if (username != null && !username.trim().isEmpty()) {
                user.setUsername(username.trim());
            }
            
            if (email != null && !email.trim().isEmpty()) {
                user.setEmail(email.trim());
            }
            
            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(userService.encodePassword(password));
            }
            
            if (profilePicture != null && !profilePicture.isEmpty()) {
                String fileName = userService.saveProfilePicture(profilePicture, user.getId());
                user.setProfilePicture(fileName);
            }
            
            User updatedUser = userService.saveUser(user);
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", updatedUser.getId());
            userInfo.put("username", updatedUser.getUsername());
            userInfo.put("email", updatedUser.getEmail());
            userInfo.put("profilePicture", updatedUser.getProfilePicture());
            
            return ResponseEntity.ok(userInfo);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}