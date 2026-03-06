package com.demo.pipeline;

import java.util.ArrayList;
import java.util.List;

import com.azure.ai.contentsafety.ContentSafetyClient;
import com.azure.ai.contentsafety.models.AnalyzeTextOptions;
import com.azure.ai.contentsafety.models.TextCategoriesAnalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Content Safety Filter — Stages 2 & 5 of the Trustworthy AI Pipeline.
 *
 * Uses Azure AI Content Safety to check both input AND output.
 * Even if a prompt injection gets past the system prompt,
 * the content safety filter catches harmful output before it reaches the user.
 */
@Component
public class ContentSafetyFilter {

    private static final Logger log = LoggerFactory.getLogger(ContentSafetyFilter.class);

    private static final int SEVERITY_THRESHOLD = 2; // Block severity 2+ (0=safe, 2=low, 4=medium, 6=high)

    private final ContentSafetyClient contentSafetyClient;

    public ContentSafetyFilter(ContentSafetyClient contentSafetyClient) {
        this.contentSafetyClient = contentSafetyClient;
    }

    /**
     * Check both input and output for content safety violations.
     */
    public SafetyResult check(String text, Direction direction) {
        var result = contentSafetyClient.analyzeText(new AnalyzeTextOptions(text));

        boolean blocked = false;
        var violations = new ArrayList<String>();

        for (TextCategoriesAnalysis cat : result.getCategoriesAnalysis()) {
            if (cat.getSeverity() >= SEVERITY_THRESHOLD) {
                violations.add(cat.getCategory() + ": severity " + cat.getSeverity());
                blocked = true;
            }
        }

        if (blocked) {
            log.warn("[CONTENT_SAFETY] {} blocked — violations: {}",
                direction, violations);
        }

        return new SafetyResult(blocked, violations, direction);
    }

    public enum Direction { INPUT, OUTPUT }

    public record SafetyResult(
        boolean blocked,
        List<String> violations,
        Direction direction
    ) {}
}
