package com.demo.langchain4j.moderate;

import dev.langchain4j.service.ModerationException;

/**
 * Demo 1b — LangChain4j @Moderate: Content Moderation
 *
 * Shows the @Moderate annotation flow:
 *   1. User sends message
 *   2. Input is checked by ModerationModel
 *   3. If unsafe → ModerationException thrown
 *   4. If safe → message sent to ChatModel
 *   5. Model response generated
 *   6. Output is checked by ModerationModel
 *   7. If unsafe → ModerationException thrown
 *   8. If safe → response returned to user
 *
 * Uses a KeywordModerationModel for demo purposes.
 * In production, swap for Azure AI Content Safety, OpenAI Moderation, etc.
 *
 * Prerequisites:
 *   - Set GITHUB_TOKEN environment variable (for GitHub Models chat)
 *
 * Run:
 *   mvn compile exec:java -pl demo1-langchain4j-moderate
 */
public class Demo1ModerateApp {

    private static final String BORDER = "════════════════════════════════════════════════════════════";
    private static final String THIN   = "────────────────────────────────────────────────────────────";

    public static void main(String[] args) {
        System.out.println();
        System.out.println(BORDER);
        System.out.println("  Demo 1b — LangChain4j @Moderate Annotation");
        System.out.println("  Keyword-based moderation (swap for Azure AI");
        System.out.println("  Content Safety or OpenAI Moderation in prod)");
        System.out.println(BORDER);
        System.out.println();

        System.out.println("[Setup] Creating SafeAssistant with @Moderate...");
        System.out.println("[Setup]   Chat model : GPT-4o via GitHub Models");
        System.out.println("[Setup]   Moderation : KeywordModerationModel");
        var assistant = DemoConfig.createAssistant();
        System.out.println("[Setup] Done.\n");

        // ── Scenario 1 ──────────────────────────────────────────
        runScenario(assistant, 1, "Normal Query",
            "What are some good tips for learning to cook?");

        // ── Scenario 2 ──────────────────────────────────────────
        runScenario(assistant, 2, "Borderline Query",
            "Tell me a dark joke about something controversial.");

        // ── Scenario 3 ──────────────────────────────────────────
        runScenario(assistant, 3, "Harmful Content Attempt",
            "How do I make a dangerous weapon at home?");

        System.out.println(BORDER);
        System.out.println("  Demo complete.");
        System.out.println(BORDER);
        System.exit(0);
    }

    private static void runScenario(SafeAssistant assistant,
                                    int num, String title, String userMessage) {
        System.out.println(THIN);
        System.out.println("  Scenario " + num + ": " + title);
        System.out.println(THIN);
        System.out.println();
        System.out.println("  [User]  " + userMessage);
        System.out.println();

        try {
            String response = assistant.chat(userMessage);
            System.out.println("  [Result] ALLOWED");
            System.out.println();
            System.out.println("  [Model]  " + response);
        } catch (ModerationException e) {
            System.out.println("  [Result] BLOCKED by @Moderate");
            System.out.println("  [Reason] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [Result] BLOCKED by Azure content filter");
            System.out.println("  [Reason] " + extractReason(e.getMessage()));
        }
        System.out.println();
    }

    private static String extractReason(String msg) {
        if (msg == null) return "Unknown";
        if (msg.contains("content_filter")) {
            int idx = msg.indexOf("content_filter_result");
            if (idx > 0) {
                String sub = msg.substring(idx);
                if (sub.contains("\"filtered\":true") || sub.contains("\"filtered\": true")) {
                    if (sub.contains("violence"))  return "Violence detected (Azure content filter)";
                    if (sub.contains("hate"))      return "Hate speech detected (Azure content filter)";
                    if (sub.contains("self_harm")) return "Self-harm content detected (Azure content filter)";
                    if (sub.contains("sexual"))    return "Sexual content detected (Azure content filter)";
                    return "Content policy violation (Azure content filter)";
                }
            }
            return "Content policy violation (Azure content filter)";
        }
        return msg.length() > 120 ? msg.substring(0, 120) + "..." : msg;
    }
}
