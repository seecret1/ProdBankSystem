package com.github.seecret1.delivery_service.mapper;

import com.github.seecret1.delivery_service.dto.BaseMessage;
import com.github.seecret1.delivery_service.dto.DeliveryResponse;
import com.github.seecret1.delivery_service.dto.order.OrderCardDeliveryDto;
import com.github.seecret1.delivery_service.dto.user.FullNameDto;
import com.github.seecret1.delivery_service.dto.user.RecipientDto;
import com.github.seecret1.delivery_service.entity.Address;
import com.github.seecret1.delivery_service.entity.CardDelivery;
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

    public CardDelivery toEntity(
            OrderCardDeliveryDto dto,
            Recipient recipient,
            Address originalAddress,
            Address destinationAddress
    ) {
        return CardDelivery.builder()
                .orderId(dto.getOrderId())
                .recipient(recipient)
                .originAddress(originalAddress)
                .destinationAddress(destinationAddress)
                .plannedDeliveryTime(dto.getPlannedDeliveryTime())
                .cardType(dto.getCardType())
                .status(DeliveryStatus.CREATED)
                .deleted(false)
                .build();
    }

    public DeliveryResponse toResponse(CardDelivery cardDelivery, RecipientDto recipient) {
        return DeliveryResponse.builder()
                .courierId(cardDelivery.getCourierId())
                .recipient(recipient)
                .courierContactPhone(cardDelivery.getCourierContactPhone())
                .originAddress(addressMapper.fromAddress(cardDelivery.getOriginAddress()))
                .destinationAddress(addressMapper.fromAddress(cardDelivery.getDestinationAddress()))
                .createdAt(cardDelivery.getCreatedAt())
                .build();
    }

    public BaseMessage toMessage(CardDelivery cardDelivery, String traceId) {
        return BaseMessage.builder()
                .traceId(traceId)
                .orderId(cardDelivery.getOrderId())
                .userId(cardDelivery.getRecipient().getUserId())
                .productId(cardDelivery.getId())
                .status(OrderStatus.PENDING)
                .data(toResponse(cardDelivery, toRecipientDto(cardDelivery.getRecipient())))
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
