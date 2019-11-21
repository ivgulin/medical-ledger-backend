package com.mokujin.user.service.impl;

import com.mokujin.user.model.chat.Chat;
import com.mokujin.user.model.chat.Message;
import com.mokujin.user.model.notification.extention.ChatNotification;
import com.mokujin.user.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {


    @Mock
    private NotificationService notificationService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ChatServiceImpl chatService;


    @Test
    void get_validInputs_chatIsReturned() {

        Chat chat = new Chat();

        when(restTemplate.getForObject(anyString(), any())).thenReturn(chat);

        Chat result = chatService.get("public", "private", "number");

        assertEquals(chat, result);
    }

    @Test
    void addMessage_validInputs_chatIsReturned() {
        Message message = new Message();

        Chat chat = new Chat();
        ArrayList<Message> messages = new ArrayList<>();
        messages.add(message);
        chat.setMessages(messages);

        when(restTemplate.postForObject(anyString(), any(), any())).thenReturn(chat);

        String number = "number";
        Chat result = chatService.addMessage("public", "private", number, number, message);

        assertEquals(chat, result);
    }

    @Test
    void addMessageWithNotification_validInputs_chatIsReturned() {
        Message message = new Message();

        Chat chat = new Chat();
        ArrayList<Message> messages = new ArrayList<>();
        messages.add(message);
        chat.setMessages(messages);

        String number = "number";

        when(restTemplate.postForObject(anyString(), any(), any())).thenReturn(chat);
        when(notificationService.addMessage(number, message)).thenReturn(new ChatNotification(message));

        Chat result = chatService.addMessageWithNotification("public", "private", number, message);

        assertEquals(chat, result);
        verify(notificationService, times(1)).addMessage(number, message);
    }

}