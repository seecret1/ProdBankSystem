package com.github.seecret1.userservice.service;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.userservice.dto.request.IndividualRequest;
import com.github.seecret1.userservice.dto.response.IndividualResponse;

public interface IndividualService {

    PageResponse<IndividualResponse> findAll(PageModel pageModel);

    IndividualResponse findById(String criterial);

    IndividualResponse findByPhoneNumber(String phoneNumber);

    IndividualResponse recordPersonalData(String userId, IndividualRequest request);

    IndividualResponse update(String criterial, IndividualRequest request);

    IndividualResponse updateYour(String userId, IndividualRequest request);

    void softDelete(String criterial);

    void hardDelete(String criterial);
}
