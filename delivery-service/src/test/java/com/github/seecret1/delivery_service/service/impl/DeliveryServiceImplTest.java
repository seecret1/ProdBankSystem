package com.github.seecret1.delivery_service.service.impl;

import com.github.seecret1.delivery_service.dto.BaseMessage;
import com.github.seecret1.delivery_service.dto.order.OrderCardDeliveryDto;
import com.github.seecret1.delivery_service.entity.CardDelivery;
import com.github.seecret1.delivery_service.entity.Recipient;
import com.github.seecret1.delivery_service.entity.enums.DeliveryStatus;
import com.github.seecret1.delivery_service.entity.enums.OrderStatus;
import com.github.seecret1.delivery_service.exception.DeliveryException;
import com.github.seecret1.delivery_service.mapper.DeliveryMapper;
import com.github.seecret1.delivery_service.repository.DeliveryRepository;
import com.github.seecret1.delivery_service.service.CourierService;
import com.github.seecret1.delivery_service.service.processed.AddressProcessed;
import com.github.seecret1.delivery_service.service.processed.RecipientProcessed;
import com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory.TRACE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeliveryService Unit Tests")
class DeliveryServiceImplTest {

    @Mock
    private RecipientProcessed recipientProcessed;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private DeliveryMapper deliveryMapper;

    @Mock
    private AddressProcessed addressProcessed;

    @Mock
    private CourierService courierService;

    @InjectMocks
    private DeliveryServiceImpl deliveryService;

    private OrderCardDeliveryDto orderDto;
    private Recipient recipient;
    private CardDelivery cardDelivery;
    private BaseMessage baseMessage;

