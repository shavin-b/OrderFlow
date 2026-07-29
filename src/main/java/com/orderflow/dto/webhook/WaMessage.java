package com.orderflow.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * A WhatsApp message received via webhook. Covers all inbound message types:
 * text, image, audio, video, document, interactive reply.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WaMessage {

    @JsonProperty("id")
    private String id;

    @JsonProperty("from")
    private String from;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("type")
    private String type;

    // Text
    @JsonProperty("text")
    private WaTextMessage text;

    // Image
    @JsonProperty("image")
    private WaMediaMessage image;

    // Audio
    @JsonProperty("audio")
    private WaMediaMessage audio;

    // Video
    @JsonProperty("video")
    private WaMediaMessage video;

    // Document
    @JsonProperty("document")
    private WaMediaMessage document;

    // Sticker
    @JsonProperty("sticker")
    private WaMediaMessage sticker;

    // Interactive (button reply / list reply)
    @JsonProperty("interactive")
    private WaInteractiveReply interactive;

    // Context (reply to message)
    @JsonProperty("context")
    private WaContext context;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WaContext {
        @JsonProperty("from")
        private String from;

        @JsonProperty("id")
        private String id;
    }
}
