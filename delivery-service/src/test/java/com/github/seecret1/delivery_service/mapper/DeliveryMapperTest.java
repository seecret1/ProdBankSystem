package com.github.seecret1.delivery_service.mapper;

import com.github.seecret1.delivery_service.dto.BaseMessage;
import com.github.seecret1.delivery_service.dto.DeliveryResponse;
import com.github.seecret1.delivery_service.dto.address.AddressResponse;
import com.github.seecret1.delivery_service.dto.order.OrderCardDeliveryDto;
import com.github.seecret1.delivery_service.dto.user.FullNameDto;
import com.github.seecret1.delivery_service.dto.user.RecipientDto;
import com.github.seecret1.delivery_service.entity.CardDelivery;
import com.github.seecret1.delivery_service.entity.FullName;
import com.github.seecret1.delivery_service.entity.Recipient;
import com.github.seecret1.delivery_service.entity.enums.DeliveryStatus;
import com.github.seecret1.delivery_service.entity.enums.OrderStatus;
import com.github.seecret1.delivery_service.entity.enums.PersonType;
import com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory.ORDER_ID;
import static com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory.TRACE_ID;
import static com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeliveryMapper Unit Tests")
class DeliveryMapperTest {

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private DeliveryMapper deliveryMapper;

    private OrderCardDeliveryDto orderDto;
    private Recipient recipient;
    private CardDelivery cardDelivery;

    @BeforeEach
    void setUp() {
        orderDto = DeliveryTestDataFactory.validOrderCardDeliveryDto();
        recipient = DeliveryTestDataFactory.defaultRecipient();
        cardDelivery = DeliveryTestDataFactory.defaultCardDelivery(recipient);
    }

    @Test
    @DisplayName("Should map order dto to CardDelivery entity")
    void shouldMapToEntity() {
        var origin = DeliveryTestDataFactory.originAddress();
        var destination = DeliveryTestDataFactory.destinationAddress();

        CardDelivery result = deliveryMapper.toEntity(orderDto, recipient, origin, destination);

        assertThat(result.getOrderId()).isEqualTo(ORDER_ID);
        assertThat(result.getRecipient()).isEqualTo(recipient);
        assertThat(result.getOriginAddress()).isEqualTo(origin);
        assertThat(result.getDestinationAddress()).isEqualTo(destination);
        assertThat(result.getPlannedDeliveryTime()).isEqualTo(orderDto.getPlannedDeliveryTime());
        assertThat(result.getCardType()).isEqualTo(orderDto.getCardType());
        assertThat(result.getStatus()).isEqualTo(DeliveryStatus.CREATED);
        assertThat(result.getDeleted()).isFalse();
    }

