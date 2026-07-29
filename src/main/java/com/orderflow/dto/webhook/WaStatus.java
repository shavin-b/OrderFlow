package com.orderflow.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Delivery/read status update for an outbound WhatsApp message.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WaStatus {

    @JsonProperty("id")
    private String id;

    @JsonProperty("status")
    private String status;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("recipient_id")
    private String recipientId;

    @JsonProperty("conversation")
    private WaConversation conversation;

    @JsonProperty("pricing")
    private WaPricing pricing;

    @JsonProperty("errors")
    private List<WaError> errors;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WaConversation {
        @JsonProperty("id")
        private String id;

        @JsonProperty("origin")
        private WaConversationOrigin origin;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WaConversationOrigin {
        @JsonProperty("type")
        private String type;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WaPricing {
        @JsonProperty("billable")
        private Boolean billable;

        @JsonProperty("pricing_model")
        private String pricingModel;

        @JsonProperty("category")
        private String category;
    }
}
