# DEMO 2 (v2): End-to-End Trustworthy AI Pipeline — Browser Walkthrough

> **Duration:** ~7 minutes  
> **Tools:** Browser only (Azure Portal, Microsoft Learn, Spring AI docs, LangChain4j docs)  
> **No live coding — guided walkthrough of portal experiences and documentation**  
> **Slide Reference:** Slide 24

---

## Source Code Reference

This is a browser-walkthrough version of Demo 2. For the live-coding version with runnable source code, see:
- **Live-coding script:** [DEMO2-EndToEnd-TrustworthyAI-Pipeline.md](DEMO2-EndToEnd-TrustworthyAI-Pipeline.md)
- **Pipeline source:** `demo2-pipeline/src/main/java/com/demo/pipeline/`

---

## Pre-Demo Setup

### Tabs to Have Open (in order)

1. **Azure Portal — AI Content Safety resource** — <https://portal.azure.com/> (navigate to your Content Safety resource)
2. **Azure Content Safety Studio** — <https://contentsafety.cognitive.azure.com/>
3. **Microsoft Learn — Content Safety quickstart** — <https://learn.microsoft.com/azure/ai-services/content-safety/quickstart-text>
4. **Microsoft Learn — Azure OpenAI content filters** — <https://learn.microsoft.com/azure/ai-services/openai/how-to/content-filters>
5. **Spring AI Advisors** — <https://docs.spring.io/spring-ai/reference/api/advisors.html>
6. **Spring AI Observability** — <https://docs.spring.io/spring-ai/reference/api/observability.html>
7. **LangChain4j Observability** — <https://docs.langchain4j.dev/tutorials/observability>
8. **Microsoft Learn — Application Insights for Java** — <https://learn.microsoft.com/azure/azure-monitor/app/java-in-process-agent>
9. **Azure Portal — Application Insights** — <https://portal.azure.com/> (navigate to your App Insights resource)

### Azure Resources to Have Ready

- An **Azure AI Content Safety** resource (free tier works)
- An **Azure OpenAI** resource with a deployed model that has content filters configured
- An **Application Insights** resource with some sample data (optional but ideal)

### Tips

- Pre-authenticate to Azure Portal and Content Safety Studio before the talk
- Set browser zoom to 125-150% for readability
- Practice tab transitions — the story flows: Safety → Filters → Logging → Observability

---

## Pipeline Architecture — Show This First

