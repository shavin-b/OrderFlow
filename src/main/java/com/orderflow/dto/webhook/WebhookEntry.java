package com.orderflow.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * A single entry in the webhook payload, corresponding to one WhatsApp Business Account.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookEntry {

    @JsonProperty("id")
    private String id;

    @JsonProperty("changes")
    private List<WebhookChange> changes;
}
