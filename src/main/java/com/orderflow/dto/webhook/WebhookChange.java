package com.orderflow.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * A single change event within a webhook entry.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookChange {

    @JsonProperty("value")
    private WebhookValue value;

    @JsonProperty("field")
    private String field;
}
