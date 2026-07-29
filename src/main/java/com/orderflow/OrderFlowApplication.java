package com.orderflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * OrderFlow Backend — Spring Boot 3.x Application Entry Point.
 *
 * <p>A production-ready SaaS backend providing:
 * <ul>
 *   <li>WhatsApp Cloud API webhook ingestion and messaging</li>
 *   <li>Customer, Conversation, and Message management</li>
 *   <li>REST APIs secured with API-key authentication</li>
 *   <li>OpenAPI documentation</li>
 * </ul>
 *
 * <p>JPA auditing is enabled via {@link com.orderflow.config.AuditConfig}.
 */
@SpringBootApplication
@EnableAsync
@ConfigurationPropertiesScan
public class OrderFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderFlowApplication.class, args);
    }
}
