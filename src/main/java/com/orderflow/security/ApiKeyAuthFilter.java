package com.orderflow.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * HTTP filter that extracts the {@code X-API-Key} header and validates it
 * against the configured set of valid API keys.
 *
 * <p>Requests without a valid key are rejected with {@code 401 Unauthorized}.
 * The webhook endpoint is excluded from API key auth because it is verified
 * via the WhatsApp verify-token and HMAC signature.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    @Value("${security.api-keys}")
    private String rawApiKeys;

    private volatile Set<String> validApiKeys;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getServletPath();

        // Skip API key check for webhook, actuator and swagger endpoints
        if (isPublicPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(API_KEY_HEADER);

        if (!StringUtils.hasText(apiKey) || !getValidApiKeys().contains(apiKey)) {
            log.warn("Rejected request to {} - invalid or missing API key", requestPath);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("""
                    {"status":401,"error":"Unauthorized","message":"Valid X-API-Key header is required"}
                    """);
            return;
        }

        SecurityContextHolder.getContext()
                .setAuthentication(new ApiKeyAuthenticationToken(apiKey));

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/webhook")
                || path.startsWith("/management")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/api-docs")
                || path.startsWith("/v3/api-docs");
    }

    private Set<String> getValidApiKeys() {
        if (validApiKeys == null) {
            synchronized (this) {
                if (validApiKeys == null) {
                    validApiKeys = Arrays.stream(rawApiKeys.split(","))
                            .map(String::trim)
                            .filter(StringUtils::hasText)
                            .collect(Collectors.toUnmodifiableSet());
                }
            }
        }
        return validApiKeys;
    }
}
