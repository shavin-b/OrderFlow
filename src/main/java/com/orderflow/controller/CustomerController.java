package com.orderflow.controller;

import com.orderflow.dto.ApiResponse;
import com.orderflow.dto.CustomerDto;
import com.orderflow.dto.PagedResponse;
import com.orderflow.dto.request.CreateCustomerRequest;
import com.orderflow.dto.request.UpdateCustomerRequest;
import com.orderflow.entity.Customer.CustomerStatus;
import com.orderflow.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for customer management.
 */
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customers", description = "Customer management APIs")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @Operation(summary = "List all customers (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<CustomerDto>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(customerService.findAll(page, size)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search customers by name, phone, or email")
    public ResponseEntity<ApiResponse<PagedResponse<CustomerDto>>> search(
            @Parameter(description = "Search keyword") @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(customerService.search(q, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<ApiResponse<CustomerDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.findById(id)));
    }

    @GetMapping("/wa/{waId}")
    @Operation(summary = "Get customer by WhatsApp ID")
    public ResponseEntity<ApiResponse<CustomerDto>> findByWaId(@PathVariable String waId) {
        return ResponseEntity.ok(ApiResponse.success(customerService.findByWaId(waId)));
    }

    @PostMapping
    @Operation(summary = "Create a new customer")
    public ResponseEntity<ApiResponse<CustomerDto>> create(
            @Valid @RequestBody CreateCustomerRequest request) {
        CustomerDto created = customerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Customer created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing customer")
    public ResponseEntity<ApiResponse<CustomerDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                customerService.update(id, request), "Customer updated successfully"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update customer status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long id,
            @RequestParam CustomerStatus status) {
        customerService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(null, "Status updated"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a customer")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
