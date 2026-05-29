package com.github.seecret1.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneOffset;

@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneOffset.UTC);
    }
}
