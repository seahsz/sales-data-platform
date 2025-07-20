package com.salesdata.platform.auth.config;

import com.salesdata.platform.auth.service.UserService;
import com.salesdata.platform.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        // Step 1: Get the authorization header
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Step 2: Check if header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token found, continue to next filter (this is normal for public endpoints)
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Extract the token (remove "Bearer " prefix)
        jwt = authHeader.substring(7);

        try {
            // Step 4: Extract username from token
            username = jwtUtil.getUsernameFromToken(jwt);

            // Step 5: Check if user is not already authenticated in this request
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Step 6: Load user details using existing service
                UserDetails userDetails = userService.loadUserByUsername(username);

                // Step 7: Validate the token (check if not expired and belongs to this user)
                if (jwtUtil.isTokenValid(jwt, userDetails)) {

                    // Step 8: Create Spring Security authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // No credentials needed since token is already validated
                            userDetails.getAuthorities()); // User permissions (empty for now)

                    // Step 9: Set additional request details
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Step 10: Tell Spring Security this user is authenticated
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

            }
        } catch (Exception e) {
            // Token is invalid (expired, malformed, etc.)
            // Don't throw an error here - just continue without authentication
            // Protected endpoints will reject the request later
            log.error("JWT authentication failed: {}", e.getMessage());
        }

        // Always continue to the next filter in the chain
        filterChain.doFilter(request, response);
    }
}
