package com.demo.springai.service;

import com.demo.springai.model.ProductRecommendation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Product recommendation service with structured output and post-validation.
 *
 * Key safety features:
 * - .entity(ProductRecommendation.class) — typed output prevents hallucination
 * - Confidence score validation — low-confidence responses are rejected
 * - Source citation validation — responses without sources are rejected
 * - Safe fallback response for any suspicious output
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ChatClient chatClient;

    public ProductService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public ProductRecommendation recommend(String userQuery) {
        var result = chatClient.prompt()
            .user(userQuery)
            .call()
            .entity(ProductRecommendation.class);  // Typed output!

        // Post-validation: check confidence
        if (result.confidenceScore() < 0.7) {
            log.warn("Low confidence response: {}", result);
            return FALLBACK_RECOMMENDATION;
        }

        // Post-validation: verify source is not empty
        if (result.source() == null || result.source().isBlank()) {
            log.warn("No source cited — rejecting response");
            return FALLBACK_RECOMMENDATION;
        }

        return result;
    }

    private static final ProductRecommendation FALLBACK_RECOMMENDATION =
        new ProductRecommendation(
            "Please try again",
            "I wasn't able to find a confident recommendation.",
            0.0, "N/A", 0.0, "system-fallback");
}