Display this diagram on the slide or whiteboard:

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   SANITIZE   │───▶│    PROMPT    │───▶│   VALIDATE   │
│  (PII Strip) │    │  (Assemble)  │    │  (Schema)    │
└──────────────┘    └──────────────┘    └──────────────┘
                                               │
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   RESPOND    │◀───│     LOG      │◀───│    FILTER    │
│  (to User)   │    │   (Audit)    │    │  (Safety)    │
└──────────────┘    └──────────────┘    └──────────────┘
```

> **Say:** "This is the pipeline every production AI application needs. Six stages — sanitize, prompt, validate, filter, log, respond. I'm going to show you how Azure services and Java frameworks cover each stage. None of this requires building from scratch."

---

## Part A: Content Safety & Built-in Filters (3 min)

### Step 1: Azure AI Content Safety Studio — Live Moderation

**Tab:** <https://contentsafety.cognitive.azure.com/>

1. Click **Moderate text content**

2. **Safe input test:**
   - Type: *"What's the best approach for a home renovation project?"*
   - Click **Run test**
   - Show results: All categories green (0 severity)

> **Say:** "Normal input — all clear. Violence zero, hate zero, self-harm zero, sexual zero. This check takes under 100 milliseconds."

3. **Unsafe input test:**
   - Type: *"How do I make a dangerous weapon from household items?"*
   - Click **Run test**
   - Show results: Violence category flagged (severity 2-4)

> **Say:** "Flagged immediately. The violence category triggered. In production, your application can block this before the request ever reaches the LLM — zero tokens spent, zero risk."

4. **Show the severity levels:**
   - Point out the 0-6 scale per category
   - Show that you can configure the threshold (e.g., block at severity 2 vs. 4)

> **Say:** "You control the sensitivity. A children's application blocks at severity 2. An adult content review tool might allow severity 4 for analysis purposes. The thresholds are configurable per deployment."

### Step 2: Azure OpenAI Built-in Content Filters

**Tab:** <https://learn.microsoft.com/azure/ai-services/openai/how-to/content-filters>

> **Say:** "Azure OpenAI has content filters built right into the service — before your code even runs. Let me show you how they're configured."

Scroll through the page and highlight:

- **Default filters** — every Azure OpenAI deployment comes with content filters pre-enabled
- **Four categories**: Violence, Hate, Sexual, Self-Harm
- **Input AND output filtering** — both directions
- **Configurable severity thresholds** per category
- **Annotation mode** — see filter results without blocking (useful for testing)

Point out the diagram showing the filter pipeline:
```
User Input → Input Filter → Model → Output Filter → Response
```

> **Say:** "This is built into Azure OpenAI itself — you don't need any code for this level of protection. But for production, you want additional layers in your Java application. Defense in depth — three layers: your Content Safety filter on input, Azure OpenAI's built-in jailbreak detection on the model call, and your Content Safety filter on output. In our pipeline, Stage 3 catches Azure OpenAI's `HttpResponseException` and returns a graceful blocked response instead of a 500 error."

### Step 3: Azure Portal — Content Filter Configuration

**Tab:** Azure Portal → Your Azure OpenAI resource → **Content filters**

1. Show the existing content filter configuration
2. Point out:
   - Per-category threshold sliders
   - Input vs. output filter settings
   - Blocklist feature (custom blocked terms)
   - Protected material detection (code, text)

> **Say:** "You can create custom content filters — tighten thresholds for specific deployments, add blocklists of terms specific to your industry, and enable protected material detection to prevent the model from reproducing copyrighted code."

---

## Part B: PII Protection — Microsoft Purview & Language Service (1.5 min)

### Step 1: PII Detection on Microsoft Learn

**Tab:** <https://learn.microsoft.com/azure/ai-services/language-service/personally-identifiable-information/overview>

> **Say:** "For PII stripping — stage one of our pipeline — you can use regex patterns in your Java code, but Azure also provides a dedicated API. The Azure AI Language PII detection service handles over 50 entity types — emails, phone numbers, SSNs, passport numbers, medical record numbers."

Scroll through and highlight:
- Supported PII entity types (show the comprehensive list)
- Language support
- Redaction vs. detection modes
- The flow diagram: Input → PII Detection → Redacted Output

> **Say:** "The key insight: you redact BEFORE sending to the LLM. The model never sees the PII. You store a mapping if you need to re-hydrate the response, but the sensitive data never leaves your server."

### Step 2: Show the Categories

Point out on the page:
- **General PII**: Names, addresses, phone numbers, SSNs
- **Financial**: Credit card numbers, bank accounts, SWIFT codes
- **Health**: Medical record numbers, health plan IDs
- **IT**: IP addresses, passwords, Azure keys

> **Say:** "This is enterprise-grade PII detection — the same service that powers Microsoft Purview. In Java, you call this API in your sanitizer stage, or you use regex patterns for the most common types. Both LangChain4j and Spring AI give you hooks to intercept the input before it reaches the model."

---

## Part C: Framework-Level Logging & Guardrails (1.5 min)

### Step 1: Spring AI Advisors — The Guard Chain

**Tab:** <https://docs.spring.io/spring-ai/reference/api/advisors.html>

> **Say:** "Now let's see how the Java frameworks implement the pipeline stages. Spring AI's advisor chain is the cleanest pattern I've seen."

Scroll to the advisor chain diagram and highlight:
- Request flows through advisors in declared order
- Each advisor can: pass through, modify, or block
- `SafeGuardAdvisor` → checks safety rules (FILTER stage)
- `QuestionAnswerAdvisor` → RAG grounding (VALIDATE stage)
- `SimpleLoggerAdvisor` → audit trail (LOG stage)

> **Say:** "The advisor chain maps directly to our pipeline. `SafeGuardAdvisor` is the filter stage. `QuestionAnswerAdvisor` is the validation stage — grounding answers in your data so the model can't hallucinate. `SimpleLoggerAdvisor` is the logging stage. Three lines of config, three pipeline stages covered."

### Step 2: Spring AI Observability

**Tab:** <https://docs.spring.io/spring-ai/reference/api/observability.html>

> **Say:** "Spring AI has built-in observability powered by Micrometer — the same metrics library you already use in Spring Boot."

Highlight on the page:
- Automatic metrics for: token usage, latency, model calls, errors
- OpenTelemetry integration
- Trace context propagation
- Properties to include prompt/completion in traces (for debugging)

```properties
spring.ai.chat.observations.include-prompt=true
spring.ai.chat.observations.include-completion=true
```

> **Say:** "Two properties and every prompt and response is captured in your observability traces. This feeds directly into Application Insights, Prometheus, Grafana — whatever you already use."

### Step 3: LangChain4j Observability

**Tab:** <https://docs.langchain4j.dev/tutorials/observability>

> **Say:** "LangChain4j also has observability support — it integrates with OpenTelemetry for distributed tracing."

Point out:
- OpenTelemetry span creation for each model call
- Token usage tracking
- Support for Application Insights via the OpenTelemetry exporter

---

## Part D: Application Insights Dashboard (1 min)

### Step 1: Show Application Insights in Azure Portal

**Tab:** Azure Portal → Your Application Insights resource

> **Say:** "All of this logging and metrics data flows into Application Insights. Let me show you what that looks like."

1. **Application Map** — show the topology of services
2. **Live Metrics** — show real-time request flow (if you have a live app)
3. **Logs** — show structured log queries

### Step 2: Show a KQL Query

Navigate to **Logs** and run:

```kusto
traces
| where message contains "AUDIT"
| project timestamp,
          message,
          customDimensions.userId,
          customDimensions.blocked,
          customDimensions.tokenCount
