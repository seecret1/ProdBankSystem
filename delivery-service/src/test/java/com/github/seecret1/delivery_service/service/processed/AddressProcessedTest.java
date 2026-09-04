package com.github.seecret1.delivery_service.service.processed;

import com.github.seecret1.delivery_service.dto.address.AddressPair;
import com.github.seecret1.delivery_service.dto.address.AddressRequest;
import com.github.seecret1.delivery_service.entity.Address;
import com.github.seecret1.delivery_service.service.AddressService;
import com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddressProcessed Unit Tests")
class AddressProcessedTest {

    @Mock
    private AddressService addressService;

    @InjectMocks
    private AddressProcessed addressProcessed;

    private AddressRequest originRequest;
    private AddressRequest destinationRequest;
    private Address originAddress;
    private Address destinationAddress;

    @BeforeEach
    void setUp() {
        originRequest = DeliveryTestDataFactory.originAddressRequest();
        destinationRequest = DeliveryTestDataFactory.destinationAddressRequest();
        originAddress = DeliveryTestDataFactory.originAddress();
        destinationAddress = DeliveryTestDataFactory.destinationAddress();
    }

    @Test
    @DisplayName("Should process origin and destination addresses")
    void shouldProcessOriginAndDestinationAddresses() {
        when(addressService.findOrCreate(originRequest)).thenReturn(originAddress);
        when(addressService.findOrCreate(destinationRequest)).thenReturn(destinationAddress);

        AddressPair result = addressProcessed.processOriginalAndDestinationAddresses(
                originRequest, destinationRequest
        );

        assertThat(result.origin()).isEqualTo(originAddress);
        assertThat(result.destination()).isEqualTo(destinationAddress);
        verify(addressService).findOrCreate(originRequest);
        verify(addressService).findOrCreate(destinationRequest);
    }

    @Test
    @DisplayName("Should call findOrCreate for both addresses independently")
    void shouldCallFindOrCreateForBothAddresses() {
        when(addressService.findOrCreate(originRequest)).thenReturn(originAddress);
        when(addressService.findOrCreate(destinationRequest)).thenReturn(destinationAddress);

        addressProcessed.processOriginalAndDestinationAddresses(originRequest, destinationRequest);

        verify(addressService).findOrCreate(originRequest);
        verify(addressService).findOrCreate(destinationRequest);
    }

    @Test
    @DisplayName("Should handle same address for origin and destination")
    void shouldHandleSameAddressForOriginAndDestination() {
        when(addressService.findOrCreate(originRequest)).thenReturn(originAddress);

        AddressPair result = addressProcessed.processOriginalAndDestinationAddresses(
                originRequest, originRequest
        );

        assertThat(result.origin()).isEqualTo(originAddress);
        assertThat(result.destination()).isEqualTo(originAddress);
        verify(addressService, times(2)).findOrCreate(originRequest);
    }
}
