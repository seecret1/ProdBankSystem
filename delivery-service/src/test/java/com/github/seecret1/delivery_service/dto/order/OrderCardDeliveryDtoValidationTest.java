package com.github.seecret1.delivery_service.dto.order;

import com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrderCardDeliveryDto Validation Tests")
class OrderCardDeliveryDtoValidationTest {

    private OrderCardDeliveryDto orderDto;

    @BeforeEach
    void setUp() {
        orderDto = DeliveryTestDataFactory.validOrderCardDeliveryDto();
    }

    @Test
    @DisplayName("Should pass validation for valid dto")
    void shouldPassValidationForValidDto() {
        assertThatCode(orderDto::validate).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should fail when orderId is blank")
    void shouldFailWhenOrderIdIsBlank() {
        orderDto.setOrderId("  ");

        assertThatThrownBy(orderDto::validate)
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Ids must not be blank");
    }

    @Test
    @DisplayName("Should fail when userId is null")
    void shouldFailWhenUserIdIsNull() {
        orderDto.setUserId(null);

        assertThatThrownBy(orderDto::validate)
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Ids must not be null");
    }

    @Test
    @DisplayName("Should fail when lastName is null")
    void shouldFailWhenLastNameIsNull() {
        orderDto.getFullName().setLastName(null);

        assertThatThrownBy(orderDto::validate)
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Must not be null full name fields");
    }
}
