package com.github.seecret1.order_service.service.impl;

import com.github.seecret1.order_service.config.kafka.properties.KafkaProperties;
import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.dto.delivery.OrderCardDeliveryDto;
import com.github.seecret1.order_service.dto.office.OfficeResponse;
import com.github.seecret1.order_service.dto.user.PersonInfo;
import com.github.seecret1.order_service.entity.OrderCard;
import com.github.seecret1.order_service.entity.enums.CardType;
import com.github.seecret1.order_service.entity.enums.OrderStatus;
import com.github.seecret1.order_service.entity.enums.PersonType;
import com.github.seecret1.order_service.feign.OfficeServiceFeignClient;
import com.github.seecret1.order_service.feign.UserServiceFeignClient;
import com.github.seecret1.order_service.kafka.producer.OrderDeliveryRequestKafkaProducerService;
import com.github.seecret1.order_service.kafka.producer.OrderInvoiceRequestKafkaProducerService;
import com.github.seecret1.order_service.kafka.producer.OrderMessageKafkaProducerService;
import com.github.seecret1.order_service.mapper.AddressManualMapper;
import com.github.seecret1.order_service.mapper.OrderCardManualMapper;
import com.github.seecret1.order_service.repository.OrderCardRepository;
import com.github.seecret1.order_service.service.OrderCardService;
import com.github.seecret1.order_service.utils.OrderValidateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCardServiceImpl implements OrderCardService {

    private final OrderInvoiceRequestKafkaProducerService orderInvoiceRequestKafkaProducerService;

    private final OrderMessageKafkaProducerService orderMessageKafkaProducerService;

    private final OrderDeliveryRequestKafkaProducerService deliveryKafkaProducerService;

    private final KafkaProperties kafkaProperties;

    private final OrderCardRepository orderCardRepository;

    private final UserServiceFeignClient userServiceFeignClient;

    private final OfficeServiceFeignClient officeServiceFeignClient;

    private final OrderCardManualMapper orderCardMapper;

    private final AddressManualMapper addressMapper;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ) //TODO: добавить метрики
    public void createOrder(OrderCardDto event) {

        // TODO: добавить отправку сообщения через notification-service
        try {
            // TODO: добавить проверку счетов карт (если имеется задолженность -> REJECT)
            //  Реализовать через invoice-service

            var personInfo = userServiceFeignClient.getPersonInfo(event.getUserId());
            OrderCard order = orderCardRepository.findByTraceId(event.getTraceId());
            order = userValidate(order, personInfo, event);

            if (order.getStatus() == OrderStatus.REJECTED) {
                sendMessage(order);
                return;
            }

            var savedOrder = processReceivingMethod(order, event, personInfo);
            orderCardRepository.save(savedOrder);
            orderInvoiceRequestKafkaProducerService.sendWithWaitToInvoiceTopic(
                    orderCardMapper.toInvoiceDto(event, savedOrder.getId())
            );

        } catch (Exception ex) {
            log.error("Error creating order: traceId={}, error={}", event.getTraceId(), ex.getMessage(), ex);
            OrderCard errorOrder = orderCardMapper.toEntity(event, OrderStatus.ERROR);
            errorOrder.setComment("Error: " + ex.getMessage());
            errorOrder = orderCardRepository.save(errorOrder);
            sendMessage(errorOrder);
        }
    }

    private OrderCard userValidate(OrderCard order, PersonInfo personInfo, OrderCardDto event) {
        if (!OrderValidateUtils.validateCard(personInfo, event)) {
            log.error("TraceId={}, Order card: {} not validate", event.getTraceId(), event.getCardId());
            order.setStatus(OrderStatus.REJECTED);
            event.setComment("Order card not validate");
            return orderCardRepository.save(order);
        }
        return order;
    }

    private OrderCard processReceivingMethod(OrderCard order, OrderCardDto event, PersonInfo personInfo) {
        String city = personInfo.address() != null ? personInfo.address().city() : null;

        switch (event.getCardReceivingMethod()) {
            case OFFICE:
                return processOfficeMethod(city, order, event, personInfo);
            case DELIVERY_COURIER:
                return processDeliveryCourierMethod(order, event, personInfo, false);
            case DIGITAL:
                return processDigitalMethod(order, personInfo);
            default:
                // TODO: добавить автоматическую обработку в card-service для retry заказа
                log.error("Unsupported card receiving method: {}", event.getCardReceivingMethod());
                order.setStatus(OrderStatus.REJECTED);
                order.setComment("Unsupported card receiving method: " + event.getCardReceivingMethod());
                return order;
        }
    }

    private OrderCard processOfficeMethod(String city, OrderCard order, OrderCardDto event, PersonInfo personInfo) {

        CardType cardType = event.getCardType();
        switch (cardType) {
            case DEBIT:
                return processDebit(city, order);
            case DEBIT_PERSONAL:
                OrderCard newOrder = processDebit(city, order);
                processDeliveryCourierMethod(newOrder, event, personInfo, true);
                return newOrder;
            case CREDIT: //TODO: заглушка на время
                order.setStatus(OrderStatus.REJECTED);
                order.setComment("This version does not include support for create credit card");
                return order;
//                return processCredit(city, order); //TODO: добавить работу с мс кредитов (надежный ли налогоплательщик?)
            default:
                log.error("Unsupported card type: {}", cardType);
                order.setStatus(OrderStatus.REJECTED);
                order.setComment("Unsupported card type: " + cardType);
                return order;
        }
    }

    private OrderCard processDeliveryCourierMethod(
            OrderCard order,
            OrderCardDto event,
            PersonInfo personInfo,
            boolean office
    ) {
        var mainOffice = officeServiceFeignClient.findMainOfficeNearestByCity(); //TODO: исправить (это лишний запрос)

        order.setStatus(OrderStatus.SUCCESS);
        orderCardRepository.save(order);

        var orderDeliveryDto = OrderCardDeliveryDto.builder()
                .traceId(event.getTraceId())
                .userId(event.getUserId())
                .orderType(event.getOrderType())
                .createdAt(event.getCreatedAt())
                .comment(event.getComment())
                .orderId(order.getId())
                .cardType(event.getCardType())
                .fullName(personInfo.fullName())
                .contactPhone(personInfo.contactPhone())
                .personType(PersonType.PHYSICAL) // TODO: временно задано жестко, пока нет работы с ФЛ и ЮЛ отдельно
                .originAddress(addressMapper.toAddressRequest(mainOffice.address()))
                .destinationAddress(event.getDeliveryRequest().address())
                .plannedDeliveryTime(event.getDeliveryRequest().plannedDeliveryTime())
                .build();

        var orderCardDelivery = orderCardMapper.toOrderCardDelivery(
                orderDeliveryDto
        );
        order.setOrderDelivery(orderCardDelivery);

        if (office) {
            orderDeliveryDto.setOfficeId(mainOffice.id());
        }

        deliveryKafkaProducerService.sendRequestWithWait(orderDeliveryDto);
        return order;
    }

    private OrderCard processDigitalMethod(
            OrderCard order,
            PersonInfo personInfo
    ) {
        if (personInfo.contactPhone() == null) {
            order.setStatus(OrderStatus.REJECTED);
            order.setComment("Contact phone must be set!");
        }
        order.setStatus(OrderStatus.SUCCESS);
        order.setComment("Digital card successfully issued and activated");
        return order;
    }

    private OrderCard processCredit(String city, OrderCard order) {
        //TODO: Проверить историю пользователя через invoice-service, сработав через кафку
        return null;
    }

    private OrderCard processDebit(String city, OrderCard order) {
        var offices = officeServiceFeignClient.findOfficeByCity(city);
        return processDebitInOffice(offices, city, order);
    }

    private OrderCard processDebitInOffice(List<OfficeResponse> offices, String city, OrderCard order) {
        var activeOffices = offices.stream()
                .filter(office -> office != null && office.active())
                .toList();
        if (activeOffices.isEmpty()) {
            String message = String.format("Not active offices in your city: %s", city);
            log.info(message);
            order.setStatus(OrderStatus.REJECTED);
            order.setComment(message);
            return order;
        }
        order.setStatus(OrderStatus.SUCCESS);
        order.setComment("Successfully create card");
        return order;
    }

    private void sendMessage(OrderCard order) {
        var message = orderCardMapper.toMessage(order); //TODO: отправлять через translate-topic
        orderMessageKafkaProducerService.sendWithWait(kafkaProperties.getCardsTopic(), message);
    }
}
