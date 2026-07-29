package com.orderflow.service;

import com.orderflow.dto.MessageDto;
import com.orderflow.dto.PagedResponse;
import com.orderflow.dto.request.SendTextMessageRequest;
import com.orderflow.entity.Conversation;
import com.orderflow.entity.Message;
import com.orderflow.entity.Message.MessageDirection;
import com.orderflow.entity.Message.MessageStatus;
import com.orderflow.entity.Message.MessageType;

import java.time.LocalDateTime;

/**
 * Service contract for message operations.
 */
public interface MessageService {

    PagedResponse<MessageDto> findByConversationId(Long conversationId, int page, int size);

    MessageDto findById(Long id);

    /**
     * Persists an inbound message received from the WhatsApp webhook.
     */
    Message saveInboundMessage(Conversation conversation,
                                String waMessageId,
                                MessageType type,
                                String body,
                                LocalDateTime timestamp);

    /**
     * Sends a text message via WhatsApp and persists the outbound record.
     */
    MessageDto sendText(Long conversationId, SendTextMessageRequest request);

    /**
     * Updates the status of a message identified by its WhatsApp message ID.
     */
    void updateStatus(String waMessageId, MessageStatus status);
}
