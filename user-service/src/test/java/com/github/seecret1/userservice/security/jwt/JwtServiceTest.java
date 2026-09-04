package com.github.seecret1.userservice.security.jwt;

import com.github.seecret1.userservice.dto.response.JwtAuthenticationDto;
import com.github.seecret1.userservice.entity.RefreshToken;
import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.entity.enums.RoleType;
import com.github.seecret1.userservice.entity.enums.UserStatus;
import com.github.seecret1.userservice.exception.AuthException;
import com.github.seecret1.userservice.repository.RefreshTokenRepository;
import com.github.seecret1.userservice.repository.UserRepository;
import com.github.seecret1.userservice.security.jwt.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtService jwtService;

    private User user;
    private String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

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

        ReflectionTestUtils.setField(jwtService, "jwtSecret", secretKey);
        ReflectionTestUtils.setField(jwtService, "tokenExpiration", Duration.ofMinutes(15));
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", Duration.ofDays(7));
    }

    @Test
    void generateAuthToken_ShouldReturnJwtAuthenticationDto_WhenUserExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(new RefreshToken());

        JwtAuthenticationDto result = jwtService.generateAuthToken("test@example.com");

        assertThat(result).isNotNull();
        assertThat(result.token()).isNotNull();
        assertThat(result.refreshToken()).isNotNull();
        verify(refreshTokenRepository).revokeAllByUserId("1");
    }

    @Test
    void generateAuthToken_ShouldThrowAuthException_WhenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jwtService.generateAuthToken("nonexistent@example.com"))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void validateJwtToken_ShouldReturnTrue_WhenValidToken() {
        String token = Jwts.builder()
                .subject("test@example.com")
                .signWith(Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(secretKey)))
                .compact();

        boolean result = jwtService.validateJwtToken(token);

        assertThat(result).isTrue();
    }

    @Test
    void validateJwtToken_ShouldReturnFalse_WhenInvalidToken() {
        boolean result = jwtService.validateJwtToken("invalid-token");
        assertThat(result).isFalse();
    }

    @Test
    void getEmailFromToken_ShouldReturnEmail_WhenValidToken() {
        String token = Jwts.builder()
                .subject("test@example.com")
                .signWith(Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(secretKey)))
                .compact();

        String email = jwtService.getEmailFromToken(token);

        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    void refreshBaseToken_ShouldThrowAuthException_WhenTokenNotFound() {
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jwtService.refreshBaseToken("test@example.com", "invalid-token"))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    void refreshBaseToken_ShouldThrowAuthException_WhenTokenRevoked() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("revoked-token");
        refreshToken.setRevoked(true);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));

        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> jwtService.refreshBaseToken("test@example.com", "revoked-token"))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Refresh token has been revoked");
    }

    @Test
    void refreshBaseToken_ShouldThrowAuthException_WhenTokenExpired() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("expired-token");
        refreshToken.setRevoked(false);
        refreshToken.setExpiryDate(LocalDateTime.now().minusDays(1));

        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> jwtService.refreshBaseToken("test@example.com", "expired-token"))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Refresh token expired");
    }
}