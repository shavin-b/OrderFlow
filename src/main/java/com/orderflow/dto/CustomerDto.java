package com.orderflow.dto;

import com.orderflow.entity.Customer.CustomerStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for {@link com.orderflow.entity.Customer}.
 */
@Getter
@Builder
public class CustomerDto {

    private Long id;
    private String waId;
    private String phone;
    private String name;
    private String email;
    private CustomerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
