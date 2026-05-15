package com.spring.ai.first.springai.config;

import java.util.List;

import com.spring.ai.first.springai.advisors.TokenPrintAdvisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

        @Bean
        public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
                return MessageWindowChatMemory.builder()
                                .chatMemoryRepository(jdbcChatMemoryRepository)
                                .maxMessages(2)
                                .build();
        }

        private Logger logger = LoggerFactory.getLogger(AiConfig.class);

        /**
         * Chat Client for OpenAI
         * 
         * @param builder
         * @return ChatClient
         */
        @Bean
        public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {

                this.logger.info("ChatMemoyImplementation class: " + chatMemory.getClass().getName());

                MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory)
                                .build();

                return builder
                                .defaultAdvisors(messageChatMemoryAdvisor, new TokenPrintAdvisor(),
                                                new SafeGuardAdvisor(List.of("games")))
                                .defaultSystem("you are a helpful assistant as a coding expert in java")
                                .defaultOptions(OpenAiChatOptions.builder()
                                                .model("meta-llama/llama-4-scout-17b-16e-instruct")
                                                .temperature(1.0)
                                                .maxTokens(200)
                                                .build())
                                .build();
        }

        // OpenAI Chat Client
        // @Bean(name = "openAiChatClient")
        // public ChatClient openAiChatModel(OpenAiChatModel chatModel) {
        // return ChatClient.builder(chatModel).build();
        // }

        // Ollama Chat Client
        // @Bean(name = "ollamaChatClient")
        // public ChatClient ollamaChatModel(OllamaChatModel chatModel) {
        // return ChatClient.builder(chatModel).build();
        // }
}
