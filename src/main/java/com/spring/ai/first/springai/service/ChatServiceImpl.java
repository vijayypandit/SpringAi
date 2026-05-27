package com.spring.ai.first.springai.service;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
// import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ChatServiceImpl.class);

    // it is pointing to the user-message.txt file which is in the prompts folder
    @Value("classpath:prompts/user-message.txt")
    private Resource userMessage;

    // it is pointing to the system-message.txt file which is in the prompts folder
    @Value("classpath:prompts/system-message.txt")
    private Resource systemMessage;

    private VectorStore vectorStore;

    public ChatServiceImpl(ChatClient chatClient, VectorStore vectorStore) {

        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    @Override
    public String chatTemplate(String query, String userId) {
        // load data from vector store
        // SearchRequest searchRequest = SearchRequest.builder()
        // .topK(3)
        // .similarityThreshold(0.6)
        // .query(query)
        // .build();

        // List<Document> documents = this.vectorStore.similaritySearch(searchRequest);
        // List<String> documentList =
        // documents.stream().map(Document::getText).toList();
        // String contextData = String.join(",", documentList);
        // logger.info("Context data: {}", contextData);

        var ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever
                        .builder()
                        .vectorStore(vectorStore)
                        .topK(3)
                        .similarityThreshold(0.5)
                        .build())
                .queryAugmenter(ContextualQueryAugmenter.builder().allowEmptyContext(true).build())
                .build();

        return this.chatClient
                .prompt()
                // .system(system -> system.text(this.systemMessage).param("documents",
                // contextData))
                // .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .advisors(ragAdvisor)
                .user(user -> user.text(this.userMessage).param("query", query))
                .call()
                .content();
    }

    /*
     * This Flux is used for stream the response from the chat client
     * It will give the response in the form of stream
     */
    @Override
    public Flux<String> streamChat(String query) {

        return this.chatClient
                .prompt()
                .system(system -> system.text(this.systemMessage))
                .user(user -> user.text(this.userMessage).param("concept", query))
                .stream()
                .content();

    }

    @Override
    public void saveData(List<String> list) {

        List<Document> documentList = list.stream()
                .map(Document::new).toList();
        this.vectorStore.add(documentList);

    }
}
