package com.github.seecret1.userservice.mapper;

import com.github.seecret1.userservice.dto.request.AddressRequest;
import com.github.seecret1.userservice.dto.request.IndividualRequest;
import com.github.seecret1.userservice.dto.response.AddressResponse;
import com.github.seecret1.userservice.dto.response.IndividualResponse;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
    private User user;
    private Country country;
    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.now();
        individualMapper.setDateTimeUtil(dateTimeUtil);

        country = new Country();
        country.setId(1);
        country.setCode("US");
        country.setName("United States");

        user = new User();
        user.setId("1");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setMiddleName("M");
        user.setEmail("test@example.com");

        address = new Address();
        address.setId("1");
        address.setAddress("123 Main St");
        address.setZipCode("12345");
        address.setCity("New York");
        address.setCountry(country);

        individual = new Individual();
        individual.setId("1");
        individual.setPassportNumber(EncryptionUtils.encrypt("1234 567890"));
        individual.setPhoneNumber(EncryptionUtils.encrypt("+71234567890"));
        individual.setUser(user);
        individual.setAddress(address);
        individual.setDeleted(false);

        individualRequest = new IndividualRequest(
                "1234 567890",
                "+71234567890",
                new AddressRequest("123 Main St", "12345", "New York", "US")
        );
    }

    // ==================== TESTS FOR toEntity ====================

    @Test
    void toEntity_ShouldMapRequestToEntity_WhenAllFieldsProvided() {
        when(addressMapper.toAddress(any(AddressRequest.class))).thenReturn(address);
        when(dateTimeUtil.now()).thenReturn(now);

        Individual result = individualMapper.toEntity(individualRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getPassportNumber()).isEqualTo(EncryptionUtils.encrypt("1234 567890"));
        assertThat(result.getPhoneNumber()).isEqualTo(EncryptionUtils.encrypt("+71234567890"));
        assertThat(result.getAddress()).isEqualTo(address);
        assertThat(result.getUser()).isNull();
        assertThat(result.getDeleted()).isFalse();
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
        assertThat(result.getDeletedAt()).isNull();
        assertThat(result.getDeletedBy()).isNull();

        verify(addressMapper).toAddress(any(AddressRequest.class));
    }

    @Test
    void toEntity_ShouldHandleNullAddress() {
        IndividualRequest requestWithoutAddress = new IndividualRequest(
                "1234 567890",
                "+71234567890",
                null
        );
        when(dateTimeUtil.now()).thenReturn(now);

        Individual result = individualMapper.toEntity(requestWithoutAddress);

        assertThat(result).isNotNull();
        assertThat(result.getPassportNumber()).isEqualTo(EncryptionUtils.encrypt("1234 567890"));
        assertThat(result.getPhoneNumber()).isEqualTo(EncryptionUtils.encrypt("+71234567890"));
        assertThat(result.getAddress()).isNull();
        assertThat(result.getDeleted()).isFalse();
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toEntity_ShouldHandleNullSensitiveFields() {
        IndividualRequest requestWithNullFields = new IndividualRequest(
                null,
                null,
                new AddressRequest("123 Main St", "12345", "New York", "US")
        );
        when(addressMapper.toAddress(any(AddressRequest.class))).thenReturn(address);
        when(dateTimeUtil.now()).thenReturn(now);

        Individual result = individualMapper.toEntity(requestWithNullFields);

        assertThat(result).isNotNull();
        assertThat(result.getPassportNumber()).isNull();
        assertThat(result.getPhoneNumber()).isNull();
        assertThat(result.getAddress()).isEqualTo(address);
        assertThat(result.getDeleted()).isFalse();
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toEntity_ShouldHandleEmptySensitiveFields() {
        IndividualRequest requestWithEmptyFields = new IndividualRequest(
                "",
                "",
                new AddressRequest("123 Main St", "12345", "New York", "US")
        );
        when(addressMapper.toAddress(any(AddressRequest.class))).thenReturn(address);
        when(dateTimeUtil.now()).thenReturn(now);

        Individual result = individualMapper.toEntity(requestWithEmptyFields);

        assertThat(result).isNotNull();
        assertThat(result.getPassportNumber()).isEmpty();
        assertThat(result.getPhoneNumber()).isEmpty();
        assertThat(result.getAddress()).isEqualTo(address);
        assertThat(result.getDeleted()).isFalse();
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    // ==================== TESTS FOR toResponseDto ====================

    @Test
    void toResponseDto_ShouldMapEntityToResponse_WhenAllFieldsPresent() {
        AddressResponse addressResponse = new AddressResponse(
                false,
                null,
                null,
                null,
                null,
                "123 Main St",
                "12345",
                "New York"
        );
        when(addressMapper.fromAddress(address)).thenReturn(addressResponse);

        IndividualResponse result = individualMapper.toResponseDto(individual);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("1");
        assertThat(result.passportNumber()).isEqualTo("12** ******");
        assertThat(result.phoneNumber()).isEqualTo("+7 (1**) ***-**-90");
        assertThat(result.firstName()).isEqualTo("Test");
        assertThat(result.lastName()).isEqualTo("User");
        assertThat(result.middleName()).isEqualTo("M");
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.address()).isEqualTo(addressResponse);
    }

    @Test
    void toResponseDto_ShouldHandleNullUser() {
        individual.setUser(null);
        when(addressMapper.fromAddress(address)).thenReturn(
                new AddressResponse(false, null, null, null, null, "123 Main St", "12345", "New York")
        );

        IndividualResponse result = individualMapper.toResponseDto(individual);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("1");
        assertThat(result.passportNumber()).isEqualTo("12** ******");
        assertThat(result.phoneNumber()).isEqualTo("+7 (1**) ***-**-90");
        assertThat(result.firstName()).isNull();
        assertThat(result.lastName()).isNull();
        assertThat(result.middleName()).isNull();
        assertThat(result.email()).isNull();
    }

    @Test
    void toResponseDto_ShouldHandleNullAddress() {
        individual.setAddress(null);
        when(addressMapper.fromAddress(null)).thenReturn(null);

        IndividualResponse result = individualMapper.toResponseDto(individual);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("1");
        assertThat(result.passportNumber()).isEqualTo("12** ******");
        assertThat(result.phoneNumber()).isEqualTo("+7 (1**) ***-**-90");
        assertThat(result.firstName()).isEqualTo("Test");
        assertThat(result.lastName()).isEqualTo("User");
        assertThat(result.middleName()).isEqualTo("M");
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.address()).isNull();
    }

    @Test
    void toResponseDto_ShouldHandleNullSensitiveFields() {
        individual.setPassportNumber(null);
        individual.setPhoneNumber(null);
        when(addressMapper.fromAddress(address)).thenReturn(
                new AddressResponse(false, null, null, null, null, "123 Main St", "12345", "New York")
        );

        IndividualResponse result = individualMapper.toResponseDto(individual);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("1");
        assertThat(result.passportNumber()).isNull();
        assertThat(result.phoneNumber()).isNull();
        assertThat(result.firstName()).isEqualTo("Test");
        assertThat(result.lastName()).isEqualTo("User");
        assertThat(result.middleName()).isEqualTo("M");
        assertThat(result.email()).isEqualTo("test@example.com");
    }

    @Test
    void toResponseDto_ShouldHandleEmptySensitiveFields() {
        individual.setPassportNumber("");
        individual.setPhoneNumber("");
        when(addressMapper.fromAddress(address)).thenReturn(
                new AddressResponse(false, null, null, null, null, "123 Main St", "12345", "New York")
        );

        IndividualResponse result = individualMapper.toResponseDto(individual);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("1");
        assertThat(result.passportNumber()).isEmpty();
        assertThat(result.phoneNumber()).isEmpty();
        assertThat(result.firstName()).isEqualTo("Test");
        assertThat(result.lastName()).isEqualTo("User");
        assertThat(result.middleName()).isEqualTo("M");
        assertThat(result.email()).isEqualTo("test@example.com");
    }

    @Test
    void toResponseDto_ShouldHandleInvalidEncryptedData() {
        individual.setPassportNumber("invalid_encrypted_data");
        individual.setPhoneNumber("invalid_encrypted_data");
        when(addressMapper.fromAddress(address)).thenReturn(
                new AddressResponse(false, null, null, null, null, "123 Main St", "12345", "New York")
        );

        IndividualResponse result = individualMapper.toResponseDto(individual);

        assertThat(result).isNotNull();
        // Should return the original value if decryption fails
        assertThat(result.passportNumber()).isEqualTo("invalid_encrypted_data");
        assertThat(result.phoneNumber()).isEqualTo("invalid_encrypted_data");
    }

    // ==================== TESTS FOR LIST MAPPING ====================

    @Test
    void toResponseDto_ShouldMapListOfIndividuals_WhenListNotEmpty() {
        Individual individual2 = new Individual();
        individual2.setId("2");
        individual2.setPassportNumber(EncryptionUtils.encrypt("9876 543210"));
        individual2.setPhoneNumber(EncryptionUtils.encrypt("+79876543210"));
        User user2 = new User();
        user2.setId("2");
        user2.setFirstName("Jane");
        user2.setLastName("Doe");
        user2.setEmail("jane@example.com");
        individual2.setUser(user2);
        individual2.setAddress(address);

        List<Individual> individuals = Arrays.asList(individual, individual2);

        when(addressMapper.fromAddress(address)).thenReturn(
                new AddressResponse(false, null, null, null, null, "123 Main St", "12345", "New York")
        );

        List<IndividualResponse> results = individualMapper.toResponseDto(individuals);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(IndividualResponse::id)
                .containsExactly("1", "2");
        assertThat(results).extracting(IndividualResponse::firstName)
                .containsExactly("Test", "Jane");
        assertThat(results).extracting(IndividualResponse::lastName)
                .containsExactly("User", "Doe");
        assertThat(results).extracting(IndividualResponse::email)
                .containsExactly("test@example.com", "jane@example.com");
    }

    @Test
    void toResponseDto_ShouldReturnEmptyList_WhenListIsEmpty() {
        List<Individual> individuals = Collections.emptyList();

        List<IndividualResponse> results = individualMapper.toResponseDto(individuals);

        assertThat(results).isEmpty();
    }

    @Test
    void toResponseDto_ShouldReturnEmptyList_WhenListIsNull() {
        List<IndividualResponse> results = individualMapper.toResponseDto((List<Individual>) null);

        assertThat(results).isEmpty();
    }

    // ==================== TESTS FOR update ====================

    @Test
    void update_ShouldUpdateAllFields_WhenAllFieldsProvided() {
        IndividualRequest updateRequest = new IndividualRequest(
                "5678 123456",
                "+79998887766",
                new AddressRequest("456 Oak Ave", "67890", "Los Angeles", "US")
        );
        when(addressMapper.toAddress(any(AddressRequest.class))).thenReturn(address);
        when(dateTimeUtil.now()).thenReturn(now);

        String originalPassport = individual.getPassportNumber();
        String originalPhone = individual.getPhoneNumber();

        individualMapper.update(individual, updateRequest);

        // Sensitive fields should NOT be overwritten (they are encrypted and the mapper ignores them in update)
        assertThat(individual.getPassportNumber()).isEqualTo(originalPassport);
        assertThat(individual.getPhoneNumber()).isEqualTo(originalPhone);
        assertThat(individual.getAddress()).isEqualTo(address);
        assertThat(individual.getUpdatedAt()).isEqualTo(now);

        verify(addressMapper).toAddress(any(AddressRequest.class));
    }

    @Test
    void update_ShouldHandleNullAddress() {
        IndividualRequest updateRequest = new IndividualRequest(
                "5678 123456",
                "+79998887766",
                null
        );
        when(dateTimeUtil.now()).thenReturn(now);

        individualMapper.update(individual, updateRequest);

        assertThat(individual.getAddress()).isNull();
        assertThat(individual.getUpdatedAt()).isEqualTo(now);
        assertThat(individual.getPassportNumber()).isEqualTo(EncryptionUtils.encrypt("1234 567890"));
        assertThat(individual.getPhoneNumber()).isEqualTo(EncryptionUtils.encrypt("+71234567890"));
    }

    @Test
    void update_ShouldHandleNullSensitiveFields() {
        IndividualRequest updateRequest = new IndividualRequest(
                null,
                null,
                new AddressRequest("456 Oak Ave", "67890", "Los Angeles", "US")
        );
        when(addressMapper.toAddress(any(AddressRequest.class))).thenReturn(address);
        when(dateTimeUtil.now()).thenReturn(now);

        individualMapper.update(individual, updateRequest);

        // Sensitive fields should remain unchanged
        assertThat(individual.getPassportNumber()).isEqualTo(EncryptionUtils.encrypt("1234 567890"));
        assertThat(individual.getPhoneNumber()).isEqualTo(EncryptionUtils.encrypt("+71234567890"));
        assertThat(individual.getAddress()).isEqualTo(address);
        assertThat(individual.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void update_ShouldHandleEmptySensitiveFields() {
        IndividualRequest updateRequest = new IndividualRequest(
                "",
                "",
                new AddressRequest("456 Oak Ave", "67890", "Los Angeles", "US")
        );
        when(addressMapper.toAddress(any(AddressRequest.class))).thenReturn(address);
        when(dateTimeUtil.now()).thenReturn(now);

        individualMapper.update(individual, updateRequest);

        // Empty strings should NOT overwrite the encrypted values
        assertThat(individual.getPassportNumber()).isEqualTo(EncryptionUtils.encrypt("1234 567890"));
        assertThat(individual.getPhoneNumber()).isEqualTo(EncryptionUtils.encrypt("+71234567890"));
        assertThat(individual.getAddress()).isEqualTo(address);
        assertThat(individual.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void update_ShouldUpdateOnlyTimestamp_WhenAllFieldsNull() {
        IndividualRequest updateRequest = new IndividualRequest(
                null,
                null,
                null
        );
        when(dateTimeUtil.now()).thenReturn(now);

        String originalPassport = individual.getPassportNumber();
        String originalPhone = individual.getPhoneNumber();

        individualMapper.update(individual, updateRequest);

        assertThat(individual.getPassportNumber()).isEqualTo(originalPassport);
        assertThat(individual.getPhoneNumber()).isEqualTo(originalPhone);
        assertThat(individual.getAddress()).isNull();
        assertThat(individual.getUpdatedAt()).isEqualTo(now);
    }
}