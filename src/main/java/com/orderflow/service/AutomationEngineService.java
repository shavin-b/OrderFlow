package com.orderflow.service;

import com.orderflow.dto.automation.GreetingDto;
import com.orderflow.entity.*;
import com.orderflow.entity.Message.MessageDirection;
import com.orderflow.entity.Message.MessageStatus;
import com.orderflow.entity.Message.MessageType;
import com.orderflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enterprise Automation Engine for OrderFlow.
 * Handles keyword matching, priority routing, multi-reply execution with non-blocking delays,
 * business hours away messages, greeting messages, and cooldown checks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AutomationEngineService {

    private final AutomationRuleRepository ruleRepository;
    private final KeywordMatcherService keywordMatcherService;
    private final BusinessHoursService businessHoursService;
    private final GreetingService greetingService;
    private final WhatsAppService whatsAppService;
    private final MessageRepository messageRepository;
    private final QueuedMessageRepository queuedMessageRepository;
    private final TaskScheduler taskScheduler;

    // Cooldown tracking: Map<RuleId_CustomerId, LastTriggerTime>
    private final Map<String, LocalDateTime> ruleCooldowns = new ConcurrentHashMap<>();

    /**
     * Entry point triggered upon receiving an inbound text message.
     *
     * @param conversation current conversation
     * @param inboundMessage raw message entity
     */
    @Transactional
    public void processInboundMessage(Conversation conversation, Message inboundMessage) {
        if (inboundMessage == null || inboundMessage.getBody() == null || inboundMessage.getBody().isBlank()) {
            return;
        }

        String text = inboundMessage.getBody();
        Customer customer = conversation.getCustomer();
        LocalDateTime now = LocalDateTime.now();

        // 1. Check if first message in conversation (Greeting trigger)
        long count = messageRepository.countByConversationId(conversation.getId());
        if (count <= 1) { // includes the inbound message just stored
            Optional<GreetingDto> greeting = greetingService.findActiveGreeting();
            if (greeting.isPresent()) {
                log.info("Sending greeting message to customer {}", customer.getPhone());
                sendReply(conversation, customer.getPhone(), greeting.get().getMessageBody(), 0);
            }
        }

        // 2. Check Business Hours (Away message)
        if (!businessHoursService.isWithinBusinessHours(now)) {
            Optional<String> awayMsg = businessHoursService.getAwayMessage(now);
            if (awayMsg.isPresent()) {
                log.info("Outside business hours. Sending away message to {}", customer.getPhone());
                sendReply(conversation, customer.getPhone(), awayMsg.get(), 0);
                return;
            }
        }

        // 3. Find matching rules sorted by priority (Highest priority first)
        List<AutomationRule> activeRules = ruleRepository.findAllActiveWithKeywordsAndReplies();

        for (AutomationRule rule : activeRules) {
            if (isCooldownActive(rule, customer, now)) {
                log.debug("Rule '{}' (id={}) is on cooldown for customer {}", rule.getName(), rule.getId(), customer.getId());
                continue;
            }

            boolean matched = rule.getKeywords().stream()
                    .anyMatch(keyword -> keywordMatcherService.matches(text, keyword));

            if (matched) {
                log.info("Message matched automation rule '{}' (id={}, priority={})", rule.getName(), rule.getId(), rule.getPriority());

                // Update cooldown & trigger count
                recordRuleTrigger(rule, customer, now);

                // Execute all configured replies in order with non-blocking delays
                executeReplies(conversation, customer.getPhone(), rule.getReplies());
                break; // Stop after highest priority rule matches
            }
        }
    }

    private boolean isCooldownActive(AutomationRule rule, Customer customer, LocalDateTime now) {
        if (rule.getCooldownSeconds() == null || rule.getCooldownSeconds() <= 0) {
            return false;
        }
        String key = rule.getId() + "_" + customer.getId();
        LocalDateTime lastTrigger = ruleCooldowns.get(key);
        if (lastTrigger == null) {
            return false;
        }
        return lastTrigger.plusSeconds(rule.getCooldownSeconds()).isAfter(now);
    }

    private void recordRuleTrigger(AutomationRule rule, Customer customer, LocalDateTime now) {
        if (rule.getCooldownSeconds() != null && rule.getCooldownSeconds() > 0) {
            String key = rule.getId() + "_" + customer.getId();
            ruleCooldowns.put(key, now);
        }
        rule.setTriggerCount(rule.getTriggerCount() + 1);
        ruleRepository.save(rule);
    }

    private void executeReplies(Conversation conversation, String recipientPhone, List<Reply> replies) {
        if (replies == null || replies.isEmpty()) {
            return;
        }

        for (Reply reply : replies) {
            int delaySeconds = reply.getDelaySeconds() != null ? reply.getDelaySeconds() : 0;
            scheduleOrSendReply(conversation, recipientPhone, reply.getMessageBody(), delaySeconds);
        }
    }

    private void scheduleOrSendReply(Conversation conversation, String recipientPhone, String body, int delaySeconds) {
        if (delaySeconds <= 0) {
            sendReply(conversation, recipientPhone, body, 0);
        } else {
            // Queue & non-blocking schedule via Spring TaskScheduler (no Thread.sleep!)
            Instant executeTime = Instant.now().plusSeconds(delaySeconds);
            LocalDateTime scheduledAt = LocalDateTime.ofInstant(executeTime, ZoneOffset.UTC);

            QueuedMessage queued = QueuedMessage.builder()
                    .conversation(conversation)
                    .recipientPhone(recipientPhone)
                    .messageBody(body)
                    .status(QueuedMessage.QueueStatus.PENDING)
                    .scheduledAt(scheduledAt)
                    .idempotencyKey("rule_" + conversation.getId() + "_" + System.nanoTime())
                    .build();

            queuedMessageRepository.save(queued);

            taskScheduler.schedule(() -> {
                try {
                    sendReply(conversation, recipientPhone, body, delaySeconds);
                    queued.setStatus(QueuedMessage.QueueStatus.SENT);
                    queued.setProcessedAt(LocalDateTime.now());
                    queuedMessageRepository.save(queued);
                } catch (Exception e) {
                    log.error("Error sending scheduled reply to {}: {}", recipientPhone, e.getMessage(), e);
                    queued.setStatus(QueuedMessage.QueueStatus.FAILED);
                    queued.setErrorMessage(e.getMessage());
                    queuedMessageRepository.save(queued);
                }
            }, executeTime);
        }
    }

    private void sendReply(Conversation conversation, String recipientPhone, String body, int delaySeconds) {
        try {
            String waMessageId = whatsAppService.sendTextMessage(recipientPhone, body);

            Message outboundMessage = Message.builder()
                    .conversation(conversation)
                    .waMessageId(waMessageId)
                    .direction(MessageDirection.OUTBOUND)
                    .type(MessageType.TEXT)
                    .body(body)
                    .status(MessageStatus.SENT)
                    .timestamp(LocalDateTime.now())
                    .build();

            messageRepository.save(outboundMessage);
            log.info("Automated reply sent to {}: wa_message_id={}", recipientPhone, waMessageId);
        } catch (Exception e) {
            log.error("Failed to send automated reply to {}: {}", recipientPhone, e.getMessage(), e);
        }
    }
}
