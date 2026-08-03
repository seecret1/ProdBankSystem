package com.github.seecret1.office_service.service;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.office_service.dto.request.OfficeCreateRequest;
import com.github.seecret1.office_service.dto.request.OfficeUpdateRequest;
import com.github.seecret1.office_service.dto.response.OfficeFullResponse;
import com.github.seecret1.office_service.dto.response.OfficeResponse;

import java.util.List;

public interface OfficeService {

    PageResponse<OfficeFullResponse> findAll(PageModel pageModel);

    OfficeResponse findById(String id);

    List<OfficeResponse> findOfficesByCity(String city, String apiKey);

    PageResponse<OfficeResponse> findOfficesByCity(String city, PageModel pageModel);

    OfficeFullResponse create(String userId, OfficeCreateRequest request);

    OfficeResponse updateOffice(String id, OfficeUpdateRequest request);

    void blocked(String id);

    void delete(String userId, String id);
}
