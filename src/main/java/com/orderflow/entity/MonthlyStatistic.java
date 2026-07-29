package com.orderflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "monthly_statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year_month", nullable = false, unique = true, length = 7)
    private String yearMonth; // Format: "YYYY-MM"

    @Column(name = "incoming_messages", nullable = false)
    @Builder.Default
    private Long incomingMessages = 0L;

    @Column(name = "outgoing_replies", nullable = false)
    @Builder.Default
    private Long outgoingReplies = 0L;

    @Column(name = "failed_replies", nullable = false)
    @Builder.Default
    private Long failedReplies = 0L;

    @Column(name = "avg_response_time_ms", nullable = false)
    @Builder.Default
    private Long avgResponseTimeMs = 0L;

    @Column(name = "active_customers", nullable = false)
    @Builder.Default
    private Long activeCustomers = 0L;

    @Column(name = "revenue", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal revenue = BigDecimal.ZERO;
}
