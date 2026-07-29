package com.orderflow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Represents a media attachment associated with a WhatsApp message.
 */
@Entity
@Table(
    name = "attachments",
    indexes = {
        @Index(name = "idx_attachments_message",  columnList = "message_id"),
        @Index(name = "idx_attachments_media_id", columnList = "media_id")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "message")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_attachments_message"))
    private Message message;

    @Column(name = "media_id", nullable = false, length = 100)
    private String mediaId;

    @Column(name = "media_url", columnDefinition = "TEXT")
    private String mediaUrl;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "sha256", length = 100)
    private String sha256;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "caption", columnDefinition = "TEXT")
    private String caption;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
