package com.orderflow.service.impl;

import com.orderflow.dto.webhook.*;
import com.orderflow.entity.Attachment;
import com.orderflow.entity.Conversation;
import com.orderflow.entity.Customer;
import com.orderflow.entity.Message;
import com.orderflow.entity.Message.MessageStatus;
import com.orderflow.entity.Message.MessageType;
import com.orderflow.repository.AttachmentRepository;
import com.orderflow.service.ConversationService;
import com.orderflow.service.CustomerService;
import com.orderflow.service.MessageService;
import com.orderflow.service.WebhookProcessorService;
import com.orderflow.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Processes incoming WhatsApp webhook payloads.
 *
 * <p>Handles all message types: text, image, audio, video, document, sticker, interactive.
 * Also handles delivery/read status updates.
 *
 * <p>Processing is asynchronous: the webhook endpoint returns 200 OK immediately
 * and this service processes events in the background.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookProcessorServiceImpl implements WebhookProcessorService {

    private final CustomerService customerService;
    private final ConversationService conversationService;
    private final MessageService messageService;
    private final AttachmentRepository attachmentRepository;
    private final com.orderflow.service.AutomationEngineService automationEngineService;

    @Override
    @Async
    public void process(WebhookPayload payload) {
        if (payload == null || CollectionUtils.isEmpty(payload.getEntry())) {
            log.warn("Received empty webhook payload");
            return;
        }

        for (WebhookEntry entry : payload.getEntry()) {
            if (CollectionUtils.isEmpty(entry.getChanges())) continue;
            for (WebhookChange change : entry.getChanges()) {
                processChange(change);
            }
        }
    }

    private void processChange(WebhookChange change) {
        if (!"messages".equals(change.getField())) {
            log.debug("Ignoring webhook field: {}", change.getField());
            return;
        }

        WebhookValue value = change.getValue();
        if (value == null) return;

        // Process inbound messages
        if (!CollectionUtils.isEmpty(value.getMessages())) {
            List<WaContact> contacts = value.getContacts();
            for (WaMessage waMessage : value.getMessages()) {
                processInboundMessage(waMessage, contacts);
            }
        }

        // Process delivery/read statuses
        if (!CollectionUtils.isEmpty(value.getStatuses())) {
            for (WaStatus status : value.getStatuses()) {
                processStatusUpdate(status);
            }
        }
    }

    @Transactional
    protected void processInboundMessage(WaMessage waMessage, List<WaContact> contacts) {
        try {
            String waId = waMessage.getFrom();
            String phone = "+" + waId;
            String name = extractContactName(contacts, waId);

            Customer customer = customerService.findOrCreate(waId, phone, name);
            Conversation conversation = conversationService.findOrCreateOpenConversation(customer);

            MessageType type = resolveMessageType(waMessage.getType());
            String body = extractBody(waMessage, type);

            Message message = messageService.saveInboundMessage(
                    conversation,
                    waMessage.getId(),
                    type,
                    body,
                    DateTimeUtil.fromEpochString(waMessage.getTimestamp())
            );

            // Persist attachment metadata if present
            saveAttachmentIfPresent(waMessage, message);

            // Trigger automation engine evaluation
            automationEngineService.processInboundMessage(conversation, message);

        } catch (Exception e) {
            log.error("Failed to process inbound message id={}: {}", waMessage.getId(), e.getMessage(), e);
        }
    }

    @Transactional
    protected void processStatusUpdate(WaStatus waStatus) {
        try {
            MessageStatus status = switch (waStatus.getStatus()) {
                case "sent"      -> MessageStatus.SENT;
                case "delivered" -> MessageStatus.DELIVERED;
                case "read"      -> MessageStatus.READ;
                case "failed"    -> MessageStatus.FAILED;
                default -> {
                    log.warn("Unknown status value: {}", waStatus.getStatus());
                    yield null;
                }
            };

            if (status != null) {
                messageService.updateStatus(waStatus.getId(), status);
            }
        } catch (Exception e) {
            log.error("Failed to process status update wa_id={}: {}", waStatus.getId(), e.getMessage(), e);
        }
    }

    private void saveAttachmentIfPresent(WaMessage waMessage, Message message) {
        WaMediaMessage media = switch (waMessage.getType()) {
            case "image"    -> waMessage.getImage();
            case "audio"    -> waMessage.getAudio();
            case "video"    -> waMessage.getVideo();
            case "document" -> waMessage.getDocument();
            case "sticker"  -> waMessage.getSticker();
            default         -> null;
        };

        if (media == null || media.getId() == null) return;

        if (attachmentRepository.existsByMediaId(media.getId())) {
            log.debug("Attachment {} already stored", media.getId());
            return;
        }

        Attachment attachment = Attachment.builder()
                .message(message)
                .mediaId(media.getId())
                .mimeType(media.getMimeType())
                .sha256(media.getSha256())
                .caption(media.getCaption())
                .fileName(media.getFilename())
                .build();

        attachmentRepository.save(attachment);
        log.debug("Saved attachment mediaId={} for message id={}", media.getId(), message.getId());
    }

    private MessageType resolveMessageType(String type) {
        if (type == null) return MessageType.UNKNOWN;
        return switch (type.toLowerCase()) {
            case "text"        -> MessageType.TEXT;
            case "image"       -> MessageType.IMAGE;
            case "audio"       -> MessageType.AUDIO;
            case "video"       -> MessageType.VIDEO;
            case "document"    -> MessageType.DOCUMENT;
            case "sticker"     -> MessageType.STICKER;
            case "interactive" -> MessageType.INTERACTIVE;
            case "location"    -> MessageType.LOCATION;
            default            -> MessageType.UNKNOWN;
        };
    }

    private String extractBody(WaMessage waMessage, MessageType type) {
        return switch (type) {
            case TEXT -> waMessage.getText() != null ? waMessage.getText().getBody() : null;
            case INTERACTIVE -> {
                WaInteractiveReply interactive = waMessage.getInteractive();
                if (interactive == null) yield null;
                if (interactive.getButtonReply() != null) {
                    yield "BUTTON:" + interactive.getButtonReply().getId()
                            + "|" + interactive.getButtonReply().getTitle();
                }
                if (interactive.getListReply() != null) {
                    yield "LIST:" + interactive.getListReply().getId()
                            + "|" + interactive.getListReply().getTitle();
                }
                yield null;
            }
            case IMAGE    -> waMessage.getImage() != null ? waMessage.getImage().getCaption() : null;
            case VIDEO    -> waMessage.getVideo() != null ? waMessage.getVideo().getCaption() : null;
            case DOCUMENT -> waMessage.getDocument() != null ? waMessage.getDocument().getCaption() : null;
            default       -> null;
        };
    }

    private String extractContactName(List<WaContact> contacts, String waId) {
        if (CollectionUtils.isEmpty(contacts)) return "Unknown";
        return contacts.stream()
                .filter(c -> waId.equals(c.getWaId()))
                .findFirst()
                .map(c -> c.getProfile() != null ? c.getProfile().getName() : "Unknown")
                .orElse("Unknown");
    }
}
