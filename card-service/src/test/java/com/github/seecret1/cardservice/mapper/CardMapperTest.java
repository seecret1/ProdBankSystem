package com.github.seecret1.cardservice.mapper;

import com.github.seecret1.cardservice.dto.order.CardReceivingMethod;
import com.github.seecret1.cardservice.dto.request.CardRequest;
import com.github.seecret1.cardservice.dto.response.CardResponse;
import com.github.seecret1.cardservice.entity.Card;
import com.github.seecret1.cardservice.entity.enums.CardStatus;
import com.github.seecret1.cardservice.entity.enums.CardType;
import com.github.seecret1.cardservice.dto.order.message.OrderCardDto;
import com.github.seecret1.cardservice.dto.order.message.OrderDto;
import com.github.seecret1.cardservice.utils.CardHashUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CardMapper Unit Tests")
class CardMapperTest {

    private CardMapper cardMapper;
    private Card card;
    private CardRequest cardRequest;
    private final String userId = "user-id";
    private final String cardNumber = "1234567890123456";
    private final String maskedNumber = "**** **** **** 3456";

    @BeforeEach
    void setUp() {
        cardMapper = new CardMapper();

        card = new Card();
        card.setId("test-id");
        card.setNumber(cardNumber);
        card.setNumberHash(CardHashUtils.hash(cardNumber));
        card.setType(CardType.DEBIT);
        card.setStatus(CardStatus.PENDING);
        card.setBalance(BigDecimal.valueOf(1000));
        card.setSpendingLimit(BigDecimal.valueOf(100000));
        card.setDateActivation(LocalDate.now());
        card.setDateExpiry(LocalDate.now().plusYears(1));
        card.setUserId(userId);
        card.setDeleted(false);

        cardRequest = new CardRequest(
                cardNumber,
                CardType.DEBIT,
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(100000),
                CardReceivingMethod.OFFICE,
                "empty comment"
        );
    }

