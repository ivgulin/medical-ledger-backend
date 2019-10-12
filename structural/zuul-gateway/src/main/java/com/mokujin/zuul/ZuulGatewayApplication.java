package com.mokujin.zuul;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties
@EnableEurekaClient
@EnableZuulProxy
@ComponentScan("com.mokujin.**")
public class ZuulGatewayApplication {

    public static void main(String[] args) {
        log.info("app is running");
        SpringApplication.run(ZuulGatewayApplication.class, args);
    }
}
