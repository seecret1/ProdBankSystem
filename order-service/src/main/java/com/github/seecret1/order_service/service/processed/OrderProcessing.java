package com.github.seecret1.order_service.service.processed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.seecret1.order_service.dto.BaseMessage;
import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.dto.invoice.CardInvoiceResponse;
import com.github.seecret1.order_service.entity.OrderCard;
import com.github.seecret1.order_service.entity.enums.InvoiceStatus;
import com.github.seecret1.order_service.entity.enums.OrderStatus;
import com.github.seecret1.order_service.entity.enums.OrderType;
import com.github.seecret1.order_service.exception.OrderTypeException;
import com.github.seecret1.order_service.kafka.producer.OrderInnerRequestKafkaProducerService;
import com.github.seecret1.order_service.kafka.producer.OrderInvoiceRequestKafkaProducerService;
import com.github.seecret1.order_service.kafka.producer.OrderMessageKafkaProducerService;
import com.github.seecret1.order_service.mapper.OrderCardManualMapper;
import com.github.seecret1.order_service.repository.OrderCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderProcessing {

    private final OrderInnerRequestKafkaProducerService orderInnerRequestKafkaProducerService;

    private final OrderInvoiceRequestKafkaProducerService orderInvoiceRequestKafkaProducerService;

    private final OrderMessageKafkaProducerService producerService;

    private final OrderCardRepository orderCardRepository;

    private final OrderCardManualMapper orderCardMapper;

    private final ObjectMapper objectMapper;

    //TODO: добавить метрики
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BaseMessage processOrder(OrderCardDto event) {
        try {
            if (event.getOrderType() != OrderType.CARD) {
                throw new OrderTypeException("Order card service works only with OrderType=CARD");
            }

            event.validate();
            OrderCard order = orderCardMapper.toEntity(event, OrderStatus.PENDING);
            order = orderCardRepository.save(order);
            orderInvoiceRequestKafkaProducerService.sendWithWait(event);
            return sendMessage(order);

        } catch (Exception ex) {
            log.error("Error processing order: traceId={}, error={}", event.getTraceId(), ex.getMessage(), ex);
            OrderCard errorOrder = orderCardMapper.toEntity(event, OrderStatus.ERROR);
            errorOrder.setComment("Error: " + ex.getMessage());
            errorOrder = orderCardRepository.save(errorOrder); //TODO: выбрасывать в Prometheus
            return sendMessage(errorOrder);
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OrderCardDto processMessage(BaseMessage message) {
        var invoiceResponse = objectMapper.convertValue(message.getData(), CardInvoiceResponse.class);

        if (invoiceResponse.deleted().equals(Boolean.TRUE)) {
            message.setStatus(OrderStatus.REJECTED);
            message.setMessage("Invoice is deleted");
        }
        if (invoiceResponse.status() != InvoiceStatus.ACTIVE) {
            message.setStatus(OrderStatus.REJECTED);
            message.setMessage("Invoice is not active");
        }
        if (invoiceResponse.balance() != null && invoiceResponse.balance().compareTo(BigDecimal.ZERO) < 0) {
            message.setStatus(OrderStatus.REJECTED);
            message.setMessage("Invoice has negative balance");
        }

        if (message.getStatus() == OrderStatus.REJECTED) {
            producerService.sendWithWait(message);
            return null;
        }

        var order = orderCardRepository.findById(message.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + message.getOrderId()));
        var dto = orderCardMapper.toDto(order);
        orderInnerRequestKafkaProducerService.sendWithWait(dto);
        return dto;
    }

    private BaseMessage sendMessage(OrderCard order) {
        var message = orderCardMapper.toMessage(order);
        producerService.sendWithWait(message);
        return message;
    }
}
