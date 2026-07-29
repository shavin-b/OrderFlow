package com.orderflow.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyStatDto {

    private LocalDate statDate;
    private long incomingMessages;
    private long outgoingReplies;
    private long failedReplies;
    private long avgResponseTimeMs;
    private long activeCustomers;
    private String topKeyword;
}
