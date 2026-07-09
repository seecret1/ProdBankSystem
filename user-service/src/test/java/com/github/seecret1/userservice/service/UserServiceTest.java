package com.github.seecret1.userservice.service;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.userservice.dto.request.CreateUserRequest;
import com.github.seecret1.userservice.dto.request.UpdateUserRequest;
import com.github.seecret1.userservice.dto.response.UserResponse;
import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.entity.enums.RoleType;
import com.github.seecret1.userservice.entity.enums.UserStatus;
import com.github.seecret1.userservice.exception.RegisterUserException;
import com.github.seecret1.userservice.exception.UserNotFoundException;
import com.github.seecret1.userservice.mapper.UserMapper;
import com.github.seecret1.userservice.model.UserFilterModel;
import com.github.seecret1.userservice.repository.UserRepository;
import com.github.seecret1.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponse userResponse;
    private CreateUserRequest createRequest;
    private UpdateUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("1");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setStatus(UserStatus.PENDING_PROFILE);
        user.setRole(RoleType.ROLE_USER);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setMiddleName("M");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setDeleted(false);

        userResponse = new UserResponse(
                "1", "testuser", UserStatus.PENDING_PROFILE,
                "test@example.com", "Test", "User", "M",
                LocalDate.of(1990, 1, 1), RoleType.ROLE_USER,
                null, null, false, null, null
        );

        createRequest = new CreateUserRequest(
                "testuser", UserStatus.PENDING_PROFILE,
                "test@example.com", "password123",
                "Test", "User", "M",
                LocalDate.of(1990, 1, 1), RoleType.ROLE_USER
        );

        updateRequest = new UpdateUserRequest(
                "updateduser", "updated@example.com"
        );
    }

    @Test
    void findAllUsers_ShouldReturnPageResponse() {
        PageModel pageModel = new PageModel(0, 10);
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(userMapper.toListResponse(anyList())).thenReturn(List.of(userResponse));

        PageResponse<UserResponse> result = userService.findAllUsers(pageModel);

        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void findAllActiveUsers_ShouldReturnPageResponse() {
        PageModel pageModel = new PageModel(0, 10);
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAllActiveUsers(any(Pageable.class))).thenReturn(page);
        when(userMapper.toListResponse(anyList())).thenReturn(List.of(userResponse));

        PageResponse<UserResponse> result = userService.findAllActiveUsers(pageModel);

        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        verify(userRepository).findAllActiveUsers(any(Pageable.class));
    }

    @Test
    void findByFilter_ShouldReturnPageResponse() {
        UserFilterModel filter = UserFilterModel.builder()
                .status(UserStatus.PENDING_PROFILE)
                .firstName("Test")
                .build();
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(userMapper.toListResponse(anyList())).thenReturn(List.of(userResponse));

        PageResponse<UserResponse> result = userService.findByFilter(filter);

        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void findById_ShouldReturnUserResponse_WhenUserExists() {
        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.findById("testuser");

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("testuser");
        verify(userRepository).findById("testuser");
    }

    @Test
    void findById_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById("nonexistent"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found by id: nonexistent");
    }

    @Test
    void findByEmail_ShouldReturnUserResponse_WhenUserExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.findByEmail("testuser");

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("testuser");
        verify(userRepository).findByEmail("testuser");
    }

    @Test
    void findByEmail_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByEmail("nonexistent"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found by email: nonexistent");
    }

    @Test
    void findByUsername_ShouldReturnUserResponse_WhenUserExists() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.findByUsername("testuser");

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("testuser");
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void findByUsername_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUsername("nonexistent"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found by username: nonexistent");
    }

    @Test
    void create_ShouldReturnUserResponse_WhenUserDoesNotExist() {
        when(userRepository.existsByUsernameOrEmail(anyString(), anyString())).thenReturn(false);
        when(userMapper.toEntity(createRequest)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.create(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("testuser");
        verify(userRepository).existsByUsernameOrEmail("testuser", "test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_ShouldThrowRegisterUserException_WhenUserExists() {
        when(userRepository.existsByUsernameOrEmail(anyString(), anyString())).thenReturn(true);
        assertThatThrownBy(() -> userService.create(createRequest))
                .isInstanceOf(RegisterUserException.class)
                .hasMessageContaining("User by username testuser or email test@example.com exists");
    }

    @Test
    void updateFull_ShouldReturnUpdatedUserResponse() {
        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.updateFull("testuser", createRequest);

        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateYour_ShouldReturnUpdatedUserResponse_WhenUserExists() {
        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.updateYour("1", updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("testuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void delete_ShouldSoftDeleteUser() {
        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.delete("admin", "testuser");

        assertThat(user.getDeleted()).isTrue();
        assertThat(user.getDeletedBy()).isEqualTo("admin");
        verify(userRepository).save(user);
    }


    @Test
    void findUserEntityById_ShouldReturnUser_WhenExists() {
        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));

        User result = userService.findUserEntityById("1");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1");
    }

    @Test
    void findUserEntityById_ShouldThrowUserNotFoundException_WhenNotExists() {
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.findUserEntityById("999"))
                .isInstanceOf(UserNotFoundException.class);
    }
}