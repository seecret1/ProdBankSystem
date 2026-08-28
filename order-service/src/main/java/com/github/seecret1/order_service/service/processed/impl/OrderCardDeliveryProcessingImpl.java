package com.github.seecret1.order_service.service.processed.impl;

import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.dto.delivery.OrderCardDeliveryDto;
import com.github.seecret1.order_service.dto.user.PersonInfo;
import com.github.seecret1.order_service.entity.OrderCard;
import com.github.seecret1.order_service.entity.enums.OrderStatus;
import com.github.seecret1.order_service.entity.enums.PersonType;
import com.github.seecret1.order_service.feign.OfficeServiceFeignClient;
import com.github.seecret1.order_service.kafka.producer.OrderDeliveryRequestKafkaProducerService;
import com.github.seecret1.order_service.mapper.AddressManualMapper;
import com.github.seecret1.order_service.mapper.OrderCardManualMapper;
import com.github.seecret1.order_service.repository.OrderCardRepository;
import com.github.seecret1.order_service.service.processed.OrderCardDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCardDeliveryProcessingImpl implements OrderCardDeliveryService {

    private final OfficeServiceFeignClient officeServiceFeignClient;

    private final OrderCardRepository orderCardRepository;

    private final AddressManualMapper addressMapper;

    private final OrderCardManualMapper orderCardMapper;

    private final OrderDeliveryRequestKafkaProducerService deliveryKafkaProducerService;

    @Override
    public OrderCard processDeliveryCourierMethod(
            OrderCard order,
            OrderCardDto event,
            PersonInfo personInfo,
            boolean office
    ) {
        var mainOffice = officeServiceFeignClient.findMainOfficeNearestByCity(); //TODO: исправить (это лишний запрос)

        order.setStatus(OrderStatus.SUCCESS);
        orderCardRepository.save(order);

        log.info("Order: {}", order);
        log.info("Request in Delivery: {} and ReceivingMethod: {}", event.getDeliveryRequest(), event.getCardReceivingMethod());

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
}
