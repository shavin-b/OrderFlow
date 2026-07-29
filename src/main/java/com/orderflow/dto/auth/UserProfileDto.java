package com.orderflow.dto.auth;

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
public class UserProfileDto {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Boolean emailVerified;
    private String status;
    private List<String> roles;
    private SubscriptionDto subscription;
    private LocalDateTime createdAt;
}
