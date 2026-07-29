package com.orderflow.service;

import com.orderflow.dto.auth.SubscriptionDto;
import com.orderflow.entity.Subscription;
import com.orderflow.entity.Subscription.PlanType;
import com.orderflow.entity.Subscription.SubscriptionStatus;
import com.orderflow.entity.User;
import com.orderflow.exception.ResourceNotFoundException;
import com.orderflow.mapper.SubscriptionMapper;
import com.orderflow.repository.SubscriptionRepository;
import com.orderflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionMapper subscriptionMapper;

    public SubscriptionDto getSubscriptionForUser(Long userId) {
        return subscriptionRepository.findLatestByUserId(userId)
                .map(subscriptionMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "userId", userId));
    }

    @Transactional
    public SubscriptionDto createTrialSubscription(User user) {
        LocalDateTime now = LocalDateTime.now();
        Subscription trial = Subscription.builder()
                .user(user)
                .planType(PlanType.TRIAL)
                .status(SubscriptionStatus.TRIAL)
                .startDate(now)
                .endDate(now.plusDays(14)) // 14-day free trial
                .autoRenew(false)
                .build();

        Subscription saved = subscriptionRepository.save(trial);
        log.info("Created 14-day trial subscription for user id={}", user.getId());
        return subscriptionMapper.toDto(saved);
    }

    @Transactional
    public SubscriptionDto upgradeSubscription(Long userId, PlanType planType, String paymentRef) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = switch (planType) {
            case TRIAL -> now.plusDays(14);
            case MONTHLY -> now.plusMonths(1);
            case YEARLY -> now.plusYears(1);
            case LIFETIME -> now.plusYears(99);
        };

        Subscription subscription = Subscription.builder()
                .user(user)
                .planType(planType)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(now)
                .endDate(endDate)
                .autoRenew(planType != PlanType.LIFETIME)
                .paymentRef(paymentRef)
                .build();

        Subscription saved = subscriptionRepository.save(subscription);
        log.info("Upgraded user id={} to plan {} until {}", userId, planType, endDate);
        return subscriptionMapper.toDto(saved);
    }

    @Transactional
    public void updateStatus(Long subscriptionId, SubscriptionStatus status) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", subscriptionId));
        sub.setStatus(status);
        subscriptionRepository.save(sub);
        log.info("Updated subscription id={} status to {}", subscriptionId, status);
    }
}
