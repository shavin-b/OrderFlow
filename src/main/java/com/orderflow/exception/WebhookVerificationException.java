package com.orderflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when WhatsApp webhook verification fails (wrong verify_token or missing parameters).
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class WebhookVerificationException extends RuntimeException {

    public WebhookVerificationException(String message) {
        super(message);
    }
}
