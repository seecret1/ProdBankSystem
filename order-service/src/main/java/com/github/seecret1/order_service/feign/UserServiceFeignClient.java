package com.github.seecret1.order_service.feign;

import com.github.seecret1.order_service.config.feign.FeignConfig;
import com.github.seecret1.order_service.dto.user.PersonInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        url = "${user-service.client.base-url}",
        configuration = FeignConfig.class
)
public interface UserServiceFeignClient {

    @GetMapping("/services/{userId}")
    PersonInfo getPersonInfo(@PathVariable String userId);
}
