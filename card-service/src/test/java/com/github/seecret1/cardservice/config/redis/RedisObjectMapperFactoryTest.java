package com.github.seecret1.cardservice.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.seecret1.cardservice.config.JacksonConfig;
import com.github.seecret1.cardservice.dto.response.CardResponse;
import com.github.seecret1.cardservice.entity.enums.CardStatus;
import com.github.seecret1.cardservice.entity.enums.CardType;
import com.github.seecret1.common.dto.PageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RedisObjectMapperFactoryTest {

    @Test
    void shouldPreserveTypedPageResponseAfterRedisRoundTrip() {
        ObjectMapper baseMapper = new JacksonConfig().objectMapper();
        ObjectMapper redisMapper = RedisObjectMapperFactory.create(baseMapper);
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisMapper);

        PageResponse<CardResponse> source = new PageResponse<>(
                1L,
                1,
                List.of(new CardResponse(
                        "1111222233334444",
                        CardType.DEBIT,
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2028, 7, 1),
                        CardStatus.ACTIVE,
                        new BigDecimal("125.50"),
                        new BigDecimal("5000.00"),
                        "user-1"
                ))
        );

        byte[] payload = serializer.serialize(source);
        Object restored = serializer.deserialize(payload);

        PageResponse<?> restoredPage = assertInstanceOf(PageResponse.class, restored);
        assertInstanceOf(CardResponse.class, restoredPage.getData().get(0));
        assertEquals("1111222233334444", ((CardResponse) restoredPage.getData().get(0)).number());
        assertEquals(1L, restoredPage.getTotalElements());
        assertEquals(1, restoredPage.getTotalPages());
    }
}

