package com.orderflow.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Text body of a WhatsApp text message.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WaTextMessage {

    @JsonProperty("body")
    private String body;
}