    @BeforeEach
    void setUp() {
        orderDto = DeliveryTestDataFactory.validOrderCardDeliveryDto();
        recipient = DeliveryTestDataFactory.defaultRecipient();
        cardDelivery = DeliveryTestDataFactory.defaultCardDelivery(recipient);
        baseMessage = BaseMessage.builder()
                .traceId(TRACE_ID)
                .orderId(orderDto.getOrderId())
                .userId(recipient.getUserId())
                .productId(cardDelivery.getId())
                .status(OrderStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Should create delivery successfully")
    void shouldCreateDeliverySuccessfully() {
        var addressPair = DeliveryTestDataFactory.defaultAddressPair();
        when(addressProcessed.processOriginalAndDestinationAddresses(
                orderDto.getOriginAddress(), orderDto.getDestinationAddress()
        )).thenReturn(addressPair);
        when(recipientProcessed.processDelivery(orderDto)).thenReturn(recipient);
        when(deliveryMapper.toEntity(
                orderDto, recipient, addressPair.origin(), addressPair.destination()
        )).thenReturn(cardDelivery);
        when(deliveryRepository.save(cardDelivery)).thenReturn(cardDelivery);
        when(deliveryMapper.toMessage(cardDelivery, TRACE_ID)).thenReturn(baseMessage);

        BaseMessage result = deliveryService.create(orderDto);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.SUCCESS);
        assertThat(result.getTraceId()).isEqualTo(TRACE_ID);

        InOrder inOrder = inOrder(
                addressProcessed, recipientProcessed, deliveryMapper, deliveryRepository, deliveryMapper
        );
        inOrder.verify(addressProcessed).processOriginalAndDestinationAddresses(
                orderDto.getOriginAddress(), orderDto.getDestinationAddress()
        );
        inOrder.verify(recipientProcessed).processDelivery(orderDto);
        inOrder.verify(deliveryMapper).toEntity(
                orderDto, recipient, addressPair.origin(), addressPair.destination()
        );
        inOrder.verify(deliveryRepository).save(cardDelivery);
        inOrder.verify(deliveryMapper).toMessage(cardDelivery, TRACE_ID);
    }

    @Test
    @DisplayName("Should throw ValidationException when orderId is null")
    void shouldThrowWhenOrderIdIsNull() {
        orderDto.setOrderId(null);

        assertThatThrownBy(() -> deliveryService.create(orderDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Ids must not be null");

        verify(addressProcessed, never()).processOriginalAndDestinationAddresses(any(), any());
        verify(deliveryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when userId is blank")
    void shouldThrowWhenUserIdIsBlank() {
        orderDto.setUserId("   ");

        assertThatThrownBy(() -> deliveryService.create(orderDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Ids must not be blank");
    }

    @Test
    @DisplayName("Should throw ValidationException when traceId is null")
    void shouldThrowWhenTraceIdIsNull() {
        orderDto.setTraceId(null);

        assertThatThrownBy(() -> deliveryService.create(orderDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Ids must not be null");
    }

    @Test
    @DisplayName("Should throw ValidationException when fullName is null")
    void shouldThrowWhenFullNameIsNull() {
        orderDto.setFullName(null);

        assertThatThrownBy(() -> deliveryService.create(orderDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Must not be null fields");
    }

    @Test
    @DisplayName("Should throw ValidationException when firstName is null")
    void shouldThrowWhenFirstNameIsNull() {
        orderDto.getFullName().setFirstName(null);

        assertThatThrownBy(() -> deliveryService.create(orderDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Must not be null full name fields");
    }

    @Test
    @DisplayName("Should throw ValidationException when originAddress is null")
    void shouldThrowWhenOriginAddressIsNull() {
        orderDto.setOriginAddress(null);

        assertThatThrownBy(() -> deliveryService.create(orderDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Must not be null fields");
    }

    @Test
    @DisplayName("Should throw ValidationException when destinationAddress is null")
    void shouldThrowWhenDestinationAddressIsNull() {
        orderDto.setDestinationAddress(null);

        assertThatThrownBy(() -> deliveryService.create(orderDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Must not be null fields");
    }

    @Test
    @DisplayName("Should throw ValidationException when orderType is null")
    void shouldThrowWhenOrderTypeIsNull() {
        orderDto.setOrderType(null);

        assertThatThrownBy(() -> deliveryService.create(orderDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Must not be null fields");
    }

    @Test
    @DisplayName("Should propagate exception from address processing")
    void shouldPropagateExceptionFromAddressProcessing() {
        when(addressProcessed.processOriginalAndDestinationAddresses(any(), any()))
                .thenThrow(new RuntimeException("Address error"));

        assertThatThrownBy(() -> deliveryService.create(orderDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Address error");

        verify(recipientProcessed, never()).processDelivery(any());
        verify(deliveryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should propagate exception from recipient processing")
    void shouldPropagateExceptionFromRecipientProcessing() {
        when(addressProcessed.processOriginalAndDestinationAddresses(any(), any()))
                .thenReturn(DeliveryTestDataFactory.defaultAddressPair());
        when(recipientProcessed.processDelivery(orderDto))
                .thenThrow(new RuntimeException("Recipient error"));

        assertThatThrownBy(() -> deliveryService.create(orderDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Recipient error");

        verify(deliveryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should propagate exception from repository save")
    void shouldPropagateExceptionFromRepositorySave() {
        var addressPair = DeliveryTestDataFactory.defaultAddressPair();
        when(addressProcessed.processOriginalAndDestinationAddresses(any(), any())).thenReturn(addressPair);
        when(recipientProcessed.processDelivery(orderDto)).thenReturn(recipient);
        when(deliveryMapper.toEntity(
                eq(orderDto), eq(recipient), eq(addressPair.origin()), eq(addressPair.destination())
        )).thenReturn(cardDelivery);
        when(deliveryRepository.save(cardDelivery)).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> deliveryService.create(orderDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }

    @Test
    @DisplayName("Should override message status to SUCCESS after mapping")
    void shouldOverrideMessageStatusToSuccess() {
        var addressPair = DeliveryTestDataFactory.defaultAddressPair();
        baseMessage.setStatus(OrderStatus.PENDING);

        when(addressProcessed.processOriginalAndDestinationAddresses(any(), any())).thenReturn(addressPair);
        when(recipientProcessed.processDelivery(orderDto)).thenReturn(recipient);
        when(deliveryMapper.toEntity(any(), any(), any(), any())).thenReturn(cardDelivery);
        when(deliveryRepository.save(cardDelivery)).thenReturn(cardDelivery);
        when(deliveryMapper.toMessage(cardDelivery, TRACE_ID)).thenReturn(baseMessage);

        BaseMessage result = deliveryService.create(orderDto);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.SUCCESS);
    }

    @Test
    @DisplayName("Should assign first available courier and set delivery to ASSIGNED")
    void shouldAssignFirstAvailableCourier() {
        var courier = DeliveryTestDataFactory.defaultCourier();
        var addressPair = DeliveryTestDataFactory.defaultAddressPair();

        when(addressProcessed.processOriginalAndDestinationAddresses(any(), any())).thenReturn(addressPair);
        when(recipientProcessed.processDelivery(orderDto)).thenReturn(recipient);
        when(deliveryMapper.toEntity(any(), any(), any(), any())).thenReturn(cardDelivery);
        when(courierService.assignFirstAvailable()).thenReturn(courier);
        when(deliveryRepository.save(cardDelivery)).thenReturn(cardDelivery);
        when(deliveryMapper.toMessage(cardDelivery, TRACE_ID)).thenReturn(baseMessage);

        BaseMessage result = deliveryService.create(orderDto);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.SUCCESS);
        assertThat(cardDelivery.getCourier()).isEqualTo(courier);
        assertThat(cardDelivery.getStatus()).isEqualTo(DeliveryStatus.ASSIGNED);
        verify(courierService).assignFirstAvailable();
    }

    @Test
    @DisplayName("Should keep delivery CREATED when no courier is available")
    void shouldKeepDeliveryCreatedWhenNoCourierAvailable() {
        var addressPair = DeliveryTestDataFactory.defaultAddressPair();

        when(addressProcessed.processOriginalAndDestinationAddresses(any(), any())).thenReturn(addressPair);
        when(recipientProcessed.processDelivery(orderDto)).thenReturn(recipient);
        when(deliveryMapper.toEntity(any(), any(), any(), any())).thenReturn(cardDelivery);
        when(courierService.assignFirstAvailable())
                .thenThrow(new DeliveryException("No available couriers"));
        when(deliveryRepository.save(cardDelivery)).thenReturn(cardDelivery);
        when(deliveryMapper.toMessage(cardDelivery, TRACE_ID)).thenReturn(baseMessage);

        BaseMessage result = deliveryService.create(orderDto);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.SUCCESS);
        assertThat(cardDelivery.getCourier()).isNull();
        assertThat(cardDelivery.getStatus()).isEqualTo(DeliveryStatus.CREATED);
        verify(deliveryRepository).save(cardDelivery);
    }

    @Test
    @DisplayName("Should throw DeliveryException when contact phone is blank")
    void shouldThrowWhenContactPhoneIsBlank() {
        orderDto.setContactPhone("   ");

        assertThatThrownBy(() -> deliveryService.create(orderDto))
                .isInstanceOf(DeliveryException.class)
                .hasMessageContaining("Phone must not be blank");

        verify(deliveryRepository, never()).save(any());
    }
}
