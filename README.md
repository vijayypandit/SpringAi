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

This README documents our progress, architectural design, and the essential concepts we have implemented so far, including **Vector Databases, Embeddings, and Memory Management**.

---

## 📑 Table of Contents

1. [🚀 Running This Project](#-running-this-project)
2. [🏗️ Architecture & Design](#️-architecture--design)
3. [🧠 Spring AI Concepts & What We Learned](#-spring-ai-concepts--what-we-learned)
4. [📝 Dynamic Prompt Templating](#-4-dynamic-prompt-templating)
5. [🛡️ Spring AI Advisors (Interceptors)](#️-5-spring-ai-advisors-interceptors)
6. [🧠 Chat Memory (Contextual Conversations)](#-6-chat-memory-contextual-conversations)
7. [🗃️ Vector Store, Embeddings & RAG](#️-7-vector-store-embeddings--rag)

---

## 🚀 Running This Project

### 1. Prerequisites
- Java 17+
- Maven
- [Groq Cloud API Key](https://console.groq.com/keys)
- MariaDB (running locally on port 3308 for Vector Store)
- Ollama (running locally with `nomic-embed-text` model)

### 2. Configuration
Set your properties in `src/main/resources/application.properties`:
```properties
spring.application.name=springai
server.port=8082

# Groq API Configuration
spring.ai.openai.api-key=gsk_your_groq_api_key_here
spring.ai.openai.base-url=https://api.groq.com/openai

# Ollama Embeddings
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.embedding.options.model=nomic-embed-text

# MariaDB Vector Database
spring.datasource.url=jdbc:mariadb://localhost:3308/springai
spring.datasource.username=root
spring.datasource.password=root
spring.ai.vectorstore.mariadb.initialize-schema=true
spring.ai.vectorstore.mariadb.distance-type=COSINE
spring.ai.vectorstore.mariadb.dimensions=768
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
Large Language Models (LLMs) are AI engines trained on vast amounts of text. Spring AI abstracts different models (OpenAI, Anthropic, Ollama, Groq) so you can switch them out without changing your code.

**Models We Are Using (Groq API):**
| Provider | Model | Cost | Best For |
| :--- | :--- | :--- | :--- |
| Groq | Llama 3.1 8B Instant | Free | Fast, general-purpose queries |
| Groq | Llama 4 Scout 17B | Free | Advanced reasoning and coding |

### 2. The ChatClient Fluent API & Prompting
While you can manually create raw `Prompt` objects, Spring AI's `ChatClient` provides a powerful **Fluent API**. This allows you to chain methods together in a highly readable, step-by-step builder pattern: `.prompt()` ➡️ `.system()` ➡️ `.user()` ➡️ `.call()` ➡️ `.content()`.

---

## 📝 4. Dynamic Prompt Templating

**What are Prompt Templates?**
Prompt Templates allow you to create dynamic, reusable prompts using placeholders (e.g., `{concept}`). Spring AI evaluates these placeholders at runtime.

We primarily use **Classpath Resources** to separate text from code:
```java
@Value("classpath:prompts/user-message.txt")
private Resource userMessage;

@Value("classpath:prompts/system-message.txt")
private Resource systemMessage;

public String chatTemplate(String query) {
    return this.chatClient
            .prompt()
            .system(system -> system.text(this.systemMessage))
            .user(user -> user.text(this.userMessage).param("query", query))
            .call()
            .content();
}
```

---

## 🛡️ 5. Spring AI Advisors (Interceptors)

### 🌟 What are Advisors?
In Spring AI, **Advisors** act as interceptors (or middleware) that wrap around your AI requests and responses. They allow you to observe, modify, or block a request *before* it goes to the AI model, and observe the response *after*.

### 📦 Pre-built & Custom Advisors
- **`TokenPrintAdvisor` (Custom)**: Logs the request, response, and calculates token usage!
- **`SafeGuardAdvisor`**: Validates the prompt (e.g., blocks queries about "games").
- **`MessageChatMemoryAdvisor`**: Automatically stores and retrieves previous conversation messages.

---

## 🧠 6. Chat Memory (Contextual Conversations)

### 🧐 What is Chat Memory?
By default, AI models are **stateless** and forget past interactions. **Chat Memory** injects previous conversation history into new requests so the AI can maintain context.

### 🗄️ In-Memory Storage (`InMemoryChatMemoryRepository`)
We are currently using the `InMemoryChatMemoryRepository` coupled with a `MessageWindowChatMemory` to keep track of the conversation context.

```java
@Bean
public ChatMemory chatMemory() {
    InMemoryChatMemoryRepository inMemoryChatMemoryRepository = new InMemoryChatMemoryRepository();
    
    return MessageWindowChatMemory.builder()
            .chatMemoryRepository(inMemoryChatMemoryRepository)
            .maxMessages(2) // Keeps only the 2 most recent messages
            .build();
}
```

**📌 Purpose:**
It acts as a temporary storage map within the application's RAM to hold conversation histories mapped by a session ID.

✅ **Pros:**
- **Extremely Fast:** Data is retrieved directly from RAM with zero network latency.
- **Easy Setup:** No need to install, configure, or connect to external databases.
- **Great for Testing:** Perfect for local development and verifying memory logic.

❌ **Cons:**
- **Volatile:** All chat history is permanently lost when the application restarts or crashes.
- **Not Scalable:** Does not work in a multi-instance (distributed) environment, as memory is localized to one specific server.

---

## 🗃️ 7. Vector Store, Embeddings & RAG

To make our AI smarter about our own private data, we implemented a **RAG (Retrieval-Augmented Generation)** architecture using embeddings and a Vector Store!

### 🧬 What is an Embedding and a Vector?
- **Embedding:** An embedding is a process that translates text (words, sentences) into an array of numbers (a Vector) that captures the semantic meaning of that text.
- **Vector:** An array of floating-point numbers. If two sentences mean similar things, their vectors will be mathematically close to each other in a multidimensional space.

We use **Ollama** with the `nomic-embed-text` model to generate these embeddings!

### 🐬 MariaDB Vector Store
Instead of storing our data in memory, we persist our embeddings in a relational database optimized for vector search: **MariaDB**.
- MariaDB computes the **COSINE distance** to find vectors (sentences) that are semantically similar to the user's query.
- It is configured in our `application.properties` to automatically initialize the schema with a dimensionality of **768** (which matches `nomic-embed-text`).

### 📦 Storing Data via `Helper.java`
We created a `Helper` class to supply dummy document data (e.g., facts about the Java programming language).
We take these strings, wrap them into Spring AI `Document` objects, and save them directly into the MariaDB Vector Store:
```java
public void saveData(List<String> list) {
    List<Document> documentList = list.stream().map(Document::new).toList();
    this.vectorStore.add(documentList); // Automatically generates embeddings and saves to DB!
}
```

### 🔍 Similarity Search & RAG Advisors
When a user asks a question, we need to find relevant data in the DB and give it to the AI.
We do this using Spring AI's powerful Advisors (like `QuestionAnswerAdvisor` or the newer `RetrievalAugmentationAdvisor`)!

**How it works:**
1. The user asks a question.
2. The Advisor automatically takes the query and converts it to a vector.
3. It performs a **Similarity Search** against MariaDB to find the most relevant documents.
4. **Query Augmentation:** It uses the `ContextualQueryAugmenter` to take the retrieved documents and "augment" (inject) them into the AI's prompt. 
   - *Pro Tip:* By setting `.allowEmptyContext(true)`, we guarantee that if the database finds *no* matching documents, the application won't fail. Instead, it allows the AI to gracefully fall back on its own baseline knowledge to answer the question!

```java
public String chatTemplate(String query, String userId) {
    // Creating the Advisor to handle retrieval
    var ragAdvisor = RetrievalAugmentationAdvisor.builder()
            .documentRetriever(VectorStoreDocumentRetriever.builder()
                    .vectorStore(vectorStore)
                    .topK(3)
                    .similarityThreshold(0.5)
                    .build())
            .queryAugmenter(ContextualQueryAugmenter.builder().allowEmptyContext(true).build())
            .build();

    return this.chatClient
            .prompt()
            // This single line automates the entire RAG flow!
            .advisors(ragAdvisor)
            .user(user -> user.text(this.userMessage).param("query", query))
            .call()
            .content();
}
```

### 🛠️ Manual Similarity Search (Alternative Approach)
Instead of using automated Advisors, we can also perform a **Manual Similarity Search**. This is highly useful when you need finer control over the context building, or want to modify the documents before sending them to the LLM.

**How it works:**
1. We manually build a `SearchRequest` specifying parameters like `topK` (number of results) and `similarityThreshold`.
2. We query the `vectorStore` to get a list of `Document`s.
3. We extract the text from these documents and join them into a single `contextData` string.
4. We inject this `contextData` explicitly into our `SystemMessage` prompt template using `.param()`.

```java
public String chatTemplate(String query, String userId) {
    // 1. Manually build the search request
    SearchRequest searchRequest = SearchRequest.builder()
            .topK(3)
            .similarityThreshold(0.5)
            .query(query)
            .build();

    // 2. Perform similarity search against MariaDB
    List<Document> documents = this.vectorStore.similaritySearch(searchRequest);
    
    // 3. Extract text and format as context string
    List<String> documentList = documents.stream().map(Document::getText).toList();
    String contextData = String.join(",", documentList);

    // 4. Inject contextData explicitly into the prompt
    return this.chatClient
            .prompt()
            .system(system -> system.text(this.systemMessage).param("documents", contextData))
            .user(user -> user.text(this.userMessage).param("query", query))
            .call()
            .content();
}
```

---

<div align="center">
  <h3>Ready to start building? 🚀</h3>
  <p>If this repository helped you learn Spring AI, give it a ⭐ on GitHub!</p>
  <p>
    <a href="https://docs.spring.io/spring-ai/reference/">Spring AI Documentation</a> •
    <a href="https://console.groq.com/">Groq Cloud</a>
  </p>
  <p>Crafted seamlessly combining Java & AI! 🤖☕</p>
</div>
