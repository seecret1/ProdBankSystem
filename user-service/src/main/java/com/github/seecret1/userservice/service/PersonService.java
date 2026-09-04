package com.github.seecret1.userservice.service;

import com.github.seecret1.userservice.dto.response.PersonInfo;

public interface PersonService {

    PersonInfo getPersonInfo(String userId, String apiKey);
}
