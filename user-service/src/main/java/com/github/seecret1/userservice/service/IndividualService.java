package com.github.seecret1.userservice.service;

import com.github.seecret1.userservice.dto.request.IndividualRequest;
import com.github.seecret1.userservice.dto.response.IndividualDto;
import com.github.seecret1.userservice.dto.response.IndividualResponse;

public interface IndividualService {

    IndividualResponse recordPersonalData(String userId, IndividualRequest request);

    IndividualDto findById(String id);

    IndividualResponse update(String id, IndividualRequest request);

    IndividualResponse updateYour(String id, IndividualRequest request);

    void softDelete(String id);

    void hardDelete(String id);
}
