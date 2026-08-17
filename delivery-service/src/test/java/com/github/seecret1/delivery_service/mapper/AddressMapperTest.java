package com.github.seecret1.delivery_service.mapper;

import com.github.seecret1.delivery_service.dto.address.AddressBaseResponse;
import com.github.seecret1.delivery_service.dto.address.AddressRequest;
import com.github.seecret1.delivery_service.dto.address.AddressResponse;
import com.github.seecret1.delivery_service.entity.Address;
import com.github.seecret1.delivery_service.entity.Country;
import com.github.seecret1.delivery_service.exception.DeliveryException;
import com.github.seecret1.delivery_service.repository.CountryRepository;
import com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory;
import com.github.seecret1.delivery_service.utils.DateTimeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddressMapper Unit Tests")
class AddressMapperTest {

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private DateTimeUtil dateTimeUtil;

    @InjectMocks
    private AddressMapperImpl addressMapper;

    private AddressRequest addressRequest;
    private Address address;
    private Country country;
    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.parse("2026-08-17T10:00:00Z");
        addressMapper.setCountryRepository(countryRepository);
        addressMapper.setDateTimeUtil(dateTimeUtil);

        country = DeliveryTestDataFactory.defaultCountry();
        addressRequest = DeliveryTestDataFactory.originAddressRequest();
        address = DeliveryTestDataFactory.originAddress();
    }

    @Test
    @DisplayName("Should map AddressRequest to Address when country exists")
    void shouldMapToAddressWhenCountryExists() {
        when(countryRepository.findByCode("RU")).thenReturn(Optional.of(country));
        when(dateTimeUtil.now()).thenReturn(now);

        Address result = addressMapper.toAddress(addressRequest);

        assertThat(result.getAddress()).isEqualTo("Lenina 1");
        assertThat(result.getZipCode()).isEqualTo("101000");
        assertThat(result.getCity()).isEqualTo("Moscow");
        assertThat(result.getCountry()).isEqualTo(country);
        assertThat(result.getDeleted()).isFalse();
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should throw DeliveryException when country not found")
    void shouldThrowWhenCountryNotFound() {
        when(countryRepository.findByCode("RU")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressMapper.toAddress(addressRequest))
                .isInstanceOf(DeliveryException.class)
                .hasMessageContaining("Unknow country code: [RU]");
    }

    @Test
    @DisplayName("Should map Address to AddressResponse")
    void shouldMapFromAddress() {
        AddressResponse result = addressMapper.fromAddress(address);

        assertThat(result.address()).isEqualTo("Lenina 1");
        assertThat(result.zipCode()).isEqualTo("101000");
        assertThat(result.city()).isEqualTo("Moscow");
        assertThat(result.deleted()).isFalse();
        assertThat(result.createdAt()).isEqualTo(address.getCreatedAt());
        assertThat(result.updatedAt()).isEqualTo(address.getUpdatedAt());
    }

    @Test
    @DisplayName("Should map Address to AddressBaseResponse")
    void shouldMapFromBaseAddress() {
        AddressBaseResponse result = addressMapper.fromBaseAddress(address);

        assertThat(result).isNotNull();
        assertThat(result.deleted()).isFalse();
    }

    @Test
    @DisplayName("Should update address fields from request")
    void shouldUpdateAddress() {
        AddressRequest updateRequest = new AddressRequest(
                "New Street 5", "999999", "Kazan", "RU"
        );
        when(countryRepository.findByCode("RU")).thenReturn(Optional.of(country));
        when(dateTimeUtil.now()).thenReturn(now);

        Address result = addressMapper.updateAddress(address, updateRequest);

        assertThat(result.getAddress()).isEqualTo("New Street 5");
        assertThat(result.getZipCode()).isEqualTo("999999");
        assertThat(result.getCity()).isEqualTo("Kazan");
        assertThat(result.getCountry()).isEqualTo(country);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should resolve country by code")
    void shouldResolveCountryByCode() {
        when(countryRepository.findByCode("RU")).thenReturn(Optional.of(country));

        Country result = addressMapper.toCountry("RU");

        assertThat(result.getCode()).isEqualTo("RU");
        assertThat(result.getName()).isEqualTo("Russia");
    }

    @Test
    @DisplayName("Should throw DeliveryException for unknown country code in toCountry")
    void shouldThrowForUnknownCountryCode() {
        when(countryRepository.findByCode("XX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressMapper.toCountry("XX"))
                .isInstanceOf(DeliveryException.class)
                .hasMessageContaining("Unknow country code: [XX]");
    }
}
