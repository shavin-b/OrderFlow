package com.orderflow.service;

import com.orderflow.dto.CustomerDto;
import com.orderflow.dto.PagedResponse;
import com.orderflow.dto.request.CreateCustomerRequest;
import com.orderflow.dto.request.UpdateCustomerRequest;
import com.orderflow.entity.Customer;
import com.orderflow.entity.Customer.CustomerStatus;

/**
 * Service contract for customer management operations.
 */
public interface CustomerService {

    PagedResponse<CustomerDto> findAll(int page, int size);

    PagedResponse<CustomerDto> search(String query, int page, int size);

    CustomerDto findById(Long id);

    CustomerDto findByWaId(String waId);

    CustomerDto create(CreateCustomerRequest request);

    CustomerDto update(Long id, UpdateCustomerRequest request);

    void updateStatus(Long id, CustomerStatus status);

    void delete(Long id);

    /**
     * Finds or creates a customer based on wa_id from an inbound webhook event.
     *
     * @param waId    WhatsApp contact ID
     * @param phone   phone number from the webhook
     * @param name    display name from the contact profile
     * @return the existing or newly created {@link Customer} entity
     */
    Customer findOrCreate(String waId, String phone, String name);
}
