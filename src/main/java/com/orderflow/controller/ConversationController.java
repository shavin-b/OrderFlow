package com.orderflow.controller;

import com.orderflow.dto.ApiResponse;
import com.orderflow.dto.ConversationDto;
import com.orderflow.dto.PagedResponse;
import com.orderflow.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for conversation management.
 */
@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Conversations", description = "Conversation management APIs")
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping
    @Operation(summary = "List all conversations (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<ConversationDto>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(conversationService.findAll(page, size)));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "List conversations for a customer")
    public ResponseEntity<ApiResponse<PagedResponse<ConversationDto>>> findByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                conversationService.findByCustomerId(customerId, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get conversation by ID")
    public ResponseEntity<ApiResponse<ConversationDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(conversationService.findById(id)));
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Close a conversation")
    public ResponseEntity<ApiResponse<ConversationDto>> close(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                conversationService.close(id), "Conversation closed successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a conversation")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        conversationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
