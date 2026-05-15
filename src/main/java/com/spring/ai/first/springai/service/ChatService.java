package com.spring.ai.first.springai.service;

import reactor.core.publisher.Flux;

public interface ChatService {

    public String chatTemplate(String query, String userId);

    public Flux<String> streamChat(String query);
}
