package com.mokujin.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
public class TestController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/test-alive")
    public String test1() {
        log.info("invoked test1!!!!!!!!!");

        String result = restTemplate.getForObject("http://test-service/test-integration", String.class);

        return result + " good";
    }

/*    @GetMapping("/test-auth")
    public ResponseEntity<String> firstPage(@AuthenticationPrincipal(expression = "username") String username) {
        log.info("username = '{}'", username);
        return ResponseEntity.ok(username);
    }*/
}