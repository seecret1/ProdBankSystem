import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.userservice.dto.request.AddressRequest;
import com.github.seecret1.userservice.dto.request.IndividualRequest;
import com.github.seecret1.userservice.dto.response.AddressResponse;
import com.github.seecret1.userservice.dto.response.IndividualResponse;
import com.github.seecret1.userservice.entity.Address;
import com.github.seecret1.userservice.entity.Country;
import com.github.seecret1.userservice.entity.Individual;
import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.entity.enums.RoleType;
import com.github.seecret1.userservice.entity.enums.UserStatus;
import com.github.seecret1.userservice.exception.IndividualDataExistsException;
import com.github.seecret1.userservice.exception.PersonException;
import com.github.seecret1.userservice.mapper.IndividualMapper;
import com.github.seecret1.userservice.repository.IndividualRepository;
import com.github.seecret1.userservice.repository.UserRepository;
import com.github.seecret1.userservice.service.InternalUserService;
import com.github.seecret1.userservice.service.impl.IndividualServiceImpl;
import com.github.seecret1.userservice.utils.AuthUtil;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndividualServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private IndividualRepository individualRepository;

    @Mock
    private IndividualMapper individualMapper;

    @Mock
    private InternalUserService internalUserService;

    @InjectMocks
    private IndividualServiceImpl individualService;

    private User user;
    private Individual individual;
    private IndividualResponse individualResponse;
    private IndividualRequest individualRequest;
    private Address address;
    private Country country;

    @BeforeEach
    void setUp() {
        country = new Country();
        country.setId(1);
        country.setCode("US");
        country.setName("United States");

        address = new Address();
        address.setId("1");
        address.setAddress("123 Main St");
        address.setZipCode("12345");
        address.setCity("New York");
        address.setCountry(country);

        user = new User();
        user.setId("1");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setStatus(UserStatus.PENDING_PROFILE);
        user.setRole(RoleType.ROLE_USER);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setMiddleName("M");
        user.setBirthDate(LocalDate.of(1990, 1, 1));

        individual = new Individual();
        individual.setId("1");
        individual.setPassportNumber("AB1234567");
        individual.setPhoneNumber("+1234567890");
        individual.setUser(user);
        individual.setAddress(address);

        AddressResponse addressResponse = new AddressResponse(
                false, null, null, null, null,
                "123 Main St", "12345", "New York"
        );

        individualResponse = new IndividualResponse(
                "1", "Test", "User", "M",
                "test@example.com", "AB1234567",
                "+1234567890", addressResponse
        );

        AddressRequest addressRequest = new AddressRequest(
                "123 Main St", "12345", "New York", "US"
        );

        individualRequest = new IndividualRequest(
                "AB1234567", "+1234567890", addressRequest
        );
    }

    @Test
    void findAll_ShouldReturnPageResponse() {
        // Arrange
        PageModel pageModel = new PageModel(0, 10);
        Page<Individual> page = new PageImpl<>(List.of(individual));
        when(individualRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(individualMapper.toResponseDto(anyList())).thenReturn(List.of(individualResponse));

        // Act
        PageResponse<IndividualResponse> result = individualService.findAll(pageModel);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(individualRepository).findAll(any(Pageable.class));
    }

    @Test
    void findByCriterial_ShouldReturnIndividualResponse_WhenExists() {
        // Arrange
        when(individualRepository.findByCriterial(anyString())).thenReturn(Optional.of(individual));
        when(individualMapper.toResponseDto(individual)).thenReturn(individualResponse);

        // Act
        IndividualResponse result = individualService.findByCriterial("AB1234567");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.passportNumber()).isEqualTo("AB1234567");
        verify(individualRepository).findByCriterial("AB1234567");
    }

    @Test
    void findByCriterial_ShouldThrowEntityNotFoundException_WhenNotExists() {
        // Arrange
        when(individualRepository.findByCriterial(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> individualService.findByCriterial("nonexistent"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Individual not found by criterial");
    }

    @Test
    void recordPersonalData_ShouldReturnIndividualResponse_WhenValidRequest() {
        // Arrange
        Individual newIndividual = new Individual();
        newIndividual.setId("1");
        newIndividual.setPassportNumber("AB1234567");
        newIndividual.setPhoneNumber("+1234567890");
        newIndividual.setAddress(address);

        when(internalUserService.findUserEntityById(anyString())).thenReturn(user);
        when(individualRepository.existsByUserId(anyString())).thenReturn(false);
        when(individualMapper.toEntity(any(IndividualRequest.class))).thenReturn(newIndividual);
        when(individualRepository.save(any(Individual.class))).thenAnswer(inv -> inv.getArgument(0));

        // Mock the response with a proper response object
        IndividualResponse mockResponse = new IndividualResponse(
                "1", "Test", "User", "M",
                "test@example.com", "AB1234567",
                "+1234567890", null
        );
        when(individualMapper.toResponseDto(any(Individual.class))).thenReturn(mockResponse);

        try (MockedStatic<AuthUtil> authUtil = mockStatic(AuthUtil.class)) {
            // Act
            IndividualResponse result = individualService.recordPersonalData("1", individualRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.passportNumber()).isEqualTo("AB1234567");
            verify(individualRepository).save(any(Individual.class));
            authUtil.verify(() -> AuthUtil.userRecordPersonalData(user));
        }
    }

    @Test
    void recordPersonalData_ShouldThrowPersonException_WhenProfileExists() {
        // Arrange
        when(internalUserService.findUserEntityById(anyString())).thenReturn(user);
        when(individualRepository.existsByUserId(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> individualService.recordPersonalData("1", individualRequest))
                .isInstanceOf(PersonException.class)
                .hasMessageContaining("Individual profile already exists");
    }


    @Test
    void update_ShouldReturnUpdatedIndividualResponse_WhenValid() {
        // Arrange
        User activeUser = new User();
        activeUser.setId("1");
        activeUser.setStatus(UserStatus.ACTIVE);
        activeUser.setEmail("test@example.com");

        Individual testIndividual = new Individual();
        testIndividual.setId("1");
        testIndividual.setPassportNumber("AB1234567");
        testIndividual.setPhoneNumber("+1234567890");
        testIndividual.setUser(activeUser);
        testIndividual.setAddress(address);

        // Create request with different data
        IndividualRequest updateRequest = new IndividualRequest(
                "XY9876543", "+9876543210",
                new AddressRequest("456 Main St", "54321", "Boston", "US")
        );

        when(individualRepository.findByCriterial(anyString())).thenReturn(Optional.of(testIndividual));
        when(individualRepository.existsIndividualByPassportNumber("XY9876543")).thenReturn(false);
        when(individualRepository.existsIndividualByPhoneNumber("+9876543210")).thenReturn(false);
        when(individualRepository.save(any(Individual.class))).thenReturn(testIndividual);
        when(individualMapper.toResponseDto(any(Individual.class))).thenReturn(individualResponse);
        doNothing().when(individualMapper).update(any(Individual.class), any(IndividualRequest.class));

        // Act
        IndividualResponse result = individualService.update("AB1234567", updateRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(individualRepository).save(testIndividual);
        verify(individualMapper).update(testIndividual, updateRequest);
    }

    @Test
    void update_ShouldThrowIndividualDataExistsException_WhenPassportAlreadyExists() {
        // Arrange
        User activeUser = new User();
        activeUser.setId("1");
        activeUser.setStatus(UserStatus.ACTIVE);

        Individual testIndividual = new Individual();
        testIndividual.setId("1");
        testIndividual.setPassportNumber("AB1234567");
        testIndividual.setPhoneNumber("+1234567890");
        testIndividual.setUser(activeUser);

        when(individualRepository.findByCriterial(anyString())).thenReturn(Optional.of(testIndividual));
        when(individualRepository.existsIndividualByPassportNumber("NEW_PASSPORT")).thenReturn(true);

        IndividualRequest requestWithNewPassport = new IndividualRequest(
                "NEW_PASSPORT", "+1234567890",
                new AddressRequest("123 Main St", "12345", "New York", "US")
        );

        // Act & Assert
        assertThatThrownBy(() -> individualService.update("AB1234567", requestWithNewPassport))
                .isInstanceOf(IndividualDataExistsException.class)
                .hasMessageContaining("Passport number already exists");
    }

    @Test
    void update_ShouldThrowIndividualDataExistsException_WhenPhoneAlreadyExists() {
        // Arrange
        User activeUser = new User();
        activeUser.setId("1");
        activeUser.setStatus(UserStatus.ACTIVE);

        Individual testIndividual = new Individual();
        testIndividual.setId("1");
        testIndividual.setPassportNumber("AB1234567");
        testIndividual.setPhoneNumber("+1234567890");
        testIndividual.setUser(activeUser);

        when(individualRepository.findByCriterial(anyString())).thenReturn(Optional.of(testIndividual));
        when(individualRepository.existsIndividualByPhoneNumber("+9999999999")).thenReturn(true);

        // Same passport, different phone
        IndividualRequest requestWithNewPhone = new IndividualRequest(
                "AB1234567", "+9999999999",
                new AddressRequest("123 Main St", "12345", "New York", "US")
        );

        // Act & Assert
        assertThatThrownBy(() -> individualService.update("AB1234567", requestWithNewPhone))
                .isInstanceOf(IndividualDataExistsException.class)
                .hasMessageContaining("Phone number already exists");
    }

    @Test
    void update_ShouldThrowPersonException_WhenIndividualNotFound() {
        // Arrange
        when(individualRepository.findByCriterial(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> individualService.update("nonexistent", individualRequest))
                .isInstanceOf(PersonException.class)
                .hasMessageContaining("Individual not found by criterial");
    }

    @Test
    void updateYour_ShouldReturnUpdatedIndividualResponse_WhenValid() {
        // Arrange
        User activeUser = new User();
        activeUser.setId("1");
        activeUser.setStatus(UserStatus.ACTIVE);
        activeUser.setEmail("test@example.com");

        Individual testIndividual = new Individual();
        testIndividual.setId("1");
        testIndividual.setPassportNumber("AB1234567");
        testIndividual.setPhoneNumber("+1234567890");
        testIndividual.setUser(activeUser);
        testIndividual.setAddress(address);
        activeUser.setIndividual(testIndividual);

        // Create request with different data
        IndividualRequest updateRequest = new IndividualRequest(
                "XY9876543", "+9876543210",
                new AddressRequest("456 Main St", "54321", "Boston", "US")
        );

        when(userRepository.findById(anyString())).thenReturn(Optional.of(activeUser));
        when(individualRepository.existsIndividualByPassportNumber("XY9876543")).thenReturn(false);
        when(individualRepository.existsIndividualByPhoneNumber("+9876543210")).thenReturn(false);
        when(individualRepository.save(any(Individual.class))).thenReturn(testIndividual);
        when(individualMapper.toResponseDto(any(Individual.class))).thenReturn(individualResponse);
        doNothing().when(individualMapper).update(any(Individual.class), any(IndividualRequest.class));

        // Act
        IndividualResponse result = individualService.updateYour("1", updateRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(individualRepository).save(testIndividual);
        verify(individualMapper).update(testIndividual, updateRequest);
    }

    @Test
    void updateYour_ShouldThrowIndividualDataExistsException_WhenPassportAlreadyExists() {
        // Arrange
        User activeUser = new User();
        activeUser.setId("1");
        activeUser.setStatus(UserStatus.ACTIVE);

        Individual testIndividual = new Individual();
        testIndividual.setId("1");
        testIndividual.setPassportNumber("AB1234567");
        testIndividual.setPhoneNumber("+1234567890");
        testIndividual.setUser(activeUser);
        activeUser.setIndividual(testIndividual);

        when(userRepository.findById(anyString())).thenReturn(Optional.of(activeUser));
        when(individualRepository.existsIndividualByPassportNumber("NEW_PASSPORT")).thenReturn(true);

        IndividualRequest requestWithNewPassport = new IndividualRequest(
                "NEW_PASSPORT", "+1234567890",
                new AddressRequest("123 Main St", "12345", "New York", "US")
        );

        // Act & Assert
        assertThatThrownBy(() -> individualService.updateYour("1", requestWithNewPassport))
                .isInstanceOf(IndividualDataExistsException.class)
                .hasMessageContaining("Passport number already exists");
    }

    @Test
    void updateYour_ShouldThrowIndividualDataExistsException_WhenPhoneAlreadyExists() {
        // Arrange
        User activeUser = new User();
        activeUser.setId("1");
        activeUser.setStatus(UserStatus.ACTIVE);

        Individual testIndividual = new Individual();
        testIndividual.setId("1");
        testIndividual.setPassportNumber("AB1234567");
        testIndividual.setPhoneNumber("+1234567890");
        testIndividual.setUser(activeUser);
        activeUser.setIndividual(testIndividual);

        when(userRepository.findById(anyString())).thenReturn(Optional.of(activeUser));
        when(individualRepository.existsIndividualByPhoneNumber("+9999999999")).thenReturn(true);

        // Same passport, different phone
        IndividualRequest requestWithNewPhone = new IndividualRequest(
                "AB1234567", "+9999999999",
                new AddressRequest("123 Main St", "12345", "New York", "US")
        );

        // Act & Assert
        assertThatThrownBy(() -> individualService.updateYour("1", requestWithNewPhone))
                .isInstanceOf(IndividualDataExistsException.class)
                .hasMessageContaining("Phone number already exists");
    }

    @Test
    void updateYour_ShouldThrowEntityNotFoundException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> individualService.updateYour("999", individualRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found by id");
    }

    @Test
    void softDelete_ShouldCallRepositorySoftDelete() {
        // Arrange
        doNothing().when(individualRepository).softDelete(anyString());

        // Act
        individualService.softDelete("AB1234567");

        // Assert
        verify(individualRepository).softDelete("AB1234567");
    }

    @Test
    void hardDelete_ShouldDeleteIndividual_WhenExists() {
        // Arrange
        when(individualRepository.findByCriterial(anyString())).thenReturn(Optional.of(individual));
        doNothing().when(individualRepository).delete(any(Individual.class));

        // Act
        individualService.hardDelete("AB1234567");

        // Assert
        verify(individualRepository).delete(individual);
    }

    @Test
    void hardDelete_ShouldThrowPersonException_WhenIndividualNotFound() {
        // Arrange
        when(individualRepository.findByCriterial(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> individualService.hardDelete("nonexistent"))
                .isInstanceOf(PersonException.class);
    }
}