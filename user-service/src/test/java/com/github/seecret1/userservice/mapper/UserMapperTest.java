package com.github.seecret1.userservice.mapper;

import com.github.seecret1.userservice.dto.request.CreateUserRequest;
import com.github.seecret1.userservice.dto.request.SignUpRequest;
import com.github.seecret1.userservice.dto.response.UserResponse;
import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.entity.enums.RoleType;
import com.github.seecret1.userservice.entity.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {UserMapperImpl.class})
@ContextConfiguration(classes = {UserMapperImpl.class})
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    private User user;
    private CreateUserRequest createUserRequest;
    private SignUpRequest signUpRequest;
    private Instant now;
    private LocalDate birthDate;

    @BeforeEach
    void setUp() {
        now = Instant.now();
        birthDate = LocalDate.of(1990, 1, 1);

        user = new User();
        user.setId("1");
        user.setUsername("testuser");
        user.setStatus(UserStatus.ACTIVE);
        user.setEmail("test@example.com");
        user.setPassword("encoded_password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setMiddleName("M");
        user.setBirthDate(birthDate);
        user.setRole(RoleType.ROLE_USER);
        user.setDeleted(false);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setDeletedAt(null);
        user.setDeletedBy(null);

        createUserRequest = new CreateUserRequest(
                "testuser",
                UserStatus.ACTIVE,
                "test@example.com",
                "password123",
                "Test",
                "User",
                "M",
                birthDate,
                RoleType.ROLE_USER
        );

        signUpRequest = new SignUpRequest(
                "testuser",
                "test@example.com",
                "password123",
                "password123",
                "Test",
                "User",
                "M",
                birthDate
        );
    }

    @Test
    void toResponse_ShouldMapUserToUserResponse_WhenAllFieldsPresent() {
        UserResponse result = userMapper.toResponse(user);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("1");
        assertThat(result.username()).isEqualTo("testuser");
        assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.firstName()).isEqualTo("Test");
        assertThat(result.lastName()).isEqualTo("User");
        assertThat(result.middleName()).isEqualTo("M");
        assertThat(result.birthDate()).isEqualTo(birthDate);
        assertThat(result.role()).isEqualTo(RoleType.ROLE_USER);
        assertThat(result.deleted()).isFalse();
        assertThat(result.createdAt()).isEqualTo(now);
        assertThat(result.updatedAt()).isEqualTo(now);
        assertThat(result.deletedAt()).isNull();
        assertThat(result.deletedBy()).isNull();
    }

    @Test
    void toResponse_ShouldHandleNullFields() {
        User userWithNulls = new User();
        userWithNulls.setId("2");
        userWithNulls.setUsername("nulluser");
        userWithNulls.setStatus(UserStatus.PENDING_PROFILE);
        userWithNulls.setEmail("null@example.com");
        userWithNulls.setPassword("encoded");
        userWithNulls.setFirstName(null);
        userWithNulls.setLastName(null);
        userWithNulls.setMiddleName(null);
        userWithNulls.setBirthDate(null);
        userWithNulls.setRole(RoleType.ROLE_USER);
        userWithNulls.setDeleted(false);

        UserResponse result = userMapper.toResponse(userWithNulls);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("2");
        assertThat(result.username()).isEqualTo("nulluser");
        assertThat(result.status()).isEqualTo(UserStatus.PENDING_PROFILE);
        assertThat(result.email()).isEqualTo("null@example.com");
        assertThat(result.firstName()).isNull();
        assertThat(result.lastName()).isNull();
        assertThat(result.middleName()).isNull();
        assertThat(result.birthDate()).isNull();
        assertThat(result.role()).isEqualTo(RoleType.ROLE_USER);
        assertThat(result.deleted()).isFalse();
    }

    @Test
    void toResponse_ShouldHandleUserWithAllNullFields() {
        User emptyUser = new User();
        emptyUser.setId(null);
        emptyUser.setUsername(null);
        emptyUser.setStatus(null);
        emptyUser.setEmail(null);
        emptyUser.setPassword(null);
        emptyUser.setFirstName(null);
        emptyUser.setLastName(null);
        emptyUser.setMiddleName(null);
        emptyUser.setBirthDate(null);
        emptyUser.setRole(null);
        emptyUser.setDeleted(false);

        UserResponse result = userMapper.toResponse(emptyUser);

        assertThat(result).isNotNull();
        assertThat(result.id()).isNull();
        assertThat(result.username()).isNull();
        assertThat(result.status()).isNull();
        assertThat(result.email()).isNull();
        assertThat(result.firstName()).isNull();
        assertThat(result.lastName()).isNull();
        assertThat(result.middleName()).isNull();
        assertThat(result.birthDate()).isNull();
        assertThat(result.role()).isNull();
        assertThat(result.deleted()).isFalse();
        assertThat(result.createdAt()).isNull();
        assertThat(result.updatedAt()).isNull();
        assertThat(result.deletedAt()).isNull();
        assertThat(result.deletedBy()).isNull();
    }

    @Test
    void toListResponse_ShouldMapListOfUsers_WhenListNotEmpty() {
        User user2 = new User();
        user2.setId("2");
        user2.setUsername("johndoe");
        user2.setStatus(UserStatus.ACTIVE);
        user2.setEmail("john@example.com");
        user2.setPassword("encoded2");
        user2.setFirstName("John");
        user2.setLastName("Doe");
        user2.setMiddleName(null);
        user2.setBirthDate(LocalDate.of(1985, 5, 15));
        user2.setRole(RoleType.ROLE_USER);
        user2.setDeleted(false);

        List<User> users = Arrays.asList(user, user2);

        List<UserResponse> results = userMapper.toListResponse(users);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results).extracting(UserResponse::id)
                .containsExactly("1", "2");
        assertThat(results).extracting(UserResponse::username)
                .containsExactly("testuser", "johndoe");
        assertThat(results).extracting(UserResponse::email)
                .containsExactly("test@example.com", "john@example.com");
        assertThat(results).extracting(UserResponse::firstName)
                .containsExactly("Test", "John");
        assertThat(results).extracting(UserResponse::lastName)
                .containsExactly("User", "Doe");
        assertThat(results).extracting(UserResponse::status)
                .containsExactly(UserStatus.ACTIVE, UserStatus.ACTIVE);
        assertThat(results).extracting(UserResponse::role)
                .containsExactly(RoleType.ROLE_USER, RoleType.ROLE_USER);
    }

    @Test
    void toListResponse_ShouldReturnEmptyList_WhenListIsEmpty() {
        List<User> users = Collections.emptyList();

        List<UserResponse> results = userMapper.toListResponse(users);

        assertThat(results).isNotNull();
        assertThat(results).isEmpty();
    }

    @Test
    void toListResponse_ShouldReturnNull_WhenListIsNull() {
        List<UserResponse> results = userMapper.toListResponse(null);

        assertThat(results).isNull();
    }

    @Test
    void toListResponse_ShouldHandleListWithNullElements() {
        User userWithNulls = new User();
        userWithNulls.setId("3");
        userWithNulls.setUsername("nulluser");
        userWithNulls.setStatus(UserStatus.PENDING_PROFILE);
        userWithNulls.setEmail("null@example.com");
        userWithNulls.setPassword("encoded");
        userWithNulls.setFirstName(null);
        userWithNulls.setLastName(null);
        userWithNulls.setMiddleName(null);
        userWithNulls.setBirthDate(null);
        userWithNulls.setRole(RoleType.ROLE_USER);
        userWithNulls.setDeleted(false);

        List<User> users = Arrays.asList(user, null, userWithNulls);

        List<UserResponse> results = userMapper.toListResponse(users);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(3);
        assertThat(results.get(0).id()).isEqualTo("1");
        assertThat(results.get(2).id()).isEqualTo("3");
    }

    @Test
    void toCreateUserRequest_ShouldMapSignUpRequestToCreateUserRequest_WhenAllFieldsPresent() {
        CreateUserRequest result = userMapper.toCreateUserRequest(signUpRequest);

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("testuser");
        assertThat(result.status()).isEqualTo(UserStatus.PENDING_PROFILE);
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.password()).isEqualTo("password123");
        assertThat(result.firstName()).isEqualTo("Test");
        assertThat(result.lastName()).isEqualTo("User");
        assertThat(result.middleName()).isEqualTo("M");
        assertThat(result.birthDate()).isEqualTo(birthDate);
        assertThat(result.role()).isEqualTo(RoleType.ROLE_USER);
    }

    @Test
    void toCreateUserRequest_ShouldMapWithConstantValues() {
        CreateUserRequest result = userMapper.toCreateUserRequest(signUpRequest);

        assertThat(result.status()).isEqualTo(UserStatus.PENDING_PROFILE);
        assertThat(result.role()).isEqualTo(RoleType.ROLE_USER);
    }

    @Test
    void toCreateUserRequest_ShouldIgnoreConfirmPassword() {
        CreateUserRequest result = userMapper.toCreateUserRequest(signUpRequest);

        assertThat(result).isNotNull();
        assertThat(result.password()).isEqualTo(signUpRequest.password());
    }

    @Test
    void toCreateUserRequest_ShouldHandleNullSignUpRequestFields() {
        SignUpRequest requestWithNulls = new SignUpRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        CreateUserRequest result = userMapper.toCreateUserRequest(requestWithNulls);

        assertThat(result).isNotNull();
        assertThat(result.username()).isNull();
        assertThat(result.status()).isEqualTo(UserStatus.PENDING_PROFILE);
        assertThat(result.email()).isNull();
        assertThat(result.password()).isNull();
        assertThat(result.firstName()).isNull();
        assertThat(result.lastName()).isNull();
        assertThat(result.middleName()).isNull();
        assertThat(result.birthDate()).isNull();
        assertThat(result.role()).isEqualTo(RoleType.ROLE_USER);
    }

    @Test
    void toCreateUserRequest_ShouldHandleNullMiddleName() {
        SignUpRequest requestWithoutMiddleName = new SignUpRequest(
                "testuser",
                "test@example.com",
                "password123",
                "password123",
                "Test",
                "User",
                null,
                birthDate
        );

        CreateUserRequest result = userMapper.toCreateUserRequest(requestWithoutMiddleName);

        assertThat(result).isNotNull();
        assertThat(result.middleName()).isNull();
        assertThat(result.status()).isEqualTo(UserStatus.PENDING_PROFILE);
        assertThat(result.role()).isEqualTo(RoleType.ROLE_USER);
    }

    @Test
    void toEntity_ShouldMapCreateUserRequestToUser_WhenAllFieldsPresent() {
        User result = userMapper.toEntity(createUserRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getPassword()).isEqualTo("password123");
        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getLastName()).isEqualTo("User");
        assertThat(result.getMiddleName()).isEqualTo("M");
        assertThat(result.getBirthDate()).isEqualTo(birthDate);
        assertThat(result.getRole()).isEqualTo(RoleType.ROLE_USER);
        assertThat(result.getDeleted()).isFalse();
        assertThat(result.getDeletedAt()).isNull();
        assertThat(result.getDeletedBy()).isNull();
        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
        assertThat(result.getIndividual()).isNull();
    }

    @Test
    void toEntity_ShouldSetDefaultDeletedFalse() {
        User result = userMapper.toEntity(createUserRequest);
        assertThat(result.getDeleted()).isFalse();
    }

    @Test
    void toEntity_ShouldIgnoreId() {
        User result = userMapper.toEntity(createUserRequest);
        assertThat(result.getId()).isNull();
    }

    @Test
    void toEntity_ShouldIgnoreIndividual() {
        User result = userMapper.toEntity(createUserRequest);
        assertThat(result.getIndividual()).isNull();
    }

    @Test
    void toEntity_ShouldIgnoreTimestamps() {
        User result = userMapper.toEntity(createUserRequest);

        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
        assertThat(result.getDeletedAt()).isNull();
        assertThat(result.getDeletedBy()).isNull();
    }

    @Test
    void toEntity_ShouldHandleNullCreateUserRequestFields() {
        CreateUserRequest requestWithNulls = new CreateUserRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        User result = userMapper.toEntity(requestWithNulls);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isNull();
        assertThat(result.getStatus()).isNull();
        assertThat(result.getEmail()).isNull();
        assertThat(result.getPassword()).isNull();
        assertThat(result.getFirstName()).isNull();
        assertThat(result.getLastName()).isNull();
        assertThat(result.getMiddleName()).isNull();
        assertThat(result.getBirthDate()).isNull();
        assertThat(result.getRole()).isNull();
        assertThat(result.getDeleted()).isFalse();
        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
        assertThat(result.getDeletedAt()).isNull();
        assertThat(result.getDeletedBy()).isNull();
        assertThat(result.getIndividual()).isNull();
    }

    @Test
    void toEntity_ShouldHandleEmptyStrings() {
        CreateUserRequest requestWithEmptyStrings = new CreateUserRequest(
                "",
                UserStatus.ACTIVE,
                "",
                "",
                "",
                "",
                "",
                birthDate,
                RoleType.ROLE_USER
        );

        User result = userMapper.toEntity(requestWithEmptyStrings);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEmpty();
        assertThat(result.getEmail()).isEmpty();
        assertThat(result.getPassword()).isEmpty();
        assertThat(result.getFirstName()).isEmpty();
        assertThat(result.getLastName()).isEmpty();
        assertThat(result.getMiddleName()).isEmpty();
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.getRole()).isEqualTo(RoleType.ROLE_USER);
        assertThat(result.getDeleted()).isFalse();
    }

    @Test
    void toEntityAndToResponse_ShouldBeConsistent() {
        CreateUserRequest request = createUserRequest;

        User entity = userMapper.toEntity(request);
        entity.setId("generated-id");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        UserResponse response = userMapper.toResponse(entity);

        assertThat(response.id()).isEqualTo("generated-id");
        assertThat(response.username()).isEqualTo(request.username());
        assertThat(response.status()).isEqualTo(request.status());
        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.firstName()).isEqualTo(request.firstName());
        assertThat(response.lastName()).isEqualTo(request.lastName());
        assertThat(response.middleName()).isEqualTo(request.middleName());
        assertThat(response.birthDate()).isEqualTo(request.birthDate());
        assertThat(response.role()).isEqualTo(request.role());
        assertThat(response.deleted()).isFalse();
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);
        assertThat(response.deletedAt()).isNull();
        assertThat(response.deletedBy()).isNull();
    }

    @Test
    void toCreateUserRequestAndToEntity_ShouldBeConsistent() {
        SignUpRequest signUp = signUpRequest;

        CreateUserRequest createRequest = userMapper.toCreateUserRequest(signUp);
        User entity = userMapper.toEntity(createRequest);

        assertThat(entity.getUsername()).isEqualTo(signUp.username());
        assertThat(entity.getEmail()).isEqualTo(signUp.email());
        assertThat(entity.getPassword()).isEqualTo(signUp.password());
        assertThat(entity.getFirstName()).isEqualTo(signUp.firstName());
        assertThat(entity.getLastName()).isEqualTo(signUp.lastName());
        assertThat(entity.getMiddleName()).isEqualTo(signUp.middleName());
        assertThat(entity.getBirthDate()).isEqualTo(signUp.birthDate());
        assertThat(entity.getStatus()).isEqualTo(UserStatus.PENDING_PROFILE);
        assertThat(entity.getRole()).isEqualTo(RoleType.ROLE_USER);
        assertThat(entity.getDeleted()).isFalse();
    }

    @Test
    void fullSignUpFlow_ShouldWorkCorrectly() {
        SignUpRequest signUp = signUpRequest;

        CreateUserRequest createRequest = userMapper.toCreateUserRequest(signUp);
        User entity = userMapper.toEntity(createRequest);
        entity.setId("generated-id");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        UserResponse response = userMapper.toResponse(entity);

        assertThat(response.id()).isEqualTo("generated-id");
        assertThat(response.username()).isEqualTo(signUp.username());
        assertThat(response.email()).isEqualTo(signUp.email());
        assertThat(response.firstName()).isEqualTo(signUp.firstName());
        assertThat(response.lastName()).isEqualTo(signUp.lastName());
        assertThat(response.middleName()).isEqualTo(signUp.middleName());
        assertThat(response.birthDate()).isEqualTo(signUp.birthDate());
        assertThat(response.status()).isEqualTo(UserStatus.PENDING_PROFILE);
        assertThat(response.role()).isEqualTo(RoleType.ROLE_USER);
        assertThat(response.deleted()).isFalse();
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);
    }

    @Test
    void toResponse_ShouldHandleUserWithSpecialCharacters() {
        User userWithSpecial = new User();
        userWithSpecial.setId("1");
        userWithSpecial.setUsername("test_user-123");
        userWithSpecial.setStatus(UserStatus.ACTIVE);
        userWithSpecial.setEmail("test+special@example.com");
        userWithSpecial.setPassword("encoded");
        userWithSpecial.setFirstName("Test-Name");
        userWithSpecial.setLastName("User's");
        userWithSpecial.setMiddleName("M&M");
        userWithSpecial.setBirthDate(birthDate);
        userWithSpecial.setRole(RoleType.ROLE_ADMIN);
        userWithSpecial.setDeleted(false);

        UserResponse result = userMapper.toResponse(userWithSpecial);

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("test_user-123");
        assertThat(result.email()).isEqualTo("test+special@example.com");
        assertThat(result.firstName()).isEqualTo("Test-Name");
        assertThat(result.lastName()).isEqualTo("User's");
        assertThat(result.middleName()).isEqualTo("M&M");
        assertThat(result.role()).isEqualTo(RoleType.ROLE_ADMIN);
    }

    @Test
    void toResponse_ShouldMapAllStatuses() {
        for (UserStatus status : UserStatus.values()) {
            user.setStatus(status);
            UserResponse result = userMapper.toResponse(user);
            assertThat(result.status()).isEqualTo(status);
        }
    }

    @Test
    void toResponse_ShouldMapAllRoles() {
        for (RoleType role : RoleType.values()) {
            user.setRole(role);
            UserResponse result = userMapper.toResponse(user);
            assertThat(result.role()).isEqualTo(role);
        }
    }

    @Test
    void toResponse_ShouldHandleUserWithDeletedTimestamps() {
        Instant deletedAt = Instant.now();
        user.setDeleted(true);
        user.setDeletedAt(deletedAt);
        user.setDeletedBy("admin");

        UserResponse result = userMapper.toResponse(user);

        assertThat(result.deleted()).isTrue();
        assertThat(result.deletedAt()).isEqualTo(deletedAt);
        assertThat(result.deletedBy()).isEqualTo("admin");
    }

    @Test
    void toEntity_ShouldHandleVeryLongStrings() {
        String longString = "a".repeat(200);
        CreateUserRequest requestWithLongStrings = new CreateUserRequest(
                longString,
                UserStatus.ACTIVE,
                longString + "@example.com",
                longString,
                longString,
                longString,
                longString,
                birthDate,
                RoleType.ROLE_USER
        );

        User result = userMapper.toEntity(requestWithLongStrings);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(longString);
        assertThat(result.getEmail()).isEqualTo(longString + "@example.com");
        assertThat(result.getPassword()).isEqualTo(longString);
        assertThat(result.getFirstName()).isEqualTo(longString);
        assertThat(result.getLastName()).isEqualTo(longString);
        assertThat(result.getMiddleName()).isEqualTo(longString);
    }

    @Test
    void toResponse_ShouldHandleUserWithNullTimestamps() {
        user.setCreatedAt(null);
        user.setUpdatedAt(null);
        user.setDeletedAt(null);

        UserResponse result = userMapper.toResponse(user);

        assertThat(result.createdAt()).isNull();
        assertThat(result.updatedAt()).isNull();
        assertThat(result.deletedAt()).isNull();
    }

    @Test
    void toEntity_ShouldMapFromSignUpRequestViaCreateUserRequest() {
        SignUpRequest signUp = signUpRequest;

        CreateUserRequest createRequest = userMapper.toCreateUserRequest(signUp);
        User entity = userMapper.toEntity(createRequest);

        assertThat(entity).isNotNull();
        assertThat(entity.getUsername()).isEqualTo(signUp.username());
        assertThat(entity.getEmail()).isEqualTo(signUp.email());
        assertThat(entity.getPassword()).isEqualTo(signUp.password());
        assertThat(entity.getFirstName()).isEqualTo(signUp.firstName());
        assertThat(entity.getLastName()).isEqualTo(signUp.lastName());
        assertThat(entity.getMiddleName()).isEqualTo(signUp.middleName());
        assertThat(entity.getBirthDate()).isEqualTo(signUp.birthDate());
        assertThat(entity.getStatus()).isEqualTo(UserStatus.PENDING_PROFILE);
        assertThat(entity.getRole()).isEqualTo(RoleType.ROLE_USER);
        assertThat(entity.getDeleted()).isFalse();
        assertThat(entity.getIndividual()).isNull();
    }
}