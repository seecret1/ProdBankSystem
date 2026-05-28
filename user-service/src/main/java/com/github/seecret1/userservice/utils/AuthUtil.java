package com.github.seecret1.userservice.utils;

import com.github.seecret1.userservice.security.CustomUserDetails;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@UtilityClass
public class AuthUtil {

    public String getCurrentUserId(UserDetails userDetails) {
        if (userDetails instanceof CustomUserDetails details) {
            return details.getId();
        }
        throw new SecurityException("UserDetails is not instance of CustomUserDetails");
    }

    public CustomUserDetails getAuthenticatedUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }

        var principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails details) {
            return details;
        }
        throw new SecurityException("Principal in security context is not instance of CustomUserDetails");
    }
}
