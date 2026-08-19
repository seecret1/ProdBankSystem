package com.github.seecret1.delivery_service.service.impl;

import com.github.seecret1.delivery_service.dto.BaseMessage;
import com.github.seecret1.delivery_service.dto.address.AddressPair;
import com.github.seecret1.delivery_service.dto.order.OrderCardDeliveryDto;
import com.github.seecret1.delivery_service.entity.CardDelivery;
import com.github.seecret1.delivery_service.entity.Courier;
import com.github.seecret1.delivery_service.entity.enums.OrderStatus;
import com.github.seecret1.delivery_service.exception.DeliveryException;
import com.github.seecret1.delivery_service.mapper.DeliveryMapper;
import com.github.seecret1.delivery_service.repository.DeliveryRepository;
import com.github.seecret1.delivery_service.service.CourierService;
import com.github.seecret1.delivery_service.service.DeliveryService;
import com.github.seecret1.delivery_service.service.processed.AddressProcessed;
import com.github.seecret1.delivery_service.service.processed.RecipientProcessed;
import com.github.seecret1.delivery_service.utils.PhoneUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final RecipientProcessed recipientProcessed;

    private final DeliveryRepository deliveryRepository;

    private final DeliveryMapper deliveryMapper;

    private final AddressProcessed addressProcessed;

    private final CourierService courierService;

    @Override
    @Transactional
    public BaseMessage create(OrderCardDeliveryDto event) {
        try {
            event.validate();
            checkPhone(event.getContactPhone());

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

            assignCourier(delivery);

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

    private void assignCourier(CardDelivery delivery) {
        try {
            Courier courier = courierService.assignFirstAvailable();
            if (courier != null) {
                delivery.assignCourier(courier);
                log.info("Assigned courier {} to delivery {}", courier.getId(), delivery.getId());
            }
        } catch (DeliveryException e) {
            log.warn("No available couriers, delivery {} stays CREATED: {}", delivery.getId(), e.getMessage());
        }
    }

    private void checkPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new DeliveryException("Phone must not be blank");
        }
        String cleaned = PhoneUtils.cleanPhoneNumber(phone);
        if (cleaned.length() < 10 || cleaned.length() > 15) {
            throw new DeliveryException("Invalid phone number: %s", phone);
        }
    }
}