package com.orderflow.controller;

import com.orderflow.dto.ApiResponse;
import com.orderflow.dto.automation.GreetingDto;
import com.orderflow.service.GreetingService;
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
@RequestMapping("/automation/greetings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Greetings", description = "Welcome and greeting message management APIs")
public class GreetingController {

    private final GreetingService greetingService;

    @GetMapping
    @Operation(summary = "List all greeting message configurations")
    public ResponseEntity<ApiResponse<List<GreetingDto>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(greetingService.findAll()));
    }

    @PostMapping
    @Operation(summary = "Create a greeting message configuration")
    public ResponseEntity<ApiResponse<GreetingDto>> create(@Valid @RequestBody GreetingDto request) {
        GreetingDto created = greetingService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Greeting created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a greeting message configuration")
    public ResponseEntity<ApiResponse<GreetingDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody GreetingDto request) {
        return ResponseEntity.ok(ApiResponse.success(
                greetingService.update(id, request), "Greeting updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a greeting configuration")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        greetingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
