package com.demo.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * PII Redaction — Stage 1 of the Trustworthy AI Pipeline.
 *
 * Strips PII from user input BEFORE it reaches the model.
 * Email addresses, SSNs, phone numbers, credit cards — all replaced with tokens.
 * The original PII never leaves your server.
 */
@Component
public class InputSanitizer {

    // Common PII patterns
    private static final Pattern EMAIL =
        Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern SSN =
        Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");
    private static final Pattern PHONE =
        Pattern.compile("\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b");
    private static final Pattern CREDIT_CARD =
        Pattern.compile("\\b\\d{4}[- ]?\\d{4}[- ]?\\d{4}[- ]?\\d{4}\\b");

    /**
     * Strips PII from user input before it reaches the model.
     * Returns both the sanitized text and a redaction report.
     */
    public SanitizationResult sanitize(String input) {
        var redactions = new ArrayList<String>();
        String sanitized = input;

        sanitized = redact(sanitized, EMAIL, "[EMAIL_REDACTED]", redactions);
        sanitized = redact(sanitized, SSN, "[SSN_REDACTED]", redactions);
        sanitized = redact(sanitized, PHONE, "[PHONE_REDACTED]", redactions);
        sanitized = redact(sanitized, CREDIT_CARD, "[CC_REDACTED]", redactions);

        return new SanitizationResult(sanitized, redactions, !redactions.isEmpty());
    }

    private String redact(String text, Pattern pattern, String replacement,
                          List<String> redactions) {
        var matcher = pattern.matcher(text);
        while (matcher.find()) {
            redactions.add(matcher.group().substring(0, 3) + "***");
        }
        return pattern.matcher(text).replaceAll(replacement);
    }

    public record SanitizationResult(
        String sanitizedText,
        List<String> redactions,
        boolean hadPii
    ) {}
}