    @Test
    @DisplayName("Should convert Card to CardResponse with masked number")
    void shouldConvertToDtoResponse() {
        CardResponse response = cardMapper.toDtoResponse(card);

        assertThat(response).isNotNull();
        assertThat(response.number()).isEqualTo(maskedNumber);
        assertThat(response.type()).isEqualTo(CardType.DEBIT);
        assertThat(response.status()).isEqualTo(CardStatus.PENDING);
        assertThat(response.balance()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(response.spendingLimit()).isEqualTo(BigDecimal.valueOf(100000));
        assertThat(response.dateActivation()).isEqualTo(card.getDateActivation());
        assertThat(response.dateExpiry()).isEqualTo(card.getDateExpiry());
        assertThat(response.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should convert Card to CardResponse with masked number when number is null")
    void shouldConvertToDtoResponseWithNullNumber() {
        card.setNumber(null);
        CardResponse response = cardMapper.toDtoResponse(card);

        assertThat(response.number()).isNull();
    }

    @Test
    @DisplayName("Should convert Card to CardResponse with unmasked number")
    void shouldConvertToYourDtoResponse() {
        CardResponse response = cardMapper.toYourDtoResponse(card);

        assertThat(response).isNotNull();
        assertThat(response.number()).isEqualTo(cardNumber);
        assertThat(response.type()).isEqualTo(CardType.DEBIT);
        assertThat(response.status()).isEqualTo(CardStatus.PENDING);
        assertThat(response.balance()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(response.spendingLimit()).isEqualTo(BigDecimal.valueOf(100000));
        assertThat(response.dateActivation()).isEqualTo(card.getDateActivation());
        assertThat(response.dateExpiry()).isEqualTo(card.getDateExpiry());
        assertThat(response.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should convert list of Cards to list of CardResponses with masked numbers")
    void shouldConvertToDtoResponseList() {
        Card card2 = new Card();
        card2.setNumber("9876543210123456");
        card2.setType(CardType.CREDIT);
        card2.setStatus(CardStatus.ACTIVE);
        card2.setBalance(BigDecimal.valueOf(2000));
        card2.setSpendingLimit(BigDecimal.valueOf(500000));
        card2.setDateActivation(LocalDate.now());
        card2.setDateExpiry(LocalDate.now().plusYears(2));
        card2.setUserId(userId);

        List<Card> cards = List.of(card, card2);
        List<CardResponse> responses = cardMapper.toDtoResponseList(cards);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).number()).isEqualTo("**** **** **** 3456");
        assertThat(responses.get(1).number()).isEqualTo("**** **** **** 3456");
    }

    @Test
    @DisplayName("Should return empty list when converting empty card list")
    void shouldConvertEmptyList() {
        List<CardResponse> responses = cardMapper.toDtoResponseList(List.of());

        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("Should convert list of Cards to list of CardResponses with unmasked numbers")
    void shouldConvertToYourDtoResponseList() {
        Card card2 = new Card();
        card2.setNumber("9876543210123456");
        card2.setType(CardType.CREDIT);
        card2.setStatus(CardStatus.ACTIVE);
        card2.setBalance(BigDecimal.valueOf(2000));
        card2.setSpendingLimit(BigDecimal.valueOf(500000));
        card2.setDateActivation(LocalDate.now());
        card2.setDateExpiry(LocalDate.now().plusYears(2));
        card2.setUserId(userId);

        List<Card> cards = List.of(card, card2);
        List<CardResponse> responses = cardMapper.toYourDtoResponseList(cards);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).number()).isEqualTo("1234567890123456");
        assertThat(responses.get(1).number()).isEqualTo("9876543210123456");
    }

    @Test
    @DisplayName("Should convert CardRequest to Card entity")
    void shouldConvertToEntity() {
        Card result = cardMapper.toEntity(cardRequest, userId);

        assertThat(result).isNotNull();
        assertThat(result.getNumber()).isEqualTo(cardNumber);
        assertThat(result.getNumberHash()).isEqualTo(CardHashUtils.hash(cardNumber));
        assertThat(result.getType()).isEqualTo(CardType.DEBIT);
        assertThat(result.getStatus()).isEqualTo(CardStatus.PENDING);
        assertThat(result.getBalance()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(result.getSpendingLimit()).isEqualTo(BigDecimal.valueOf(100000));
        assertThat(result.getDateActivation()).isEqualTo(cardRequest.dateActivation());
        assertThat(result.getDateExpiry()).isEqualTo(cardRequest.dateExpiry());
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getDeleted()).isFalse();
    }

    @Test
    @DisplayName("Should convert CardRequest with different card type to Card entity")
    void shouldConvertToEntityWithDifferentType() {
        CardRequest creditRequest = new CardRequest(
                "9876543210123456",
                CardType.CREDIT,
                LocalDate.now(),
                LocalDate.now().plusYears(2),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(500000),
                "Credit card comment"
        );

        Card result = cardMapper.toEntity(creditRequest, "other-user");

        assertThat(result.getType()).isEqualTo(CardType.CREDIT);
        assertThat(result.getUserId()).isEqualTo("other-user");
        assertThat(result.getSpendingLimit()).isEqualTo(BigDecimal.valueOf(500000));
        assertThat(result.getBalance()).isEqualTo(BigDecimal.valueOf(0));
    }

    @Test
    @DisplayName("Should convert Card to OrderCardDto")
    void shouldConvertToOrderCardDto() {
        String comment = "Test comment";
        OrderCardDto dto = cardMapper.toOrderCardDto(card, CardReceivingMethod.OFFICE, comment, userId);

        assertThat(dto).isNotNull();
        assertThat(dto.getTraceId()).isNotNull();
        assertThat(dto.getUserData()).isEqualTo(userId);
        assertThat(dto.getOrderType()).isEqualTo(OrderDto.OrderType.CARD);
        assertThat(dto.getCardId()).isEqualTo(card.getId());
        assertThat(dto.getCardType()).isEqualTo(CardType.DEBIT);
        assertThat(dto.getSpendingLimit()).isEqualTo(BigDecimal.valueOf(100000));
        assertThat(dto.getComment()).isEqualTo(comment);
        assertThat(dto.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should convert Card to OrderCardDto with empty comment")
    void shouldConvertToOrderCardDtoWithEmptyComment() {
        OrderCardDto dto = cardMapper.toOrderCardDto(card, "", userId);

        assertThat(dto).isNotNull();
        assertThat(dto.getComment()).isEqualTo("");
        assertThat(dto.getTraceId()).isNotNull();
    }

    @Test
    @DisplayName("Should generate unique traceId for each conversion")
    void shouldGenerateUniqueTraceId() {
        OrderCardDto dto1 = cardMapper.toOrderCardDto(card, "comment1", userId);
        OrderCardDto dto2 = cardMapper.toOrderCardDto(card, "comment2", userId);

        assertThat(dto1.getTraceId()).isNotEqualTo(dto2.getTraceId());
    }
}