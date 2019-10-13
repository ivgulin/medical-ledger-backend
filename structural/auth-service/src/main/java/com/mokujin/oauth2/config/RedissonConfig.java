package com.mokujin.oauth2.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient(@Value("${redis.host}") final String redisHost) {

        Config config = new Config();
        config
                .useSingleServer()
                .setAddress(redisHost);

        return Redisson.create(config);
    }
}