    @Test
    @DisplayName("Should map CardDelivery to BaseMessage")
    void shouldMapToMessage() {
        BaseMessage result = deliveryMapper.toMessage(cardDelivery, TRACE_ID);

        assertThat(result.getTraceId()).isEqualTo(TRACE_ID);
        assertThat(result.getOrderId()).isEqualTo(ORDER_ID);
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getProductId()).isEqualTo(cardDelivery.getId());
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getMessage()).isEqualTo("Delivery request accepted");
        assertThat(result.getTimestamp()).isNotNull();
        assertThat(result.getData()).isInstanceOf(DeliveryResponse.class);
    }

    @Test
    @DisplayName("Should map CardDelivery to DeliveryResponse")
    void shouldMapToResponse() {
        var originResponse = new AddressResponse(
                false, cardDelivery.getOriginAddress().getCreatedAt(),
                cardDelivery.getOriginAddress().getUpdatedAt(),
                null, null, "Lenina 1", "101000", "Moscow"
        );
        var destinationResponse = new AddressResponse(
                false, cardDelivery.getDestinationAddress().getCreatedAt(),
                cardDelivery.getDestinationAddress().getUpdatedAt(),
                null, null, "Pushkina 10", "190000", "Saint Petersburg"
        );
        RecipientDto recipientDto = DeliveryTestDataFactory.defaultRecipientDto();

        when(addressMapper.fromAddress(cardDelivery.getOriginAddress())).thenReturn(originResponse);
        when(addressMapper.fromAddress(cardDelivery.getDestinationAddress())).thenReturn(destinationResponse);

        DeliveryResponse result = deliveryMapper.toResponse(cardDelivery, recipientDto);

        assertThat(result.getRecipient()).isEqualTo(recipientDto);
        assertThat(result.getOriginAddress()).isEqualTo(originResponse);
        assertThat(result.getDestinationAddress()).isEqualTo(destinationResponse);
        assertThat(result.getCreatedAt()).isEqualTo(cardDelivery.getCreatedAt());
    }

    @Test
    @DisplayName("Should map RecipientDto to Recipient entity")
    void shouldMapRecipientDtoToEntity() {
        RecipientDto dto = DeliveryTestDataFactory.defaultRecipientDto();

        Recipient result = deliveryMapper.toRecipientEntity(dto);

        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getContactPhone()).isEqualTo(dto.getContactPhone());
        assertThat(result.getOfficeId()).isEqualTo(dto.getOfficeId());
        assertThat(result.getPersonType()).isEqualTo(PersonType.PHYSICAL);
        assertThat(result.getFullName().getFirstName()).isEqualTo("Ivan");
        assertThat(result.getFullName().getLastName()).isEqualTo("Petrov");
        assertThat(result.getFullName().getMiddleName()).isEqualTo("Sergeevich");
    }

    @Test
    @DisplayName("Should map Recipient entity to RecipientDto")
    void shouldMapRecipientEntityToDto() {
        RecipientDto result = deliveryMapper.toRecipientDto(recipient);

        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getContactPhone()).isEqualTo(recipient.getContactPhone());
        assertThat(result.getOfficeId()).isEqualTo(recipient.getOfficeId());
        assertThat(result.getPersonType()).isEqualTo(PersonType.PHYSICAL);
        assertThat(result.getFullName().getFirstName()).isEqualTo("Ivan");
    }

    @Test
    @DisplayName("Should map FullNameDto to FullName entity")
    void shouldMapFullNameDtoToEntity() {
        FullNameDto dto = DeliveryTestDataFactory.defaultFullNameDto();

        FullName result = deliveryMapper.toFullNameEntity(dto);

        assertThat(result.getFirstName()).isEqualTo("Ivan");
        assertThat(result.getLastName()).isEqualTo("Petrov");
        assertThat(result.getMiddleName()).isEqualTo("Sergeevich");
    }

    @Test
    @DisplayName("Should map FullName entity to FullNameDto")
    void shouldMapFullNameEntityToDto() {
        FullName entity = DeliveryTestDataFactory.defaultFullName();

        FullNameDto result = deliveryMapper.toFullNameDto(entity);

        assertThat(result.getFirstName()).isEqualTo("Ivan");
        assertThat(result.getLastName()).isEqualTo("Petrov");
        assertThat(result.getMiddleName()).isEqualTo("Sergeevich");
    }

    @Test
    @DisplayName("Should include courier fields in DeliveryResponse when present")
    void shouldIncludeCourierFieldsInResponse() {
        cardDelivery.setCourier(DeliveryTestDataFactory.defaultCourier());
        RecipientDto recipientDto = DeliveryTestDataFactory.defaultRecipientDto();

        when(addressMapper.fromAddress(cardDelivery.getOriginAddress())).thenReturn(
                new AddressResponse(false, null, null, null, null, "a", "z", "c")
        );
        when(addressMapper.fromAddress(cardDelivery.getDestinationAddress())).thenReturn(
                new AddressResponse(false, null, null, null, null, "a", "z", "c")
        );

        DeliveryResponse result = deliveryMapper.toResponse(cardDelivery, recipientDto);

        assertThat(result.getCourierId()).isEqualTo(DeliveryTestDataFactory.COURIER_ID);
        assertThat(result.getCourierContactPhone()).isEqualTo(DeliveryTestDataFactory.COURIER_CONTACT_PHONE);
    }
}
