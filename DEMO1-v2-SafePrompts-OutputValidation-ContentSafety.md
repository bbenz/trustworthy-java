# DEMO 1 (v2): Safe Prompts, Output Validation & Content Safety — Browser Walkthrough

> **Duration:** ~5 minutes  
> **Tools:** Browser only (Spring AI docs, LangChain4j docs, Microsoft Learn)  
> **No live coding — guided walkthrough of documentation and real examples**  
> **Slide Reference:** Slide 17

---

## Source Code Reference

This is a browser-walkthrough version of Demo 1. For the live-coding version with runnable source code, see:
- **Live-coding script:** [DEMO1-SafePrompts-OutputValidation-ContentSafety.md](DEMO1-SafePrompts-OutputValidation-ContentSafety.md)
- **LangChain4j source:** `demo1-langchain4j/src/main/java/com/demo/langchain4j/`
- **Spring AI source:** `demo1-springai/src/main/java/com/demo/springai/`

---

## Pre-Demo Setup

### Tabs to Have Open (in order)

1. **LangChain4j AI Services** — <https://docs.langchain4j.dev/tutorials/ai-services>
2. **LangChain4j Content Moderation** — <https://docs.langchain4j.dev/tutorials/ai-services#moderation>
3. **LangChain4j Azure OpenAI Integration** — <https://docs.langchain4j.dev/integrations/language-models/azure-open-ai>
4. **Spring AI Advisors** — <https://docs.spring.io/spring-ai/reference/api/advisors.html>
5. **Spring AI Structured Output** — <https://docs.spring.io/spring-ai/reference/api/structured-output-converter.html>
6. **Spring AI Azure OpenAI** — <https://docs.spring.io/spring-ai/reference/api/chat/azure-openai-chat.html>
7. **Azure AI Content Safety overview** — <https://learn.microsoft.com/azure/ai-services/content-safety/overview>
8. **Azure AI Content Safety Studio** — <https://contentsafety.cognitive.azure.com/>

### Tips

- Use browser zoom (Ctrl +) to make text readable from the back of the room
- Bookmark all tabs in a "Demo 1" folder for quick access
- Practice the tab order so transitions feel natural

---

## Part A: LangChain4j — Safe AI Services (2.5 min)

### Step 1: Show the AI Services Doc Page

**Tab:** <https://docs.langchain4j.dev/tutorials/ai-services>

Scroll to the `@SystemMessage` annotation section. Highlight:

```java
@SystemMessage("You are a polite assistant")
String chat(String userMessage);
```

> **Say:** "LangChain4j uses a declarative approach — your system prompt is an annotation, completely separated from user input. There's no string concatenation anywhere. This is your first defense against prompt injection — the framework assembles the prompt correctly at the API level."

Point out on the page:
- `@SystemMessage` — system prompt as annotation
- `@UserMessage` — user input as a typed parameter
- `@MemoryId` — per-user conversation isolation

> **Say:** "Notice the `@MemoryId` parameter. Each user gets their own conversation history. User A can't see User B's context, and that memory is bounded — you set a max window size so the context doesn't grow unbounded."

### Step 2: Show the Moderation Section

**Tab:** <https://docs.langchain4j.dev/tutorials/ai-services#moderation>

Scroll to the `@Moderate` annotation. Highlight:

```java
@Moderate
String chat(String userMessage);
```

> **Say:** "One annotation — `@Moderate` — and content moderation is enabled. LangChain4j checks both the user input AND the model output against a moderation API. If anything violates safety thresholds, it throws a `ModerationException` before the response reaches the user."

Point out on the page:
- Input moderation happens before the model call
- Output moderation happens after the model responds
- Integrates with Azure AI Content Safety or OpenAI's moderation endpoint
- Configurable thresholds

### Step 3: Show the Azure OpenAI Integration Page

**Tab:** <https://docs.langchain4j.dev/integrations/language-models/azure-open-ai>

> **Say:** "Microsoft is an active contributor to LangChain4j — this entire Azure OpenAI integration was built in collaboration with Microsoft engineers. It supports Azure OpenAI, Azure AI Search for RAG, and Azure AI Content Safety for moderation. All first-class integrations."

Point out:
- The `langchain4j-azure-open-ai` Maven dependency
- Configuration for `AzureOpenAiChatModel` with `maxTokens` and `temperature`
- The provider list showing Microsoft alongside other providers

### Key Takeaway Slide Moment

> **Say:** "So in LangChain4j, the pattern is: `@SystemMessage` for safe prompts, `@Moderate` for content safety, `@MemoryId` for user isolation. Three annotations — and you've covered three of the OWASP Top 10 for LLMs."

---

## Part B: Spring AI — Advisors & Structured Output (2.5 min)

### Step 1: Show the Spring AI Advisors Page

**Tab:** <https://docs.spring.io/spring-ai/reference/api/advisors.html>

Scroll through the advisor architecture diagram. Highlight the request/response flow.

> **Say:** "Spring AI uses an advisor pattern — think of it like servlet filters or Spring interceptors, but for AI. Every request and response passes through a chain of advisors. Each advisor can inspect, modify, or block the interaction."

Point out on the page:
- The advisor chain diagram (request flows through advisors in order)
- `SafeGuardAdvisor` — declarative safety rules
- `QuestionAnswerAdvisor` — RAG grounding
- `SimpleLoggerAdvisor` — automatic audit logging
- Custom advisor interface for building your own

