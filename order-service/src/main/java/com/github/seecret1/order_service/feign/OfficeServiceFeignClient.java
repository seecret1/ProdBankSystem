package com.github.seecret1.order_service.feign;

import com.github.seecret1.order_service.config.feign.FeignConfig;
import com.github.seecret1.order_service.dto.office.OfficeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "office-service",
        url = "${office-service.client.base-url}",
        configuration = FeignConfig.class
)
public interface OfficeServiceFeignClient {

    @GetMapping("/api/v1/office/services/find-by-city/{city}")
    OfficeResponse findOfficeByCity(@PathVariable String city);
}
