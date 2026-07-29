package com.orderflow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Represents an automated reply message associated with an {@link AutomationRule}.
 */
@Entity
@Table(
    name = "replies",
    indexes = {
        @Index(name = "idx_replies_rule_order", columnList = "automation_rule_id, reply_order ASC")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "automationRule")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Reply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "automation_rule_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_replies_automation_rule"))
    private AutomationRule automationRule;

    @Column(name = "message_body", nullable = false, columnDefinition = "TEXT")
    private String messageBody;

    @Column(name = "reply_order", nullable = false)
    @Builder.Default
    private Integer replyOrder = 0;

    @Column(name = "delay_seconds", nullable = false)
    @Builder.Default
    private Integer delaySeconds = 0;

    @Column(name = "media_url", columnDefinition = "TEXT")
    private String mediaUrl;

    @Column(name = "media_type", length = 50)
    private String mediaType;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
