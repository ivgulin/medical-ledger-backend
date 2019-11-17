package com.mokujin.user.controller;

import com.mokujin.user.model.chat.Chat;
import com.mokujin.user.model.chat.Message;
import com.mokujin.user.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/get/{connectionNumber}")
    public ResponseEntity<Chat> get(@PathVariable String connectionNumber,
                                    @PathVariable String userNumber,
                                    @RequestParam String notificationToken,
                                    @RequestHeader("Public-Key") String publicKey,
                                    @RequestHeader("Private-Key") String privateKey) {
        log.info("'get' invoked with params '{}, {}, {}, {}'", connectionNumber, notificationToken, publicKey, privateKey);

        Chat chat = chatService.get(publicKey, privateKey, connectionNumber, userNumber, notificationToken);

        log.info("'get' returned '{}'", chat);
        return ResponseEntity.ok(chat);
    }

    @PostMapping("/add/{connectionNumber}")
    public ResponseEntity<Chat> addMessage(@PathVariable String connectionNumber,
                                           @RequestBody @Valid Message message,
                                           @RequestParam String notificationToken,
                                           @RequestHeader("Public-Key") String publicKey,
                                           @RequestHeader("Private-Key") String privateKey) {
        log.info("'addMessage' invoked with params '{}, {}, {}, {}, {}'", connectionNumber, notificationToken,
                message, publicKey, privateKey);

        Chat chat = chatService.addMessage(publicKey, privateKey, connectionNumber, message);

        log.info("'addMessage' returned '{}'", chat);
        return ResponseEntity.ok(chat);
    }

    @PostMapping("/add/{connectionNumber}/notification")
    public ResponseEntity<Chat> addMessageWithNotification(@PathVariable String connectionNumber,
                                                           @RequestBody @Valid Message message,
                                                           @RequestParam String notificationToken,
                                                           @RequestHeader("Public-Key") String publicKey,
                                                           @RequestHeader("Private-Key") String privateKey) {
        log.info("'addMessageWithNotification' invoked with params '{}, {}, {}, {}, {}'", connectionNumber,
                notificationToken, message, publicKey, privateKey);

        Chat chat = chatService.addMessageWithNotification(publicKey, privateKey, message.getContact(), message);

        log.info("'addMessageWithNotification' returned '{}'", chat);
        return ResponseEntity.ok(chat);
    }

}
