package com.github.seecret1.userservice.service;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.userservice.dto.request.IndividualWriteDto;
import com.github.seecret1.userservice.dto.response.IndividualDto;
import com.github.seecret1.userservice.dto.response.IndividualWriteResponseDto;

import java.util.Set;

public interface IndividualService {

    IndividualWriteResponseDto register(IndividualWriteDto request);

    PageResponse<IndividualWriteResponseDto> findByEmails(Set<String> emails, PageModel pageModel);

    IndividualDto findById(String id);

    IndividualWriteResponseDto update(String id, IndividualWriteDto request);

    void softDelete(String id);

    void hardDelete(String id);
}
