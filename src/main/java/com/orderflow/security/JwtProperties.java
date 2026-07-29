package com.orderflow.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** Secret key for signing JWT tokens (HMAC-SHA256). */
    private String secret = "404E635266556A586E3272357538782F413F4428472B4B6250655368566D5971";

    /** Access token expiration in milliseconds (default 15 minutes: 900000ms). */
    private long expirationMs = 900000L;

    /** Refresh token expiration in milliseconds (default 7 days: 604800000ms). */
    private long refreshExpirationMs = 604800000L;
}
