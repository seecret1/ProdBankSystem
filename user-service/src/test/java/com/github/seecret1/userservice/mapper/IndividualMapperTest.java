package com.github.seecret1.userservice.mapper;

import com.github.seecret1.userservice.dto.request.AddressRequest;
import com.github.seecret1.userservice.dto.request.IndividualRequest;
import com.github.seecret1.userservice.dto.response.AddressResponse;
import com.github.seecret1.userservice.entity.Address;
import com.github.seecret1.userservice.entity.Country;
import com.github.seecret1.userservice.entity.Individual;
import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.utils.DateTimeUtil;
import com.github.seecret1.userservice.utils.EncryptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndividualMapperTest {

    @Mock
    private AddressMapper addressMapper;

    @Mock
    private DateTimeUtil dateTimeUtil;

    @InjectMocks
    private IndividualMapperImpl individualMapper;

    private Individual individual;
    private IndividualRequest individualRequest;
    private Address address;
    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.now();
        individualMapper.setDateTimeUtil(dateTimeUtil);

        Country country = new Country();
        country.setId(1);
        country.setCode("US");
        country.setName("United States");

        User user = new User();
        user.setId("1");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setMiddleName("M");
        user.setEmail("test@example.com");

        Address address = new Address();
        address.setId("1");
        address.setAddress("123 Main St");
        address.setZipCode("12345");
        address.setCity("New York");
        address.setCountry(country);
        this.address = address;

        individual = new Individual();
        individual.setId("1");
        individual.setPassportNumber(EncryptionUtils.encrypt("1234 567890"));
        individual.setPhoneNumber(EncryptionUtils.encrypt("+71234567890"));
        individual.setUser(user);
        individual.setAddress(address);

        individualRequest = new IndividualRequest(
                "1234 567890",
                "+71234567890",
                new AddressRequest("123 Main St", "12345", "New York", "US")
        );
    }

    @Test
    void toEntity_ShouldEncryptSensitiveFields() {
        when(addressMapper.toAddress(any(AddressRequest.class))).thenReturn(address);
        when(dateTimeUtil.now()).thenReturn(now);

        Individual result = individualMapper.toEntity(individualRequest);

        assertThat(result.getPassportNumber()).isEqualTo(EncryptionUtils.encrypt("1234 567890"));
        assertThat(result.getPhoneNumber()).isEqualTo(EncryptionUtils.encrypt("+71234567890"));
        assertThat(result.getPassportNumber()).isNotEqualTo("1234 567890");
        assertThat(result.getPhoneNumber()).isNotEqualTo("+71234567890");
    }

    @Test
    void toResponseDto_ShouldDecryptAndMaskSensitiveFields() {
        when(addressMapper.fromAddress(address)).thenReturn(
                new AddressResponse(false, null, null, null, null, "123 Main St", "12345", "New York")
        );

        var result = individualMapper.toResponseDto(individual);

        assertThat(result.passportNumber()).isEqualTo("12** ******");
        assertThat(result.phoneNumber()).isEqualTo("+7 (1**) ***-**-90");
        assertThat(result.firstName()).isEqualTo("Test");
        assertThat(result.email()).isEqualTo("test@example.com");
    }

    @Test
    void update_ShouldNotOverwriteEncryptedSensitiveFields() {
        when(addressMapper.toAddress(any(AddressRequest.class))).thenReturn(address);
        when(dateTimeUtil.now()).thenReturn(now);

        String encryptedPassport = individual.getPassportNumber();
        String encryptedPhone = individual.getPhoneNumber();

        individualMapper.update(individual, individualRequest);

        assertThat(individual.getPassportNumber()).isEqualTo(encryptedPassport);
        assertThat(individual.getPhoneNumber()).isEqualTo(encryptedPhone);
        assertThat(individual.getUpdatedAt()).isEqualTo(now);
    }
}
