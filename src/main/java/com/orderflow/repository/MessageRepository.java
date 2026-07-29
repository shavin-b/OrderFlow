package com.orderflow.repository;

import com.orderflow.entity.Message;
import com.orderflow.entity.Message.MessageDirection;
import com.orderflow.entity.Message.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationId(Long conversationId, Pageable pageable);

    Page<Message> findByConversationIdAndDirection(Long conversationId, MessageDirection direction, Pageable pageable);

    Optional<Message> findByWaMessageId(String waMessageId);

    boolean existsByWaMessageId(String waMessageId);

    @Modifying
    @Query("UPDATE Message m SET m.status = :status WHERE m.waMessageId = :waMessageId")
    int updateStatusByWaMessageId(@Param("waMessageId") String waMessageId,
                                   @Param("status") MessageStatus status);

    long countByConversationId(Long conversationId);
}
