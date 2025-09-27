# Role-Based Access Control System Implementation

## Overview

This guide explains how to create and use a role-based access control (RBAC) system in your Spring Boot application. The system supports three user roles: `USER`, `ADMIN`, and `SUPER_ADMIN`.

## System Architecture

### 1. Role Enum
```java
public enum Role {
    USER,           // Regular users
    ADMIN,          // Administrative users
    SUPER_ADMIN     // Super administrative users
}
```

### 2. User Entity with Role
```java
@Entity
public class User {
    // ... other fields
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER; // Default role for new users
}
```

### 3. Security Components

#### CustomUserDetails
- Implements Spring Security's `UserDetails` interface
- Maps user roles to Spring Security authorities
- Provides access to user information

#### CustomUserDetailsService
- Implements Spring Security's `UserDetailsService`
- Loads user details by email for authentication

#### JwtAuthenticationFilter
- Filters incoming requests to extract and validate JWT tokens
- Sets up Spring Security context with user authentication

#### JwtService (Enhanced)
- Generates JWT tokens with role information
- Extracts role information from tokens
- Validates tokens against user details

## Configuration

### SecurityConfig
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/api/register", "/api/login", "/api/test").permitAll()
                .requestMatchers("/api/books", "/api/books/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                
                // Admin-only endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // User endpoints (require authentication)
                .requestMatchers("/api/profile", "/api/transactions/**", "/api/wishlist/**", 
                               "/api/reviews/**", "/api/events/**", "/api/bids/**").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

## Usage Examples

### 1. Method-Level Security Annotations

#### Admin-Only Operations
```java
@DeleteMapping("/books/{bookId}")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public ResponseEntity<?> deleteBook(@PathVariable Long bookId) {
    // Only admins and super admins can access this
    return ResponseEntity.ok("Book deleted");
}
```

#### User Operations
```java
@PostMapping("/books")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public ResponseEntity<?> addBook(@RequestBody BookRequest request) {
    // Authenticated users can access this
    return ResponseEntity.ok("Book added");
}
```

#### Super Admin Operations
```java
@PutMapping("/users/{userId}/role")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public ResponseEntity<?> updateUserRole(@PathVariable Long userId, @RequestBody RoleUpdateRequest request) {
    // Only super admins can change user roles
    return ResponseEntity.ok("Role updated");
}
```

### 2. JWT Token Generation with Roles

```java
// In AuthController
String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getUsername(), user.getRole());

// In response
Map<String, Object> userInfo = new HashMap<>();
userInfo.put("id", user.getId());
userInfo.put("username", user.getUsername());
userInfo.put("email", user.getEmail());
userInfo.put("role", user.getRole()); // Include role in response
response.put("user", userInfo);
```

### 3. Role-Based Login

#### Regular User Login
```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    // ... validation logic
    User user = userService.findByEmail(request.getEmail()).orElseThrow();
    String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getUsername(), user.getRole());
    // ... return response with token and user info including role
}
```

#### Admin Login
```java
@PostMapping("/admin/login")
public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> request) {
    User user = userService.findByEmail(request.get("email")).orElseThrow();
    
    // Check if user has admin privileges
    if (user.getRole() != Role.ADMIN && user.getRole() != Role.SUPER_ADMIN) {
        return ResponseEntity.badRequest().body(Map.of("error", "Admin access required"));
    }
    
    // ... validate password and generate token
}
```

## User Management

### Promoting Users to Admin

#### Using UserManagementController
```java
@PostMapping("/admin/users/promote-to-admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public ResponseEntity<?> promoteToAdmin(@RequestBody Map<String, String> request) {
    String email = request.get("email");
    User user = userService.findByEmail(email).orElseThrow();
    user.setRole(Role.ADMIN);
    userService.saveUser(user);
    return ResponseEntity.ok("User promoted to admin");
}
```

#### Updating User Roles
```java
@PutMapping("/admin/users/{userId}/role")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public ResponseEntity<?> updateUserRole(@PathVariable Long userId, @RequestBody Map<String, String> request) {
    String roleString = request.get("role");
    Role newRole = Role.valueOf(roleString.toUpperCase());
    
    User user = userService.findById(userId).orElseThrow();
    user.setRole(newRole);
    userService.saveUser(user);
    return ResponseEntity.ok("Role updated");
}
```

## Frontend Integration

### JWT Token Usage
```javascript
// Store token after login
localStorage.setItem('token', response.data.token);
localStorage.setItem('userRole', response.data.user.role);

// Include token in API requests
const token = localStorage.getItem('token');
axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;

// Check user role for UI decisions
const userRole = localStorage.getItem('userRole');
if (userRole === 'ADMIN' || userRole === 'SUPER_ADMIN') {
    // Show admin features
}
```

### Role-Based UI Components
```javascript
// Show/hide features based on role
const isAdmin = userRole === 'ADMIN' || userRole === 'SUPER_ADMIN';
const isSuperAdmin = userRole === 'SUPER_ADMIN';

// Conditional rendering
{isAdmin && <AdminPanel />}
{isSuperAdmin && <UserManagementPanel />}
```

## Security Features

### 1. JWT Token Security
- Tokens include user role information
- Automatic role validation on each request
- Token expiration handling

### 2. Method-Level Security
- `@PreAuthorize` annotations for fine-grained control
- Role-based access control at method level
- Automatic authorization checks

### 3. URL-Based Security
- Public endpoints for registration/login
- Admin-only endpoints
- Authenticated user endpoints

### 4. Role Hierarchy
- `SUPER_ADMIN` > `ADMIN` > `USER`
- Super admins can manage all users
- Admins can manage content but not users
- Users can only access their own resources

## Testing the System

### 1. Register a User
```bash
POST /api/register
{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
}
```

### 2. Login as User
```bash
POST /api/login
{
    "email": "test@example.com",
    "password": "password123"
}
```

### 3. Promote User to Admin (Super Admin only)
```bash
POST /api/admin/users/promote-to-admin
Authorization: Bearer <super-admin-token>
{
    "email": "test@example.com"
}
```

### 4. Test Admin Endpoints
```bash
DELETE /api/admin/books/1
Authorization: Bearer <admin-token>
```

## Best Practices

1. **Always validate roles on the server side** - Never rely only on frontend role checks
2. **Use method-level security** - Prefer `@PreAuthorize` over manual checks
3. **Include roles in JWT tokens** - For stateless authentication
4. **Implement proper error handling** - Return appropriate HTTP status codes
5. **Log security events** - Track role changes and access attempts
6. **Regular token rotation** - Implement token refresh mechanism
7. **Principle of least privilege** - Users should only have necessary permissions

## Troubleshooting

### Common Issues

1. **"Access Denied" errors**
   - Check if user has correct role
   - Verify JWT token is valid and includes role
   - Ensure method has proper `@PreAuthorize` annotation

2. **JWT token issues**
   - Verify token includes role claim
   - Check token expiration
   - Ensure proper token format in Authorization header

3. **Role not updating**
   - Check if user is saved after role change
   - Verify database transaction is committed
   - Ensure new token is generated after role change

### Debug Tips

1. **Enable debug logging**
```properties
logging.level.org.springframework.security=DEBUG
logging.level.com.bookexchange.security=DEBUG
```

2. **Check token contents**
```java
Claims claims = jwtService.extractClaims(token);
System.out.println("Role: " + claims.get("role"));
```

3. **Verify user authorities**
```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
System.out.println("Authorities: " + auth.getAuthorities());
```
