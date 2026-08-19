package com.github.seecret1.delivery_service.service.impl;

import com.github.seecret1.delivery_service.SpringBootApplicationTest;
import com.github.seecret1.delivery_service.dto.BaseMessage;
import com.github.seecret1.delivery_service.dto.order.OrderCardDeliveryDto;
import com.github.seecret1.delivery_service.entity.Country;
import com.github.seecret1.delivery_service.entity.enums.OrderStatus;
import com.github.seecret1.delivery_service.repository.CountryRepository;
import com.github.seecret1.delivery_service.repository.DeliveryRepository;
import com.github.seecret1.delivery_service.service.DeliveryService;
import com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DeliveryService Integration Tests")
class DeliveryServiceIT extends SpringBootApplicationTest {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private CountryRepository countryRepository;

    private Country testCountry;

    @BeforeEach
    void setUp() {
        testCountry = new Country();
        testCountry.setCode("RU");
        testCountry.setName("Russia");
        testCountry.setDeleted(false);
        testCountry = countryRepository.save(testCountry);
    }

    @Test
    @DisplayName("Should create delivery successfully with valid order")
    void shouldCreateDeliverySuccessfully() {
        OrderCardDeliveryDto orderDto = DeliveryTestDataFactory.validOrderCardDeliveryDto();

        BaseMessage result = deliveryService.create(orderDto);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.SUCCESS);
        assertThat(result.getTraceId()).isEqualTo(orderDto.getTraceId());
        assertThat(result.getOrderId()).isEqualTo(orderDto.getOrderId());
    }

    @Test
    @DisplayName("Should preserve delivery data after creation")
    void shouldPreserveDeliveryDataAfterCreation() {
        OrderCardDeliveryDto orderDto = DeliveryTestDataFactory.validOrderCardDeliveryDto();

        BaseMessage result = deliveryService.create(orderDto);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.SUCCESS);
        assertThat(result.getOrderId()).isEqualTo(orderDto.getOrderId());
        assertThat(result.getUserId()).isEqualTo(orderDto.getUserId());
    }

    @Test
    @DisplayName("Should throw exception when order validation fails")
    void shouldThrowExceptionWhenOrderValidationFails() {
        OrderCardDeliveryDto invalidOrder = OrderCardDeliveryDto.builder()
                .traceId(null)
                .userId(null)
                .orderId(null)
                .orderType(null)
                .cardType(null)
                .personType(null)
                .fullName(null)
                .contactPhone(null)
                .originAddress(null)
                .destinationAddress(null)
                .build();

        assertThatThrownBy(() -> deliveryService.create(invalidOrder))
                .isNotNull();
    }

    @Test
    @DisplayName("Should create multiple deliveries for different orders")
    void shouldCreateMultipleDeliveries() {
        OrderCardDeliveryDto order1 = OrderCardDeliveryDto.builder()
                .traceId("trace-1")
                .userId("user-1")
                .orderId("order-1")
                .orderType(DeliveryTestDataFactory.validOrderCardDeliveryDto().getOrderType())
                .createdAt(DeliveryTestDataFactory.validOrderCardDeliveryDto().getCreatedAt())
                .plannedDeliveryTime(DeliveryTestDataFactory.validOrderCardDeliveryDto().getPlannedDeliveryTime())
                .cardType(DeliveryTestDataFactory.validOrderCardDeliveryDto().getCardType())
                .officeId("office-1")
                .fullName(DeliveryTestDataFactory.defaultFullNameDto())
                .contactPhone("+79991111111")
                .originAddress(DeliveryTestDataFactory.originAddressRequest())
                .destinationAddress(DeliveryTestDataFactory.destinationAddressRequest())
                .personType(DeliveryTestDataFactory.validOrderCardDeliveryDto().getPersonType())
                .build();

        OrderCardDeliveryDto order2 = OrderCardDeliveryDto.builder()
                .traceId("trace-2")
                .userId("user-2")
                .orderId("order-2")
                .orderType(DeliveryTestDataFactory.validOrderCardDeliveryDto().getOrderType())
                .createdAt(DeliveryTestDataFactory.validOrderCardDeliveryDto().getCreatedAt())
                .plannedDeliveryTime(DeliveryTestDataFactory.validOrderCardDeliveryDto().getPlannedDeliveryTime())
                .cardType(DeliveryTestDataFactory.validOrderCardDeliveryDto().getCardType())
                .officeId("office-2")
                .fullName(DeliveryTestDataFactory.defaultFullNameDto())
                .contactPhone("+79992222222")
                .originAddress(DeliveryTestDataFactory.originAddressRequest())
                .destinationAddress(DeliveryTestDataFactory.destinationAddressRequest())
                .personType(DeliveryTestDataFactory.validOrderCardDeliveryDto().getPersonType())
                .build();

        BaseMessage result1 = deliveryService.create(order1);
        BaseMessage result2 = deliveryService.create(order2);

        assertThat(result1.getOrderId()).isNotEqualTo(result2.getOrderId());
        assertThat(result1.getUserId()).isNotEqualTo(result2.getUserId());

        long deliveryCount = deliveryRepository.count();
        assertThat(deliveryCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle delivery with unicode characters in name")
    void shouldHandleDeliveryWithUnicodeCharacters() {
        OrderCardDeliveryDto orderDto = OrderCardDeliveryDto.builder()
                .traceId("trace-unicode")
                .userId("user-unicode")
                .orderId("order-unicode")
                .orderType(DeliveryTestDataFactory.validOrderCardDeliveryDto().getOrderType())
                .createdAt(DeliveryTestDataFactory.validOrderCardDeliveryDto().getCreatedAt())
                .plannedDeliveryTime(DeliveryTestDataFactory.validOrderCardDeliveryDto().getPlannedDeliveryTime())
                .cardType(DeliveryTestDataFactory.validOrderCardDeliveryDto().getCardType())
                .officeId("office-unicode")
                .fullName(new com.github.seecret1.delivery_service.dto.user.FullNameDto(
                        "Иван", "Петров", "Сергеевич"
                ))
                .contactPhone("+79993333333")
                .originAddress(DeliveryTestDataFactory.originAddressRequest())
                .destinationAddress(DeliveryTestDataFactory.destinationAddressRequest())
                .personType(DeliveryTestDataFactory.validOrderCardDeliveryDto().getPersonType())
                .build();

        BaseMessage result = deliveryService.create(orderDto);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.SUCCESS);
        long deliveryCount = deliveryRepository.count();
        assertThat(deliveryCount).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should return correct trace ID in response message")
    void shouldReturnCorrectTraceIdInResponse() {
        String expectedTraceId = "trace-correct-id-12345";
        OrderCardDeliveryDto orderDto = OrderCardDeliveryDto.builder()
                .traceId(expectedTraceId)
                .userId("user-trace-id")
                .orderId("order-trace-id")
                .orderType(DeliveryTestDataFactory.validOrderCardDeliveryDto().getOrderType())
                .createdAt(DeliveryTestDataFactory.validOrderCardDeliveryDto().getCreatedAt())
                .plannedDeliveryTime(DeliveryTestDataFactory.validOrderCardDeliveryDto().getPlannedDeliveryTime())
                .cardType(DeliveryTestDataFactory.validOrderCardDeliveryDto().getCardType())
                .officeId("office-trace")
                .fullName(DeliveryTestDataFactory.defaultFullNameDto())
                .contactPhone("+79995555555")
                .originAddress(DeliveryTestDataFactory.originAddressRequest())
                .destinationAddress(DeliveryTestDataFactory.destinationAddressRequest())
                .personType(DeliveryTestDataFactory.validOrderCardDeliveryDto().getPersonType())
                .build();

        BaseMessage result = deliveryService.create(orderDto);

        assertThat(result.getTraceId()).isEqualTo(expectedTraceId);
    }
}
