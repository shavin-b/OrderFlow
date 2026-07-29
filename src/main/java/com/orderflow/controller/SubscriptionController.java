package com.orderflow.controller;

import com.orderflow.dto.ApiResponse;
import com.orderflow.dto.auth.SubscriptionDto;
import com.orderflow.entity.Subscription.PlanType;
import com.orderflow.entity.Subscription.SubscriptionStatus;
import com.orderflow.repository.UserRepository;
import com.orderflow.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subscriptions", description = "SaaS subscription management APIs")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;

    @GetMapping("/my-subscription")
    @Operation(summary = "Get current user's active subscription")
    public ResponseEntity<ApiResponse<SubscriptionDto>> getMySubscription(Authentication authentication) {
        Long userId = userRepository.findByEmail(authentication.getName())
                .orElseThrow().getId();
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getSubscriptionForUser(userId)));
    }

    @PostMapping("/upgrade")
    @Operation(summary = "Upgrade plan (MONTHLY, YEARLY, LIFETIME)")
    public ResponseEntity<ApiResponse<SubscriptionDto>> upgradePlan(
            Authentication authentication,
            @RequestParam PlanType planType,
            @RequestParam(required = false) String paymentRef) {
        Long userId = userRepository.findByEmail(authentication.getName())
                .orElseThrow().getId();
        SubscriptionDto upgraded = subscriptionService.upgradeSubscription(userId, planType, paymentRef);
        return ResponseEntity.ok(ApiResponse.success(upgraded, "Subscription upgraded successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Update subscription status (Admin only)")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long id,
            @RequestParam SubscriptionStatus status) {
        subscriptionService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(null, "Subscription status updated"));
    }
}
