package com.orderflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "daily_statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_date", nullable = false, unique = true)
    private LocalDate statDate;

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

    @Column(name = "top_keyword", length = 100)
    private String topKeyword;
}
