package com.mokujin.user.service.impl;

import com.mokujin.user.model.chat.Chat;
import com.mokujin.user.model.chat.Message;
import com.mokujin.user.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final RestTemplate restTemplate;

    @Override
    @SneakyThrows
    public Chat get(String publicKey, String privateKey, String connectionNumber) {
        String url = "http://self-sovereign-identity-service/chat/get/" + connectionNumber +
                "?public=" + publicKey +
                "&private=" + privateKey;
        return restTemplate.getForObject(url, Chat.class);
    }

    @Override
    @SneakyThrows
    public Chat addMessage(String publicKey, String privateKey, String connectionNumber, Message message) {
        String url = "http://self-sovereign-identity-service/chat/add/" + connectionNumber +
                "?public=" + publicKey +
                "&private=" + privateKey;
        return restTemplate.postForObject(url, message, Chat.class);
    }
}
