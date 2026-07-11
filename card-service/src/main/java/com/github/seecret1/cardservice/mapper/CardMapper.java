package com.github.seecret1.cardservice.mapper;

import com.github.seecret1.cardservice.dto.request.CardRequest;
import com.github.seecret1.cardservice.dto.response.CardResponse;
import com.github.seecret1.cardservice.entity.Card;
import com.github.seecret1.cardservice.entity.enums.CardStatus;
import com.github.seecret1.cardservice.order.message.OrderCardDto;
import com.github.seecret1.cardservice.order.message.OrderDto;
import com.github.seecret1.cardservice.utils.CardHashUtils;
import com.github.seecret1.cardservice.utils.CardMaskUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public CardResponse toDtoResponse(Card card) {
        return new CardResponse(
                CardMaskUtils.maskCardNumber(card.getNumber()),
                card.getType(),
                card.getDateActivation(),
                card.getDateExpiry(),
                card.getStatus(),
                card.getBalance(),
                card.getSpendingLimit(),
                card.getUserId()
        );
    }

    public CardResponse toYourDtoResponse(Card card) {
        return new CardResponse(
                card.getNumber(),
                card.getType(),
                card.getDateActivation(),
                card.getDateExpiry(),
                card.getStatus(),
                card.getBalance(),
                card.getSpendingLimit(),
                card.getUserId()
        );
    }

    public Card toEntity(CardRequest request, String userId) {
        return Card.builder()
                .number(request.number())
                .type(request.type())
                .numberHash(CardHashUtils.hash(request.number()))
                .dateActivation(request.dateActivation())
                .dateExpiry(request.dateExpiry())
                .balance(request.balance())
                .spendingLimit(request.spendingLimit())
                .status(CardStatus.PENDING)
                .deleted(false)
                .userId(userId)
                .build();
    }

    public OrderCardDto toOrderCardDto(Card card, String comment, String userId) {
        String traceId = UUID.randomUUID().toString();
        OrderCardDto dto = new OrderCardDto();
        dto.setTraceId(traceId);
        dto.setUserId(userId);
        dto.setOrderType(OrderDto.OrderType.CARD);
        dto.setCardId(card.getId());
        dto.setCardType(card.getType());
        dto.setSpendingLimit(card.getSpendingLimit());
        dto.setComment(comment);
        dto.setCreatedAt(Instant.now());
        return dto;
    }
}
