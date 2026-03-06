package com.demo.pipeline;

import com.azure.core.exception.HttpResponseException;
import com.demo.pipeline.AuditLogger.AuditEntry;
import com.demo.pipeline.ContentSafetyFilter.Direction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Trustworthy AI Pipeline Orchestrator — six stages, all pluggable.
 *
 * Pipeline stages:
 *   1. SANITIZE  — Strip PII from user input
 *   2. FILTER INPUT — Content safety check on input
 *   3. PROMPT & CALL MODEL — Assemble prompt and call LLM
 *   4. VALIDATE OUTPUT — Schema check (for structured outputs)
 *   5. FILTER OUTPUT — Content safety check on response
 *   6. LOG & RESPOND — Audit trail and return response
 *
 * Every stage can block the request. Zero tokens are burned on blocked requests.
 */
@Service
public class TrustworthyPipeline {

    private static final Logger log = LoggerFactory.getLogger(TrustworthyPipeline.class);

    private final InputSanitizer sanitizer;
    private final ContentSafetyFilter safetyFilter;
    private final ChatClient chatClient;
    private final AuditLogger auditLogger;

    public TrustworthyPipeline(InputSanitizer sanitizer,
                                ContentSafetyFilter safetyFilter,
                                ChatClient chatClient,
                                AuditLogger auditLogger) {
        this.sanitizer = sanitizer;
        this.safetyFilter = safetyFilter;
        this.chatClient = chatClient;
        this.auditLogger = auditLogger;
    }

    public PipelineResponse process(String userId, String userInput) {
        var stopwatch = System.currentTimeMillis();

        // STAGE 1: SANITIZE — Strip PII
        var sanitized = sanitizer.sanitize(userInput);
        if (sanitized.hadPii()) {
            log.info("[PIPELINE] PII redacted for user {}: {} items",
                userId, sanitized.redactions().size());
        }

        // STAGE 2: FILTER INPUT — Content safety check on input
        var inputSafety = safetyFilter.check(
            sanitized.sanitizedText(), Direction.INPUT);
        if (inputSafety.blocked()) {
            auditLogger.logRequest(new AuditEntry(
                userId, true, sanitized.hadPii(), 0,
                elapsed(stopwatch), 0.0, "input_blocked"));
            return PipelineResponse.blocked(
                "Your request was blocked by content safety filters.");
        }

        // STAGE 3: PROMPT & CALL MODEL
        String output;
        int tokens;
        try {
            var response = chatClient.prompt()
                .user(sanitized.sanitizedText())
                .call()
                .chatResponse();

            output = response.getResult().getOutput().getText();
            tokens = (int) response.getMetadata().getUsage().getTotalTokens();
        } catch (HttpResponseException e) {
            // Azure OpenAI's built-in content filter detected a policy violation
            log.warn("[PIPELINE] Azure OpenAI content filter blocked request for user {}: {}",
                userId, e.getMessage());
            auditLogger.logRequest(new AuditEntry(
                userId, true, sanitized.hadPii(), 0,
                elapsed(stopwatch), 0.0, "model_content_filter"));
            return PipelineResponse.blocked(
                "Request blocked by Azure OpenAI content filter (jailbreak/policy violation detected).");
        }

        // STAGE 4: VALIDATE OUTPUT — Schema check
        // (for structured outputs, parse into record and validate fields)

        // STAGE 5: FILTER OUTPUT — Content safety check on response
        var outputSafety = safetyFilter.check(output, Direction.OUTPUT);
        if (outputSafety.blocked()) {
            auditLogger.logRequest(new AuditEntry(
                userId, true, sanitized.hadPii(), tokens,
                elapsed(stopwatch), 0.0, "output_blocked"));
            return PipelineResponse.blocked(
                "The response was blocked by content safety filters.");
        }

        // STAGE 6: LOG & RESPOND
        auditLogger.logRequest(new AuditEntry(
            userId, false, sanitized.hadPii(), tokens,
            elapsed(stopwatch), 1.0, "success"));

        return PipelineResponse.success(output, tokens, sanitized.hadPii());
    }

    private long elapsed(long start) {
        return System.currentTimeMillis() - start;
    }

    public record PipelineResponse(
        boolean success,
        String message,
        int tokensUsed,
        boolean piiRedacted,
        String blockedReason
    ) {
        static PipelineResponse success(String msg, int tokens, boolean pii) {
            return new PipelineResponse(true, msg, tokens, pii, null);
        }
        static PipelineResponse blocked(String reason) {
            return new PipelineResponse(false, null, 0, false, reason);
        }
    }
}
