package com.orderflow.dto.automation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyDto {

    private Long id;

    @NotBlank(message = "Reply message body is required")
    private String messageBody;

    @Min(value = 0, message = "Reply order must be non-negative")
    @Builder.Default
    private Integer replyOrder = 0;

    @Min(value = 0, message = "Delay seconds must be non-negative")
    @Builder.Default
    private Integer delaySeconds = 0;

    private String mediaUrl;
    private String mediaType;
}
