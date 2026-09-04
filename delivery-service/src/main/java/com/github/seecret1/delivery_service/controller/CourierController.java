package com.github.seecret1.delivery_service.controller;

import com.github.seecret1.delivery_service.dto.CourierDto;
import com.github.seecret1.delivery_service.service.CourierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/private/couriers")
@RequiredArgsConstructor
public class CourierController {

    private final CourierService courierService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<CourierDto> create(
            @Valid @RequestBody CourierDto dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(courierService.create(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<List<CourierDto>> findAll() {
        return ResponseEntity.ok(courierService.findAll());
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<CourierDto> findAvailable() {
        return ResponseEntity.ok(courierService.findAvailable());
    }

    @DeleteMapping("/{courierId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String courierId) {
        courierService.delete(courierId);
        return ResponseEntity.noContent().build();
    }
}