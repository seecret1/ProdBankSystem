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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${user-service.jwt.secret}")
    private String jwtSecret;

    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUserIdFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", String.class);
    }

    public String getEmailFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("email", String.class);
    }

    @SuppressWarnings("unchecked")
    public String getRoleFromToken(String token) {
        Claims claims = extractAllClaims(token);
        Object rolesObj = claims.get("roles");

        log.info("Raw roles from token: {}", rolesObj);
        log.info("Roles class: {}", rolesObj != null ? rolesObj.getClass().getName() : "null");

        if (rolesObj == null) {
            log.warn("No roles found in token");
            return "ROLE_USER";
        }

        if (rolesObj instanceof List) {
            List<?> rolesList = (List<?>) rolesObj;
            if (!rolesList.isEmpty() && rolesList.get(0) != null) {
                String role = rolesList.get(0).toString();
                log.info("Role from List: {}", role);
                return role;
            }
        }

        if (rolesObj instanceof String) {
            String roleStr = (String) rolesObj;
            log.info("Role from String: {}", roleStr);
            return roleStr;
        }

        log.warn("Unexpected roles type: {}", rolesObj.getClass().getName());
        return "ROLE_USER";
    }

    public List<String> getRolesFromToken(String token) {
        Claims claims = extractAllClaims(token);
        Object rolesObj = claims.get("roles");

        if (rolesObj == null) {
            return new ArrayList<>();
        }

        if (rolesObj instanceof List) {
            List<?> rolesList = (List<?>) rolesObj;
            List<String> roles = new ArrayList<>();
            for (Object role : rolesList) {
                if (role != null) {
                    roles.add(role.toString());
                }
            }
            return roles;
        }

        if (rolesObj instanceof String) {
            return List.of((String) rolesObj);
        }

        return new ArrayList<>();
    }

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

    private Claims extractAllClaims(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        log.info("=== JWT DEBUG ===");
        log.info("Token: {}", token);
        log.info("All claims: {}", claims);
        log.info("Subject: {}", claims.getSubject());
        log.info("Email: {}", claims.get("email"));
        log.info("UserId: {}", claims.get("userId"));
        log.info("Roles: {}", claims.get("roles"));
        log.info("=================");

        return claims;
    }

    public UserDetails getUserDetailsFromToken(String token) {
        String userId = getUserIdFromToken(token);
        String email = getEmailFromToken(token);
        String role = getRoleFromToken(token);

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role));

        return new UserPrincipal(
                email != null ? email : userId,
                "",
                authorities,
                userId,
                email,
                role
        );
    }
}