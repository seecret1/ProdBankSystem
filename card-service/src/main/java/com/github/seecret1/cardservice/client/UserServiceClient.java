package com.github.seecret1.cardservice.client;

import com.github.seecret1.cardservice.dto.user.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service", url = "${services.user.url}")
public interface UserServiceClient {

    @GetMapping("/api/v1/users/{criterial}")
    UserResponse findUserByCriterial(@PathVariable String criterial);
}
