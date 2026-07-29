package com.orderflow.dto.auth;

import com.orderflow.entity.Subscription.PlanType;
import com.orderflow.entity.Subscription.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDto {

    private Long id;
    private Long userId;
    private PlanType planType;
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean autoRenew;
    private String paymentRef;
    private Boolean isActive;
}
