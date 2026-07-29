package com.orderflow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single WhatsApp message (inbound or outbound) in a conversation.
 */
@Entity
@Table(
    name = "messages",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_messages_wa_id", columnNames = "wa_message_id")
    },
    indexes = {
        @Index(name = "idx_messages_conversation", columnList = "conversation_id"),
        @Index(name = "idx_messages_wa_id",        columnList = "wa_message_id"),
        @Index(name = "idx_messages_direction",    columnList = "direction"),
        @Index(name = "idx_messages_status",       columnList = "status"),
        @Index(name = "idx_messages_timestamp",    columnList = "timestamp")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"conversation", "attachments"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_messages_conversation"))
    private Conversation conversation;

    @Column(name = "wa_message_id", length = 100)
    private String waMessageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 10)
    private MessageDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private MessageType type;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private MessageStatus status = MessageStatus.RECEIVED;

    @Column(name = "timestamp", nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();

    public enum MessageDirection {
        INBOUND, OUTBOUND
    }

    public enum MessageType {
        TEXT, IMAGE, DOCUMENT, VIDEO, AUDIO, INTERACTIVE, TEMPLATE, STICKER, LOCATION, UNKNOWN
    }

    public enum MessageStatus {
        SENT, DELIVERED, READ, FAILED, PENDING, RECEIVED
    }
}
