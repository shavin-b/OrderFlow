package com.orderflow.service;

import com.orderflow.entity.QueuedMessage;
import com.orderflow.entity.QueuedMessage.QueueStatus;
import com.orderflow.repository.QueuedMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Background scheduler to process pending queued messages and retry failed attempts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageQueueScheduler {

    private final QueuedMessageRepository queuedMessageRepository;
    private final WhatsAppService whatsAppService;

    @Scheduled(fixedDelay = 10000) // Poll every 10 seconds
    @Transactional
    public void processQueue() {
        LocalDateTime now = LocalDateTime.now();
        List<QueuedMessage> pendingList = queuedMessageRepository.findPendingMessagesToProcess(
                QueueStatus.PENDING, now, PageRequest.of(0, 50));

        if (pendingList.isEmpty()) {
            return;
        }

        log.info("Processing {} queued messages", pendingList.size());

        for (QueuedMessage item : pendingList) {
            try {
                item.setStatus(QueueStatus.PROCESSING);
                whatsAppService.sendTextMessage(item.getRecipientPhone(), item.getMessageBody());
                item.setStatus(QueueStatus.SENT);
                item.setProcessedAt(LocalDateTime.now());
                log.info("Successfully processed queued message id={}", item.getId());
            } catch (Exception e) {
                int retries = item.getRetryCount() + 1;
                item.setRetryCount(retries);
                item.setErrorMessage(e.getMessage());
                if (retries >= item.getMaxRetries()) {
                    item.setStatus(QueueStatus.FAILED);
                    log.error("Queued message id={} failed permanently after {} retries", item.getId(), retries);
                } else {
                    item.setStatus(QueueStatus.PENDING);
                    item.setScheduledAt(LocalDateTime.now().plusSeconds( retries * 15L )); // Exponential backoff
                    log.warn("Queued message id={} failed (retry {}/{}): {}", item.getId(), retries, item.getMaxRetries(), e.getMessage());
                }
            }
            queuedMessageRepository.save(item);
        }
    }
}
