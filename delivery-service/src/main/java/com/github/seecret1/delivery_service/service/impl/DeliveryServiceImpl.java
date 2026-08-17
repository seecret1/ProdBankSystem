package com.github.seecret1.delivery_service.service.impl;

import com.github.seecret1.delivery_service.dto.BaseMessage;
import com.github.seecret1.delivery_service.dto.address.AddressPair;
import com.github.seecret1.delivery_service.dto.order.OrderCardDeliveryDto;
import com.github.seecret1.delivery_service.entity.enums.OrderStatus;
import com.github.seecret1.delivery_service.mapper.DeliveryMapper;
import com.github.seecret1.delivery_service.repository.DeliveryRepository;
import com.github.seecret1.delivery_service.service.AddressProcessed;
import com.github.seecret1.delivery_service.service.DeliveryService;
import com.github.seecret1.delivery_service.service.RecipientProcessed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final RecipientProcessed recipientProcessed;

    private final DeliveryRepository deliveryRepository;

    private final DeliveryMapper deliveryMapper;

    private final AddressProcessed addressProcessed;

    @Override
    public BaseMessage create(OrderCardDeliveryDto event) {
        try {
            event.validate();

            AddressPair addressPair = addressProcessed.processOriginalAndDestinationAddresses(
                    event.getOriginAddress(), event.getDestinationAddress()
            );

            var recipient = recipientProcessed.processDelivery(event);
            var delivery = deliveryMapper.toEntity(
                    event,
                    recipient,
                    addressPair.origin(),
                    addressPair.destination()
            );

            deliveryRepository.save(delivery);
            var message = deliveryMapper.toMessage(delivery, event.getTraceId());
            message.setStatus(OrderStatus.SUCCESS); // TODO: временная обработка
            log.info("Successfully processed message and send response");
            return message;
        } catch (Exception e) {
            log.error("Error create delivery: {}", e.getMessage());
            throw e;
        }
    }
}
