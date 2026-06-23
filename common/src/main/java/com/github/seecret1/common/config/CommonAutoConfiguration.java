package com.github.seecret1.common.config;

import com.github.seecret1.common.annotation.EnableCommonConfig;
import com.github.seecret1.common.config.CacheConfig;
import com.github.seecret1.common.config.RedisConfig;
import com.github.seecret1.common.config.SwaggerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass(EnableCommonConfig.class)
@Import({
        CacheConfig.class,
        RedisConfig.class,
        SwaggerConfig.class
})
public class CommonAutoConfiguration {
}
