package com.github.seecret1.delivery_service.mapper;

import com.github.seecret1.delivery_service.dto.BaseMessage;
import com.github.seecret1.delivery_service.dto.DeliveryResponse;
import com.github.seecret1.delivery_service.dto.order.OrderDeliveryDto;
import com.github.seecret1.delivery_service.dto.user.FullNameDto;
import com.github.seecret1.delivery_service.dto.user.RecipientDto;
import com.github.seecret1.delivery_service.entity.Delivery;
import com.github.seecret1.delivery_service.entity.FullName;
import com.github.seecret1.delivery_service.entity.Recipient;
import com.github.seecret1.delivery_service.entity.enums.DeliveryStatus;
import com.github.seecret1.delivery_service.entity.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DeliveryMapper {

    private final AddressMapper addressMapper;

    public Delivery toEntity(OrderDeliveryDto dto, RecipientDto recipientDto) {
        return Delivery.builder()
                .orderId(dto.getOrderId())
                .recipient(toRecipientEntity(recipientDto))
                .originAddress(addressMapper.toAddress(dto.getOriginAddress()))
                .destinationAddress(addressMapper.toAddress(dto.getDestinationAddress()))
                .status(DeliveryStatus.CREATED)
                .build();
    }

    public DeliveryResponse toResponse(Delivery delivery, RecipientDto recipient) {
        return DeliveryResponse.builder()
                .courierId(delivery.getCourierId())
                .recipient(recipient)
                .courierContactPhone(delivery.getCourierContactPhone())
                .originAddress(addressMapper.fromAddress(delivery.getOriginAddress()))
                .destinationAddress(addressMapper.fromAddress(delivery.getDestinationAddress()))
                .createdAt(delivery.getCreatedAt())
                .build();
    }

    public BaseMessage toMessage(Delivery delivery, String traceId) {
        return BaseMessage.builder()
                .traceId(traceId)
                .orderId(delivery.getOrderId())
                .userId(delivery.getRecipient().getUserId())
                .productId(delivery.getId())
                .status(OrderStatus.PENDING)
                .data(toResponse(delivery, toRecipientDto(delivery.getRecipient())))
                .message("Delivery request accepted")
                .timestamp(Instant.now())
                .build();
    }

    public Recipient toRecipientEntity(RecipientDto dto) {
        return Recipient.builder()
                .userId(dto.getUserId())
                .fullName(toFullNameEntity(dto.getFullName()))
                .contactPhone(dto.getContactPhone())
                .personType(dto.getPersonType())
                .officeId(dto.getOfficeId())
                .build();
    }

    public RecipientDto toRecipientDto(Recipient entity) {
        return RecipientDto.builder()
                .userId(entity.getUserId())
                .fullName(toFullNameDto(entity.getFullName()))
                .contactPhone(entity.getContactPhone())
                .personType(entity.getPersonType())
                .officeId(entity.getOfficeId())
                .build();
    }

    public FullName toFullNameEntity(FullNameDto dto) {
        return FullName.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .middleName(dto.getMiddleName())
                .build();
    }

    public FullNameDto toFullNameDto(FullName entity) {
        return FullNameDto.builder()
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .middleName(entity.getMiddleName())
                .build();
    }
}
