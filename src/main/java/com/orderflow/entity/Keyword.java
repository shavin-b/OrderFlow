package com.orderflow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Keyword pattern used to trigger an {@link AutomationRule}.
 */
@Entity
@Table(
    name = "keywords",
    indexes = {
        @Index(name = "idx_keywords_rule", columnList = "automation_rule_id")
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
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "automation_rule_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_keywords_automation_rule"))
    private AutomationRule automationRule;

    @Column(name = "pattern", nullable = false, length = 255)
    private String pattern;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", nullable = false, length = 20)
    @Builder.Default
    private MatchType matchType = MatchType.CONTAINS;

    @Column(name = "ignore_case", nullable = false)
    @Builder.Default
    private Boolean ignoreCase = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum MatchType {
        CONTAINS, EXACT, STARTS_WITH, ENDS_WITH, REGEX
    }
}
