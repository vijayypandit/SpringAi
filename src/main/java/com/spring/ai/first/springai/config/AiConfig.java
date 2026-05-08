package com.spring.ai.first.springai.config;

import java.util.List;

import com.spring.ai.first.springai.advisors.TokenPrintAdvisor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

        /**
         * Chat Client for OpenAI
         * 
         * @param builder
         * @return ChatClient
         */
        @Bean
        public ChatClient chatClient(ChatClient.Builder builder) {
                return builder
                                .defaultAdvisors(new TokenPrintAdvisor(), new SafeGuardAdvisor(List.of("games"))) // using
                                                                                                                  // to
                                                                                                                  // log
                                // prompt and
                                // response
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
