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

    @Mock
    private EncryptionUtils encryptionUtils;

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
        individual.setPassportNumber(encryptionUtils.encrypt("1234 567890"));
        individual.setPhoneNumber(encryptionUtils.encrypt("+71234567890"));
        individual.setUser(user);
        individual.setAddress(address);
        individual.setDeleted(false);

        individualRequest = new IndividualRequest(
                "1234 567890",
                "+71234567890",
                new AddressRequest("123 Main St", "12345", "New York", "US")
        );
    }

    @Test
    void toEntity_ShouldMapRequestToEntity_WhenAllFieldsProvided() {
        when(addressMapper.toAddress(any(AddressRequest.class))).thenReturn(address);
        when(dateTimeUtil.now()).thenReturn(now);

        Individual result = individualMapper.toEntity(individualRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getPassportNumber()).isEqualTo(encryptionUtils.encrypt("1234 567890"));
        assertThat(result.getPhoneNumber()).isEqualTo(encryptionUtils.encrypt("+71234567890"));
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
        assertThat(result.getPassportNumber()).isEqualTo(encryptionUtils.encrypt("1234 567890"));
        assertThat(result.getPhoneNumber()).isEqualTo(encryptionUtils.encrypt("+71234567890"));
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
        String invalidData = "invalid_encrypted_data";
        individual.setPassportNumber(invalidData);
        individual.setPhoneNumber(invalidData);
        when(addressMapper.fromAddress(address)).thenReturn(
                new AddressResponse(false, null, null, null, null, "123 Main St", "12345", "New York")
        );

        IndividualResponse result = individualMapper.toResponseDto(individual);

        assertThat(result).isNotNull();
        assertThat(result.passportNumber()).isNotNull();
        assertThat(result.phoneNumber()).isNotNull();
    }

    // ==================== TESTS FOR LIST MAPPING ====================

    @Test
    void toResponseDto_ShouldMapListOfIndividuals_WhenListNotEmpty() {
        Individual individual2 = new Individual();
        individual2.setId("2");
        individual2.setPassportNumber(encryptionUtils.encrypt("9876 543210"));
        individual2.setPhoneNumber(encryptionUtils.encrypt("+79876543210"));
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

    @Test
    void update_ShouldUpdateAllFields_WhenAllFieldsProvided() {
        String originalEncryptedPassport = individual.getPassportNumber();
        String originalEncryptedPhone = individual.getPhoneNumber();

        AddressRequest newAddressRequest = new AddressRequest("456 Oak Ave", "67890", "Los Angeles", "US");
        Address newAddress = new Address();
        newAddress.setId("2");
        newAddress.setAddress("456 Oak Ave");
        newAddress.setZipCode("67890");
        newAddress.setCity("Los Angeles");
        newAddress.setCountry(country);

        IndividualRequest updateRequest = new IndividualRequest(
                "5678 123456",
                "+79998887766",
                newAddressRequest
        );
        when(addressMapper.toAddress(any(AddressRequest.class))).thenReturn(newAddress);
        when(dateTimeUtil.now()).thenReturn(now);

        individualMapper.update(individual, updateRequest);

        assertThat(individual.getAddress()).isEqualTo(newAddress);
        assertThat(individual.getUpdatedAt()).isEqualTo(now);
        assertThat(individual.getPassportNumber()).isEqualTo(encryptionUtils.encrypt("5678 123456"));
        assertThat(individual.getPhoneNumber()).isEqualTo(encryptionUtils.encrypt("+79998887766"));
        verify(addressMapper).toAddress(any(AddressRequest.class));
    }

    @Test
    void update_ShouldHandleNullAddress() {
        String originalEncryptedPassport = individual.getPassportNumber();
        String originalEncryptedPhone = individual.getPhoneNumber();

        IndividualRequest updateRequest = new IndividualRequest(
                "5678 123456",
                "+79998887766",
                null
        );
        when(dateTimeUtil.now()).thenReturn(now);

        individualMapper.update(individual, updateRequest);

        assertThat(individual.getAddress()).isNull();
        assertThat(individual.getUpdatedAt()).isEqualTo(now);
        assertThat(individual.getPassportNumber()).isEqualTo(encryptionUtils.encrypt("5678 123456"));
        assertThat(individual.getPhoneNumber()).isEqualTo(encryptionUtils.encrypt("+79998887766"));
    }

    @Test
    void update_ShouldHandleNullSensitiveFields() {
        Address newAddress = new Address();
        newAddress.setId("2");
        newAddress.setAddress("456 Oak Ave");
        newAddress.setZipCode("67890");
        newAddress.setCity("Los Angeles");
        newAddress.setCountry(country);

        IndividualRequest updateRequest = new IndividualRequest(
                null,
                null,
                new AddressRequest("456 Oak Ave", "67890", "Los Angeles", "US")
        );
        when(addressMapper.toAddress(any(AddressRequest.class))).thenReturn(newAddress);
        when(dateTimeUtil.now()).thenReturn(now);

        individualMapper.update(individual, updateRequest);

        assertThat(individual.getPassportNumber()).isNull();
        assertThat(individual.getPhoneNumber()).isNull();
        assertThat(individual.getAddress()).isEqualTo(newAddress);
        assertThat(individual.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void update_ShouldHandleEmptySensitiveFields() {
        Address newAddress = new Address();
        newAddress.setId("2");
        newAddress.setAddress("456 Oak Ave");
        newAddress.setZipCode("67890");
        newAddress.setCity("Los Angeles");
        newAddress.setCountry(country);

        IndividualRequest updateRequest = new IndividualRequest(
                "",
                "",
                new AddressRequest("456 Oak Ave", "67890", "Los Angeles", "US")
        );
        when(addressMapper.toAddress(any(AddressRequest.class))).thenReturn(newAddress);
        when(dateTimeUtil.now()).thenReturn(now);

        individualMapper.update(individual, updateRequest);

        assertThat(individual.getPassportNumber()).isEmpty();
        assertThat(individual.getPhoneNumber()).isEmpty();
        assertThat(individual.getAddress()).isEqualTo(newAddress);
        assertThat(individual.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void update_ShouldUpdateOnlyTimestampAndAddress_WhenAllFieldsNull() {
        Address newAddress = new Address();
        newAddress.setId("2");
        newAddress.setAddress("456 Oak Ave");
        newAddress.setZipCode("67890");
        newAddress.setCity("Los Angeles");
        newAddress.setCountry(country);

        IndividualRequest updateRequest = new IndividualRequest(
                null,
                null,
                new AddressRequest("456 Oak Ave", "67890", "Los Angeles", "US")
        );
        when(addressMapper.toAddress(any(AddressRequest.class))).thenReturn(newAddress);
        when(dateTimeUtil.now()).thenReturn(now);

        individualMapper.update(individual, updateRequest);

        assertThat(individual.getPassportNumber()).isNull();
        assertThat(individual.getPhoneNumber()).isNull();
        assertThat(individual.getAddress()).isEqualTo(newAddress);
        assertThat(individual.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void update_ShouldUpdateAllFieldsWithNullAddress_WhenAllFieldsProvided() {
        String originalEncryptedPassport = individual.getPassportNumber();
        String originalEncryptedPhone = individual.getPhoneNumber();

        IndividualRequest updateRequest = new IndividualRequest(
                "5678 123456",
                "+79998887766",
                null
        );
        when(dateTimeUtil.now()).thenReturn(now);

        individualMapper.update(individual, updateRequest);

        assertThat(individual.getAddress()).isNull();
        assertThat(individual.getUpdatedAt()).isEqualTo(now);
        assertThat(individual.getPassportNumber()).isEqualTo(encryptionUtils.encrypt("5678 123456"));
        assertThat(individual.getPhoneNumber()).isEqualTo(encryptionUtils.encrypt("+79998887766"));
    }

    @Test
    void toResponseDto_ShouldHandleUserWithAllNullFields() {
        Individual individualWithNullUser = new Individual();
        individualWithNullUser.setId("2");
        individualWithNullUser.setPassportNumber(encryptionUtils.encrypt("1234 567890"));
        individualWithNullUser.setPhoneNumber(encryptionUtils.encrypt("+71234567890"));
        individualWithNullUser.setUser(null);
        individualWithNullUser.setAddress(null);
        individualWithNullUser.setDeleted(false);

        IndividualResponse result = individualMapper.toResponseDto(individualWithNullUser);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("2");
        assertThat(result.passportNumber()).isEqualTo("12** ******");
        assertThat(result.phoneNumber()).isEqualTo("+7 (1**) ***-**-90");
        assertThat(result.firstName()).isNull();
        assertThat(result.lastName()).isNull();
        assertThat(result.middleName()).isNull();
        assertThat(result.email()).isNull();
        assertThat(result.address()).isNull();
    }

    @Test
    void toResponseDto_ShouldHandleUserWithPartialFields() {
        User partialUser = new User();
        partialUser.setId("3");
        partialUser.setFirstName("John");
        partialUser.setEmail("john@example.com");

        Individual individualWithPartialUser = new Individual();
        individualWithPartialUser.setId("3");
        individualWithPartialUser.setPassportNumber(encryptionUtils.encrypt("1234 567890"));
        individualWithPartialUser.setPhoneNumber(encryptionUtils.encrypt("+71234567890"));
        individualWithPartialUser.setUser(partialUser);
        individualWithPartialUser.setAddress(address);

        when(addressMapper.fromAddress(address)).thenReturn(
                new AddressResponse(false, null, null, null, null, "123 Main St", "12345", "New York")
        );

        IndividualResponse result = individualMapper.toResponseDto(individualWithPartialUser);

        assertThat(result).isNotNull();
        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.lastName()).isNull();
        assertThat(result.middleName()).isNull();
        assertThat(result.email()).isEqualTo("john@example.com");
    }

    @Test
    void toEntity_ShouldHandleSpecialCharactersInSensitiveFields() {
        IndividualRequest requestWithSpecialChars = new IndividualRequest(
                "1234-567890",
                "+7 (123) 456-78-90",
                new AddressRequest("123 Main St", "12345", "New York", "US")
        );
        when(addressMapper.toAddress(any(AddressRequest.class))).thenReturn(address);
        when(dateTimeUtil.now()).thenReturn(now);

        Individual result = individualMapper.toEntity(requestWithSpecialChars);

        assertThat(result).isNotNull();
        assertThat(result.getPassportNumber()).isEqualTo(encryptionUtils.encrypt("1234-567890"));
        assertThat(result.getPhoneNumber()).isEqualTo(encryptionUtils.encrypt("+7 (123) 456-78-90"));
    }

    @Test
    void update_ShouldPreserveDeletedStatusAndTimestamps() {
        individual.setDeleted(true);
        individual.setDeletedAt(now);
        individual.setDeletedBy("admin");
        Instant createdAt = Instant.now().minusSeconds(3600);
        individual.setCreatedAt(createdAt);

        Address newAddress = new Address();
        newAddress.setId("2");
        newAddress.setAddress("456 Oak Ave");
        newAddress.setZipCode("67890");
        newAddress.setCity("Los Angeles");
        newAddress.setCountry(country);

        IndividualRequest updateRequest = new IndividualRequest(
                "5678 123456",
                "+79998887766",
                new AddressRequest("456 Oak Ave", "67890", "Los Angeles", "US")
        );
        when(addressMapper.toAddress(any(AddressRequest.class))).thenReturn(newAddress);
        when(dateTimeUtil.now()).thenReturn(now);

        individualMapper.update(individual, updateRequest);

        assertThat(individual.getDeleted()).isTrue();
        assertThat(individual.getDeletedAt()).isEqualTo(now);
        assertThat(individual.getDeletedBy()).isEqualTo("admin");
        assertThat(individual.getCreatedAt()).isEqualTo(createdAt);
        assertThat(individual.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toResponseDto_ShouldHandlePassportWithDifferentFormats() {
        String[] passportFormats = {
                "1234 567890",
                "1234-567890",
                "1234567890",
                "12 3456789"
        };

        for (String passport : passportFormats) {
            individual.setPassportNumber(encryptionUtils.encrypt(passport));
            when(addressMapper.fromAddress(address)).thenReturn(
                    new AddressResponse(false, null, null, null, null, "123 Main St", "12345", "New York")
            );

            IndividualResponse result = individualMapper.toResponseDto(individual);
            assertThat(result.passportNumber()).isNotNull();
            assertThat(result.passportNumber()).contains("*");
        }
    }

    @Test
    void toResponseDto_ShouldPreserveNonSensitiveFields() {
        User userWithAllFields = new User();
        userWithAllFields.setId("1");
        userWithAllFields.setFirstName("John");
        userWithAllFields.setLastName("Doe");
        userWithAllFields.setMiddleName("Michael");
        userWithAllFields.setEmail("john.doe@example.com");

        individual.setUser(userWithAllFields);
        when(addressMapper.fromAddress(address)).thenReturn(
                new AddressResponse(false, null, null, null, null, "123 Main St", "12345", "New York")
        );

        IndividualResponse result = individualMapper.toResponseDto(individual);

        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.lastName()).isEqualTo("Doe");
        assertThat(result.middleName()).isEqualTo("Michael");
        assertThat(result.email()).isEqualTo("john.doe@example.com");
    }
}