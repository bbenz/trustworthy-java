package com.demo.langchain4j.moderate;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

/**
 * Configuration that wires the SafeAssistant with:
 *   - A ChatModel (GitHub Models / GPT-4o via OpenAI-compatible endpoint)
 *   - A KeywordModerationModel (simple pattern-based content moderation)
 *
 * Prerequisites (env vars set before running):
 *   - GITHUB_TOKEN  — for the chat model (GitHub Models)
 */
public class DemoConfig {

    public static SafeAssistant createAssistant() {

        ChatModel chatModel = OpenAiChatModel.builder()
            .baseUrl("https://models.inference.ai.azure.com")
            .apiKey(System.getenv("GITHUB_TOKEN"))
            .modelName("gpt-4o")
            .maxTokens(500)
            .temperature(0.3)
            .build();

        return AiServices.builder(SafeAssistant.class)
            .chatModel(chatModel)
            .moderationModel(new KeywordModerationModel())
            .build();
    }
}
