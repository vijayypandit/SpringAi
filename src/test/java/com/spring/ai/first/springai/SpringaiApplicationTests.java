package com.spring.ai.first.springai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.spring.ai.first.springai.helper.Helper;
import com.spring.ai.first.springai.service.ChatService;

@SpringBootTest
class SpringaiApplicationTests {

    @Autowired
    private ChatService chatService;

    @Test
    void saveDataTOVectorDB() {

        System.out.println("Saving data to vector store...");
        this.chatService.saveData(Helper.getData());
        System.out.println("Data saved to vector store...");

    }
}
