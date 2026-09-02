package com.zestindia.productapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Fire-and-forget async audit logging so write operations are not
 * slowed down by non-critical logging/notification work.
 */
@Slf4j
@Service
public class AuditLogService {

    @Async("auditExecutor")
    public void logProductChange(String action, Integer productId, String actor) {
        // In production this would persist to an audit table / send to a message queue.
        log.info("AUDIT: action={} productId={} actor={}", action, productId, actor);
    }
}
