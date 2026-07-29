package com.orderflow.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Root payload sent by Meta to the webhook endpoint.
 *
 * <pre>
 * {
 *   "object": "whatsapp_business_account",
 *   "entry": [ ... ]
 * }
 * </pre>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookPayload {

    @JsonProperty("object")
    private String object;

    @JsonProperty("entry")
    private List<WebhookEntry> entry;
}
