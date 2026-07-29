package com.orderflow.service;

import com.orderflow.dto.PagedResponse;
import com.orderflow.dto.auth.AuditLogDto;
import com.orderflow.entity.AuditLog;
import com.orderflow.entity.User;
import com.orderflow.mapper.AuditLogMapper;
import com.orderflow.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Transactional
    public void log(User user, String action, String resource, String details, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action(action)
                .resource(resource)
                .details(details)
                .ipAddress(ipAddress)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);
    }

    public PagedResponse<AuditLogDto> findAll(int page, int size) {
        Page<AuditLog> pageResult = auditLogRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp")));
        return PagedResponse.from(pageResult.map(auditLogMapper::toDto));
    }

    public PagedResponse<AuditLogDto> findByUserId(Long userId, int page, int size) {
        Page<AuditLog> pageResult = auditLogRepository.findByUserId(
                userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp")));
        return PagedResponse.from(pageResult.map(auditLogMapper::toDto));
    }
}
