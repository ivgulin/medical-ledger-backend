package com.mokujin.government.config;

import com.zaxxer.hikari.HikariDataSource;
import liquibase.integration.spring.SpringLiquibase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class LiquibaseConfig {

    private final HikariDataSource datasource;

    @Bean
    public SpringLiquibase liquibase(){
        SpringLiquibase springLiquibase = new SpringLiquibase();

        springLiquibase.setDataSource(datasource);
        springLiquibase.setChangeLog("classpath:changelog/changelog-master.xml");
        springLiquibase.setDefaultSchema("public");

        return springLiquibase;
    }
}