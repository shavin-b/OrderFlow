package com.orderflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderflow.dto.webhook.WebhookPayload;
import com.orderflow.exception.WebhookVerificationException;
import com.orderflow.service.WebhookProcessorService;
import com.orderflow.util.WebhookSignatureValidator;
import com.orderflow.whatsapp.WhatsAppProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * WhatsApp Cloud API webhook controller.
 *
 * <ul>
 *   <li>{@code GET /webhook} — Meta subscription verification challenge</li>
 *   <li>{@code POST /webhook} — Inbound event ingestion</li>
 * </ul>
 *
 * <p>Both endpoints are public (no API key required).
 * The POST endpoint validates the X-Hub-Signature-256 HMAC header.
 */
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhook", description = "WhatsApp Cloud API webhook endpoints (public)")
public class WebhookController {

    private static final String HUB_MODE_SUBSCRIBE = "subscribe";

    private final WhatsAppProperties whatsAppProperties;
    private final WebhookSignatureValidator signatureValidator;
    private final WebhookProcessorService webhookProcessorService;
    private final ObjectMapper objectMapper;

    /**
     * Webhook verification endpoint called by Meta when you set up or update the webhook URL.
     *
     * <p>Meta sends: GET /webhook?hub.mode=subscribe&hub.verify_token=...&hub.challenge=...
     * If the verify_token matches, we return the challenge value as plain text.
     */
    @GetMapping
    @Operation(
        summary = "Webhook verification",
        description = "Meta webhook verification endpoint. Returns hub.challenge on success.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Verification successful"),
            @ApiResponse(responseCode = "403", description = "Verification failed")
        }
    )
    public ResponseEntity<String> verify(
            @Parameter(description = "Must be 'subscribe'")
            @RequestParam("hub.mode") String mode,

            @Parameter(description = "Must match WHATSAPP_VERIFY_TOKEN")
            @RequestParam("hub.verify_token") String verifyToken,

            @Parameter(description = "Challenge string to echo back")
            @RequestParam("hub.challenge") String challenge) {

        log.info("Webhook verification request — mode={}", mode);

        if (!HUB_MODE_SUBSCRIBE.equals(mode)) {
            throw new WebhookVerificationException("Invalid hub.mode: " + mode);
        }

        if (!whatsAppProperties.getVerifyToken().equals(verifyToken)) {
            log.warn("Webhook verify_token mismatch");
            throw new WebhookVerificationException("Invalid verify_token");
        }

        log.info("Webhook verified successfully");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(challenge);
    }

    /**
     * Receives WhatsApp webhook events (messages, statuses, etc.).
     *
     * <p>Reads the raw body as bytes to validate the HMAC signature, then
     * deserializes the payload and processes it asynchronously.
     * Returns 200 OK immediately — Meta will retry if it doesn't receive 200 within 20 seconds.
     */
    @PostMapping
    @Operation(
        summary = "Receive webhook event",
        description = "Receives WhatsApp Cloud API events. Validates HMAC signature.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Event accepted"),
            @ApiResponse(responseCode = "403", description = "Invalid signature")
        }
    )
    public ResponseEntity<String> receive(
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody byte[] rawPayload) {

        log.debug("Received webhook event, bytes={}, signature present={}",
                rawPayload.length, signature != null);

        // Validate HMAC signature (skip only if app secret is placeholder/blank)
        String appSecret = whatsAppProperties.getAppSecret();
        if (appSecret != null && !appSecret.isBlank()
                && !appSecret.equals("your_app_secret_here")) {
            if (!signatureValidator.isValid(rawPayload, signature, appSecret)) {
                throw new WebhookVerificationException("Invalid X-Hub-Signature-256");
            }
        }

        try {
            WebhookPayload payload = objectMapper.readValue(rawPayload, WebhookPayload.class);
            webhookProcessorService.process(payload);
        } catch (Exception e) {
            log.error("Failed to deserialize webhook payload: {}", e.getMessage(), e);
            // Still return 200 to prevent Meta from retrying malformed payloads
        }

        return ResponseEntity.ok("EVENT_RECEIVED");
    }
}
