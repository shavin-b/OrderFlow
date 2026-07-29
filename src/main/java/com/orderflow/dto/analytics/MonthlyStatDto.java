package com.orderflow.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyStatDto {

    private String yearMonth;
    private long incomingMessages;
    private long outgoingReplies;
    private long failedReplies;
    private long avgResponseTimeMs;
    private long activeCustomers;
    private BigDecimal revenue;
}
