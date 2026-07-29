package com.orderflow.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Metadata about the receiving WhatsApp Business phone number.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WaMetadata {

    @JsonProperty("display_phone_number")
    private String displayPhoneNumber;

    @JsonProperty("phone_number_id")
    private String phoneNumberId;
}
