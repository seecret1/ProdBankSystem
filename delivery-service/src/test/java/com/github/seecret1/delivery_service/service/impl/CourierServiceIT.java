package com.github.seecret1.delivery_service.service.impl;

import com.github.seecret1.delivery_service.SpringBootApplicationTest;
import com.github.seecret1.delivery_service.dto.CourierDto;
import com.github.seecret1.delivery_service.entity.Courier;
import com.github.seecret1.delivery_service.exception.DeliveryException;
import com.github.seecret1.delivery_service.repository.CourierRepository;
import com.github.seecret1.delivery_service.service.CourierService;
import com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CourierService Integration Tests")
class CourierServiceIT extends SpringBootApplicationTest {

    @Autowired
    private CourierService courierService;

    @Autowired
    private CourierRepository courierRepository;

    @Test
    @DisplayName("Should create and find courier")
    void shouldCreateAndFindCourier() {
        CourierDto dto = DeliveryTestDataFactory.defaultCourierDto();

        CourierDto created = courierService.create(dto);

        assertThat(created.userId()).isEqualTo(DeliveryTestDataFactory.COURIER_USER_ID);
        assertThat(created.busy()).isFalse();

        Courier stored = courierRepository.findByUserId(DeliveryTestDataFactory.COURIER_USER_ID).orElseThrow();
        assertThat(stored.getContactPhone()).isEqualTo(DeliveryTestDataFactory.COURIER_CONTACT_PHONE);
        assertThat(stored.getBusy()).isFalse();
        assertThat(stored.getDeleted()).isFalse();
    }

    @Test
    @DisplayName("Should not create duplicate courier by userId")
    void shouldRejectDuplicateUserId() {
        CourierDto dto = DeliveryTestDataFactory.defaultCourierDto();

        courierService.create(dto);

        assertThatThrownBy(() -> courierService.create(dto))
                .isInstanceOf(DeliveryException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should return first available courier")
    void shouldReturnAvailableCourier() {
        courierService.create(DeliveryTestDataFactory.defaultCourierDto());

        CourierDto available = courierService.findAvailable();

        assertThat(available.userId()).isEqualTo(DeliveryTestDataFactory.COURIER_USER_ID);
    }

    @Test
    @DisplayName("Should assign first available courier and then run out of couriers")
    void shouldAssignThenRunOutOfCouriers() {
        courierService.create(DeliveryTestDataFactory.defaultCourierDto());

        Courier assigned = courierService.assignFirstAvailable();

        assertThat(assigned.getUserId()).isEqualTo(DeliveryTestDataFactory.COURIER_USER_ID);
        assertThat(assigned.getBusy()).isTrue();

        assertThatThrownBy(() -> courierService.assignFirstAvailable())
                .isInstanceOf(DeliveryException.class)
                .hasMessageContaining("No available couriers");
    }

    @Test
    @DisplayName("Should release courier and make it available again")
    void shouldReleaseCourier() {
        CourierDto created = courierService.create(DeliveryTestDataFactory.defaultCourierDto());

        Courier assigned = courierService.assignFirstAvailable();
        assertThat(assigned.getBusy()).isTrue();

        courierService.release(assigned);

        CourierDto available = courierService.findAvailable();
        assertThat(available.userId()).isEqualTo(created.userId());
    }

    @Test
    @DisplayName("Should set courier busy flag")
    void shouldSetBusy() {
        CourierDto created = courierService.create(DeliveryTestDataFactory.defaultCourierDto());

        Courier stored = courierRepository.findByUserId(created.userId()).orElseThrow();
        CourierDto updated = courierService.setBusy(stored.getId(), true);

        assertThat(updated.busy()).isTrue();
        assertThatThrownBy(() -> courierService.findAvailable())
                .isInstanceOf(DeliveryException.class)
                .hasMessageContaining("No available couriers");
    }

    @Test
    @DisplayName("Should soft delete courier")
    void shouldDeleteCourier() {
        CourierDto created = courierService.create(DeliveryTestDataFactory.defaultCourierDto());
        Courier stored = courierRepository.findByUserId(created.userId()).orElseThrow();

        courierService.delete(stored.getId());

        assertThat(courierRepository.findByIdAndDeletedFalse(stored.getId())).isEmpty();
        assertThatThrownBy(() -> courierService.findAvailable())
                .isInstanceOf(DeliveryException.class)
                .hasMessageContaining("No available couriers");
    }
}