package com.github.seecret1.userservice.aop;

import com.github.seecret1.userservice.entity.enums.UserStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireUserStatus {

    UserStatus[] allowed() default { UserStatus.ACTIVE };

    boolean checkDeleted() default true;
}