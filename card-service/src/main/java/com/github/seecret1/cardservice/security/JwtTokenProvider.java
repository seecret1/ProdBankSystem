package com.github.seecret1.cardservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${user-service.jwt.secret}")
    private String jwtSecret;

    /**
     * Извлечение SecretKey из строки
     */
    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Получение userId из токена
     */
    public String getUserIdFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    /**
     * Получение email из токена
     */
    public String getEmailFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("email", String.class);
    }

    /**
     * Получение ролей из токена
     */
    public List<String> getRolesFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roles", List.class);
    }

    /**
     * Проверка, что пользователь имеет роль ADMIN
     */
    public boolean isAdmin(String token) {
        List<String> roles = getRolesFromToken(token);
        return roles != null && roles.contains("ROLE_ADMIN");
    }

    /**
     * Проверка, что пользователь имеет роль USER
     */
    public boolean isUser(String token) {
        List<String> roles = getRolesFromToken(token);
        return roles != null && roles.contains("ROLE_USER");
    }

    /**
     * Проверка валидности токена
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Извлечение всех claims из токена
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Получение срока истечения токена
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getExpiration();
    }

    /**
     * Проверка, что токен не истек
     */
    private boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Создание UserDetails из токена (для Spring Security)
     */
    public UserDetails getUserDetailsFromToken(String token) {
        String userId = getUserIdFromToken(token);
        String email = getEmailFromToken(token);
        List<String> roles = getRolesFromToken(token);

        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                email,
                "", // Пароль не нужен, т.к. аутентификация через JWT
                authorities
        );
    }
}