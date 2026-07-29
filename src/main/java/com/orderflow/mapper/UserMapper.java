package com.orderflow.mapper;

import com.orderflow.dto.auth.UserProfileDto;
import com.orderflow.entity.Role;
import com.orderflow.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {SubscriptionMapper.class})
public interface UserMapper {

    @Mapping(target = "roles", source = "roles")
    @Mapping(target = "subscription", ignore = true)
    UserProfileDto toDto(User entity);

    default List<String> mapRoles(Set<Role> roles) {
        if (roles == null) return List.of();
        return roles.stream().map(r -> r.getName().name()).toList();
    }
}
