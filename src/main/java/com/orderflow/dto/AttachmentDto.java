package com.orderflow.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for {@link com.orderflow.entity.Attachment}.
 */
@Getter
@Builder
public class AttachmentDto {

    private Long id;
    private Long messageId;
    private String mediaId;
    private String mediaUrl;
    private String mimeType;
    private String sha256;
    private Long fileSize;
    private String fileName;
    private String caption;
    private LocalDateTime createdAt;
}
