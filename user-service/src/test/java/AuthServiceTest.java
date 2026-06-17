import com.github.seecret1.userservice.dto.request.*;
import com.github.seecret1.userservice.dto.response.JwtAuthenticationDto;
import com.github.seecret1.userservice.dto.response.UserResponse;
import com.github.seecret1.userservice.entity.RefreshToken;
import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.entity.enums.RoleType;
import com.github.seecret1.userservice.entity.enums.UserStatus;
import com.github.seecret1.userservice.exception.AuthException;
import com.github.seecret1.userservice.exception.CheckPasswordException;
import com.github.seecret1.userservice.exception.PasswordUpdateException;
import com.github.seecret1.userservice.mapper.UserMapper;
import com.github.seecret1.userservice.repository.RefreshTokenRepository;
import com.github.seecret1.userservice.security.jwt.JwtService;
import com.github.seecret1.userservice.service.InternalUserService;
import com.github.seecret1.userservice.service.UserService;
import com.github.seecret1.userservice.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private InternalUserService internalUserService;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private JwtAuthenticationDto jwtAuthDto;
    private SignUpRequest signUpRequest;
    private SignInByEmailRequest signInEmailRequest;
    private SignInByUsernameRequest signInUsernameRequest;
    private ChangePasswordRequest changePasswordRequest;
    private RefreshTokenRequest refreshTokenRequest;
    private CreateUserRequest createUserRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("1");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(RoleType.ROLE_USER);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setMiddleName("M");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setDeleted(false);

        jwtAuthDto = new JwtAuthenticationDto("jwt-token", "refresh-token");

        signUpRequest = new SignUpRequest(
                "testuser", "test@example.com",
                "password123", "password123",
                "Test", "User", "M",
                LocalDate.of(1990, 1, 1)
        );

        signInEmailRequest = new SignInByEmailRequest(
                "test@example.com", "password123"
        );

        signInUsernameRequest = new SignInByUsernameRequest(
                "testuser", "password123"
        );

        changePasswordRequest = new ChangePasswordRequest(
                "oldPassword", "newPassword123", "newPassword123"
        );

        refreshTokenRequest = new RefreshTokenRequest("refresh-token");

        createUserRequest = new CreateUserRequest(
                "testuser", UserStatus.PENDING_PROFILE,
                "test@example.com", "password123",
                "Test", "User", "M",
                LocalDate.of(1990, 1, 1), RoleType.ROLE_USER
        );
    }

    @Test
    void signInByEmail_ShouldReturnJwtAuthenticationDto_WhenValidCredentials() {
        // Arrange
        when(internalUserService.findUserEntityByCriterial(anyString())).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateAuthToken(anyString())).thenReturn(jwtAuthDto);

        // Act
        JwtAuthenticationDto result = authService.signIn(signInEmailRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        verify(jwtService).generateAuthToken("test@example.com");
    }

    @Test
    void signInByEmail_ShouldThrowAuthException_WhenInvalidPassword() {
        // Arrange
        when(internalUserService.findUserEntityByCriterial(anyString())).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.signIn(signInEmailRequest))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void signInByUsername_ShouldReturnJwtAuthenticationDto_WhenValidCredentials() {
        // Arrange
        when(internalUserService.findUserEntityByCriterial(anyString())).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateAuthToken(anyString())).thenReturn(jwtAuthDto);

        // Act
        JwtAuthenticationDto result = authService.signIn(signInUsernameRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("jwt-token");
        verify(jwtService).generateAuthToken("test@example.com");
    }

    @Test
    void signUp_ShouldReturnJwtAuthenticationDto_WhenValidRequest() {
        // Arrange
        UserResponse userResponse = new UserResponse(
                "1", "testuser", UserStatus.PENDING_PROFILE,
                "test@example.com", "Test", "User", "M",
                LocalDate.of(1990, 1, 1), RoleType.ROLE_USER,
                null, null, false, null, null
        );
        when(userMapper.toCreateUserRequest(any(SignUpRequest.class))).thenReturn(createUserRequest);
        when(userService.create(any(CreateUserRequest.class))).thenReturn(userResponse);
        when(jwtService.generateAuthToken(anyString())).thenReturn(jwtAuthDto);

        // Act
        JwtAuthenticationDto result = authService.signUp(signUpRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("jwt-token");
        verify(userService).create(any(CreateUserRequest.class));
        verify(jwtService).generateAuthToken("test@example.com");
    }

    @Test
    void signUp_ShouldThrowCheckPasswordException_WhenPasswordsDoNotMatch() {
        // Arrange
        SignUpRequest invalidRequest = new SignUpRequest(
                "testuser", "test@example.com",
                "password123", "differentPassword",
                "Test", "User", "M",
                LocalDate.of(1990, 1, 1)
        );

        // Act & Assert
        assertThatThrownBy(() -> authService.signUp(invalidRequest))
                .isInstanceOf(CheckPasswordException.class)
                .hasMessageContaining("Password does not match");
    }

    @Test
    void signOut_ShouldRevokeTokens_WhenValidRefreshToken() {
        // Arrange
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        refreshToken.setRevoked(false);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));

        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));
        doNothing().when(refreshTokenRepository).revokeAllByUserId(anyString());

        // Act
        authService.signOut("1", refreshTokenRequest);

        // Assert
        verify(refreshTokenRepository).revokeAllByUserId("1");
    }

    @Test
    void signOut_ShouldThrowAuthException_WhenTokenNotFound() {
        // Arrange
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.signOut("1", refreshTokenRequest))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Refresh token not found");
    }

    @Test
    void signOut_ShouldThrowAuthException_WhenTokenRevoked() {
        // Arrange
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        refreshToken.setRevoked(true);
        refreshToken.setUser(user);

        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));

        // Act & Assert
        assertThatThrownBy(() -> authService.signOut("1", refreshTokenRequest))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("This token is revoked");
    }

    @Test
    void changePassword_ShouldReturnJwtAuthenticationDto_WhenValidRequest() {
        // Arrange
        when(internalUserService.findUserEntityById(anyString())).thenReturn(user);
        when(passwordEncoder.matches("oldPassword", user.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("newPassword123", user.getPassword())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        doNothing().when(refreshTokenRepository).revokeAllByUserId(anyString());
        doNothing().when(internalUserService).saveUser(any(User.class));
        when(jwtService.generateAuthToken(anyString())).thenReturn(jwtAuthDto);

        // Act
        JwtAuthenticationDto result = authService.changePassword("1", changePasswordRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("jwt-token");
        verify(refreshTokenRepository).revokeAllByUserId("1");
        verify(internalUserService).saveUser(user);
    }

    @Test
    void changePassword_ShouldThrowPasswordUpdateException_WhenCurrentPasswordIncorrect() {
        // Arrange
        when(internalUserService.findUserEntityById(anyString())).thenReturn(user);
        when(passwordEncoder.matches("oldPassword", user.getPassword())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.changePassword("1", changePasswordRequest))
                .isInstanceOf(PasswordUpdateException.class)
                .hasMessageContaining("The password was entered incorrectly");
    }

    @Test
    void changePassword_ShouldThrowPasswordUpdateException_WhenNewPasswordSameAsCurrent() {
        // Arrange
        when(internalUserService.findUserEntityById(anyString())).thenReturn(user);
        when(passwordEncoder.matches("oldPassword", user.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("newPassword123", user.getPassword())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.changePassword("1", changePasswordRequest))
                .isInstanceOf(PasswordUpdateException.class)
                .hasMessageContaining("New password must be different from current password");
    }

    @Test
    void refreshToken_ShouldReturnJwtAuthenticationDto_WhenValidToken() {
        // Arrange
        when(jwtService.validateRefreshToken(anyString())).thenReturn(true);
        when(jwtService.getEmailFromToken(anyString())).thenReturn("test@example.com");
        when(internalUserService.findUserEntityByCriterial(anyString())).thenReturn(user);
        when(jwtService.refreshBaseToken(anyString(), anyString())).thenReturn(jwtAuthDto);

        // Act
        JwtAuthenticationDto result = authService.refreshToken(refreshTokenRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("jwt-token");
        verify(jwtService).refreshBaseToken("test@example.com", "refresh-token");
    }

    @Test
    void refreshToken_ShouldThrowAuthException_WhenInvalidRefreshToken() {
        // Arrange
        when(jwtService.validateRefreshToken(anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(refreshTokenRequest))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Invalid or expired refresh token");
    }

    @Test
    void refreshToken_ShouldThrowAuthException_WhenUserBlocked() {
        // Arrange
        user.setStatus(UserStatus.BLOCKED);
        when(jwtService.validateRefreshToken(anyString())).thenReturn(true);
        when(jwtService.getEmailFromToken(anyString())).thenReturn("test@example.com");
        when(internalUserService.findUserEntityByCriterial(anyString())).thenReturn(user);

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(refreshTokenRequest))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("is blocked");
    }
}