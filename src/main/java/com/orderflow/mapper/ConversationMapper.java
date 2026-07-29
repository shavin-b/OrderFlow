package com.orderflow.mapper;

import com.orderflow.dto.ConversationDto;
import com.orderflow.entity.Conversation;
import org.mapstruct.*;

/**
 * MapStruct mapper between {@link Conversation} entity and its DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConversationMapper {

    @Mapping(source = "customer.id",    target = "customerId")
    @Mapping(source = "customer.name",  target = "customerName")
    @Mapping(source = "customer.phone", target = "customerPhone")
    ConversationDto toDto(Conversation conversation);
}
