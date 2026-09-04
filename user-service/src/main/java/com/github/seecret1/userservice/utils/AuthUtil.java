package com.github.seecret1.userservice.utils;

import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.entity.enums.UserStatus;
import com.github.seecret1.userservice.exception.AuthException;
import com.github.seecret1.userservice.exception.PersonException;
import com.github.seecret1.userservice.security.CustomUserDetails;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.userdetails.UserDetails;

@UtilityClass
public class AuthUtil {

    public String getCurrentUserId(UserDetails userDetails) {
        if (userDetails instanceof CustomUserDetails details) {
            return details.getId();
        }
        throw new SecurityException("UserDetails is not instance of CustomUserDetails");
    }

    public void checkUserPersonalData(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new PersonException("Personal data is empty");
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
