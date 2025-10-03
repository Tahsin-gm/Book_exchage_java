package com.bookexchange.service;

import com.bookexchange.entity.User;
import com.bookexchange.repository.UserRepository;
import com.bookexchange.security.JwtService;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service

public class    UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       ApplicationContext applicationContext,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ----------------- Registration -----------------
    public Map<String, Object> registerUserWithProfile(String username, String email, String password, MultipartFile profilePicture) {
        User user = registerUser(username, email, password);

        if (profilePicture != null && !profilePicture.isEmpty()) {
            String fileName = saveProfilePicture(profilePicture, user.getId());
            user.setProfilePicture(fileName);
            userRepository.save(user);
        }

        return Map.of("message", "User registered successfully");
    }

    public User registerUser(String username, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        return userRepository.save(user);
    }




    // ----------------- Profile Update -----------------
    public Map<String, Object> updateUserProfile(User user, String username, String email, String password, MultipartFile profilePicture) {
        if (username != null && !username.trim().isEmpty()) user.setUsername(username.trim());
        if (email != null && !email.trim().isEmpty()) user.setEmail(email.trim());
        if (password != null && !password.trim().isEmpty()) user.setPassword(passwordEncoder.encode(password));
        if (profilePicture != null && !profilePicture.isEmpty()) {
            String fileName = saveProfilePicture(profilePicture, user.getId());
            user.setProfilePicture(fileName);
        }

        User updatedUser = userRepository.save(user)    ;

        return Map.of(
                "id", updatedUser.getId(),
                "username", updatedUser.getUsername(),
                "email", updatedUser.getEmail(),
                "profilePicture", updatedUser.getProfilePicture(),
                "role", updatedUser.getRole()
        );
    }

    // ----------------- Helper Methods -----------------

    public String saveProfilePicture(MultipartFile file, Long userId) {
        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads/profiles/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            String fileName = "profile_" + userId + "_" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName));
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save profile picture: " + e.getMessage());
        }
    }
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
