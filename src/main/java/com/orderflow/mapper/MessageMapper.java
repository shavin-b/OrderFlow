package com.orderflow.mapper;

import com.orderflow.dto.AttachmentDto;
import com.orderflow.dto.MessageDto;
import com.orderflow.entity.Message;
import org.mapstruct.*;

/**
 * MapStruct mapper between {@link Message} entity and its DTOs.
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {AttachmentMapper.class})
public interface MessageMapper {

    @Mapping(source = "conversation.id", target = "conversationId")
    MessageDto toDto(Message message);
}
