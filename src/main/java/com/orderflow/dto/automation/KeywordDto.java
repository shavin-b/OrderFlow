package com.orderflow.dto.automation;

import com.orderflow.entity.Keyword.MatchType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeywordDto {

    private Long id;

    @NotBlank(message = "Keyword pattern is required")
    private String pattern;

    @NotNull(message = "Match type is required")
    @Builder.Default
    private MatchType matchType = MatchType.CONTAINS;

    @Builder.Default
    private Boolean ignoreCase = true;
}
