package com.orderflow.service.impl;

import com.orderflow.dto.MessageDto;
import com.orderflow.dto.PagedResponse;
import com.orderflow.dto.request.SendTextMessageRequest;
import com.orderflow.entity.Conversation;
import com.orderflow.entity.Message;
import com.orderflow.entity.Message.MessageDirection;
import com.orderflow.entity.Message.MessageStatus;
import com.orderflow.entity.Message.MessageType;
import com.orderflow.exception.ResourceNotFoundException;
import com.orderflow.mapper.MessageMapper;
import com.orderflow.repository.ConversationRepository;
import com.orderflow.repository.MessageRepository;
import com.orderflow.service.MessageService;
import com.orderflow.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of {@link MessageService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final MessageMapper messageMapper;
    private final WhatsAppService whatsAppService;

    @Override
    public PagedResponse<MessageDto> findByConversationId(Long conversationId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("timestamp").ascending());
        return PagedResponse.from(
                messageRepository.findByConversationId(conversationId, pageable)
                        .map(messageMapper::toDto));
    }

    @Override
    public MessageDto findById(Long id) {
        return messageMapper.toDto(getMessageOrThrow(id));
    }

    @Override
    @Transactional
    public Message saveInboundMessage(Conversation conversation,
                                       String waMessageId,
                                       MessageType type,
                                       String body,
                                       LocalDateTime timestamp) {
        // Idempotency: skip if already persisted
        if (waMessageId != null && messageRepository.existsByWaMessageId(waMessageId)) {
            log.debug("Message {} already stored, skipping", waMessageId);
            return messageRepository.findByWaMessageId(waMessageId).orElseThrow();
        }

        Message message = Message.builder()
                .conversation(conversation)
                .waMessageId(waMessageId)
                .direction(MessageDirection.INBOUND)
                .type(type)
                .body(body)
                .status(MessageStatus.RECEIVED)
                .timestamp(timestamp != null ? timestamp : LocalDateTime.now())
                .build();

        Message saved = messageRepository.save(message);
        log.info("Saved inbound message id={}, wa_id={}, type={}", saved.getId(), waMessageId, type);
        return saved;
    }

    @Override
    @Transactional
    public MessageDto sendText(Long conversationId, SendTextMessageRequest request) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

        String waMessageId = whatsAppService.sendTextMessage(request.getTo(), request.getBody());

        Message message = Message.builder()
                .conversation(conversation)
                .waMessageId(waMessageId)
                .direction(MessageDirection.OUTBOUND)
                .type(MessageType.TEXT)
                .body(request.getBody())
                .status(MessageStatus.SENT)
                .timestamp(LocalDateTime.now())
                .build();

        Message saved = messageRepository.save(message);
        log.info("Sent and saved outbound message id={}, wa_id={}", saved.getId(), waMessageId);
        return messageMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void updateStatus(String waMessageId, MessageStatus status) {
        int updated = messageRepository.updateStatusByWaMessageId(waMessageId, status);
        if (updated > 0) {
            log.debug("Updated message status: wa_id={}, status={}", waMessageId, status);
        } else {
            log.warn("No message found to update status for wa_id={}", waMessageId);
        }
    }

    private Message getMessageOrThrow(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", id));
    }
}
