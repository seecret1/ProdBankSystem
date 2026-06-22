package com.github.seecret1.cardservice.service;

import com.github.seecret1.cardservice.dto.request.CardRequest;
import com.github.seecret1.cardservice.dto.request.ExtendCardRequest;
import com.github.seecret1.cardservice.dto.request.UpdateStatusCardRequest;
import com.github.seecret1.cardservice.dto.response.CardResponse;
import com.github.seecret1.cardservice.model.CardFilterModel;
import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;

public interface CardService {

    PageResponse<CardResponse> findAll(PageModel pageModel);

    PageResponse<CardResponse> findByFilter(CardFilterModel filter);

    CardResponse findByCriterial(String criterial);

    PageResponse<CardResponse> findYourCards(String userId, PageModel pageModel);

    CardResponse create(CardRequest request);

    CardResponse updateStatus(UpdateStatusCardRequest request);

    CardResponse extendCard(ExtendCardRequest request);

    void softDelete(String userId, String criterial);

    void hardDelete(String criterial);
}
