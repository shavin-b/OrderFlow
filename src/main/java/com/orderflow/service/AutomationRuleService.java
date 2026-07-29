package com.orderflow.service;

import com.orderflow.dto.automation.AutomationRuleDto;
import com.orderflow.entity.AutomationRule;
import com.orderflow.exception.ResourceNotFoundException;
import com.orderflow.mapper.AutomationMapper;
import com.orderflow.repository.AutomationRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AutomationRuleService {

    private final AutomationRuleRepository ruleRepository;
    private final AutomationMapper automationMapper;

    public List<AutomationRuleDto> findAll() {
        return ruleRepository.findAll().stream()
                .map(automationMapper::toDto)
                .toList();
    }

    public AutomationRuleDto findById(Long id) {
        return ruleRepository.findById(id)
                .map(automationMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("AutomationRule", "id", id));
    }

    @Transactional
    public AutomationRuleDto create(AutomationRuleDto dto) {
        AutomationRule rule = automationMapper.toEntity(dto);
        AutomationRule saved = ruleRepository.save(rule);
        log.info("Created automation rule id={}, name='{}'", saved.getId(), saved.getName());
        return automationMapper.toDto(saved);
    }

    @Transactional
    public AutomationRuleDto update(Long id, AutomationRuleDto dto) {
        AutomationRule existing = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AutomationRule", "id", id));

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPriority(dto.getPriority());
        existing.setActive(dto.getActive());
        existing.setCooldownSeconds(dto.getCooldownSeconds());

        existing.getKeywords().clear();
        if (dto.getKeywords() != null) {
            dto.getKeywords().forEach(k -> existing.addKeyword(automationMapper.toEntity(k)));
        }

        existing.getReplies().clear();
        if (dto.getReplies() != null) {
            dto.getReplies().forEach(r -> existing.addReply(automationMapper.toEntity(r)));
        }

        AutomationRule saved = ruleRepository.save(existing);
        log.info("Updated automation rule id={}", saved.getId());
        return automationMapper.toDto(saved);
    }

    @Transactional
    public void toggleActive(Long id, boolean active) {
        AutomationRule existing = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AutomationRule", "id", id));
        existing.setActive(active);
        ruleRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!ruleRepository.existsById(id)) {
            throw new ResourceNotFoundException("AutomationRule", "id", id);
        }
        ruleRepository.deleteById(id);
        log.info("Deleted automation rule id={}", id);
    }
}
