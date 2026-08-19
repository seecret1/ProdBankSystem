package com.github.seecret1.delivery_service.service;

import com.github.seecret1.delivery_service.dto.CourierDto;
import com.github.seecret1.delivery_service.entity.Courier;

import java.util.List;

public interface CourierService {

    CourierDto create(CourierDto dto);

    List<CourierDto> findAll();

    CourierDto findAvailable();

    Courier assignFirstAvailable();

    CourierDto setBusy(String courierId, boolean busy);

    void release(Courier courier);

    void delete(String courierId);
}