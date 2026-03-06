package com.demo.springai.model;

/**
 * Typed output record for product recommendations.
 *
 * Spring AI forces model output into this schema via .entity().
 * If the model hallucinates a field or skips the source citation,
 * the JSON parse will fail — catching bad data before it reaches users.
 */
public record ProductRecommendation(
    String productName,
    String description,
    double price,
    String category,
    double confidenceScore,  // How confident is the model?
    String source            // Where did this come from?
) {}
