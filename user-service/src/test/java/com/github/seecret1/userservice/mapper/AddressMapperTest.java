package com.github.seecret1.userservice.mapper;

import com.github.seecret1.userservice.dto.request.AddressRequest;
import com.github.seecret1.userservice.dto.response.AddressResponse;
import com.github.seecret1.userservice.entity.Address;
import com.github.seecret1.userservice.entity.Country;
import com.github.seecret1.userservice.exception.PersonException;
import com.github.seecret1.userservice.mapper.AddressMapperImpl;
import com.github.seecret1.userservice.repository.CountryRepository;
import com.github.seecret1.userservice.utils.DateTimeUtil;
import org.junit.jupiter.api.BeforeEach;
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
        now = Instant.now();
        addressMapper.setDateTimeUtil(dateTimeUtil);

        country = new Country();
        country.setId(1);
        country.setCode("US");
        country.setName("United States");

        addressRequest = new AddressRequest(
                "123 Main St", "12345", "New York", "US"
        );

        address = new Address();
        address.setId("1");
        address.setAddress("123 Main St");
        address.setZipCode("12345");
        address.setCity("New York");
        address.setCountry(country);
        address.setDeleted(false);
        address.setCreatedAt(now);
        address.setUpdatedAt(now);
    }

    @Test
    void toAddress_ShouldReturnAddress_WhenCountryExists() {
        when(countryRepository.findByCode("US")).thenReturn(Optional.of(country));
        when(dateTimeUtil.now()).thenReturn(now);

        Address result = addressMapper.toAddress(addressRequest);

        assertThat(result).isNotNull();
        assertThat(result.getAddress()).isEqualTo("123 Main St");
        assertThat(result.getZipCode()).isEqualTo("12345");
        assertThat(result.getCity()).isEqualTo("New York");
        assertThat(result.getCountry()).isEqualTo(country);
        assertThat(result.getDeleted()).isFalse();
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toAddress_ShouldThrowPersonException_WhenCountryNotFound() {
        when(countryRepository.findByCode("US")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressMapper.toAddress(addressRequest))
                .isInstanceOf(PersonException.class)
                .hasMessageContaining("Unknow country code");
    }

    @Test
    void fromAddress_ShouldReturnAddressResponse() {
        AddressResponse result = addressMapper.fromAddress(address);

        assertThat(result).isNotNull();
        assertThat(result.address()).isEqualTo("123 Main St");
        assertThat(result.zipCode()).isEqualTo("12345");
        assertThat(result.city()).isEqualTo("New York");
        assertThat(result.deleted()).isFalse();
        assertThat(result.createdAt()).isEqualTo(now);
        assertThat(result.updatedAt()).isEqualTo(now);
    }

    @Test
    void updateAddress_ShouldUpdateAddressFields() {
        AddressRequest updateRequest = new AddressRequest(
                "456 Oak Ave", "67890", "Los Angeles", "US"
        );
        when(countryRepository.findByCode("US")).thenReturn(Optional.of(country));
        when(dateTimeUtil.now()).thenReturn(now);

        Address result = addressMapper.updateAddress(address, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getAddress()).isEqualTo("456 Oak Ave");
        assertThat(result.getZipCode()).isEqualTo("67890");
        assertThat(result.getCity()).isEqualTo("Los Angeles");
        assertThat(result.getCountry()).isEqualTo(country);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toCountry_ShouldReturnCountry_WhenExists() {
        when(countryRepository.findByCode("US")).thenReturn(Optional.of(country));

        Country result = addressMapper.toCountry("US");

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("US");
        assertThat(result.getName()).isEqualTo("United States");
    }

    @Test
    void toCountry_ShouldThrowPersonException_WhenCountryNotFound() {
        when(countryRepository.findByCode("XX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressMapper.toCountry("XX"))
                .isInstanceOf(PersonException.class)
                .hasMessageContaining("Unknow country code");
    }
}