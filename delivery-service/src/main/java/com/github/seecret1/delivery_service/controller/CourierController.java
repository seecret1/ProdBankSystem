package com.github.seecret1.delivery_service.controller;

import com.github.seecret1.delivery_service.dto.CourierDto;
import com.github.seecret1.delivery_service.service.CourierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/couriers")
@RequiredArgsConstructor
public class CourierController {

    private final CourierService courierService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourierDto create(@Valid @RequestBody CourierDto dto) {
        return courierService.create(dto);
    }

    @GetMapping
    public List<CourierDto> findAll() {
        return courierService.findAll();
    }

    @GetMapping("/available")
    public CourierDto findAvailable() {
        return courierService.findAvailable();
    }

    @PatchMapping("/{courierId}/availability")
    public CourierDto setAvailability(
            @PathVariable String courierId,
            @RequestParam boolean busy
    ) {
        return courierService.setBusy(courierId, busy);
    }

    @DeleteMapping("/{courierId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String courierId) {
        courierService.delete(courierId);
    }
}