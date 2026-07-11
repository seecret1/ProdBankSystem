package com.github.seecret1.order_service.service.impl;

import com.github.seecret1.order_service.dto.OrderMessage;
import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.entity.OrderCard;
import com.github.seecret1.order_service.kafka.OrderKafkaProducerService;
import com.github.seecret1.order_service.mapper.OrderCardManualMapper;
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
    public OrderMessage createOrder(OrderCardDto event) {

        // TODO: Добавить обработку и работу со статусами
        OrderCard order = OrderCardManualMapper.toEntity(event);
        orderCardRepository.save(order);

        var message = OrderCardManualMapper.toMessage(order);
        orderKafkaProducerService.sendWithWait(event, message);

        log.info("Response sent to Kafka: order ID={}", order.getId());
        return message;
    }
}
