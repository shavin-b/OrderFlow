package com.orderflow.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsSummaryDto {

    private long totalIncomingMessages;
    private long totalOutgoingReplies;
    private long totalFailedReplies;
    private long avgResponseTimeMs;
    private long activeCustomersCount;
    private BigDecimal monthlyRevenue;
    private double successRatePercentage;
    private String topKeywordPattern;
    private List<DailyStatDto> dailyBreakdown;
    private List<KeywordUsageDto> topKeywords;
}
