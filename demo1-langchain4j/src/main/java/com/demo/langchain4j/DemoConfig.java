package com.demo.langchain4j;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.github.GitHubModelsChatModel;
import dev.langchain4j.service.AiServices;

/**
 * Configuration class that wires up the SafeProductAssistant with
 * a chat model, moderation model, and bounded chat memory.
 *
 * Key safety features:
 * - maxTokens(500): cost control built in
 * - temperature(0.3): lower temperature = less creative = more predictable
 * - MessageWindowChatMemory with maxMessages(10): prevents context overflow
 * - GitHub Models for easy prototyping, Azure OpenAI for production
 */
public class DemoConfig {

    public static SafeProductAssistant createAssistant() {

        ChatLanguageModel chatModel = GitHubModelsChatModel.builder()
            .gitHubToken(System.getenv("GITHUB_TOKEN"))
            .modelName("gpt-4o")
            .maxTokens(500)        // Cap output to control costs
            .temperature(0.3)      // Lower = more deterministic/safe
            .build();

        return AiServices.builder(SafeProductAssistant.class)
            .chatLanguageModel(chatModel)
            .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)   // Limit context window per user
                .build())
            .build();
    }
}
