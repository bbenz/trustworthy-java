package com.demo.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Audit Logger — Stage 6 of the Trustworthy AI Pipeline.
 *
 * Every interaction is logged — tokens used, whether PII was detected,
 * whether content was blocked, latency, safety scores.
 * This feeds into your observability dashboard and compliance audit trail.
 */
@Component
public class AuditLogger {

    private static final Logger log = LoggerFactory.getLogger(AuditLogger.class);

    private final Counter requestCounter;
    private final Counter blockedCounter;
    private final Counter piiDetectedCounter;
    private final Counter tokenCounter;
    private final Timer responseTimer;

    public AuditLogger(MeterRegistry registry) {
        this.requestCounter = Counter.builder("ai.requests.total")
            .description("Total AI requests")
            .register(registry);
        this.blockedCounter = Counter.builder("ai.requests.blocked")
            .description("Blocked requests")
            .tag("reason", "content_safety")
            .register(registry);
        this.piiDetectedCounter = Counter.builder("ai.pii.detected")
            .description("PII detections")
            .register(registry);
        this.tokenCounter = Counter.builder("ai.tokens.total")
            .description("Token usage")
            .register(registry);
        this.responseTimer = Timer.builder("ai.response.time")
            .description("Response latency")
            .register(registry);
    }

    public void logRequest(AuditEntry entry) {
        requestCounter.increment();

        if (entry.blocked()) {
            blockedCounter.increment();
        }
        if (entry.piiDetected()) {
            piiDetectedCounter.increment();
        }
        tokenCounter.increment(entry.tokenCount());

        // Structured log for audit trail
        log.info("[AUDIT] user={} blocked={} pii={} tokens={} latency={}ms safety={}",
            entry.userId(),
            entry.blocked(),
            entry.piiDetected(),
            entry.tokenCount(),
            entry.latencyMs(),
            entry.safetyScore());
    }

    public record AuditEntry(
        String userId,
        boolean blocked,
        boolean piiDetected,
        int tokenCount,
        long latencyMs,
        double safetyScore,
        String reason
    ) {}
}
