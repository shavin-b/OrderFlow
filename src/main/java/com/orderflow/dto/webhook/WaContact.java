package com.orderflow.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * WhatsApp contact profile included in a webhook event.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WaContact {

    @JsonProperty("profile")
    private WaProfile profile;

    @JsonProperty("wa_id")
    private String waId;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WaProfile {
        @JsonProperty("name")
        private String name;
    }
}
