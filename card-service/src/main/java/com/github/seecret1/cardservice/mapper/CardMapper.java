package com.github.seecret1.cardservice.mapper;

import com.github.seecret1.cardservice.dto.request.CardRequest;
import com.github.seecret1.cardservice.dto.response.CardResponse;
import com.github.seecret1.cardservice.dto.response.CardSummaryResponse;
import com.github.seecret1.cardservice.entity.Card;
import com.github.seecret1.cardservice.entity.enums.CardStatus;
import com.github.seecret1.cardservice.utils.CardHashUtils;
import com.github.seecret1.cardservice.utils.CardMaskUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public final class CardMapper {

    public List<CardResponse> toDtoResponseList(List<Card> cards) {
        List<CardResponse> dtoList = new ArrayList<>(cards.size());

        for (int i = 0; i < cards.size(); i++) {
            dtoList.add(toDtoResponse(cards.get(i)));
        }

        return dtoList;
    }

    public List<CardResponse> toYourDtoResponseList(List<Card> cards) {
        List<CardResponse> dtoList = new ArrayList<>(cards.size());

        for (int i = 0; i < cards.size(); i++) {
            dtoList.add(toYourDtoResponse(cards.get(i)));
        }

        return dtoList;
    }

    public Set<CardSummaryResponse> toResponseList(Set<Card> cards) {
        Set<CardSummaryResponse> dtoList = new HashSet<>(cards.size());

        for (var card : cards) {
            dtoList.add(toResponse(card));
        }

        return dtoList;
    }

    public CardResponse toDtoResponse(Card card) {
        return new CardResponse(
                CardMaskUtils.maskCardNumber(card.getNumber()),
                card.getDateActivation(),
                card.getDateExpiry(),
                card.getStatus(),
                card.getBalance(),
                card.getUserId()
        );
    }

    public CardResponse toYourDtoResponse(Card card) {
        return new CardResponse(
                card.getNumber(),
                card.getDateActivation(),
                card.getDateExpiry(),
                card.getStatus(),
                card.getBalance(),
                card.getUserId()
        );
    }

    public CardSummaryResponse toResponse(Card card) {
        return new CardSummaryResponse(
                CardMaskUtils.maskCardNumber(card.getNumber()),
                card.getStatus(),
                card.getBalance()
        );
    }

    public Card toEntity(CardRequest request, String userId) {
        Card card = new Card();
        card.setNumber(request.number());
        card.setNumberHash(CardHashUtils.hash(request.number()));
        card.setDateActivation(request.dateActivation());
        card.setDateExpiry(request.dateExpiry());
        card.setBalance(request.balance());
        card.setStatus(CardStatus.ACTIVE);
        card.setDeleted(false);
        card.setUserId(userId);
        return card;
    }
}
