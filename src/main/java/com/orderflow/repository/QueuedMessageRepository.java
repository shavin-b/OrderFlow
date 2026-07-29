package com.orderflow.repository;

import com.orderflow.entity.QueuedMessage;
import com.orderflow.entity.QueuedMessage.QueueStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QueuedMessageRepository extends JpaRepository<QueuedMessage, Long> {

    Optional<QueuedMessage> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query("SELECT q FROM QueuedMessage q WHERE q.status = :status AND q.scheduledAt <= :now ORDER BY q.scheduledAt ASC")
    List<QueuedMessage> findPendingMessagesToProcess(@Param("status") QueueStatus status,
                                                     @Param("now") LocalDateTime now,
                                                     Pageable pageable);

    List<QueuedMessage> findByStatusAndScheduledAtBefore(QueueStatus status, LocalDateTime now);
}
