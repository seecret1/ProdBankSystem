package com.github.seecret1.cardservice.service;

import com.github.seecret1.cardservice.dto.request.CardRequest;
import com.github.seecret1.cardservice.dto.response.CardResponse;
import com.github.seecret1.cardservice.entity.enums.CardStatus;
import com.github.seecret1.cardservice.entity.enums.CardType;
import com.github.seecret1.cardservice.model.CardFilterModel;
import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;

import java.time.LocalDate;

public interface CardService {

    PageResponse<CardResponse> findAll(PageModel pageModel);

    PageResponse<CardResponse> findOnlyNotDeleted(PageModel pageModel);

    PageResponse<CardResponse> findByFilter(CardFilterModel filter);

    CardResponse findById(String id);

    CardResponse findByNumber(String number);

    PageResponse<CardResponse> findYourCards(String userId, PageModel pageModel);

    CardResponse activateCard(String criterial);

    CardResponse create(CardRequest request);

    CardResponse updateStatus(String id, CardStatus status);

    CardResponse extendCard(String id, LocalDate dateExpiry);

    CardResponse refreshSpendingLimit(String cardId, CardType cardType);

    void softDelete(String userId, String criterial);

    void hardDelete(String criterial);
}
