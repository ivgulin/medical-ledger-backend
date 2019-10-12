package com.mokujin.documentation.config;


import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "swagger")
public class ExcludedServicesNames {

    @Getter
    private List<String> excluded = new ArrayList<>();

}