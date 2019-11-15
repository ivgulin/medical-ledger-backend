package com.mokujin.documentation.controller;

import com.mokujin.documentation.service.ServiceDefinitionsContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ServiceDefinitionController {

    private final ServiceDefinitionsContext definitionContext;

    @GetMapping("/service/{servicename}")
    public String getServiceDefinition(@PathVariable("servicename") String serviceName) {
        return definitionContext.getSwaggerDefinition(serviceName + "-service");
    }
}