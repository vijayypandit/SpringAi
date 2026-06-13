package com.spring.ai.first.springai.service;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

@Service
public class DataTransformerImpl implements DataTransformer {

    @Override
    public List<Document> transform(List<Document> documents) {
        System.out.println("DATA TRANSFORMER CALLED");
        TokenTextSplitter textSplitter = TokenTextSplitter.builder()
                .withChunkSize(50)
                .withMinChunkSizeChars(200)
                .withMinChunkLengthToEmbed(100)
                .withMaxNumChunks(1000)
                .withKeepSeparator(true)
                .build();

        return textSplitter.apply(documents);
    }

}
