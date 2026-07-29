package com.orderflow.service;

import com.orderflow.dto.webhook.WebhookPayload;

/**
 * Service contract for processing incoming WhatsApp webhook events.
 */
public interface WebhookProcessorService {

    /**
     * Fan-out entry point: iterates entries → changes → messages/statuses.
     *
     * @param payload the deserialized root webhook payload from Meta
     */
    void process(WebhookPayload payload);
}
