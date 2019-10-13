package com.mokujin.oauth2;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableAutoConfiguration
@EnableEurekaClient
@ComponentScan("com.mokujin.**")
public class Oauth2Application {

    public static void main(String[] args) {
        log.info("app is running");
        SpringApplication.run(Oauth2Application.class, args);
    }
}
