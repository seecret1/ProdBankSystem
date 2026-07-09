package com.github.seecret1.userservice.service;

import com.github.seecret1.userservice.entity.User;

public interface InternalUserService {

    User findUserEntityById(String id);

    User findUserEntityByEmail(String email);

    User findUserEntityByUsername(String username);

    void saveUser(User user);
}
