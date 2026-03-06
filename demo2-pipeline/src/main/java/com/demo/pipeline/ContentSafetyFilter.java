package com.demo.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.moderation.ModerationPrompt;
import org.springframework.ai.moderation.ModerationResponse;
import org.springframework.ai.moderation.ModerationModel;
import org.springframework.stereotype.Component;

/**
 * Content Safety Filter — Stages 2 & 5 of the Trustworthy AI Pipeline.
 *
 * Checks both input AND output for content safety violations.
 * Even if a prompt injection gets past the system prompt,
 * the content safety filter catches harmful output before it reaches the user.
 */
@Component
public class ContentSafetyFilter {

    private static final Logger log = LoggerFactory.getLogger(ContentSafetyFilter.class);

    private final ModerationModel moderationModel;

    // Block thresholds — configurable per deployment
    private static final double VIOLENCE_THRESHOLD = 0.3;
    private static final double HATE_THRESHOLD = 0.3;
    private static final double SEXUAL_THRESHOLD = 0.3;
    private static final double SELF_HARM_THRESHOLD = 0.3;

    public ContentSafetyFilter(ModerationModel moderationModel) {
        this.moderationModel = moderationModel;
    }

    /**
     * Check both input and output for content safety violations.
     */
    public SafetyResult check(String text, Direction direction) {
        ModerationResponse response = moderationModel.call(
            new ModerationPrompt(text));

        var result = response.getResult();
        var categories = result.getCategories();

        boolean blocked = false;
        var violations = new ArrayList<String>();

        if (categories.getViolence() > VIOLENCE_THRESHOLD) {
            violations.add("violence: " + categories.getViolence());
            blocked = true;
        }
        if (categories.getHate() > HATE_THRESHOLD) {
            violations.add("hate: " + categories.getHate());
            blocked = true;
        }
        if (categories.getSexual() > SEXUAL_THRESHOLD) {
            violations.add("sexual: " + categories.getSexual());
            blocked = true;
        }
        if (categories.getSelfHarm() > SELF_HARM_THRESHOLD) {
            violations.add("self-harm: " + categories.getSelfHarm());
            blocked = true;
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
