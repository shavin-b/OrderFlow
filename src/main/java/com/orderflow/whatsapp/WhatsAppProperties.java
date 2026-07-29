package com.orderflow.whatsapp;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalized configuration for the WhatsApp Cloud API.
 * Bound from {@code whatsapp.api.*} properties.
 */
@Data
@ConfigurationProperties(prefix = "whatsapp.api")
public class WhatsAppProperties {

    /** Graph API base URL (e.g. {@code https://graph.facebook.com/v20.0}). */
    private String baseUrl;

    /** Long-lived or System User access token from Meta for Developers. */
    private String accessToken;

    /** The WhatsApp Business Account phone number ID used to send messages. */
    private String phoneNumberId;

    /** Secret string used to verify incoming webhook subscriptions from Meta. */
    private String verifyToken;

    /** App secret from Meta — used to validate X-Hub-Signature-256 HMAC. */
    private String appSecret;

    /** Timeout in seconds for outbound API calls. Default: 30. */
    private int timeoutSeconds = 30;
}
