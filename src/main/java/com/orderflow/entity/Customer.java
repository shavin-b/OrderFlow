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
 * Represents a WhatsApp customer/contact in the OrderFlow system.
 */
@Entity
@Table(
    name = "customers",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_customers_wa_id",  columnNames = "wa_id"),
        @UniqueConstraint(name = "uq_customers_phone",  columnNames = "phone")
    },
    indexes = {
        @Index(name = "idx_customers_phone",   columnList = "phone"),
        @Index(name = "idx_customers_status",  columnList = "status"),
        @Index(name = "idx_customers_created", columnList = "created_at")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "conversations")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "wa_id", nullable = false, length = 20)
    private String waId;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "name", nullable = false, length = 255)
    @Builder.Default
    private String name = "Unknown";

    @Column(name = "email", length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Conversation> conversations = new ArrayList<>();

    public enum CustomerStatus {
        ACTIVE, BLOCKED, INACTIVE
    }
}