| order by timestamp desc
| take 20
```

> **Say:** "This is your compliance audit trail. Every AI interaction — who made the request, was it blocked, how many tokens were used, what was the latency. You can prove to your security team exactly what your AI app did and didn't do."

If you don't have live data, show the query structure and explain what the output would look like.

### Step 3: (Optional) Show the Java Agent Setup Page

**Tab:** <https://learn.microsoft.com/azure/azure-monitor/app/java-in-process-agent>

> **Say:** "Setting this up in Java is one line in your build — the Application Insights Java agent attaches automatically. No code changes required for basic telemetry."

---

## Wrap-Up Talking Points

> "Here's what we just walked through — the full pipeline:"
>
> | Pipeline Stage | Azure Service / Framework Feature |
> |---|---|
> | **Sanitize** (PII) | Azure AI Language PII Detection, or regex in your Java code |
> | **Prompt** (Assemble) | `@SystemMessage` (LangChain4j), `.defaultSystem()` (Spring AI) |
> | **Validate** (Schema) | Structured output with Java records, `QuestionAnswerAdvisor` for RAG grounding |
> | **Filter** (Safety) | Three layers: Azure AI Content Safety SDK (input & output), Azure OpenAI built-in jailbreak/policy filter (model call), `SafeGuardAdvisor` |
> | **Log** (Audit) | `SimpleLoggerAdvisor`, Micrometer, OpenTelemetry → Application Insights |
> | **Respond** (to User) | Safe, validated, logged response |
>
> "None of this is custom-built. Every stage is covered by an Azure service, a framework feature, or both. Your job as a responsible Java developer is to wire them together."

---

## Tab Quick Reference (Print This)

| Order | What | URL |
|-------|------|-----|
| 1 | Content Safety Studio | <https://contentsafety.cognitive.azure.com/> |
| 2 | Azure OpenAI Content Filters | <https://learn.microsoft.com/azure/ai-services/openai/how-to/content-filters> |
| 3 | Azure Portal — Content Filters | Azure Portal → OpenAI resource → Content filters |
| 4 | PII Detection Overview | <https://learn.microsoft.com/azure/ai-services/language-service/personally-identifiable-information/overview> |
| 5 | Spring AI Advisors | <https://docs.spring.io/spring-ai/reference/api/advisors.html> |
| 6 | Spring AI Observability | <https://docs.spring.io/spring-ai/reference/api/observability.html> |
| 7 | LangChain4j Observability | <https://docs.langchain4j.dev/tutorials/observability> |
| 8 | App Insights Java Agent | <https://learn.microsoft.com/azure/azure-monitor/app/java-in-process-agent> |
| 9 | Azure Portal — App Insights | Azure Portal → Application Insights resource |

---

## Backup Plans

| Issue | Workaround |
|-------|------------|
| Content Safety Studio requires login | Pre-authenticate; have screenshots ready |
| Azure Portal slow to load | Use the Microsoft Learn pages instead — they have diagrams and examples |
| App Insights has no data | Walk through the KQL query structure and explain expected output |
| Spring AI docs page changed | Fall back to GitHub repo: <https://github.com/spring-projects/spring-ai> |
| LangChain4j docs page changed | Fall back to GitHub repo: <https://github.com/langchain4j/langchain4j> |
| Internet connectivity issues | Have offline screenshots of each tab (export as PDF from browser beforehand) |
