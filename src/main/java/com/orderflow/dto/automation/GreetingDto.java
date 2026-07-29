package com.orderflow.dto.automation;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GreetingDto {

    private Long id;

    @NotBlank(message = "Greeting name is required")
    private String name;

    @NotBlank(message = "Greeting message body is required")
    private String messageBody;

    @Builder.Default
    private Boolean active = true;

    private String mediaUrl;
    private String mediaType;
}
