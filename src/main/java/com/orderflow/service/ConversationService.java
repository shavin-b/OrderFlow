package com.orderflow.service;

import com.orderflow.dto.ConversationDto;
import com.orderflow.dto.PagedResponse;
import com.orderflow.entity.Conversation;
import com.orderflow.entity.Customer;

/**
 * Service contract for conversation management.
 */
public interface ConversationService {

    PagedResponse<ConversationDto> findAll(int page, int size);

    PagedResponse<ConversationDto> findByCustomerId(Long customerId, int page, int size);

    ConversationDto findById(Long id);

    /**
     * Finds the latest open conversation for a customer, or creates a new one.
     */
    Conversation findOrCreateOpenConversation(Customer customer);

    ConversationDto close(Long id);

    void delete(Long id);
}
