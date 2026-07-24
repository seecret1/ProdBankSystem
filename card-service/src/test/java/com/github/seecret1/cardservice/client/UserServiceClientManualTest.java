package com.github.seecret1.cardservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.seecret1.cardservice.dto.user.UserResponse;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import feign.Feign;
import feign.RequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.EncodeException;
import feign.RequestTemplate;
import java.io.InputStream;
import com.fasterxml.jackson.databind.JavaType;
import feign.slf4j.Slf4jLogger;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Instant;
import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class UserServiceClientManualTest {

    private UserServiceClient userServiceClient;

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    void setUp() {
        RequestInterceptor interceptor = requestTemplate -> requestTemplate.header("X-Internal-Api-Key", "test-api-key");

        Decoder decoder = (response, type) -> {
            if (response == null || response.body() == null) return null;
            JavaType javaType = mapper.getTypeFactory().constructType(type);
            try (InputStream is = response.body().asInputStream()) {
                return mapper.readValue(is, javaType);
            }
        };

        Encoder encoder = new Encoder() {
            @Override
            public void encode(Object object, java.lang.reflect.Type bodyType, RequestTemplate template) throws EncodeException {
                try {
                    String json = mapper.writeValueAsString(object);
                    template.body(json);
                } catch (Exception e) {
                    throw new EncodeException(e.getMessage(), e);
                }
            }
        };

        this.userServiceClient = Feign.builder()
                .contract(new SpringMvcContract())
                .encoder(encoder)
                .decoder(decoder)
                .logger(new Slf4jLogger(UserServiceClient.class))
                .requestInterceptor(interceptor)
                .target(UserServiceClient.class, wireMockExtension.baseUrl());
    }

    @Test
    void findUserById_shouldReturnUserAndSendApiKeyHeader() throws Exception {
        String userId = "123";

        UserResponse expected = new UserResponse(
                userId,
                "jdoe",
                "ACTIVE",
                "jdoe@example.com",
                "John",
                "Doe",
                null,
                LocalDate.of(1990, 1, 1),
                "USER",
                Instant.now(),
                Instant.now(),
                false,
                null,
                null
        );

        String body = mapper.writeValueAsString(expected);

        wireMockExtension.stubFor(get(urlEqualTo("/api/v1/public/users/services/" + userId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)
                ));

        UserResponse actual = userServiceClient.findUserById(userId);

        assertThat(actual).isNotNull();
        assertThat(actual.id()).isEqualTo(expected.id());
        assertThat(actual.username()).isEqualTo(expected.username());

        wireMockExtension.verify(getRequestedFor(urlEqualTo("/api/v1/public/users/services/" + userId))
                .withHeader("X-Internal-Api-Key", equalTo("test-api-key")));
    }

}