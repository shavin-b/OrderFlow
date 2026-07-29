package com.orderflow.security;

import com.orderflow.entity.Subscription;
import com.orderflow.entity.User;
import com.orderflow.repository.SubscriptionRepository;
import com.orderflow.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Filter that intercepts HTTP requests, extracts the JWT Bearer token,
 * validates user authentication, and enforces active subscription rules.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        // Skip auth/public endpoints
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String bearerToken = extractBearerToken(request);

        if (StringUtils.hasText(bearerToken) && jwtProvider.validateToken(bearerToken)) {
            String email = jwtProvider.getUsernameFromToken(bearerToken);

            Optional<User> optUser = userRepository.findByEmailWithRolesAndSubscriptions(email);
            if (optUser.isPresent()) {
                User user = optUser.get();

                // 1. User status check
                if (user.getStatus() != User.UserStatus.ACTIVE) {
                    log.warn("Blocked request for user {} - status is {}", email, user.getStatus());
                    sendErrorResponse(response, HttpStatus.FORBIDDEN, "User account is inactive or suspended");
                    return;
                }

                // 2. Subscription status check (Skip check for platform Admins)
                boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_ADMIN"));
                if (!isAdmin && isOperationalPath(path)) {
                    Optional<Subscription> latestSub = subscriptionRepository.findLatestByUserId(user.getId());
                    if (latestSub.isEmpty() || !latestSub.get().isActiveOrTrial()) {
                        log.warn("Blocked request for user {} - subscription expired or suspended", email);
                        sendErrorResponse(response, HttpStatus.PAYMENT_REQUIRED, "Subscription expired or suspended. Upgrade required.");
                        return;
                    }
                }

                List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r.getName().name()))
                        .toList();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/auth/")
                || path.startsWith("/webhook")
                || path.startsWith("/management")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/api-docs")
                || path.startsWith("/v3/api-docs");
    }

    private boolean isOperationalPath(String path) {
        // Paths requiring active subscription
        return path.startsWith("/messages") || path.startsWith("/conversations") || path.startsWith("/automation");
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(String.format("""
                {"status":%d,"error":"%s","message":"%s"}
                """, status.value(), status.getReasonPhrase(), message));
    }
}
