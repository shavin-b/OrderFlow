package com.orderflow.dto.request;

import com.orderflow.entity.Customer.CustomerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for creating a new customer.
 */
@Data
public class CreateCustomerRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{6,14}$", message = "Invalid phone number format (E.164)")
    private String phone;

    @NotBlank(message = "WhatsApp ID (wa_id) is required")
    @Size(max = 20, message = "wa_id must not exceed 20 characters")
    private String waId;

    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Email(message = "Invalid email format")
    @Size(max = 255)
    private String email;
}
