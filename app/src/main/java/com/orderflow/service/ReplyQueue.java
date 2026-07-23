package com.orderflow.service;

import com.orderflow.data.model.MessageLog;

/**
 * REPLY QUEUE (Singleton)
 *
 * Purpose:
 * Safely passes data between the Notification Listener and the Accessibility Service.
 * Because these are two separate Android System Services running asynchronously,
 * we use a singleton pattern to temporarily hold the generated reply text while
 * WhatsApp is opening on the screen.
 */
public class ReplyQueue {

    private static ReplyQueue instance;

    /**
     * Inner class representing a pending reply task.
     */
    public static class ReplyTask {
        public final String replyContent;
        public final MessageLog partialLog;

        public ReplyTask(String replyContent, MessageLog partialLog) {
            this.replyContent = replyContent;
            this.partialLog = partialLog; // We hold the log so the AccessibilityService can finish saving it
        }
    }

    private ReplyTask pendingTask;

    private ReplyQueue() {}

    public static synchronized ReplyQueue getInstance() {
        if (instance == null) {
            instance = new ReplyQueue();
        }
        return instance;
    }

    /**
     * Pushes a new reply task into the queue.
     */
    public synchronized void setPendingTask(ReplyTask task) {
        this.pendingTask = task;
    }

    /**
     * Retrieves the pending task.
     */
    public synchronized ReplyTask getPendingTask() {
        return pendingTask;
    }

    /**
     * Clears the queue after the reply is sent or if an error occurs.
     */
    public synchronized void clear() {
        this.pendingTask = null;
    }
}
