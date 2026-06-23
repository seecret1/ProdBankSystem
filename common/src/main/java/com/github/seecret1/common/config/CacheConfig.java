package com.github.seecret1.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@ConditionalOnClass(RedisConnectionFactory.class)
@ConditionalOnBean(RedisConnectionFactory.class)
@EnableConfigurationProperties(ApplicationCacheProperties.class)
public class CacheConfig {

    @Bean
    public CacheManager redisCacheManager(
            ApplicationCacheProperties applicationCacheProperties,
            RedisConnectionFactory connectionFactory
    ) {
        RedisSerializer<Object> jsonSerializer = RedisSerializer.json();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
                );

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        applicationCacheProperties.getCacheNames().forEach(cacheName -> {
            Duration ttl = applicationCacheProperties.getCaches()
                    .getOrDefault(cacheName, new ApplicationCacheProperties.CacheProperties())
                    .getExpiry();

            RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                    .disableCachingNullValues()
                    .serializeKeysWith(
                            RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                    )
                    .serializeValuesWith(
                            RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
                    );

            if (ttl != null && !ttl.isZero()) {
                config = config.entryTtl(ttl);
            }

            cacheConfigs.put(cacheName, config);
        });

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}