package com.orderflow.controller;

import com.orderflow.dto.ApiResponse;
import com.orderflow.dto.automation.BusinessHoursDto;
import com.orderflow.service.BusinessHoursService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/automation/business-hours")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Business Hours", description = "Business hours and away message management APIs")
public class BusinessHoursController {

    private final BusinessHoursService businessHoursService;

    @GetMapping
    @Operation(summary = "Get all configured business hours")
    public ResponseEntity<ApiResponse<List<BusinessHoursDto>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(businessHoursService.findAll()));
    }

    @PutMapping
    @Operation(summary = "Configure or update business hours for a day of week")
    public ResponseEntity<ApiResponse<BusinessHoursDto>> saveOrUpdate(
            @Valid @RequestBody BusinessHoursDto request) {
        return ResponseEntity.ok(ApiResponse.success(
                businessHoursService.saveOrUpdate(request), "Business hours updated"));
    }
}
