package com.mokujin.ssi.controller;

import com.mokujin.ssi.model.user.response.User;
import com.mokujin.ssi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserConroller {

    private final UserService userService;

    @SneakyThrows
    @GetMapping("/get")
    public ResponseEntity<User> get(@RequestParam("public") String publicKey,
                                    @RequestParam("private") String privateKey) {
        log.info("'get' invoked with params '{}, {}'", publicKey, privateKey);

        User user = userService.get(publicKey, privateKey);

        log.info("'get' returned '{}'", user);
        return ResponseEntity.ok(user);
    }
}
