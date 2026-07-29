package com.orderflow.mapper;

import com.orderflow.dto.AttachmentDto;
import com.orderflow.entity.Attachment;
import org.mapstruct.*;

/**
 * MapStruct mapper between {@link Attachment} entity and its DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AttachmentMapper {

    @Mapping(source = "message.id", target = "messageId")
    AttachmentDto toDto(Attachment attachment);
}
