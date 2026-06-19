package com.github.seecret1.userservice.security.jwt;

import com.github.seecret1.userservice.dto.response.JwtAuthenticationDto;
import com.github.seecret1.userservice.entity.RefreshToken;
import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.exception.AuthException;
import com.github.seecret1.userservice.repository.RefreshTokenRepository;
import com.github.seecret1.userservice.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtService {

    @Value("${user-service.jwt.secret}")
    private String jwtSecret;

    @Value("${user-service.jwt.tokenExpiration}")
    private Duration tokenExpiration;

    @Value("${user-service.jwt.refreshTokenExpiration}")
    private Duration refreshTokenExpiration;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public JwtAuthenticationDto generateAuthToken(String email) {
        log.info("Generating auth token for user: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("User not found"));

        refreshTokenRepository.revokeAllByUserId(user.getId());
        return createNewTokenPair(user);
    }

    private JwtAuthenticationDto generateNewRefreshToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("User not found"));

        log.debug("Keeping other refresh tokens active for user: {}", email);
        return createNewTokenPair(user);
    }

    @Transactional
    public JwtAuthenticationDto refreshBaseToken(String email, String oldRefreshToken) {
        log.info("Generating refresh token for user: {}", email);
        RefreshToken storedToken = refreshTokenRepository.findByToken(oldRefreshToken)
                .orElseThrow(() -> new AuthException("Invalid refresh token"));

        if (storedToken.isRevoked()) {
            throw new AuthException("Refresh token has been revoked");
        }
        if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new AuthException("Refresh token expired");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        return generateNewRefreshToken(email);
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("userId", String.class);
    }

    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("roles", List.class);
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch (ExpiredJwtException ex) {
            log.error("Expired JwtException", ex);
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JwtException", ex);
        } catch (MalformedJwtException ex) {
            log.error("Malformed JwtException", ex);
        } catch (SecurityException ex) {
            log.error("Security Exception", ex);
        } catch (Exception ex) {
            log.error("Invalid token", ex);
        }
        return false;
    }

    public boolean validateRefreshToken(String token) {
        try {
            if (!validateJwtToken(token)) {
                return false;
            }
            RefreshToken storedToken = refreshTokenRepository.findByToken(token)
                    .orElse(null);

            return storedToken != null &&
                    !storedToken.isRevoked() &&
                    storedToken.getExpiryDate().isAfter(LocalDateTime.now());

        } catch (Exception ex) {
            log.error("Error validating refresh token", ex);
            return false;
        }
    }

    private JwtAuthenticationDto createNewTokenPair(User user) {
        String jwtToken = generateJwtToken(user);
        String refreshToken = generateRefreshToken(user.getEmail());

        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setToken(refreshToken);
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setExpiryDate(LocalDateTime.now().plus(refreshTokenExpiration));
        refreshTokenEntity.setRevoked(false);
        refreshTokenRepository.save(refreshTokenEntity);

        JwtAuthenticationDto jwtDto = new JwtAuthenticationDto(jwtToken, refreshToken);
        log.debug("Created new token pair for user: {}", user.getEmail());
        return jwtDto;
    }

    /**
     * Генерация JWT токена с полной информацией о пользователе
     */
    private String generateJwtToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());                          // userId (UUID)
        claims.put("email", user.getEmail());                        // email
        claims.put("roles", List.of(user.getRole().name())); // роли
        claims.put("username", user.getUsername());                 // username (опционально)

        Date expirationDate = Date.from(
                LocalDateTime.now().plusMinutes(tokenExpiration.toMinutes())
                        .atZone(ZoneId.systemDefault()).toInstant()
        );

        return Jwts.builder()
                .claims(claims)
                .subject(user.getId())  // <-- ВАЖНО: subject = userId, а не email!
                .issuedAt(new Date())
                .expiration(expirationDate)
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Генерация Refresh токена
     */
    private String generateRefreshToken(String email) {
        Date expirationDate = Date.from(
                LocalDateTime.now().plusMinutes(refreshTokenExpiration.toMinutes())
                        .atZone(ZoneId.systemDefault()).toInstant()
        );

        // Для refresh токена используем email как subject
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(expirationDate)
                .signWith(getSignInKey())
                .compact();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}