package com.orderflow.mapper;

import com.orderflow.dto.automation.BusinessHoursDto;
import com.orderflow.dto.automation.GreetingDto;
import com.orderflow.entity.BusinessHours;
import com.orderflow.entity.Greeting;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BusinessHoursMapper {

    BusinessHoursDto toDto(BusinessHours entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BusinessHours toEntity(BusinessHoursDto dto);

    GreetingDto toDto(Greeting entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Greeting toEntity(GreetingDto dto);
}
