package com.salesdata.platform.testing;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    /* Start of AUTH endpoints */
    // 1. PUBLIC ENDPOINT - should work without any token
    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> publicEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is a public endpoint - no authentication required!");
        response.put("timestamp", System.currentTimeMillis());
        response.put("authenticated", false);

        return ResponseEntity.ok(response);
    }

    // 2. PROTECTED ENDPOINT - should require valid JWT token
    @GetMapping("/protected")
    public ResponseEntity<Map<String, Object>> protectedEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is a protected endpoint - authentication required!");
        response.put("timestamp", System.currentTimeMillis());
        response.put("authenticated", true);

        return ResponseEntity.ok(response);
    }

    // 3. PROTECTED ENDPOINT - should return current user information
    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo() {
        // Get the current authenticated user from Spring Security context
        Authentication  authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Current user information");
            response.put("timestamp", System.currentTimeMillis());
            response.put("username", userDetails.getUsername());
            response.put("authorities", userDetails.getAuthorities());
            response.put("authenticated", true);

            return ResponseEntity.ok(response);
        }

        // This shouldn't happen if our filter works correctly
        Map<String, Object> response = new HashMap<>();
        response.put("error", "No authenticated user found");
        response.put("authenticated", false);

        return ResponseEntity.badRequest().body(response);
    }
}
