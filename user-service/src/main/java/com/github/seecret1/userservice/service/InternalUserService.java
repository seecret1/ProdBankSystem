package com.github.seecret1.userservice.service;

import com.github.seecret1.userservice.entity.User;

public interface InternalUserService {

    User findUserEntityByCriterial(String criterial);

    User findUserEntityById(String id);
}
