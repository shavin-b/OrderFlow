package com.orderflow.mapper;

import com.orderflow.dto.auth.SubscriptionDto;
import com.orderflow.entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "isActive", expression = "java(entity.isActiveOrTrial())")
    SubscriptionDto toDto(Subscription entity);
}
