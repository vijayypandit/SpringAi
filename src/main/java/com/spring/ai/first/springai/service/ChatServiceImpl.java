package com.spring.ai.first.springai.service;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
// import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

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
        public void saveData(List<String> list) {

        }

        @Override
        public String getResponse(String userQuery) {

                var ragAdvisor = RetrievalAugmentationAdvisor.builder()
                                .queryTransformers(
                                                RewriteQueryTransformer.builder()
                                                                .chatClientBuilder(chatClient.mutate().clone())
                                                                .build(),
                                                TranslationQueryTransformer.builder()
                                                                .chatClientBuilder(chatClient.mutate().clone())
                                                                .targetLanguage("english")
                                                                .build())
                                .queryExpander(MultiQueryExpander.builder()
                                                .chatClientBuilder(chatClient.mutate().clone())
                                                .build())
                                .documentRetriever(
                                                VectorStoreDocumentRetriever.builder()
                                                                .vectorStore(vectorStore)
                                                                .topK(10)
                                                                .similarityThreshold(0.5)
                                                                .build())
                                .documentJoiner(new ConcatenationDocumentJoiner())
                                .queryAugmenter(ContextualQueryAugmenter
                                                .builder()
                                                .build())
                                .build();

                // actual call to LLM
                return chatClient.prompt()
                                .advisors(ragAdvisor)
                                .user(userQuery)
                                .call()
                                .content();

        }
}
