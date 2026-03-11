package com.demo.langchain4j.moderate;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.moderation.Moderation;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.model.output.Response;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A simple keyword/pattern-based ModerationModel that demonstrates
 * LangChain4j's @Moderate annotation without requiring any external API.
 *
 * In production you would swap this for a real moderation service
 * (Azure AI Content Safety, OpenAI Moderation, etc.).
 */
public class KeywordModerationModel implements ModerationModel {

    private static final Pattern HARMFUL_PATTERN = Pattern.compile(
        "\\b(weapon|bomb|explosi|kill|murder|hack into|steal|"
        + "drug|narcotic|illegal substance|"
        + "hate|slur|racial|discriminat)\\b",
        Pattern.CASE_INSENSITIVE
    );

    @Override
    public Response<Moderation> moderate(String text) {
        System.out.println("  [@Moderate] Scanning input for harmful keywords...");
        System.out.println("  [@Moderate] Text: \"" + truncate(text, 60) + "\"");

        var matcher = HARMFUL_PATTERN.matcher(text);
        if (matcher.find()) {
            String matched = matcher.group();
            System.out.println("  [@Moderate] >>> FLAGGED — matched keyword: \"" + matched + "\"");
            return Response.from(Moderation.flagged(
                "Keyword moderation triggered on: \"" + matched + "\""));
        }

        System.out.println("  [@Moderate] >>> PASSED — no harmful keywords found");
        return Response.from(Moderation.notFlagged());
    }

    @Override
    public Response<Moderation> moderate(List<ChatMessage> messages) {
        String combined = messages.stream()
            .map(Object::toString)
            .collect(Collectors.joining("\n"));
        return moderate(combined);
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
