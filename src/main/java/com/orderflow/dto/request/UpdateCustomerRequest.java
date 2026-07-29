package com.orderflow.dto.request;

import com.orderflow.entity.Customer.CustomerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for updating an existing customer.
 */
@Data
public class UpdateCustomerRequest {

    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Email(message = "Invalid email format")
    @Size(max = 255)
    private String email;

    private CustomerStatus status;
}
