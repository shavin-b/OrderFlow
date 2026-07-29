package com.orderflow.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Validates the HMAC-SHA256 signature that Meta includes in the
 * {@code X-Hub-Signature-256} header of every webhook POST request.
 *
 * <p>The header value is in the format {@code sha256=<hex_signature>}.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WebhookSignatureValidator {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String SIGNATURE_PREFIX = "sha256=";

    /**
     * Validates the webhook signature.
     *
     * @param payload   raw request body bytes
     * @param signature value of the {@code X-Hub-Signature-256} header
     * @param appSecret WhatsApp app secret from Meta
     * @return {@code true} if the signature is valid
     */
    public boolean isValid(byte[] payload, String signature, String appSecret) {
        if (signature == null || !signature.startsWith(SIGNATURE_PREFIX)) {
            log.warn("Missing or malformed X-Hub-Signature-256 header");
            return false;
        }

        try {
            String expectedHex = signature.substring(SIGNATURE_PREFIX.length());
            String computedHex = computeHmacSha256(payload, appSecret);
            boolean valid = constantTimeEquals(expectedHex, computedHex);
            if (!valid) {
                log.warn("Webhook signature mismatch — request may not be from Meta");
            }
            return valid;
        } catch (Exception e) {
            log.error("Signature validation error: {}", e.getMessage(), e);
            return false;
        }
    }

    private String computeHmacSha256(byte[] data, String secret)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec keySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
        mac.init(keySpec);
        byte[] hash = mac.doFinal(data);
        return bytesToHex(hash);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Constant-time string comparison to prevent timing attacks.
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
