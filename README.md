<div align="center">
  <h1>🚀 Spring AI & Groq Integration Learning Journey</h1>
  <p><strong>A lightning-fast Spring Boot API powered by Groq's Llama Models!</strong></p>
</div>

<hr/>

## 📖 Overview

This project serves as a comprehensive learning repository for integrating **Spring AI** with **Groq's API**. We've successfully repurposed the standard Spring AI OpenAI starter package to interact natively with Groq's OpenAI-compatible endpoints, giving us sub-second inference speeds!

### 🤔 What is Spring AI?
Spring AI is an application framework that brings the portability and modular design patterns of the Spring ecosystem to the AI domain. It allows developers to write code once and swap between different AI providers (like OpenAI, Groq, Ollama, Anthropic) just by changing application properties—no code rewrites required!

### ⚡ Why Groq?
Groq has developed custom hardware (LPUs - Language Processing Units) specifically designed for running AI models. By using Groq's API (which is perfectly compatible with the OpenAI specification), we can run powerful open-source models like LLaMA 3 with unprecedented, blazing-fast speed.

This README documents our progress, architectural design, and the essential concepts we have implemented so far.

---

## 📑 Table of Contents

1. [🚀 Running This Project](#-running-this-project)
2. [🏗️ Architecture & Design](#️-architecture--design)
3. [🧠 Spring AI Concepts & What We Learned](#-spring-ai-concepts--what-we-learned)
   - [1. Models](#1-models)
   - [2. Prompts & Prompting](#2-prompts--prompting)
   - [3. Parsing & Output Converters](#3-parsing--output-converters)
   - [4. Default Prompts Configuration](#4-default-prompts-configuration)
4. [📝 Dynamic Prompt Templating](#-4-dynamic-prompt-templating)
   - [Approach A: Classpath Resources](#approach-a-fluent-api-with-classpath-resources-best-practice)
   - [Approach B: Explicit Templates](#approach-b-explicit-prompt-and-message-templates)
   - [Approach C: Inline Parameterization](#approach-c-inline-parameterization-with-fluent-api)
5. [🛡️ Spring AI Advisors (Interceptors)](#️-5-spring-ai-advisors-interceptors)
   - [What are Advisors?](#-what-are-advisors)
   - [Why Use Advisors?](#-why-use-advisors)
   - [Pre-built Advisors](#-sample-pre-built-advisors)
   - [Creating a Custom Advisor](#-creating-a-custom-advisor)
   - [Configuring Advisors](#️-configuring-advisors)

---

## 🚀 Running This Project

### 1. Prerequisites
- Java 17+
- Maven
- [Groq Cloud API Key](https://console.groq.com/keys)

### 2. Configuration
Set your Groq API Key in `src/main/resources/application.properties`:
```properties
spring.application.name=springai
server.port=8082

# Groq API Configuration
spring.ai.openai.api-key=gsk_your_groq_api_key_here
spring.ai.openai.base-url=https://api.groq.com/openai
```

### 3. Build & Run
Run the application using Maven:
```bash
mvn spring-boot:run
```
The server will start locally on port **8082**.

---

## 🏗️ Architecture & Design

Here is the high-level architecture of our Spring AI setup:

```mermaid
graph TD
    Client[👨‍💻 Client / Postman] -->|GET /chat?q=...| Controller[🎮 ChatController]
    Controller -->|Calls chat(query)| Service[⚙️ ChatServiceImpl]
    Service -->|Uses| ChatClient[💬 Spring AI ChatClient]
    ChatClient -->|Configured via| AiConfig[🛠️ AiConfig]
    ChatClient -->|HTTP Request| GroqAPI[☁️ Groq API / Llama Models]
    GroqAPI -->|Response| ChatClient
    ChatClient -->|Returns String| Service
    Service -->|ResponseEntity| Controller
    Controller -->|JSON/String| Client
```

---

## 🧠 Spring AI Concepts & What We Learned

### 1. Models
**What are Models?**  
Large Language Models (LLMs) are AI engines trained on vast amounts of text. They predict and generate text based on your input. Spring AI abstracts different models (OpenAI, Anthropic, Ollama, Groq) so you can switch them out without changing your code.

**Ways to implement models in Spring AI:**
- Using `ChatModel` (Low-level underlying interface)
- Using `ChatClient` (High-level fluent API built on top of `ChatModel` - *This is what we use!*)

**Models We Are Using (Groq API):**
| Provider | Model | Cost | Best For |
| :--- | :--- | :--- | :--- |
| Groq | Llama 3.1 8B Instant | Free | Fast, general-purpose queries |
| Groq | Llama 4 Scout 17B | Free | Advanced reasoning and coding |

---

### 2. The ChatClient Fluent API & Prompting
**What is a Prompt?**  
A prompt is the instruction or context you send to the AI. In Spring AI, a `Prompt` object is a container that holds a list of `Message` objects (like `UserMessage`, `SystemMessage`) and model configuration options (`ChatOptions`).

**The Power of the Fluent API:**
While you *can* manually create raw `Prompt` objects, Spring AI's `ChatClient` provides a powerful **Fluent API**. This allows you to chain methods together in a highly readable, step-by-step builder pattern: `.prompt()` ➡️ `.system()` ➡️ `.user()` ➡️ `.call()` ➡️ `.content()`.

Here is why the Fluent API is the absolute standard for Spring AI:

1. **Dynamic Templating (Placeholders):** 
   You never have to use messy string concatenation (like `"Hello " + name + "!"`). Spring AI natively handles template placeholders. You simply write variables inside `{brackets}` and use the `.param()` method to safely inject dynamic variables at runtime.
2. **External File Support (`Resource`):**
   Writing massive prompt instructions inside Java strings makes code ugly and hard to maintain. The Fluent API can natively read `org.springframework.core.io.Resource` objects. You can save your prompts as `.txt` files in your `src/main/resources` folder, and Spring will automatically read them *and* substitute their `{placeholders}`!
3. **Role Separation:**
   You can effortlessly separate instructions using `.system()` (defining the AI's rules and persona) and `.user()` (the actual query) in a single fluid chain.

**Basic Concept Example:**
```java
chatClient.prompt()
          // Reads from string, dynamically injecting 'Java' into {subject}
          .system(s -> s.text("Act as an expert in {subject}").param("subject", "Java"))
          .user("Explain what a NullPointerException is.")
          .call()
          .content();
```
*(Check out Section 4 below to see exactly how we implemented these advanced file resources and templates in this project!)*

---

### 3. Parsing & Output Converters
**What is Parsing?**  
AI models natively return raw text. Parsing (or Output Converters) is the process of instructing the AI to return data in a specific structure and then extracting that data into Java Objects, Lists, or Maps.

**Ways to parse data in Spring AI:**
1. **Raw String:** Using `.content()` (Extracts simple text).
2. **Entity Type Extraction:** Using `.entity(MyClass.class)` to automatically map the AI's JSON output directly into a Java Class or Record.
3. **Structured Output Converters:** Spring AI provides tools like `BeanOutputConverter`, `ListOutputConverter`, or `MapOutputConverter` for advanced mapping.
   ```java
   // Example: Parsing a list of strings directly
   List<String> list = chatClient.prompt("Give me 5 colors")
       .call()
       .entity(new ParameterizedTypeReference<List<String>>() {});
   ```

---

### 4. Default Prompts Configuration
To avoid repeating system prompts (like the AI's persona) and configuration options in every API call, we set defaults globally via the `ChatClient.Builder`.

**Snippet from `AiConfig.java`:**
```java
@Bean
public ChatClient chatClient(ChatClient.Builder builder) {
    return builder
            // Default Persona
            .defaultSystem("You are a helpful assistant as a coding expert in Java")
            // Default Model & Parameters
            .defaultOptions(OpenAiChatOptions.builder()
                    .model("meta-llama/llama-4-scout-17b-16e-instruct")
                    .temperature(1.0)
                    .build())
            .build();
}
```

---

## 📝 4. Dynamic Prompt Templating

**What are Prompt Templates?**
Prompt Templates allow you to create dynamic, reusable prompts using placeholders (e.g., `{concept}`, `{techname}`). Spring AI evaluates these placeholders at runtime by replacing them with real data from a `Map` or parameters. This cleanly separates prompt engineering from your Java logic.

We have implemented dynamic prompting using two distinct approaches:

### Approach A: Fluent API with Classpath Resources (Best Practice)
Instead of hardcoding prompts in your Java classes, you can store them as plain text (`.txt`) files in your `src/main/resources` folder. Spring's `@Value` annotation injects these files as `Resource` objects, which the `ChatClient` can natively parse and inject parameters into.

```java
// Injecting text files from src/main/resources/prompts/
@Value("classpath:prompts/user-message.txt")
private Resource userMessage;

@Value("classpath:prompts/system-message.txt")
private Resource systemMessage;

public String chatTemplate() {
    return this.chatClient
            .prompt()
            .system(system -> system.text(this.systemMessage))
            .user(user -> user.text(this.userMessage)
                              .param("concept", "SpringBoot Framework validation"))
            .call()
            .content();
}
```

### Approach B: Explicit Prompt and Message Templates
This approach involves manually building `SystemPromptTemplate` and `PromptTemplate` objects, passing parameters via `Map.of()`, and generating concrete `Message` objects. These messages are then bundled into a `Prompt` wrapper before calling the API.

```java
public String chatTemplate() {
    // 1. Build the System Message
    var systemPromptTemplate = SystemPromptTemplate.builder()
            .template("You are a helpful assistant. You are a coding expert.")
            .build();
    var systemMessage = systemPromptTemplate.createMessage();

    // 2. Build the User Message with dynamic parameters
    var userPromptTemplate = PromptTemplate.builder()
            .template("what is {techname} tell me about {techExample}")
            .build();
    var userMessage = userPromptTemplate.createMessage(Map.of(
            "techname", "java",
            "techExample", "SpringBoot"));

    // 3. Wrap messages in a Prompt and call the API
    Prompt prompt = new Prompt(systemMessage, userMessage);
    return this.chatClient.prompt(prompt).call().content();
}
```

### Approach C: Inline Parameterization with Fluent API
If you don't want to use external files or build complex objects, you can dynamically construct and enrich prompts inline. This is incredibly useful for intercepting a user's raw query and wrapping it in strict instructions before sending it to the model.

```java
public String chat(String query) {
    // We wrap the user's raw query with explicit instructions
    String queryStr = "As an expert in coding and programming. Always write programs in Java. Now reply to this question: {query}";

    return chatClient
            .prompt()
            // We pass the template string and substitute {query} inline!
            .user(u -> u.text(queryStr).param("query", query))
            .call()
            .content();
}
```

<hr/>

## 🛡️ 5. Spring AI Advisors (Interceptors)

### 🌟 What are Advisors?
In Spring AI, **Advisors** act as interceptors (or middleware) that wrap around your AI requests and responses. They allow you to observe, modify, or completely block a request *before* it goes to the AI model, and observe or modify the response *after* it returns. 

### 🎯 Why Use Advisors?
- **Logging & Monitoring:** Track input prompts, output responses, and token usage precisely.
- **Safety & Guardrails:** Block malicious prompts or prevent the AI from discussing restricted topics (e.g., Profanity filters).
- **Memory Management:** Automatically inject past conversation history into new requests (Chat Memory) for continuous chat experiences.
- **Security:** Sanitize inputs or mask PII data before they hit the LLM.

### 🛠️ How Can We Use Them?
You can easily attach Advisors to your `ChatClient` using the `.defaultAdvisors()` builder method. Once attached, every request made by that client will automatically flow through the configured advisor chain!

### 📦 Sample Pre-built Advisors
Spring AI comes with several powerful out-of-the-box advisors ready to use:
- **`SimpleLoggerAdvisor`**: Logs the raw request sent to the LLM and the raw response received.
- **`SafeGuardAdvisor`**: Validates the prompt and throws an exception if it contains prohibited words/topics.
- **`MessageChatMemoryAdvisor`**: Automatically stores and retrieves previous conversation messages to maintain context across API calls.
- **`PromptChatMemoryAdvisor`**: Similar to the above, but injects memory into a specific section of the prompt template.

### 🎨 Creating a Custom Advisor
We created our own custom advisor, `TokenPrintAdvisor`, to log the request, response, and calculate the exact token usage of our Groq LLM calls!

To create a custom advisor, simply implement `CallAdvisor` (and/or `StreamAdvisor`) and override the `adviseCall` method:

```java
public class TokenPrintAdvisor implements CallAdvisor, StreamAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(TokenPrintAdvisor.class);

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("My token advisor is called");
        logger.info("Request: " + request.prompt().getContents());
        
        // Let the request proceed to the next advisor / LLM
        ChatClientResponse response = chain.nextCall(request);
        
        logger.info("Token Advisor : Response received from Model...........");
        logger.info("Response  : " + response.chatResponse().getResult().getOutput().getText());
        logger.info("Total Tokens: " + response.chatResponse().getMetadata().getUsage().getTotalTokens());

        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        return null; // Implementation for streaming interactions
    }

    @Override
    public String getName() { return "TokenPrintAdvisor"; }

    @Override
    public int getOrder() { return 0; }
}
```

### ⚙️ Configuring Advisors
We configured both our custom `TokenPrintAdvisor` and the pre-built `SafeGuardAdvisor` (to strictly block any prompts about "games") globally in our `AiConfig`. This ensures all chat calls abide by these rules.

```java
@Bean
public ChatClient chatClient(ChatClient.Builder builder) {
    return builder
        // Adding our Custom Advisor & SafeGuard Advisor to the chain
        .defaultAdvisors(
            new TokenPrintAdvisor(), 
            new SafeGuardAdvisor(List.of("games")) // Blocks prompts related to games
        )
        .defaultSystem("you are a helpful assistant as a coding expert in java")
        .defaultOptions(OpenAiChatOptions.builder()
                .model("meta-llama/llama-4-scout-17b-16e-instruct")
                .temperature(1.0)
                .maxTokens(200)
                .build())
        .build();
}
```

<hr/>

<div align="center">
  <h3>Ready to start building? 🚀</h3>
  <p>If this repository helped you learn Spring AI, give it a ⭐ on GitHub!</p>
  <p>
    <a href="https://docs.spring.io/spring-ai/reference/">Spring AI Documentation</a> •
    <a href="https://console.groq.com/">Groq Cloud</a>
  </p>
  <p>Crafted seamlessly combining Java & AI! 🤖☕</p>
</div>
