package com.bookexchange.controller;

import com.bookexchange.dto.LoginRequest;
import com.bookexchange.security.CustomUserDetails;
import com.bookexchange.security.JwtService;
import com.bookexchange.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")

public class AuthController {
    @Autowired
    private  UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture) {

        return ResponseEntity.ok(userService.registerUserWithProfile(username, email, password, profilePicture));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String token = jwtService.generateToken(userDetails);

        Map<String, Object> userMap = new HashMap<>();
        var user = userDetails.getUser();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("email", user.getEmail());
        userMap.put("profilePicture", user.getProfilePicture());
        userMap.put("role", user.getRole());

        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("user", userMap);
        return ResponseEntity.ok(resp);
    }


    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateProfile(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(userService.updateUserProfile(userDetails.getUser(), username, email, password, profilePicture));
    }
}
