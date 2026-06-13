package com.spring.ai.first.springai.service;

import java.util.List;

public interface ChatService {

    String getResponse(String userQuery);

    void saveData(List<String> list);
}
