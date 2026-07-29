package com.orderflow.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Interactive reply payload — covers button replies and list replies.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WaInteractiveReply {

    @JsonProperty("type")
    private String type;

    @JsonProperty("button_reply")
    private ButtonReply buttonReply;

    @JsonProperty("list_reply")
    private ListReply listReply;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ButtonReply {
        @JsonProperty("id")
        private String id;

        @JsonProperty("title")
        private String title;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ListReply {
        @JsonProperty("id")
        private String id;

        @JsonProperty("title")
        private String title;

        @JsonProperty("description")
        private String description;
    }
}
