package com.orderflow.service.impl;

import com.orderflow.dto.ConversationDto;
import com.orderflow.dto.PagedResponse;
import com.orderflow.entity.Conversation;
import com.orderflow.entity.Conversation.ConversationStatus;
import com.orderflow.entity.Customer;
import com.orderflow.exception.BusinessException;
import com.orderflow.exception.ResourceNotFoundException;
import com.orderflow.mapper.ConversationMapper;
import com.orderflow.repository.ConversationRepository;
import com.orderflow.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of {@link ConversationService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMapper conversationMapper;

    @Override
    public PagedResponse<ConversationDto> findAll(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("openedAt").descending());
        return PagedResponse.from(conversationRepository.findAll(pageable).map(conversationMapper::toDto));
    }

    @Override
    public PagedResponse<ConversationDto> findByCustomerId(Long customerId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("openedAt").descending());
        return PagedResponse.from(conversationRepository.findByCustomerId(customerId, pageable)
                .map(conversationMapper::toDto));
    }

    @Override
    public ConversationDto findById(Long id) {
        return conversationMapper.toDto(getConversationOrThrow(id));
    }

    @Override
    @Transactional
    public Conversation findOrCreateOpenConversation(Customer customer) {
        return conversationRepository
                .findLatestByCustomerIdAndStatus(customer.getId(), ConversationStatus.OPEN)
                .orElseGet(() -> {
                    Conversation conv = Conversation.builder()
                            .customer(customer)
                            .status(ConversationStatus.OPEN)
                            .openedAt(LocalDateTime.now())
                            .build();
                    Conversation saved = conversationRepository.save(conv);
                    log.info("Created new conversation id={} for customer id={}", saved.getId(), customer.getId());
                    return saved;
                });
    }

    @Override
    @Transactional
    public ConversationDto close(Long id) {
        Conversation conversation = getConversationOrThrow(id);
        if (conversation.getStatus() == ConversationStatus.CLOSED) {
            throw new BusinessException("Conversation " + id + " is already closed");
        }
        conversation.setStatus(ConversationStatus.CLOSED);
        conversation.setClosedAt(LocalDateTime.now());
        Conversation saved = conversationRepository.save(conversation);
        log.info("Closed conversation id={}", id);
        return conversationMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Conversation conversation = getConversationOrThrow(id);
        conversationRepository.delete(conversation);
        log.info("Deleted conversation id={}", id);
    }

    private Conversation getConversationOrThrow(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", id));
    }
}