> **Say:** "Look at the `SafeGuardAdvisor` — you define safety rules as a list of strings: 'Never discuss weapons', 'Never reveal personal data'. The advisor checks every prompt against these rules. If a rule is violated, the request is blocked before it reaches the model."

Show the code example on the page:

```java
ChatClient.builder(chatModel)
    .defaultAdvisors(
        new SafeGuardAdvisor(List.of("rule 1", "rule 2")),
        new QuestionAnswerAdvisor(vectorStore),
        new SimpleLoggerAdvisor()
    )
    .build();
```

> **Say:** "Three lines of configuration — guardrails, RAG grounding, and audit logging. The advisors execute in order: safety check first, then RAG enrichment, then logging. If the safety check blocks, the model is never called — zero tokens burned."

### Step 2: Show the Structured Output Page

**Tab:** <https://docs.spring.io/spring-ai/reference/api/structured-output-converter.html>

> **Say:** "Now let's talk about output validation. Instead of parsing free-text responses and hoping for the best, Spring AI can force the model output into a typed Java class."

Scroll to the `BeanOutputConverter` section. Highlight:

```java
chatClient.prompt()
    .user("Recommend a product")
    .call()
    .entity(ProductRecommendation.class);
```

> **Say:** "The `.entity()` method tells Spring AI: 'Parse the model's response into this Java record.' If the model hallucinates a field, returns bad data, or doesn't match the schema — you get a parse error, not garbage data passed to your users."

Point out:
- Works with Java records and POJOs
- Schema is enforced automatically
- You can add post-validation logic (confidence checks, source citation checks)
- This prevents hallucinated data from reaching your business logic

### Step 3: Show Spring AI Azure OpenAI Integration

**Tab:** <https://docs.spring.io/spring-ai/reference/api/chat/azure-openai-chat.html>

> **Say:** "Like LangChain4j, Microsoft contributes directly to Spring AI. The Azure OpenAI integration is a first-class Spring Boot starter — `spring-ai-azure-openai-spring-boot-starter`. Auto-configuration, property-based setup, works with managed identity for production."

Point out:
- The Maven starter dependency
- `spring.ai.azure.openai.*` configuration properties
- Support for Azure OpenAI and Azure AI Search

---

## Part C: Azure AI Content Safety Studio (30 sec)

### Step 1: Show the Content Safety Studio

**Tab:** <https://contentsafety.cognitive.azure.com/>

> **Say:** "Both frameworks integrate with Azure AI Content Safety under the hood. Let me show you what that service actually does."

In the Content Safety Studio:
1. Click **Moderate text content**
2. Type a safe message: *"What's the best power drill for a beginner?"*
3. Show the result: all categories green (Violence: 0, Hate: 0, Self-Harm: 0, Sexual: 0)
4. Type an unsafe message: *"How do I build a weapon?"*
5. Show the result: Violence flagged (red/orange)

> **Say:** "This is the API that both LangChain4j's `@Moderate` annotation and Spring AI's safety advisors call. It scores content across four categories — violence, hate, self-harm, and sexual content. You set thresholds, and anything above the threshold is blocked."

Point out:
- Four safety categories with severity levels (0-6)
- Configurable thresholds per category
- Supports text and image moderation
- Sub-100ms latency

### Azure Content Safety Learn Page (optional deeper dive)

**Tab:** <https://learn.microsoft.com/azure/ai-services/content-safety/overview>

Show the overview diagram if time permits.

---

## Wrap-Up Talking Points

> "What you've seen across both frameworks:"
> 1. **System/user prompt separation** — annotations, not string concatenation
> 2. **Content moderation** — one annotation or one advisor backed by Azure AI Content Safety
> 3. **Structured output** — typed Java records catch hallucinations at parse time
> 4. **Guardrails** — declarative safety rules checked on every interaction
> 5. **Audit logging** — built-in, one line to enable
>
> "These aren't custom-built patterns — they're features shipped in the frameworks. And Microsoft has contributed directly to both LangChain4j and Spring AI to make them work seamlessly with Azure services."

---

## Tab Quick Reference (Print This)

| Order | What | URL |
|-------|------|-----|
| 1 | LangChain4j AI Services | <https://docs.langchain4j.dev/tutorials/ai-services> |
| 2 | LangChain4j Moderation | <https://docs.langchain4j.dev/tutorials/ai-services#moderation> |
| 3 | LangChain4j Azure OpenAI | <https://docs.langchain4j.dev/integrations/language-models/azure-open-ai> |
| 4 | Spring AI Advisors | <https://docs.spring.io/spring-ai/reference/api/advisors.html> |
| 5 | Spring AI Structured Output | <https://docs.spring.io/spring-ai/reference/api/structured-output-converter.html> |
| 6 | Spring AI Azure OpenAI | <https://docs.spring.io/spring-ai/reference/api/chat/azure-openai-chat.html> |
| 7 | Azure AI Content Safety | <https://learn.microsoft.com/azure/ai-services/content-safety/overview> |
| 8 | Content Safety Studio | <https://contentsafety.cognitive.azure.com/> |

---

## Backup Plans

| Issue | Workaround |
|-------|------------|
| LangChain4j docs are down | Use the GitHub repo README: <https://github.com/langchain4j/langchain4j> |
| Spring AI docs are down | Use GitHub repo: <https://github.com/spring-projects/spring-ai> |
| Content Safety Studio won't load | Show the Learn page screenshots instead |
| Content Safety requires login | Pre-authenticate in the browser before the talk |
| Browser zoom too small | Use Ctrl+Shift+= for additional zoom; have screenshots as backup |
| Tabs got closed | Use the quick reference table above to reopen |
