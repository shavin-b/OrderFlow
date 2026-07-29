package com.orderflow.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Error detail embedded in WhatsApp status updates.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WaError {

    @JsonProperty("code")
    private Integer code;

    @JsonProperty("title")
    private String title;

    @JsonProperty("message")
    private String message;

    @JsonProperty("error_data")
    private WaErrorData errorData;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WaErrorData {
        @JsonProperty("details")
        private String details;
    }
}
