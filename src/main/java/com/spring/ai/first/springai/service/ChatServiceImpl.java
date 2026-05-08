package com.spring.ai.first.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;

    // it is pointing to the user-message.txt file which is in the prompts folder
    @Value("classpath:prompts/user-message.txt")
    private Resource userMessage;

    // it is pointing to the system-message.txt file which is in the prompts folder
    @Value("classpath:prompts/system-message.txt")
    private Resource systemMessage;

    public ChatServiceImpl(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public String chatTemplate(String query) {

        return this.chatClient
                .prompt()
                .system(system -> system.text(this.systemMessage))
                .user(user -> user.text(this.userMessage).param("concept", query))
                .call()
                .content();
    }

}
