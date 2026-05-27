package com.spring.ai.first.springai.service;

import java.util.List;

import reactor.core.publisher.Flux;

public interface ChatService {

    public String chatTemplate(String query, String userId);

    public Flux<String> streamChat(String query);

    void saveData(List<String> list);
}
