package com.orderflow.service;

/**
 * Service contract for outbound WhatsApp Cloud API messaging.
 */
public interface WhatsAppService {

    /**
     * Sends a plain text message.
     *
     * @param to   recipient phone number in E.164 format (without +)
     * @param body message text (max 4096 chars)
     * @return the WhatsApp message ID from the API response
     */
    String sendTextMessage(String to, String body);

    /**
     * Sends a template message.
     *
     * @param to           recipient phone number
     * @param templateName approved template name
     * @param languageCode language code (e.g. "en_US")
     * @param components   JSON string of template components
     * @return the WhatsApp message ID from the API response
     */
    String sendTemplateMessage(String to, String templateName, String languageCode, Object components);

    /**
     * Marks a received message as "read" in WhatsApp.
     *
     * @param waMessageId the WhatsApp message ID to mark as read
     */
    void markMessageAsRead(String waMessageId);
}
