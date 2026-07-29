package com.orderflow.controller;

import com.orderflow.dto.ApiResponse;
import com.orderflow.dto.MessageDto;
import com.orderflow.dto.PagedResponse;
import com.orderflow.dto.request.SendTextMessageRequest;
import com.orderflow.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for message operations.
 */
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Messages", description = "Message management and sending APIs")
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/conversation/{conversationId}")
    @Operation(summary = "List messages in a conversation (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<MessageDto>>> findByConversation(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                messageService.findByConversationId(conversationId, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get message by ID")
    public ResponseEntity<ApiResponse<MessageDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(messageService.findById(id)));
    }

    @PostMapping("/conversation/{conversationId}/send-text")
    @Operation(summary = "Send a text message in a conversation")
    public ResponseEntity<ApiResponse<MessageDto>> sendText(
            @PathVariable Long conversationId,
            @Valid @RequestBody SendTextMessageRequest request) {
        MessageDto sent = messageService.sendText(conversationId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(sent, "Message sent successfully"));
    }
}
