package com.orderflow.service;

import com.orderflow.dto.automation.GreetingDto;
import com.orderflow.entity.Greeting;
import com.orderflow.exception.ResourceNotFoundException;
import com.orderflow.mapper.BusinessHoursMapper;
import com.orderflow.repository.GreetingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GreetingService {

    private final GreetingRepository greetingRepository;
    private final BusinessHoursMapper businessHoursMapper;

    public List<GreetingDto> findAll() {
        return greetingRepository.findAll().stream()
                .map(businessHoursMapper::toDto)
                .toList();
    }

    public Optional<GreetingDto> findActiveGreeting() {
        return greetingRepository.findFirstByActiveTrueOrderByIdAsc()
                .map(businessHoursMapper::toDto);
    }

    @Transactional
    public GreetingDto create(GreetingDto dto) {
        Greeting greeting = businessHoursMapper.toEntity(dto);
        Greeting saved = greetingRepository.save(greeting);
        return businessHoursMapper.toDto(saved);
    }

    @Transactional
    public GreetingDto update(Long id, GreetingDto dto) {
        Greeting existing = greetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Greeting", "id", id));
        existing.setName(dto.getName());
        existing.setMessageBody(dto.getMessageBody());
        existing.setActive(dto.getActive());
        existing.setMediaUrl(dto.getMediaUrl());
        existing.setMediaType(dto.getMediaType());
        return businessHoursMapper.toDto(greetingRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        greetingRepository.deleteById(id);
    }
}
