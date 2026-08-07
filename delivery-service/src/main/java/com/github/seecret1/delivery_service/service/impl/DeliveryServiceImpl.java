package com.github.seecret1.delivery_service.service.impl;

import com.github.seecret1.delivery_service.dto.BaseMessage;
import com.github.seecret1.delivery_service.dto.order.OrderDeliveryDto;
import com.github.seecret1.delivery_service.dto.user.RecipientDto;
import com.github.seecret1.delivery_service.entity.enums.OrderStatus;
import com.github.seecret1.delivery_service.mapper.DeliveryMapper;
import com.github.seecret1.delivery_service.repository.DeliveryRepository;
import com.github.seecret1.delivery_service.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;

    private final DeliveryMapper deliveryMapper;

    @Override
    public BaseMessage create(OrderDeliveryDto event) {
        try {
            event.validate();
            var recipientDto = RecipientDto.builder()
                    .personType(event.getPersonType())
                    .contactPhone(event.getContactPhone())
                    .fullName(event.getFullName())
                    .userId(event.getUserId())
                    .officeId(event.getOfficeId())
                    .build();
            var delivery = deliveryMapper.toEntity(event, recipientDto);
            deliveryRepository.save(delivery);
            var message = deliveryMapper.toMessage(delivery, event.getTraceId());
            message.setStatus(OrderStatus.SUCCESS); // TODO: временная обработка
            return message;
        } catch (Exception e) {
            log.error("Error create delivery: {}", e.getMessage());
            throw e;
        }
    }
}
