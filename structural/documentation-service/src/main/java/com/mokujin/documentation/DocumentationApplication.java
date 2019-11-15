package com.mokujin.documentation;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Slf4j
@SpringBootApplication
@EnableSwagger2
@EnableScheduling
@EnableEurekaClient
public class DocumentationApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentationApplication.class, args);
    }
}
