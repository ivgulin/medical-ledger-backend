package com.mokujin.user.service.impl;

import com.mokujin.user.model.chat.Chat;
import com.mokujin.user.model.chat.Message;
import com.mokujin.user.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final RestTemplate restTemplate;
    private final RedissonClient redissonClient;

    @Override
    @SneakyThrows
    public Chat get(String publicKey, String privateKey, String connectionNumber) {

        String url = "http://self-sovereign-identity-service/chat/get/" + connectionNumber +
                "?public=" + publicKey +
                "&private=" + privateKey;
        return restTemplate.getForObject(url, Chat.class);
    }

    @Override
    public Chat addMessage(String publicKey, String privateKey, String connectionNumber, Message message) {

        String url = "http://self-sovereign-identity-service/chat/add/" + connectionNumber +
                "?public=" + publicKey +
                "&private=" + privateKey;
        Chat chat = restTemplate.postForObject(url, message, Chat.class);

        RList<Message> messages = redissonClient.getList("messages_" + connectionNumber);
        messages.remove(message);
        return chat;
    }

    @Override
    public Chat addMessageWithNotification(String publicKey, String privateKey, String connectionNumber, Message message) {

        String url = "http://self-sovereign-identity-service/chat/add/" + connectionNumber +
                "?public=" + publicKey +
                "&private=" + privateKey;
        Chat chat = restTemplate.postForObject(url, message, Chat.class);

        RList<Message> messages = redissonClient.getList("messages_" + connectionNumber);
        messages.add(message);

        return chat;
    }
}
