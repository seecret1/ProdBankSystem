package com.github.seecret1.office_service.feign;

import com.github.seecret1.office_service.config.feign.FeignConfig;
import com.github.seecret1.office_service.dto.user.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        url = "${user-service.client.base-url}",
        configuration = FeignConfig.class
)
public interface UserServiceFeignClient {

    @GetMapping("/api/v1/public/users/services/{id}")
    UserResponse findUserById(@PathVariable String id);
}
