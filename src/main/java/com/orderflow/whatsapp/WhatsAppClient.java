package com.orderflow.whatsapp;

import com.orderflow.exception.WhatsAppApiException;
import com.orderflow.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * WhatsApp Cloud API client using Spring WebClient.
 * Handles text messages, template messages, and read receipts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppClient implements WhatsAppService {

    private final WebClient.Builder webClientBuilder;
    private final WhatsAppProperties props;

    @Override
    public String sendTextMessage(String to, String body) {
        log.info("Sending text message to {}", to);

        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", to,
                "type", "text",
                "text", Map.of("preview_url", false, "body", body)
        );

        return sendMessage(payload);
    }

    @Override
    public String sendTemplateMessage(String to, String templateName, String languageCode, Object components) {
        log.info("Sending template '{}' to {}", templateName, to);

        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", to,
                "type", "template",
                "template", Map.of(
                        "name", templateName,
                        "language", Map.of("code", languageCode),
                        "components", components != null ? components : List.of()
                )
        );

        return sendMessage(payload);
    }

    @Override
    public void markMessageAsRead(String waMessageId) {
        log.debug("Marking message {} as read", waMessageId);

        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "status", "read",
                "message_id", waMessageId
        );

        try {
            buildClient()
                .post()
                .uri("/{phoneNumberId}/messages", props.getPhoneNumberId())
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        } catch (WebClientResponseException e) {
            log.warn("Failed to mark message {} as read: {}", waMessageId, e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Internal helpers
    // ----------------------------------------------------------------

    private String sendMessage(Map<String, Object> payload) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = buildClient()
                .post()
                .uri("/{phoneNumberId}/messages", props.getPhoneNumberId())
                .bodyValue(payload)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(
                                        new WhatsAppApiException(
                                                "WhatsApp API error: " + errorBody,
                                                clientResponse.statusCode().value()))))
                .bodyToMono(Map.class)
                .block();

            if (response == null) {
                throw new WhatsAppApiException("Empty response from WhatsApp API", 500);
            }

            @SuppressWarnings("unchecked")
            List<Map<String, String>> messages = (List<Map<String, String>>) response.get("messages");
            if (messages == null || messages.isEmpty()) {
                throw new WhatsAppApiException("No message ID in WhatsApp API response", 500);
            }

            String messageId = messages.get(0).get("id");
            log.info("Message sent successfully, wa_message_id={}", messageId);
            return messageId;

        } catch (WhatsAppApiException e) {
            throw e;
        } catch (Exception e) {
            throw new WhatsAppApiException("Failed to send WhatsApp message: " + e.getMessage(), e);
        }
    }

    private WebClient buildClient() {
        return webClientBuilder
                .baseUrl(props.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.getAccessToken())
                .build();
    }
}
