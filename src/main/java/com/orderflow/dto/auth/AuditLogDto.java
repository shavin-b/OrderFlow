package com.orderflow.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {

    private Long id;
    private Long userId;
    private String userEmail;
    private String action;
    private String resource;
    private String details;
    private String ipAddress;
    private LocalDateTime timestamp;
}
