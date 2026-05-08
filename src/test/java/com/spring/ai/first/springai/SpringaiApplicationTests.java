package com.spring.ai.first.springai;

import com.spring.ai.first.springai.service.ChatService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringaiApplicationTests {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SpringaiApplicationTests.class);

	@Test
	void contextLoads() {
	}

	@Autowired
	private ChatService chatService;

	// @Test
	// void testTemplateRenderer() {
	// var output = this.chatService.chatTemplate();
	// System.out.println(output);
	// }
}
