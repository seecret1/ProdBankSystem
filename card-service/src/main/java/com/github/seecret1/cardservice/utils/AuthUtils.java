package com.github.seecret1.cardservice.utils;

import com.github.seecret1.cardservice.entity.Card;
import com.github.seecret1.cardservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUtils {

    private final JwtTokenProvider tokenProvider;

    private String getTokenFromContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (String) auth.getCredentials();
    }

    public void checkCardAccess(Card card) {
        String token = getTokenFromContext();
        String userId = tokenProvider.getUserIdFromToken(token);
        if (!card.getUserId().equals(userId)) {
            throw new AccessDeniedException("Access is denied");
        }
    }
}
