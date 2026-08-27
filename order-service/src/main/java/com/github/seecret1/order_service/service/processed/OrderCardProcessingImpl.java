package com.github.seecret1.order_service.service.processed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.seecret1.order_service.config.kafka.properties.KafkaProperties;
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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED) //TODO: вынести transactional и работать только с сервисами
public class OrderCardProcessingImpl implements OrderCardProcessing {

    private final OrderInnerRequestKafkaProducerService orderInnerRequestKafkaProducerService;

    private final OrderInvoiceRequestKafkaProducerService orderInvoiceRequestKafkaProducerService;

    private final OrderMessageKafkaProducerService producerService;

    private final KafkaProperties kafkaProperties;

    private final OrderCardRepository orderCardRepository;

    private final OrderCardManualMapper orderCardMapper;

    private final ObjectMapper objectMapper;

    private final OrderMessageKafkaProducerService orderMessageKafkaProducerService;

    //TODO: добавить метрики
    @Override
    public void processOrder(OrderCardDto event) {
        try {
            if (event.getOrderType() != OrderType.CARD) {
                throw new OrderTypeException("Order card service works only with OrderType=CARD");
            }

            event.validate();
            //TODO: вынести в отдельный сервис
            var order = orderCardMapper.toEntity(event, OrderStatus.PENDING);
            var savedOrder = orderCardRepository.save(order);
            var invoice = orderCardMapper.toInvoiceDto(event, savedOrder.getId());
            log.info("Sending message by order ID: {}", savedOrder.getId());
            orderInvoiceRequestKafkaProducerService.sendWithWaitToRequestInvoiceTopic(invoice);

        } catch (Exception ex) {
            log.error("Error processing order: traceId={}, error={}", event.getTraceId(), ex.getMessage(), ex);
            OrderCard errorOrder = orderCardMapper.toEntity(event, OrderStatus.ERROR);
            errorOrder.setComment("Error: " + ex.getMessage());
            orderCardRepository.save(errorOrder); //TODO: выбрасывать в Prometheus
            var message = orderCardMapper.toMessage(errorOrder);
            producerService.sendWithWait(kafkaProperties.getCardsTopic(), message);
        }
    }

    @Override
    public void processMessage(BaseMessage message) {
        List<CardInvoiceResponse> invoiceResponses = extractInvoices(message.getData());
        log.info("Invoice responses List: {}", invoiceResponses.size());

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
            producerService.sendWithWait(kafkaProperties.getCardsTopic(), message);
            log.debug("Rejected create card. Send message with cards topic");
            return;
        }

        pushToInnerTopic(message);
        log.debug("Send message with inner topic");
    }

    //TODO: изменить логику, сначала делать проверку, получая сравнивая топики из header и kafkaProps
    @Override
    public void processMessageOnInvoiceService(BaseMessage message) {
            var order = orderCardRepository.findById(message.getOrderId()) //TODO: вынести в отдельный сервис
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Order not found by ID: " + message.getOrderId()
                    ));
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
        var dto = findOrder(message);
        orderInnerRequestKafkaProducerService.sendWithWait(dto);
    }

    private OrderCardDto findOrder(BaseMessage message) {
        var order = orderCardRepository.findById(message.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + message.getOrderId()));

        return orderCardMapper.toDto(order);
    }

    private List<CardInvoiceResponse> extractInvoices(Object data) {
        if (data == null) {
            return Collections.emptyList();
        }

        try {
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                if (list.isEmpty()) {
                    return Collections.emptyList();
                }

                Object first = list.get(0);
                if (first instanceof CardInvoiceResponse) {
                    return (List<CardInvoiceResponse>) list;
                }

                return objectMapper.convertValue(data,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, CardInvoiceResponse.class));
            }
            CardInvoiceResponse single = objectMapper.convertValue(data, CardInvoiceResponse.class);
            return List.of(single);

        } catch (Exception ex) {
            log.error("Failed to parse invoice data: {}", data, ex);
            return Collections.emptyList();
        }
    }
}
