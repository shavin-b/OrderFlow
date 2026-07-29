package com.orderflow.dto.automation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutomationRuleDto {

    private Long id;

    @NotBlank(message = "Rule name is required")
    private String name;

    private String description;

    @Builder.Default
    private Integer priority = 0;

    @Builder.Default
    private Boolean active = true;

    @Min(value = 0, message = "Cooldown seconds must be non-negative")
    @Builder.Default
    private Integer cooldownSeconds = 0;

    private Long triggerCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @NotEmpty(message = "Rule must have at least one keyword")
    @Valid
    private List<KeywordDto> keywords;

    @NotEmpty(message = "Rule must have at least one reply")
    @Valid
    private List<ReplyDto> replies;
}
