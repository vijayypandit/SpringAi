package com.spring.ai.first.springai.advisors;

import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;

public class TokenPrintAdvisor implements CallAdvisor, StreamAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(TokenPrintAdvisor.class);

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        logger.info("My token advisor is called");
        logger.info("Request: " + chatClientRequest.prompt().getContents());
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
        logger.info("Token Advisor : Response received from Model...........");
        logger.info(
                "Response  : " + chatClientResponse.chatResponse().getResult().getOutput().getText());
        // logger.info(
        // "Prompt Tokens: " +
        // chatClientResponse.chatResponse().getResult().getMetadata().getPromptTokens());
        // logger.info(
        // "Response Tokens: "
        // +
        // chatClientResponse.chatResponse().getResult().getMetadata().getResponseTokens());
        logger.info(
                "Total Tokens: "
                        + chatClientResponse
                                .chatResponse()
                                .getMetadata()
                                .getUsage()
                                .getTotalTokens());

        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
            StreamAdvisorChain streamAdvisorChain) {
        logger.info("My stream advisor is called ........ ....................");
        Flux<ChatClientResponse> chatClientResponseFlux = streamAdvisorChain.nextStream(chatClientRequest);
        return chatClientResponseFlux;
    }

    @Override
    public String getName() {
        return "TokenPrintAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
