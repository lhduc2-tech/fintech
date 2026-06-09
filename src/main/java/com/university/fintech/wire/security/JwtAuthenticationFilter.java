package com.university.fintech.wire.security;

import com.university.fintech.domain.port.primary.TokenBlacklistService;
import com.university.fintech.wire.security.impl.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // Collect authentication header
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String email;

        // If not having token skip filter
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token from header
        jwt = authHeader.substring(7);
        // Check if token is valid
        if (jwtUtil.isTokenValid(jwt) && !tokenBlacklistService.isBlacklisted(jwt)) {
            // Extract information from token (email, claims)
            email = jwtUtil.extractEmail(jwt);
            var claims = jwtUtil.extractAllClaims(jwt);

            // If having auth skip set up again
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Collect data from claims
                String userId = claims.get("userId", String.class);
                String fullName = claims.get("fullName", String.class);

                List<String> roles = extractClaimList(claims.get("roles"));
                String legacyRole = claims.get("role", String.class);
                if (legacyRole != null && !legacyRole.isBlank()) {
                    roles = new ArrayList<>(roles);
                    roles.add(legacyRole);
                }

                List<String> permissions = extractClaimList(claims.get("permissions"));

                // Create a principal object that contains both email and userId
                Map<String, String> principal = new HashMap<>();
                principal.put("email", email);
                principal.put("userId", userId);
                principal.put("fullName", fullName);

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                // Spring security requires role having prefix "ROLE_"
                authorities.addAll(roles.stream()
                        .filter(role -> role != null && !role.isBlank())
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.trim().toUpperCase()))
                        .collect(Collectors.toList()));
                authorities.addAll(permissions.stream()
                        .filter(permission -> permission != null && !permission.isBlank())
                        .map(permission -> new SimpleGrantedAuthority(permission.trim().toUpperCase()))
                        .collect(Collectors.toList()));

                // Create authentication includes
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        // user info
                        principal,
                        // login before so credentials null
                        null,
                        // roles and permissions
                        authorities
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Set authentication to security context to use for authorization later
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Allow request to continue to controller
        filterChain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private List<String> extractClaimList(Object claim) {
        // Check data type for safe
        if (claim instanceof List<?> list) {
            // Convert into List<String>
            return list.stream().map(Object::toString).toList();
        }
        return Collections.emptyList();
    }
}
