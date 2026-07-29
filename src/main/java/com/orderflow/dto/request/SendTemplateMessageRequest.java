package com.orderflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * Request body for sending a WhatsApp template message.
 */
@Data
public class SendTemplateMessageRequest {

    @NotBlank(message = "Recipient phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{6,14}$", message = "Invalid phone number format")
    private String to;

    @NotBlank(message = "Template name is required")
    private String templateName;

    @NotBlank(message = "Language code is required (e.g. en_US)")
    private String languageCode;

    private List<TemplateComponent> components;

    @Data
    public static class TemplateComponent {
        @NotNull(message = "Component type is required")
        private String type;

        private List<TemplateParameter> parameters;
    }

    @Data
    public static class TemplateParameter {
        @NotNull(message = "Parameter type is required")
        private String type;

        private String text;
        private String imageLink;
    }
}
