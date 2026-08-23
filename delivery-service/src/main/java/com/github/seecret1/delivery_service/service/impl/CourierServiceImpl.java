package com.github.seecret1.delivery_service.service.impl;

import com.github.seecret1.delivery_service.dto.CourierDto;
import com.github.seecret1.delivery_service.entity.Courier;
import com.github.seecret1.delivery_service.exception.DeliveryException;
import com.github.seecret1.delivery_service.mapper.CourierMapper;
import com.github.seecret1.delivery_service.repository.CourierRepository;
import com.github.seecret1.delivery_service.service.CourierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourierServiceImpl implements CourierService {

    private final CourierRepository courierRepository;

    private final CourierMapper courierMapper;

    @Override
    @Transactional
    public CourierDto create(CourierDto dto) {
        if (courierRepository.existsByUserId(dto.userId())) {
            throw new DeliveryException("Courier with userId: %s already exists", dto.userId());
        }
        Courier courier = courierMapper.toEntity(dto);
        courierRepository.save(courier);
        log.info("Courier created: id={}, userId={}", courier.getId(), courier.getUserId());
        return courierMapper.toDto(courier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourierDto> findAll() {
        return courierRepository.findAllByDeletedFalseOrderByCreatedAtAsc().stream()
                .map(courierMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CourierDto findAvailable() {
        return courierRepository.findFirstByBusyFalseAndDeletedFalseOrderByCreatedAtAsc()
                .map(courierMapper::toDto)
                .orElseThrow(() -> new DeliveryException("No available couriers"));
    }

    @Override
    @Transactional
    public Courier assignFirstAvailable() {
        var courier = courierRepository.findFirstByBusyFalseAndDeletedFalseOrderByCreatedAtAsc()
                .orElseThrow(() -> new DeliveryException("No available couriers"));
        courier.setBusy(true);
        return courier;
    }

    @Override
    @Transactional
    public CourierDto setBusy(String courierId, boolean busy) {
        Courier courier = findActive(courierId);
        courier.setBusy(busy);
        log.info("Courier {} marked busy={}", courierId, busy);
        return courierMapper.toDto(courier);
    }

    @Override
    @Transactional
    public void release(Courier courier) {
        courier.setBusy(false);
        log.info("Courier {} released", courier.getId());
    }

    @Override
    @Transactional
    public void delete(String courierId) {
        Courier courier = findActive(courierId);
        courier.softDelete("system");
        log.info("Courier {} soft deleted", courierId);
    }

    private Courier findActive(String courierId) {
        return courierRepository.findByIdAndDeletedFalse(courierId)
                .orElseThrow(() -> new DeliveryException("Courier %s not found", courierId));
    }
}