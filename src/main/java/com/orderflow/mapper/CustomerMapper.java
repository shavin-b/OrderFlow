package com.orderflow.mapper;

import com.orderflow.dto.CustomerDto;
import com.orderflow.dto.request.CreateCustomerRequest;
import com.orderflow.entity.Customer;
import org.mapstruct.*;

/**
 * MapStruct mapper between {@link Customer} entity and its DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerMapper {

    CustomerDto toDto(Customer customer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "conversations", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    Customer toEntity(CreateCustomerRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "waId", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "conversations", ignore = true)
    void updateEntityFromRequest(com.orderflow.dto.request.UpdateCustomerRequest request,
                                  @MappingTarget Customer customer);
}
