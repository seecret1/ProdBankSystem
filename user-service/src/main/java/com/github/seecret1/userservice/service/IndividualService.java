package com.github.seecret1.userservice.service;

import com.github.seecret1.userservice.dto.request.IndividualRequest;
import com.github.seecret1.userservice.dto.response.IndividualDto;
import com.github.seecret1.userservice.dto.response.IndividualResponse;

public interface IndividualService {

    IndividualDto findByCriterial(String criterial);

    IndividualResponse recordPersonalData(String userId, IndividualRequest request);

    IndividualResponse update(String criterial, IndividualRequest request);

    IndividualResponse updateYour(String userId, IndividualRequest request);

    void softDelete(String criterial);

    void hardDelete(String criterial);
}
