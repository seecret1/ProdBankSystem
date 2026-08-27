package com.github.seecret1.order_service.service.impl;

import com.github.seecret1.order_service.config.kafka.properties.KafkaProperties;
import com.github.seecret1.order_service.dto.BaseMessage;
import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.dto.invoice.CardInvoiceResponse;
import com.github.seecret1.order_service.dto.invoice.OrderInvoiceDto;
import com.github.seecret1.order_service.entity.OrderCard;
import com.github.seecret1.order_service.entity.enums.InvoiceStatus;
import com.github.seecret1.order_service.entity.enums.OrderStatus;
import com.github.seecret1.order_service.kafka.producer.OrderInnerRequestKafkaProducerService;
import com.github.seecret1.order_service.kafka.producer.OrderInvoiceRequestKafkaProducerService;
import com.github.seecret1.order_service.kafka.producer.OrderMessageKafkaProducerService;
import com.github.seecret1.order_service.mapper.OrderCardManualMapper;
import com.github.seecret1.order_service.repository.OrderCardRepository;
import com.github.seecret1.order_service.service.OrderCardService;
import com.github.seecret1.order_service.service.OrderMessagesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class OrderMessageServiceImpl implements OrderMessagesService {

    private final OrderMessageKafkaProducerService orderMessageKafkaProducerService;

    private final OrderInvoiceRequestKafkaProducerService orderInvoiceRequestKafkaProducerService;

    private final OrderInnerRequestKafkaProducerService orderInnerRequestKafkaProducerService;

    private final OrderCardManualMapper orderCardMapper;

    private final OrderCardRepository orderCardRepository;

    private final KafkaProperties kafkaProperties;

    private final OrderCardService orderCardService;

    @Override
    public OrderInvoiceDto saveOrder(OrderCardDto message) {
        var order = orderCardMapper.toEntity(message, OrderStatus.PENDING);
        var savedOrder = orderCardRepository.save(order);
        var invoice = orderCardMapper.toInvoiceDto(message, savedOrder.getId());
        orderInvoiceRequestKafkaProducerService.sendWithWaitToRequestInvoiceTopic(invoice);
        return invoice;
    }

    @Override
    public void processingError(OrderCardDto event, Exception ex) {
        OrderCard errorOrder = orderCardMapper.toEntity(event, OrderStatus.ERROR);
        errorOrder.setComment("Error: " + ex.getMessage());
        orderCardRepository.save(errorOrder);
        BaseMessage message = orderCardMapper.toMessage(errorOrder);
        orderMessageKafkaProducerService.sendWithWait(kafkaProperties.getCardsTopic(), message);
    }

    @Override
    public void processingInvoice(BaseMessage message, List<CardInvoiceResponse> invoiceResponses) {

        if (invoiceResponses.isEmpty()) {
            pushToInnerTopic(message);
            log.debug("Send message with inner topic");
            return;
        }

        for (var invoice : invoiceResponses) {
            if (invoice.deleted().equals(Boolean.TRUE)) {
                message.setStatus(OrderStatus.REJECTED);
                message.setMessage("Invoice is deleted");
            }
            if (invoice.status() != InvoiceStatus.ACTIVE) {
                message.setStatus(OrderStatus.REJECTED);
                message.setMessage("Invoice is not active");
            }
            if (invoice.balance() != null && invoice.balance().compareTo(BigDecimal.ZERO) < 0) {
                message.setStatus(OrderStatus.REJECTED);
                message.setMessage("Invoice has negative balance");
            }
        }

        if (message.getStatus() == OrderStatus.REJECTED) {
            orderMessageKafkaProducerService.sendWithWait(kafkaProperties.getCardsTopic(), message);
            log.debug("Rejected create card. Send message with cards topic");
            return;
        }

        pushToInnerTopic(message);
        log.debug("Send message with inner topic");
    }

    @Override
    public void processingResponseInCardService(BaseMessage message) {

        //TODO: изменить логику, сначала делать проверку, получая сравнивая топики из header и kafkaProps
        var order = orderCardService.findById(message.getOrderId());

        if (message.getTraceId().equals(order.getTraceId())) {
            order.setInvoiceId(message.getProductId());
            orderCardRepository.save(order);
            message.setData(orderCardMapper.toResponse(order));
            message.setProductId(order.getCardId());
            orderMessageKafkaProducerService.sendWithWait(kafkaProperties.getCardsTopic(), message);
            log.debug("Setting invoiceId={} in order", message.getProductId());
        }
        else {
            log.error("Message not processed: traceId={}, Order traceId={}",
                    message.getTraceId(), order.getTraceId());
        }
    }

    private void pushToInnerTopic(BaseMessage message) {
        var order = orderCardRepository.findById(message.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + message.getOrderId()));
        orderInnerRequestKafkaProducerService.sendWithWait(orderCardMapper.toDto(order));
    }
}
