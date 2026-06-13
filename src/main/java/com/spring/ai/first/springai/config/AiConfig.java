package com.spring.ai.first.springai.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

        private Logger logger = LoggerFactory.getLogger(AiConfig.class);

        /**
         * Chat Client for OpenAI
         *
         * @param builder
         * @return ChatClient
         */
        @Bean
        public ChatClient chatClient(ChatClient.Builder builder) {

                return builder
                                .defaultAdvisors(new SimpleLoggerAdvisor(),
                                                new SafeGuardAdvisor(List.of("games")))
                                // .defaultSystem("you are a helpful assistant as a coding expert in java")
                                .defaultOptions(OpenAiChatOptions.builder()
                                                .model("meta-llama/llama-4-scout-17b-16e-instruct")
                                                .temperature(1.0)
                                                .maxTokens(200)
                                                .build())
                                .build();
        }

}
