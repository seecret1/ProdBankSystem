package com.github.seecret1.userservice.utils;

import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.entity.enums.UserStatus;
import com.github.seecret1.userservice.exception.AuthException;
import com.github.seecret1.userservice.exception.PersonException;
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

    public void checkUserPersonalData(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new PersonException("Personal data is empty");
        }
    }

    public void userRecordPersonalData(User user) {
        if (user.getDeleted()) {
            throw new PersonException("Your profile is deleted");
        }
        if (user.getStatus() != UserStatus.PENDING_PROFILE) {
            throw new PersonException(
                    "User id=%s cannot complete profile, status=%s",
                    user.getId(),
                    user.getStatus()
            );
        }
    }

    public void checkValidUser(User user) {
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new AuthException("User account: %s is blocked", user.getUsername());
        }
        if (user.getDeleted()) {
            throw new AuthException("User account: %s is deleted", user.getUsername());
        }
    }
}
