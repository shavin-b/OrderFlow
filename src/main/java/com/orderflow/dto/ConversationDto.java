package com.orderflow.dto;

import com.orderflow.entity.Conversation.ConversationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for {@link com.orderflow.entity.Conversation}.
 */
@Getter
@Builder
public class ConversationDto {

    private Long id;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private ConversationStatus status;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
