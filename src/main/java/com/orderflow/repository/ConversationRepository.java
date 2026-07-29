package com.orderflow.repository;

import com.orderflow.entity.Conversation;
import com.orderflow.entity.Conversation.ConversationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Page<Conversation> findByCustomerId(Long customerId, Pageable pageable);

    Page<Conversation> findByStatus(ConversationStatus status, Pageable pageable);

    @Query("""
           SELECT c FROM Conversation c
           WHERE c.customer.id = :customerId
             AND c.status = :status
           ORDER BY c.openedAt DESC
           LIMIT 1
           """)
    Optional<Conversation> findLatestByCustomerIdAndStatus(
            @Param("customerId") Long customerId,
            @Param("status") ConversationStatus status);

    long countByStatus(ConversationStatus status);
}
