package com.orderflow.mapper;

import com.orderflow.dto.automation.AutomationRuleDto;
import com.orderflow.dto.automation.KeywordDto;
import com.orderflow.dto.automation.ReplyDto;
import com.orderflow.entity.AutomationRule;
import com.orderflow.entity.Keyword;
import com.orderflow.entity.Reply;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AutomationMapper {

    AutomationRuleDto toDto(AutomationRule entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "triggerCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AutomationRule toEntity(AutomationRuleDto dto);

    KeywordDto toDto(Keyword entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "automationRule", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Keyword toEntity(KeywordDto dto);

    ReplyDto toDto(Reply entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "automationRule", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Reply toEntity(ReplyDto dto);

    @AfterMapping
    default void linkAssociations(@MappingTarget AutomationRule rule) {
        if (rule.getKeywords() != null) {
            rule.getKeywords().forEach(k -> k.setAutomationRule(rule));
        }
        if (rule.getReplies() != null) {
            rule.getReplies().forEach(r -> r.setAutomationRule(rule));
        }
    }
}
