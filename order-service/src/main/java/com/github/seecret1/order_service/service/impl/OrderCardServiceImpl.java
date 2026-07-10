package com.github.seecret1.order_service.service.impl;

import com.github.seecret1.order_service.dto.card.OrderCardResponse;
import com.github.seecret1.order_service.dto.card.OrderCreateCardDto;
import com.github.seecret1.order_service.entity.OrderCard;
import com.github.seecret1.order_service.kafka.OrderKafkaProducerService;
import com.github.seecret1.order_service.mapper.OrderManualMapper;
import com.github.seecret1.order_service.repository.OrderCardRepository;
import com.github.seecret1.order_service.service.OrderCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCardServiceImpl implements OrderCardService {

    private final OrderCardRepository orderCardRepository;

    private final OrderKafkaProducerService orderKafkaProducerService;

    @Override
    @Transactional
    public OrderCardResponse createOrder(OrderCreateCardDto event) {

        OrderCard order = OrderManualMapper.toEntity(event);
        orderCardRepository.save(order);

        var response = OrderManualMapper.toResponse(order);
        orderKafkaProducerService.sendWithWait(event, response);

        log.info("Response sent to Kafka: order ID={}", order.getId());
        return response;
    }
}
