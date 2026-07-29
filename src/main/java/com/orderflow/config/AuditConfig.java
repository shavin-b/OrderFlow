package com.orderflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing so {@code @CreatedDate} and {@code @LastModifiedDate}
 * are auto-populated on all entities.
 */
@Configuration
@EnableJpaAuditing
public class AuditConfig {
    // JPA auditing is enabled via annotation; no beans required.
}
