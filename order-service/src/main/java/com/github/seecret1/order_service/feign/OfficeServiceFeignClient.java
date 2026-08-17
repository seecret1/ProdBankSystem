package com.github.seecret1.order_service.feign;

import com.github.seecret1.order_service.config.feign.FeignConfig;
import com.github.seecret1.order_service.dto.office.OfficeMainResponse;
import com.github.seecret1.order_service.dto.office.OfficeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name = "office-service",
        url = "${office-service.client.base-url}",
        configuration = FeignConfig.class
)
public interface OfficeServiceFeignClient {

    @GetMapping("/services/{city}")
    List<OfficeResponse> findOfficeByCity(@PathVariable String city);

    @GetMapping("/services/main")
    OfficeMainResponse findMainOfficeNearestByCity();
}
