package com.github.seecret1.delivery_service.mapper;

import com.github.seecret1.delivery_service.dto.CourierDto;
import com.github.seecret1.delivery_service.entity.Courier;
import com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory.COURIER_CONTACT_PHONE;
import static com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory.COURIER_USER_ID;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CourierMapper Unit Tests")
class CourierMapperTest {

    private CourierMapper courierMapper;

    @BeforeEach
    void setUp() {
        courierMapper = new CourierMapperImpl();
    }

    @Test
    @DisplayName("Should map CourierDto to Courier entity")
    void shouldMapDtoToEntity() {
        CourierDto dto = DeliveryTestDataFactory.defaultCourierDto();

        Courier result = courierMapper.toEntity(dto);

        assertThat(result.getUserId()).isEqualTo(COURIER_USER_ID);
        assertThat(result.getContactPhone()).isEqualTo(COURIER_CONTACT_PHONE);
        assertThat(result.getDeleted()).isFalse();
        assertThat(result.getBusy()).isTrue();
        assertThat(result.getFullName().getFirstName()).isEqualTo("Ivan");
        assertThat(result.getFullName().getLastName()).isEqualTo("Petrov");
        assertThat(result.getFullName().getMiddleName()).isEqualTo("Sergeevich");
    }

    @Test
    @DisplayName("Should map Courier entity to CourierDto")
    void shouldMapEntityToDto() {
        Courier entity = DeliveryTestDataFactory.defaultCourier();

        CourierDto result = courierMapper.toDto(entity);

        assertThat(result.userId()).isEqualTo(COURIER_USER_ID);
        assertThat(result.contactPhone()).isEqualTo(COURIER_CONTACT_PHONE);
        assertThat(result.busy()).isFalse();
        assertThat(result.fullName().getFirstName()).isEqualTo("Ivan");
        assertThat(result.fullName().getLastName()).isEqualTo("Petrov");
    }
}