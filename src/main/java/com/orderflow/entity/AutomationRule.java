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
 * Represents an automation rule with keywords, ordered replies, priority, and cooldown settings.
 */
@Entity
@Table(
    name = "automation_rules",
    indexes = {
        @Index(name = "idx_automation_rules_active_priority", columnList = "active, priority DESC")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"keywords", "replies"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AutomationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "cooldown_seconds", nullable = false)
    @Builder.Default
    private Integer cooldownSeconds = 0;

    @Column(name = "trigger_count", nullable = false)
    @Builder.Default
    private Long triggerCount = 0L;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "automationRule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Keyword> keywords = new ArrayList<>();

    @OneToMany(mappedBy = "automationRule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("replyOrder ASC")
    @Builder.Default
    private List<Reply> replies = new ArrayList<>();

    public void addKeyword(Keyword keyword) {
        keywords.add(keyword);
        keyword.setAutomationRule(this);
    }

    public void removeKeyword(Keyword keyword) {
        keywords.remove(keyword);
        keyword.setAutomationRule(null);
    }

    public void addReply(Reply reply) {
        replies.add(reply);
        reply.setAutomationRule(this);
    }

    public void removeReply(Reply reply) {
        replies.remove(reply);
        reply.setAutomationRule(null);
    }
}
