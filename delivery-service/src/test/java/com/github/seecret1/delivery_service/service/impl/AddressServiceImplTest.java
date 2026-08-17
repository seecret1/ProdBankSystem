package com.github.seecret1.delivery_service.service.impl;

import com.github.seecret1.delivery_service.dto.address.AddressRequest;
import com.github.seecret1.delivery_service.entity.Address;
import com.github.seecret1.delivery_service.mapper.AddressMapper;
import com.github.seecret1.delivery_service.repository.AddressRepository;
import com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddressService Unit Tests")
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    private AddressRequest addressRequest;
    private Address existingAddress;
    private Address newAddress;

    @BeforeEach
    void setUp() {
        addressRequest = DeliveryTestDataFactory.originAddressRequest();
        existingAddress = DeliveryTestDataFactory.originAddress();
        newAddress = DeliveryTestDataFactory.originAddress();
        newAddress.setId("new-address-id");
    }

    @Test
    @DisplayName("Should return existing address when found")
    void shouldReturnExistingAddress() {
        when(addressRepository.findByCityAndAddressAndCountryCode(
                addressRequest.city(), addressRequest.address(), addressRequest.countryCode()
        )).thenReturn(Optional.of(existingAddress));

        Address result = addressService.findOrCreate(addressRequest);

        assertThat(result).isEqualTo(existingAddress);
        verify(addressMapper, never()).toAddress(any());
        verify(addressRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create and save new address when not found")
    void shouldCreateNewAddressWhenNotFound() {
        when(addressRepository.findByCityAndAddressAndCountryCode(
                addressRequest.city(), addressRequest.address(), addressRequest.countryCode()
        )).thenReturn(Optional.empty());
        when(addressMapper.toAddress(addressRequest)).thenReturn(newAddress);
        when(addressRepository.save(newAddress)).thenReturn(newAddress);

        Address result = addressService.findOrCreate(addressRequest);

        assertThat(result).isEqualTo(newAddress);
        verify(addressMapper).toAddress(addressRequest);
        verify(addressRepository).save(newAddress);
    }

    @Test
    @DisplayName("Should search by city, address and country code")
    void shouldSearchByCityAddressAndCountryCode() {
        when(addressRepository.findByCityAndAddressAndCountryCode(
                "Moscow", "Lenina 1", "RU"
        )).thenReturn(Optional.of(existingAddress));

        Address result = addressService.findOrCreate(addressRequest);

        assertThat(result.getCity()).isEqualTo("Moscow");
        assertThat(result.getAddress()).isEqualTo("Lenina 1");
        verify(addressRepository).findByCityAndAddressAndCountryCode("Moscow", "Lenina 1", "RU");
    }

    @Test
    @DisplayName("Should not save when address already exists")
    void shouldNotSaveWhenAddressExists() {
        when(addressRepository.findByCityAndAddressAndCountryCode(
                addressRequest.city(), addressRequest.address(), addressRequest.countryCode()
        )).thenReturn(Optional.of(existingAddress));

        addressService.findOrCreate(addressRequest);

        verify(addressRepository, never()).save(any());
    }
}
