package com.orderflow.repository;

import com.orderflow.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByMessageId(Long messageId);

    Optional<Attachment> findByMediaId(String mediaId);

    boolean existsByMediaId(String mediaId);
}
