package com.demo.pipeline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demo 2 — End-to-End Trustworthy AI Pipeline
 *
 * A Spring Boot application demonstrating a six-stage AI safety pipeline:
 * Sanitize → Filter Input → Prompt & Call → Validate → Filter Output → Log & Respond
 *
 * Prerequisites:
 *   - Set AZURE_OPENAI_ENDPOINT, AZURE_OPENAI_API_KEY environment variables
 *   - Optional: Set APPLICATIONINSIGHTS_CONNECTION_STRING for observability
 *
 * Run:
 *   mvn spring-boot:run -pl demo2-pipeline
 *
 * Test endpoints: see ChatController for curl examples.
 */
@SpringBootApplication
public class Demo2PipelineApp {

    public static void main(String[] args) {
        SpringApplication.run(Demo2PipelineApp.class, args);
    }
}
