package com.orderflow.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Media payload for image, audio, video, document, and sticker messages.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WaMediaMessage {

    @JsonProperty("id")
    private String id;

    @JsonProperty("mime_type")
    private String mimeType;

    @JsonProperty("sha256")
    private String sha256;

    @JsonProperty("caption")
    private String caption;

    @JsonProperty("filename")
    private String filename;

    @JsonProperty("voice")
    private Boolean voice;
}
