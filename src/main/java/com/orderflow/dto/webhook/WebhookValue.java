package com.orderflow.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * The core value object inside a webhook change, containing messages, contacts,
 * statuses and metadata.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookValue {

    @JsonProperty("messaging_product")
    private String messagingProduct;

    @JsonProperty("metadata")
    private WaMetadata metadata;

    @JsonProperty("contacts")
    private List<WaContact> contacts;

    @JsonProperty("messages")
    private List<WaMessage> messages;

    @JsonProperty("statuses")
    private List<WaStatus> statuses;
}
