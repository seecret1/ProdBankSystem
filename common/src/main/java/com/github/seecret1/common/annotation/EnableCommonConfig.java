package com.github.seecret1.common.annotation;

import com.github.seecret1.common.config.CacheConfig;
import com.github.seecret1.common.config.RedisConfig;
import com.github.seecret1.common.config.SwaggerConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        CacheConfig.class,
        RedisConfig.class,
        SwaggerConfig.class
})
public @interface EnableCommonConfig {
}