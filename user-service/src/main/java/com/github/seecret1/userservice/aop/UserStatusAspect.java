package com.github.seecret1.userservice.aop;

import com.github.seecret1.userservice.exception.AuthException;
import com.github.seecret1.userservice.exception.UserDeletedException;
import com.github.seecret1.userservice.exception.UserStatusException;
import com.github.seecret1.userservice.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class UserStatusAspect {

    @Before("@annotation(requireUserStatus)")
    public void checkUserStatus(JoinPoint joinPoint, RequireUserStatus requireUserStatus) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new AuthException("User is not authenticated");
        }

        var user = userDetails.user();

        if (requireUserStatus.checkDeleted() && Boolean.TRUE.equals(user.getDeleted())) {
            throw new UserDeletedException("User is deleted");
        }

        var status = user.getStatus();
        if (!Arrays.asList(requireUserStatus.allowed()).contains(status)) {
            throw new UserStatusException(
                    "Operation '%s' is not allowed for user status '%s'",
                    joinPoint.getSignature().getName(), status
            );
        }
    }
}
