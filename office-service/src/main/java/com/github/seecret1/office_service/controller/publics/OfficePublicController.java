package com.github.seecret1.office_service.controller.publics;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.office_service.dto.response.OfficeResponse;
import com.github.seecret1.office_service.service.OfficeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/offices")
@Tag(name = "Office Service Management", description = "API for managing office")
public class OfficePublicController {

    // TODO: вынести
    private static final String X_INTERNAL_API_KEY = "X-Internal-Api-Key";

    private final OfficeService officeService;

    @GetMapping("/services/{city}")
    @Operation(summary = "Find offices by city", description = "Find offices by city")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success find offices"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    public List<OfficeResponse> findOfficesByCity(
            @PathVariable String city,
            @RequestHeader(value = X_INTERNAL_API_KEY, required = false) String apiKey
    ) {
        return officeService.findOfficesByCity(city, apiKey);
    }
}
