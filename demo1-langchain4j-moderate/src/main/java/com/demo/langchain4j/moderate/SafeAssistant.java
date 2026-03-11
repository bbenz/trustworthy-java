package com.demo.langchain4j.moderate;

import dev.langchain4j.service.Moderate;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * AI service interface with @Moderate annotation for content moderation.
 *
 * The @Moderate annotation tells LangChain4j to:
 *   1. Check user INPUT through the ModerationModel before sending to ChatModel
 *   2. Check model OUTPUT through the ModerationModel before returning to user
 *
 * If either check flags the content, a ModerationException is thrown.
 * This uses Azure OpenAI's built-in moderation endpoint (not Azure AI Content Safety).
 */
public interface SafeAssistant {

    @SystemMessage("You are a helpful assistant.")
    @Moderate  // Checks input + output via ModerationModel
    String chat(@UserMessage String message);
}
