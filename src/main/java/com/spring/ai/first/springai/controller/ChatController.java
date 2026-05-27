package com.spring.ai.first.springai.controller;

import com.spring.ai.first.springai.service.ChatService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@RestController
public class ChatController {

    private ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // private ChatClient openAiChatClient;
    // public ChatController(@Qualifier("openAiChatClient") ChatClient
    // openAiChatClient) {
    // this.openAiChatClient = openAiChatClient;
    // }
    // public ChatController(ChatClient.Builder chatClientBuilder) {
    // this.chatclient = chatClientBuilder.build();
    // }
    // public ChatController(OpenAiChatModel openAiChatModel) {
    // this.openAiChatClient = ChatClient
    // .builder(openAiChatModel)
    // .build();
    // }
    // @GetMapping("/chat")
    @GetMapping("/chat")
    public ResponseEntity<String> chat(@RequestParam(value = "q", required = true) String q,
            @RequestHeader("userId") String userId) {

        return ResponseEntity.ok(chatService.chatTemplate(q, userId));
    }

    @GetMapping("/stream-chat")
    public ResponseEntity<Flux<String>> streamChat(
            @RequestParam(value = "q", required = true) String query) {

        return ResponseEntity.ok(this.chatService.streamChat(query));
    }

}
