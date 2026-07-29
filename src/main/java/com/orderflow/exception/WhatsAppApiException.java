package com.orderflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an outbound call to the WhatsApp Cloud API fails.
 */
@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class WhatsAppApiException extends RuntimeException {

    private final int statusCode;

    public WhatsAppApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public WhatsAppApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 500;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
