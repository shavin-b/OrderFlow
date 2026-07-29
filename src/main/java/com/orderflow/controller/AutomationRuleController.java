package com.orderflow.controller;

import com.orderflow.dto.ApiResponse;
import com.orderflow.dto.automation.AutomationRuleDto;
import com.orderflow.service.AutomationRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/automation/rules")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Automation Rules", description = "Keyword-based automation rule management APIs")
public class AutomationRuleController {

    private final AutomationRuleService automationRuleService;

    @GetMapping
    @Operation(summary = "List all automation rules")
    public ResponseEntity<ApiResponse<List<AutomationRuleDto>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(automationRuleService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get automation rule by ID")
    public ResponseEntity<ApiResponse<AutomationRuleDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(automationRuleService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new automation rule with keywords and replies")
    public ResponseEntity<ApiResponse<AutomationRuleDto>> create(@Valid @RequestBody AutomationRuleDto request) {
        AutomationRuleDto created = automationRuleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Automation rule created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an automation rule")
    public ResponseEntity<ApiResponse<AutomationRuleDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody AutomationRuleDto request) {
        return ResponseEntity.ok(ApiResponse.success(
                automationRuleService.update(id, request), "Automation rule updated successfully"));
    }

    @PatchMapping("/{id}/active")
    @Operation(summary = "Enable or disable an automation rule")
    public ResponseEntity<ApiResponse<Void>> toggleActive(
            @PathVariable Long id,
            @RequestParam boolean active) {
        automationRuleService.toggleActive(id, active);
        return ResponseEntity.ok(ApiResponse.success(null, "Rule status updated"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an automation rule")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        automationRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
