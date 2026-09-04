package com.github.seecret1.userservice.config.custom;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "user-service.passport")
public class PassportProperties {

    private int firstAge;

    private int secondAge;

    private int daysNotified;

    private int daysThreshold;
}
