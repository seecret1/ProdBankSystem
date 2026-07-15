package com.github.seecret1.cardservice.client;

import com.github.seecret1.cardservice.config.feign.FeignConfig;
import com.github.seecret1.cardservice.dto.user.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "user-service",
        url = "${user-service.client.base-url}",
        configuration = FeignConfig.class
)
public interface UserServiceClient {

    @GetMapping("/api/v1/public/users/services/{id}")
    UserResponse findUserById(@PathVariable String id);
}
