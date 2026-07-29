package com.orderflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for sending a plain text WhatsApp message.
 */
@Data
public class SendTextMessageRequest {

    @NotBlank(message = "Recipient phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{6,14}$", message = "Invalid phone number format")
    private String to;

    @NotBlank(message = "Message body is required")
    @Size(max = 4096, message = "Message body must not exceed 4096 characters")
    private String body;

    /** If true, WhatsApp will render URLs as clickable links. */
    private boolean previewUrl = false;
}
