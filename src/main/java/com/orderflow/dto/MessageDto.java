package com.orderflow.dto;

import com.orderflow.entity.Message.MessageDirection;
import com.orderflow.entity.Message.MessageStatus;
import com.orderflow.entity.Message.MessageType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for {@link com.orderflow.entity.Message}.
 */
@Getter
@Builder
public class MessageDto {

    private Long id;
    private Long conversationId;
    private String waMessageId;
    private MessageDirection direction;
    private MessageType type;
    private String body;
    private MessageStatus status;
    private LocalDateTime timestamp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AttachmentDto> attachments;
}
