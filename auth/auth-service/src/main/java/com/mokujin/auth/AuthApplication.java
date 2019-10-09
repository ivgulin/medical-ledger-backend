package com.mokujin.auth;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableAutoConfiguration
@ComponentScan({
        "com.mokujin.*"
})
public class AuthApplication {

    public static void main(String[] args) {
        log.info("app is running");
        SpringApplication.run(AuthApplication.class, args);
    }
}
