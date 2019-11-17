package com.mokujin.user.controller;

import com.mokujin.user.model.User;
import com.mokujin.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserConroller {

    private final UserService userService;

    @GetMapping("/get")
    public ResponseEntity<User> get(@RequestHeader("Public-Key") String publicKey,
                                    @RequestHeader("Private-Key") String privateKey) {
        log.info("'get' invoked with params '{}, {}'", publicKey, privateKey);

        User user = userService.get(publicKey, privateKey);

        log.info("'get' returned '{}'", user);
        return ResponseEntity.ok(user);
    }
}
