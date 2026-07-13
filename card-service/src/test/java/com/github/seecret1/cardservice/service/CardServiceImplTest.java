package com.github.seecret1.cardservice.service;

import com.github.seecret1.cardservice.client.UserServiceClient;
import com.github.seecret1.cardservice.config.CardSpendingLimitsConfig;
import com.github.seecret1.cardservice.dto.request.CardRequest;
import com.github.seecret1.cardservice.dto.response.CardResponse;
import com.github.seecret1.cardservice.dto.user.UserResponse;
import com.github.seecret1.cardservice.entity.Card;
import com.github.seecret1.cardservice.entity.enums.CardStatus;
import com.github.seecret1.cardservice.entity.enums.CardType;
import com.github.seecret1.cardservice.exception.*;
import com.github.seecret1.cardservice.kafka.service.OrderKafkaProducerService;
import com.github.seecret1.cardservice.mapper.CardMapper;
import com.github.seecret1.cardservice.model.CardFilterModel;
import com.github.seecret1.cardservice.repository.CardRepository;
import com.github.seecret1.cardservice.service.impl.CardServiceImpl;
import com.github.seecret1.cardservice.utils.AuthUtils;
import com.github.seecret1.cardservice.utils.CardHashUtils;
import com.github.seecret1.cardservice.utils.CardMaskUtils;
import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardService Unit Tests")
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private AuthUtils authUtils;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private OrderKafkaProducerService orderKafkaProducerService;

    @Mock
    private CardSpendingLimitsConfig spendingLimitsConfig;

    @InjectMocks
    private CardServiceImpl cardService;

    private Card card;
    private CardRequest cardRequest;
    private UserResponse userResponse;
    private CardResponse cardResponse;
    private String cardNumberHash;
    private final String userId = "user-id";
    private final String cardId = "test-id";
    private final String cardNumber = "1234567890123456";

    @BeforeEach
    void setUp() {
        card = new Card();
        card.setId(cardId);
        card.setNumber(cardNumber);
        cardNumberHash = CardHashUtils.hash(cardNumber);
        card.setNumberHash(cardNumberHash);
        card.setStatus(CardStatus.PENDING);
        card.setBalance(BigDecimal.valueOf(1000));
        card.setDateExpiry(LocalDate.now().plusYears(1));
        card.setDateActivation(LocalDate.now());
        card.setUserId(userId);
        card.setDeleted(false);

        cardRequest = new CardRequest(
                cardNumber,
                CardType.DEBIT,
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(100000),
                "empty comment"
        );

        userResponse = new UserResponse(
                userId,
                "username",
                "ACTIVE",
                "user@email.com",
                "First",
                "Last",
                "Middle",
                LocalDate.now().minusYears(20),
                "ROLE_USER",
                Instant.now(),
                Instant.now(),
                false,
                null,
                null
        );

        cardResponse = new CardResponse(
                "**** **** **** 3456",
                CardType.DEBIT,
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                CardStatus.PENDING,
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(100000),
                userId
        );
    }

    @Test
    @DisplayName("Should find all cards with pagination")
    void shouldFindAllCards() {
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        when(cardRepository.findAll(any(Pageable.class))).thenReturn(cardPage);
        when(cardMapper.toDtoResponseList(anyList())).thenReturn(List.of(cardResponse));

        PageResponse<CardResponse> result = cardService.findAll(new PageModel(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(cardRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should find only not deleted cards")
    void shouldFindOnlyNotDeletedCards() {
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        when(cardRepository.findNotDeletedCards(any(Pageable.class))).thenReturn(cardPage);
        when(cardMapper.toDtoResponseList(anyList())).thenReturn(List.of(cardResponse));

        PageResponse<CardResponse> result = cardService.findOnlyNotDeleted(new PageModel(0, 10));

        assertThat(result.getData()).hasSize(1);
        verify(cardRepository).findNotDeletedCards(any(Pageable.class));
    }

    @Test
    @DisplayName("Should find cards by filter")
    void shouldFindCardsByFilter() {
        CardFilterModel filter = CardFilterModel.builder()
                .status(CardStatus.PENDING)
                .page(new PageModel(0, 10))
                .build();
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        when(cardRepository.findNotDeletedCards(any(), any(Pageable.class))).thenReturn(cardPage);
        when(cardMapper.toDtoResponseList(anyList())).thenReturn(List.of(cardResponse));

        PageResponse<CardResponse> result = cardService.findByFilter(filter);

        assertThat(result.getData()).hasSize(1);
        verify(cardRepository).findNotDeletedCards(any(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should find card by id when user has access")
    void shouldFindCardById() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        doNothing().when(authUtils).checkCardAccess(card);
        when(cardMapper.toYourDtoResponse(card)).thenReturn(cardResponse);

        CardResponse result = cardService.findById(cardId);

        assertThat(result).isEqualTo(cardResponse);
        verify(authUtils).checkCardAccess(card);
    }

    @Test
    @DisplayName("Should throw CardNotFoundException when card not found by id")
    void shouldThrowExceptionWhenCardNotFoundById() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.findById(cardId))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessageContaining("Card not found by id: " + cardId);
    }

    @Test
    @DisplayName("Should find card by id even if deleted or not")
    void shouldFindCardByIdDeletedOrNot() {
        when(cardRepository.findByIdDeletedOrNot(cardId)).thenReturn(Optional.of(card));
        when(cardMapper.toYourDtoResponse(card)).thenReturn(cardResponse);

        CardResponse result = cardService.findByIdDeletedOrNot(cardId);

        assertThat(result).isEqualTo(cardResponse);
    }

    @Test
    @DisplayName("Should throw CardDeletedException when card is deleted")
    void shouldThrowExceptionWhenCardIsDeleted() {
        card.setDeleted(true);
        card.setDeletedBy("admin");
        when(cardRepository.findByIdDeletedOrNot(cardId)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.findByIdDeletedOrNot(cardId))
                .isInstanceOf(CardDeletedException.class)
                .hasMessageContaining("Card already deleted");
    }

    @Test
    @DisplayName("Should find card by number")
    void shouldFindCardByNumber() {
        when(cardRepository.findByNumberHash(cardNumberHash)).thenReturn(Optional.of(card));
        doNothing().when(authUtils).checkCardAccess(card);
        when(cardMapper.toYourDtoResponse(card)).thenReturn(cardResponse);

        CardResponse result = cardService.findByNumber(cardNumber);

        assertThat(result).isEqualTo(cardResponse);
        verify(authUtils).checkCardAccess(card);
    }

    @Test
    @DisplayName("Should throw CardNotFoundException when card not found by number")
    void shouldThrowExceptionWhenCardNotFoundByNumber() {
        when(cardRepository.findByNumberHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.findByNumber(cardNumber))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessageContaining("Card not found by number");
    }

    @Test
    @DisplayName("Should find cards by user id")
    void shouldFindCardsByUserId() {
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        when(cardRepository.findAllByUserId(eq(userId), any(Pageable.class))).thenReturn(cardPage);
        when(cardMapper.toYourDtoResponseList(anyList())).thenReturn(List.of(cardResponse));

        PageResponse<CardResponse> result = cardService.findYourCards(userId, new PageModel(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no cards found for user")
    void shouldThrowExceptionWhenNoCardsFoundForUser() {
        Page<Card> emptyPage = new PageImpl<>(List.of());
        when(cardRepository.findAllByUserId(eq(userId), any(Pageable.class))).thenReturn(emptyPage);

        assertThatThrownBy(() -> cardService.findYourCards(userId, new PageModel(0, 10)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should create card successfully")
    void shouldCreateCardSuccessfully() {
        when(userServiceClient.findUserById(userId)).thenReturn(userResponse);
        when(cardRepository.findByNumberHash(cardNumberHash)).thenReturn(Optional.empty());
        when(cardMapper.toEntity(any(CardRequest.class), anyString())).thenReturn(card);
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardMapper.toDtoResponse(any(Card.class))).thenReturn(cardResponse);

        CardResponse result = cardService.create(userId, cardRequest);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(CardStatus.PENDING);
        verify(cardRepository, times(1)).save(any(Card.class));
        verify(orderKafkaProducerService, times(1)).sendNoWait(any());
    }

    @Test
    @DisplayName("Should throw exception when user not found during card creation")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userServiceClient.findUserById(userId)).thenReturn(null);

        assertThatThrownBy(() -> cardService.create(userId, cardRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found by id " + userId);
    }

    @Test
    @DisplayName("Should throw exception when card already exists")
    void shouldThrowExceptionWhenCardExists() {
        when(userServiceClient.findUserById(userId)).thenReturn(userResponse);
        when(cardRepository.findByNumberHash(cardNumberHash)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.create(userId, cardRequest))
                .isInstanceOf(CardExistsException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should throw exception when date expiry is before now")
    void shouldThrowExceptionWhenDateExpiryBeforeNow() {
        CardRequest invalidRequest = new CardRequest(
                cardNumber,
                CardType.DEBIT,
                LocalDate.now(),
                LocalDate.now().minusDays(1),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(100000),
                "comment"
        );
        when(userServiceClient.findUserById(userId)).thenReturn(userResponse);
        when(cardRepository.findByNumberHash(cardNumberHash)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.create(userId, invalidRequest))
                .isInstanceOf(CardExpiryDateException.class)
                .hasMessageContaining("Date expiry is before now");
    }

    @Test
    @DisplayName("Should activate PENDING card")
    void shouldActivatePendingCard() {
        card.setStatus(CardStatus.PENDING);
        when(cardRepository.findByIdUseLock(cardId)).thenReturn(Optional.of(card));
        when(cardMapper.toYourDtoResponse(card)).thenReturn(cardResponse);

        CardResponse result = cardService.activateCard(cardId);

        assertThat(result).isNotNull();
        assertThat(card.getStatus()).isEqualTo(CardStatus.ACTIVE);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @DisplayName("Should return same card if already ACTIVE")
    void shouldReturnSameCardIfAlreadyActive() {
        card.setStatus(CardStatus.ACTIVE);
        when(cardRepository.findByIdUseLock(cardId)).thenReturn(Optional.of(card));
        when(cardMapper.toYourDtoResponse(card)).thenReturn(cardResponse);

        CardResponse result = cardService.activateCard(cardId);

        assertThat(result).isEqualTo(cardResponse);
        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw CardException when cannot activate")
    void shouldThrowExceptionWhenCannotActivate() {
        card.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findByIdUseLock(cardId)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.activateCard(cardId))
                .isInstanceOf(CardException.class)
                .hasMessageContaining("Card cannot be activated");
    }

    @Test
    @DisplayName("Should update status to BLOCKED successfully")
    void shouldUpdateStatusToBlockedSuccessfully() {
        card.setStatus(CardStatus.ACTIVE);
        CardStatus newStatus = CardStatus.BLOCKED;
        when(cardRepository.findByIdUseLock(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenReturn(card);
        when(cardMapper.toDtoResponse(card)).thenReturn(cardResponse);

        CardResponse result = cardService.updateStatus(cardId, newStatus);

        assertThat(result).isNotNull();
        assertThat(card.getStatus()).isEqualTo(newStatus);
        verify(cardRepository).save(card);
    }

    @Test
    @DisplayName("Should throw exception when card not found for status update")
    void shouldThrowExceptionWhenCardNotFoundForStatusUpdate() {
        when(cardRepository.findByIdUseLock(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.updateStatus(cardId, CardStatus.ACTIVE))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessageContaining("Card not found by ID: " + cardId);
    }

    @Test
    @DisplayName("Should throw CardStatusUpdateException when trying to activate expired card")
    void shouldThrowExceptionWhenActivatingExpiredCard() {
        card.setStatus(CardStatus.EXPIRED);
        card.setDateExpiry(LocalDate.now().minusDays(1));
        when(cardRepository.findByIdUseLock(cardId)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.updateStatus(cardId, CardStatus.ACTIVE))
                .isInstanceOf(CardStatusUpdateException.class)
                .hasMessageContaining("The card status cannot be active");
    }

    @Test
    @DisplayName("Should extend card validity")
    void shouldExtendCardSuccessfully() {
        card.setStatus(CardStatus.ACTIVE);
        LocalDate newExpiry = LocalDate.now().plusYears(2);
        when(cardRepository.findByIdUseLock(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);
        when(cardMapper.toDtoResponse(card)).thenReturn(cardResponse);

        CardResponse result = cardService.extendCard(cardId, newExpiry);

        assertThat(result).isNotNull();
        assertThat(card.getDateExpiry()).isEqualTo(newExpiry);
        assertThat(card.getStatus()).isEqualTo(CardStatus.EXTENDED);
        verify(cardRepository).save(card);
    }

    @Test
    @DisplayName("Should throw CardStatusUpdateException when extending card with invalid status")
    void shouldThrowExceptionWhenExtendingInvalidStatus() {
        card.setStatus(CardStatus.BLOCKED);
        card.setBalance(BigDecimal.valueOf(100));
        when(cardRepository.findByIdUseLock(cardId)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.extendCard(cardId, LocalDate.now().plusYears(1)))
                .isInstanceOf(CardStatusUpdateException.class)
                .hasMessageContaining("The card status cannot be EXTENDED");
    }

    @Test
    @DisplayName("Should throw CardStatusUpdateException when extending with same expiry date")
    void shouldThrowExceptionWhenExtendingWithSameExpiry() {
        card.setStatus(CardStatus.ACTIVE);
        LocalDate currentExpiry = card.getDateExpiry();
        when(cardRepository.findByIdUseLock(cardId)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.extendCard(cardId, currentExpiry))
                .isInstanceOf(CardStatusUpdateException.class)
                .hasMessageContaining("The card status cannot be EXTENDED");
    }

    @Test
    @DisplayName("Should refresh spending limit for card")
    void shouldRefreshSpendingLimit() {
        BigDecimal newLimit = BigDecimal.valueOf(50000);
        when(cardRepository.findByIdUseLock(cardId)).thenReturn(Optional.of(card));
        when(spendingLimitsConfig.getMaxLimitForType(card.getType())).thenReturn(newLimit);
        when(cardRepository.save(card)).thenReturn(card);
        when(cardMapper.toDtoResponse(card)).thenReturn(cardResponse);

        CardResponse result = cardService.refreshSpendingLimit(cardId, card.getType());

        assertThat(result).isNotNull();
        assertThat(card.getSpendingLimit()).isEqualTo(newLimit);
        verify(cardRepository).save(card);
    }

    @Test
    @DisplayName("Should soft delete card")
    void shouldSoftDeleteCard() {
        when(cardRepository.findByIdUseLock(cardId)).thenReturn(Optional.of(card));
        when(userServiceClient.findUserById(userId)).thenReturn(userResponse);
        when(cardRepository.save(card)).thenReturn(card);

        cardService.softDelete(userId, cardId);

        assertThat(card.getDeleted()).isTrue();
        assertThat(card.getDeletedBy()).isEqualTo(userResponse.username());
        verify(cardRepository).save(card);
    }

    @Test
    @DisplayName("Should throw CardNotFoundException when soft deleting non-existing card")
    void shouldThrowExceptionWhenSoftDeleteCardNotFound() {
        when(cardRepository.findByIdUseLock(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.softDelete(userId, cardId))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    @DisplayName("Should hard delete card")
    void shouldHardDeleteCard() {
        when(cardRepository.findByIdDeletedOrNot(cardId)).thenReturn(Optional.of(card));

        cardService.hardDelete(cardId);

        verify(cardRepository).delete(card);
    }

    @Test
    @DisplayName("Should throw CardNotFoundException when hard deleting non-existing card")
    void shouldThrowExceptionWhenHardDeleteCardNotFound() {
        when(cardRepository.findByIdDeletedOrNot(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.hardDelete(cardId))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    @DisplayName("Should use CardMaskUtils for logging masked card numbers")
    void shouldUseCardMaskUtilsForLogging() {
        String number = "1234567890123456";
        String masked = CardMaskUtils.maskCardNumber(number);

        assertThat(masked).isEqualTo("**** **** **** 3456");
        assertThat(masked).doesNotContain("123456789012");
    }

    @Test
    @DisplayName("Should handle null card number in CardMaskUtils")
    void shouldHandleNullCardNumberInMaskUtils() {
        String masked = CardMaskUtils.maskCardNumber(null);
        assertThat(masked).isNull();
    }

    @Test
    @DisplayName("Should handle short card number in CardMaskUtils")
    void shouldHandleShortCardNumberInMaskUtils() {
        String masked = CardMaskUtils.maskCardNumber("123");
        assertThat(masked).isEqualTo("123");
    }
}