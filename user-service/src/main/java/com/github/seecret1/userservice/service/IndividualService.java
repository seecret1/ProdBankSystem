package com.github.seecret1.userservice.service;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.userservice.dto.request.IndividualRequest;
import com.github.seecret1.userservice.dto.response.IndividualDto;
import com.github.seecret1.userservice.dto.response.IndividualResponse;

import java.util.Set;

public interface IndividualService {

    IndividualResponse recordPersonalData(String userId, IndividualRequest request);

    PageResponse<IndividualResponse> findByEmails(Set<String> emails, PageModel pageModel);

    IndividualDto findById(String id);

    IndividualResponse update(String id, IndividualRequest request);

    void softDelete(String id);

    void hardDelete(String id);
}
