package com.mokujin.ssi.controller;


import com.mokujin.ssi.model.chat.Chat;
import com.mokujin.ssi.model.chat.Message;
import com.mokujin.ssi.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/get/{connectionNumber}")
    public ResponseEntity<Chat> get(@PathVariable String connectionNumber,
                                    @RequestParam("public") String publicKey,
                                    @RequestParam("private") String privateKey) {
        log.info("'get' invoked with params '{}, {}, {}'", connectionNumber, publicKey, privateKey);

        Chat chat = chatService.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            get(publicKey, privateKey, connectionNumber);

        log.info("'get' returned '{}'", chat);
        return ResponseEntity.ok(chat);
    }

    @PostMapping("/add/{connectionNumber}")
    public ResponseEntity<Chat> addMessage(@PathVariable String connectionNumber,
                                           @RequestBody Message message,
                                           @RequestParam("public") String publicKey,
                                           @RequestParam("private") String privateKey) {
        log.info("'addMessage' invoked with params '{}, {}, {}, {}'", connectionNumber, message, publicKey, privateKey);

        Chat chat = chatService.addMessage(publicKey, privateKey, connectionNumber, message);

        log.info("'addMessage' returned '{}'", chat);
        return ResponseEntity.ok(chat);
    }

}
