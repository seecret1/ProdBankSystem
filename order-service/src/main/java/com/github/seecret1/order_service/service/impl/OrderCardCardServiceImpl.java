package com.github.seecret1.order_service.service.impl;

import com.github.seecret1.order_service.entity.OrderCard;
import com.github.seecret1.order_service.repository.OrderCardRepository;
import com.github.seecret1.order_service.service.OrderCardService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCardCardServiceImpl implements OrderCardService {

    private final OrderCardRepository orderCardRepository;

    @Override
    public OrderCard findById(String id) {
        return orderCardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Order not found by ID: " + id
                ));
    }
}
