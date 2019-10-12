package com.mokujin.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {


    @GetMapping("/test-integration")
    public String test1() {
        log.info("invoked test1!!!!!!!!!");
        return "Integration went";
    }
}