package com.github.seecret1.delivery_service.controller;

import com.github.seecret1.delivery_service.dto.CourierDto;
import com.github.seecret1.delivery_service.service.CourierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/couriers")
@RequiredArgsConstructor
public class PublicCourierController {

    private final CourierService courierService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<CourierDto> register(
            @Valid @RequestBody CourierDto dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(courierService.register(dto));
    }

    @PatchMapping("/{courierId}/availability")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<CourierDto> setAvailability(
            @PathVariable String courierId,
            @RequestParam boolean busy
    ) {
        return ResponseEntity.ok(courierService.setBusy(courierId, busy));
    }
}
