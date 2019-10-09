package com.mokujin.auth.controller;

import com.mokujin.auth.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {

    @GetMapping("/test")
    public ResponseEntity<User> firstPage(@AuthenticationPrincipal User user) {
        log.info("user = '{}'", user);
        return ResponseEntity.ok(user);
    }
}