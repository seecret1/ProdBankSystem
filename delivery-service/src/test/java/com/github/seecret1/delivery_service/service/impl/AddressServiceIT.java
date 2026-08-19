package com.github.seecret1.delivery_service.service.impl;

import com.github.seecret1.delivery_service.SpringBootApplicationTest;
import com.github.seecret1.delivery_service.dto.address.AddressRequest;
import com.github.seecret1.delivery_service.entity.Address;
import com.github.seecret1.delivery_service.entity.Country;
import com.github.seecret1.delivery_service.repository.AddressRepository;
import com.github.seecret1.delivery_service.repository.CountryRepository;
import com.github.seecret1.delivery_service.service.AddressService;
import com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AddressService Integration Tests")
class AddressServiceIT extends SpringBootApplicationTest {

    @Autowired
    private AddressService addressService;

    @Autowired
    private AddressRepository addressRepository;

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
    @DisplayName("Should create new address when not found")
    void shouldCreateNewAddressWhenNotFound() {
        AddressRequest addressRequest = new AddressRequest(
                "Lenina 1",
                "101000",
                "Moscow",
                "RU"
        );

        Address result = addressService.findOrCreate(addressRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getAddress()).isEqualTo("Lenina 1");
        assertThat(result.getZipCode()).isEqualTo("101000");
        assertThat(result.getCity()).isEqualTo("Moscow");
        assertThat(result.getCountry().getCode()).isEqualTo("RU");
        assertThat(result.getDeleted()).isFalse();
    }

    @Test
    @DisplayName("Should create different addresses for different cities")
    void shouldCreateDifferentAddressesForDifferentCities() {
        AddressRequest moscowAddress = new AddressRequest(
                "Kremlin Street",
                "119991",
                "Moscow",
                "RU"
        );
        AddressRequest spbAddress = new AddressRequest(
                "Nevsky Prospect",
                "191186",
                "Saint Petersburg",
                "RU"
        );

        Address moscow = addressService.findOrCreate(moscowAddress);
        Address spb = addressService.findOrCreate(spbAddress);

        assertThat(moscow.getId()).isNotEqualTo(spb.getId());
        assertThat(moscow.getCity()).isEqualTo("Moscow");
        assertThat(spb.getCity()).isEqualTo("Saint Petersburg");

        long count = addressRepository.count();
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should create different addresses for same city but different streets")
    void shouldCreateDifferentAddressesForSameCityDifferentStreets() {
        AddressRequest addressRequest1 = new AddressRequest(
                "Red Square",
                "109012",
                "Moscow",
                "RU"
        );
        AddressRequest addressRequest2 = new AddressRequest(
                "Tverskaya Street",
                "125009",
                "Moscow",
                "RU"
        );

        Address address1 = addressService.findOrCreate(addressRequest1);
        Address address2 = addressService.findOrCreate(addressRequest2);

        assertThat(address1.getId()).isNotEqualTo(address2.getId());
        assertThat(address1.getAddress()).isNotEqualTo(address2.getAddress());
        assertThat(address1.getCity()).isEqualTo(address2.getCity());

        long count = addressRepository.count();
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle address creation with unicode characters")
    void shouldHandleAddressWithUnicodeCharacters() {
        AddressRequest addressRequest = new AddressRequest(
                "Бульвар 'Петра' №123",
                "101000",
                "Москва",
                "RU"
        );

        Address result = addressService.findOrCreate(addressRequest);

        assertThat(result).isNotNull();
        assertThat(result.getAddress()).contains("Бульвар");
        assertThat(result.getCity()).isEqualTo("Москва");
    }

    @Test
    @DisplayName("Should preserve address data after creation")
    void shouldPreserveAddressDataAfterCreation() {
        AddressRequest addressRequest = DeliveryTestDataFactory.originAddressRequest();

        Address result = addressService.findOrCreate(addressRequest);
        String addressId = result.getId();

        Address fetched = addressRepository.findById(addressId).orElseThrow();

        assertThat(fetched.getId()).isEqualTo(addressId);
        assertThat(fetched.getAddress()).isEqualTo(addressRequest.address());
        assertThat(fetched.getZipCode()).isEqualTo(addressRequest.zipCode());
        assertThat(fetched.getCity()).isEqualTo(addressRequest.city());
    }

    @Test
    @DisplayName("Should reuse existing address on second creation attempt")
    void shouldReuseExistingAddress() {
        AddressRequest addressRequest = new AddressRequest(
                "Immutable Street",
                "999999",
                "Unchangeable City",
                "RU"
        );

        Address original = addressService.findOrCreate(addressRequest);
        Address second = addressService.findOrCreate(addressRequest);

        assertThat(original.getId()).isEqualTo(second.getId());
        assertThat(original.getAddress()).isEqualTo(second.getAddress());
        assertThat(original.getCity()).isEqualTo(second.getCity());
        assertThat(original.getZipCode()).isEqualTo(second.getZipCode());
    }
}
