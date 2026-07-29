package com.orderflow.service;

import com.orderflow.dto.automation.BusinessHoursDto;
import com.orderflow.entity.BusinessHours;
import com.orderflow.mapper.BusinessHoursMapper;
import com.orderflow.repository.BusinessHoursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing business hours and checking open/closed status.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BusinessHoursService {

    private final BusinessHoursRepository businessHoursRepository;
    private final BusinessHoursMapper businessHoursMapper;

    public List<BusinessHoursDto> findAll() {
        return businessHoursRepository.findAll().stream()
                .map(businessHoursMapper::toDto)
                .toList();
    }

    @Transactional
    public BusinessHoursDto saveOrUpdate(BusinessHoursDto dto) {
        Optional<BusinessHours> existing = businessHoursRepository.findByDayOfWeek(dto.getDayOfWeek());
        BusinessHours entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.setStartTime(dto.getStartTime());
            entity.setEndTime(dto.getEndTime());
            entity.setTimezone(dto.getTimezone());
            entity.setEnabled(dto.getEnabled());
            entity.setAwayMessage(dto.getAwayMessage());
        } else {
            entity = businessHoursMapper.toEntity(dto);
        }
        BusinessHours saved = businessHoursRepository.save(entity);
        return businessHoursMapper.toDto(saved);
    }

    /**
     * Checks if the given timestamp is within configured business hours.
     *
     * @param dateTime local timestamp to test
     * @return true if business hours are enabled and current time is within bounds
     */
    public boolean isWithinBusinessHours(LocalDateTime dateTime) {
        DayOfWeek day = dateTime.getDayOfWeek();
        Optional<BusinessHours> optHours = businessHoursRepository.findByDayOfWeek(day);
        if (optHours.isEmpty()) {
            return true; // Default open if not configured
        }

        BusinessHours hours = optHours.get();
        if (!Boolean.TRUE.equals(hours.getEnabled())) {
            return true; // Open if rule is disabled
        }

        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(hours.getTimezone());
        } catch (Exception e) {
            zoneId = ZoneId.of("UTC");
        }

        ZonedDateTime zdt = dateTime.atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId);
        LocalTime time = zdt.toLocalTime();

        return !time.isBefore(hours.getStartTime()) && !time.isAfter(hours.getEndTime());
    }

    /**
     * Gets the current away message for out-of-hours responses.
     */
    public Optional<String> getAwayMessage(LocalDateTime dateTime) {
        DayOfWeek day = dateTime.getDayOfWeek();
        return businessHoursRepository.findByDayOfWeek(day)
                .filter(bh -> Boolean.TRUE.equals(bh.getEnabled()))
                .map(BusinessHours::getAwayMessage)
                .filter(msg -> msg != null && !msg.isBlank());
    }
}
