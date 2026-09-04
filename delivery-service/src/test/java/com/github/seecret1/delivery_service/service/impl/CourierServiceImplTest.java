package com.github.seecret1.delivery_service.service.impl;

import com.github.seecret1.delivery_service.dto.CourierDto;
import com.github.seecret1.delivery_service.entity.Courier;
import com.github.seecret1.delivery_service.exception.DeliveryException;
import com.github.seecret1.delivery_service.mapper.CourierMapper;
import com.github.seecret1.delivery_service.repository.CourierRepository;
import com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory.COURIER_ID;
import static com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory.COURIER_USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourierService Unit Tests")
class CourierServiceImplTest {

    @Mock
    private CourierRepository courierRepository;

    @Mock
    private CourierMapper courierMapper;

    @InjectMocks
    private CourierServiceImpl courierService;

    @Test
    @DisplayName("Should create courier with busy=false")
    void shouldCreateCourier() {
        CourierDto dto = DeliveryTestDataFactory.defaultCourierDto();
        Courier entity = Courier.builder()
                .id(COURIER_ID)
                .userId(COURIER_USER_ID)
                .fullName(DeliveryTestDataFactory.defaultFullName())
                .busy(true)
                .contactPhone(DeliveryTestDataFactory.COURIER_CONTACT_PHONE)
                .deleted(false)
                .build();

        when(courierRepository.existsByUserId(COURIER_USER_ID)).thenReturn(false);
        when(courierMapper.toEntity(dto)).thenReturn(entity);
        when(courierRepository.save(entity)).thenReturn(entity);
        when(courierMapper.toDto(entity)).thenReturn(dto);

        CourierDto result = courierService.create(dto);

        assertThat(result).isEqualTo(dto);
        assertThat(entity.getBusy()).isFalse();
        verify(courierRepository).save(entity);
    }

    @Test
    @DisplayName("Should throw when courier with same userId already exists")
    void shouldThrowWhenUserAlreadyExists() {
        CourierDto dto = DeliveryTestDataFactory.defaultCourierDto();

        when(courierRepository.existsByUserId(COURIER_USER_ID)).thenReturn(true);

        assertThatThrownBy(() -> courierService.create(dto))
                .isInstanceOf(DeliveryException.class)
                .hasMessageContaining("already exists");

        verify(courierRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return all active couriers")
    void shouldFindAll() {
        Courier courier = DeliveryTestDataFactory.defaultCourier();
        CourierDto dto = DeliveryTestDataFactory.defaultCourierDto();

        when(courierRepository.findAllByDeletedFalseOrderByCreatedAtAsc()).thenReturn(List.of(courier));
        when(courierMapper.toDto(courier)).thenReturn(dto);

        List<CourierDto> result = courierService.findAll();

        assertThat(result).containsExactly(dto);
    }

    @Test
    @DisplayName("Should return first available courier")
    void shouldFindAvailable() {
        Courier courier = DeliveryTestDataFactory.defaultCourier();
        CourierDto dto = DeliveryTestDataFactory.defaultCourierDto();

        when(courierRepository.findFirstByBusyFalseAndDeletedFalseOrderByCreatedAtAsc())
                .thenReturn(Optional.of(courier));
        when(courierMapper.toDto(courier)).thenReturn(dto);

        CourierDto result = courierService.findAvailable();

        assertThat(result).isEqualTo(dto);
    }

    @Test
    @DisplayName("Should throw when no available courier")
    void shouldThrowWhenNoAvailableCourier() {
        when(courierRepository.findFirstByBusyFalseAndDeletedFalseOrderByCreatedAtAsc())
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> courierService.findAvailable())
                .isInstanceOf(DeliveryException.class)
                .hasMessageContaining("No available couriers");
    }

    @Test
    @DisplayName("Should assign first available courier and mark busy")
    void shouldAssignFirstAvailable() {
        Courier courier = DeliveryTestDataFactory.defaultCourier();

        when(courierRepository.findFirstByBusyFalseAndDeletedFalseOrderByCreatedAtAsc())
                .thenReturn(Optional.of(courier));

        Courier result = courierService.assignFirstAvailable();

        assertThat(result).isEqualTo(courier);
        assertThat(courier.getBusy()).isTrue();
    }

    @Test
    @DisplayName("Should throw when assigning with no available courier")
    void shouldThrowWhenAssigningWithNoAvailableCourier() {
        when(courierRepository.findFirstByBusyFalseAndDeletedFalseOrderByCreatedAtAsc())
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> courierService.assignFirstAvailable())
                .isInstanceOf(DeliveryException.class)
                .hasMessageContaining("No available couriers");
    }

    @Test
    @DisplayName("Should set courier busy flag")
    void shouldSetBusy() {
        Courier courier = DeliveryTestDataFactory.defaultCourier();
        CourierDto dto = DeliveryTestDataFactory.defaultCourierDto();

        when(courierRepository.findByIdAndDeletedFalse(COURIER_ID)).thenReturn(Optional.of(courier));
        when(courierMapper.toDto(courier)).thenReturn(dto);

        CourierDto result = courierService.setBusy(COURIER_ID, true);

        assertThat(result).isEqualTo(dto);
        assertThat(courier.getBusy()).isTrue();
    }

    @Test
    @DisplayName("Should throw when courier not found for setBusy")
    void shouldThrowWhenSetBusyCourierNotFound() {
        when(courierRepository.findByIdAndDeletedFalse(COURIER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courierService.setBusy(COURIER_ID, true))
                .isInstanceOf(DeliveryException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should release courier")
    void shouldReleaseCourier() {
        Courier courier = DeliveryTestDataFactory.defaultCourier();
        courier.setBusy(true);

        courierService.release(courier);

        assertThat(courier.getBusy()).isFalse();
    }

    @Test
    @DisplayName("Should soft delete courier")
    void shouldDeleteCourier() {
        Courier courier = DeliveryTestDataFactory.defaultCourier();

        when(courierRepository.findByIdAndDeletedFalse(COURIER_ID)).thenReturn(Optional.of(courier));

        courierService.delete(COURIER_ID);

        assertThat(courier.getDeleted()).isTrue();
        assertThat(courier.getDeletedAt()).isNotNull();
        assertThat(courier.getDeletedBy()).isEqualTo("system");
    }
}