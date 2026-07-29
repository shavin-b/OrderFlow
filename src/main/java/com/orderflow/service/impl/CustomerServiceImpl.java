package com.orderflow.service.impl;

import com.orderflow.dto.CustomerDto;
import com.orderflow.dto.PagedResponse;
import com.orderflow.dto.request.CreateCustomerRequest;
import com.orderflow.dto.request.UpdateCustomerRequest;
import com.orderflow.entity.Customer;
import com.orderflow.entity.Customer.CustomerStatus;
import com.orderflow.exception.BusinessException;
import com.orderflow.exception.ResourceNotFoundException;
import com.orderflow.mapper.CustomerMapper;
import com.orderflow.repository.CustomerRepository;
import com.orderflow.service.CustomerService;
import com.orderflow.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link CustomerService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public PagedResponse<CustomerDto> findAll(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PagedResponse.from(customerRepository.findAll(pageable).map(customerMapper::toDto));
    }

    @Override
    public PagedResponse<CustomerDto> search(String query, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PagedResponse.from(customerRepository.searchCustomers(query, pageable)
                .map(customerMapper::toDto));
    }

    @Override
    public CustomerDto findById(Long id) {
        return customerMapper.toDto(getCustomerOrThrow(id));
    }

    @Override
    public CustomerDto findByWaId(String waId) {
        return customerRepository.findByWaId(waId)
                .map(customerMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "waId", waId));
    }

    @Override
    @Transactional
    public CustomerDto create(CreateCustomerRequest request) {
        String normalizedPhone = PhoneNumberUtil.normalize(request.getPhone());
        String waId = request.getWaId();

        if (customerRepository.existsByWaId(waId)) {
            throw new BusinessException("Customer with wa_id '" + waId + "' already exists");
        }
        if (customerRepository.existsByPhone(normalizedPhone)) {
            throw new BusinessException("Customer with phone '" + normalizedPhone + "' already exists");
        }

        Customer customer = customerMapper.toEntity(request);
        customer.setPhone(normalizedPhone);
        Customer saved = customerRepository.save(customer);
        log.info("Created customer id={}, wa_id={}", saved.getId(), saved.getWaId());
        return customerMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CustomerDto update(Long id, UpdateCustomerRequest request) {
        Customer customer = getCustomerOrThrow(id);
        customerMapper.updateEntityFromRequest(request, customer);
        Customer saved = customerRepository.save(customer);
        log.info("Updated customer id={}", saved.getId());
        return customerMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, CustomerStatus status) {
        Customer customer = getCustomerOrThrow(id);
        customer.setStatus(status);
        customerRepository.save(customer);
        log.info("Customer id={} status changed to {}", id, status);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Customer customer = getCustomerOrThrow(id);
        customerRepository.delete(customer);
        log.info("Deleted customer id={}", id);
    }

    @Override
    @Transactional
    public Customer findOrCreate(String waId, String phone, String name) {
        return customerRepository.findByWaId(waId).orElseGet(() -> {
            String normalizedPhone = PhoneNumberUtil.normalize(phone);
            Customer customer = Customer.builder()
                    .waId(waId)
                    .phone(normalizedPhone)
                    .name(name != null ? name : "Unknown")
                    .status(CustomerStatus.ACTIVE)
                    .build();
            Customer saved = customerRepository.save(customer);
            log.info("Auto-created customer from webhook: id={}, wa_id={}", saved.getId(), waId);
            return saved;
        });
    }

    private Customer getCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }
}
