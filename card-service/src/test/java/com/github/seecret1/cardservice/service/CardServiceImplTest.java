package com.github.seecret1.cardservice.service;

import com.github.seecret1.cardservice.client.UserServiceClient;
import com.github.seecret1.cardservice.dto.request.CardRequest;
import com.github.seecret1.cardservice.dto.request.UpdateStatusCardRequest;
import com.github.seecret1.cardservice.dto.response.CardResponse;
import com.github.seecret1.cardservice.dto.user.UserResponse;
import com.github.seecret1.cardservice.entity.Card;
import com.github.seecret1.cardservice.entity.enums.CardStatus;
import com.github.seecret1.cardservice.exception.CardAlreadyActivated;
import com.github.seecret1.cardservice.exception.CardExistsException;
import com.github.seecret1.cardservice.exception.CardNotFoundException;
import com.github.seecret1.cardservice.mapper.CardMapper;
import com.github.seecret1.cardservice.repository.CardRepository;
import com.github.seecret1.cardservice.service.impl.CardServiceImpl;
import com.github.seecret1.cardservice.utils.AuthUtils;
import com.github.seecret1.cardservice.utils.CardHashUtils;
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
import org.springframework.dao.DataIntegrityViolationException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    @InjectMocks
    private CardServiceImpl cardService;

    private Card card;
    private CardRequest cardRequest;
    private UserResponse userResponse;
    private CardResponse cardResponse;
    private String cardNumberHash;

    @BeforeEach
    void setUp() {
        card = new Card();
        card.setId("test-id");
        card.setNumber("1234567890123456");
        cardNumberHash = CardHashUtils.hash("1234567890123456");
        card.setNumberHash(cardNumberHash);
        card.setStatus(CardStatus.PENDING);
        card.setBalance(BigDecimal.valueOf(1000));
        card.setDateExpiry(LocalDate.now().plusYears(1));
        card.setDateActivation(LocalDate.now());
        card.setUserId("user-id");
        card.setDeleted(false);

        cardRequest = new CardRequest(
                "1234567890123456",
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                BigDecimal.valueOf(1000),
                "user-email"
        );

        userResponse = new UserResponse(
                "user-id",
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
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                CardStatus.PENDING,
                BigDecimal.valueOf(1000),
                "user-id"
        );
    }

    @Test
    @DisplayName("Should create card successfully")
    void shouldCreateCardSuccessfully() {
        when(userServiceClient.findUserByCriterial(anyString())).thenReturn(userResponse);
        when(cardMapper.toEntity(any(CardRequest.class), anyString())).thenReturn(card);
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardMapper.toDtoResponse(any(Card.class))).thenReturn(cardResponse);

        CardResponse result = cardService.create(cardRequest);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(CardStatus.PENDING);
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found during card creation")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userServiceClient.findUserByCriterial(anyString())).thenReturn(null);

        assertThatThrownBy(() -> cardService.create(cardRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should throw exception when card already exists")
    void shouldThrowExceptionWhenCardExists() {
        when(userServiceClient.findUserByCriterial(anyString())).thenReturn(userResponse);
        when(cardMapper.toEntity(any(CardRequest.class), anyString())).thenReturn(card);
        when(cardRepository.save(any(Card.class))).thenThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> cardService.create(cardRequest))
                .isInstanceOf(CardExistsException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should throw exception when activating already active card")
    void shouldThrowExceptionWhenActivatingActiveCard() {
        card.setStatus(CardStatus.ACTIVE);
        when(cardRepository.findByNumberHash(anyString())).thenReturn(Optional.of(card));
        doNothing().when(authUtils).checkCardAccess(any(Card.class));

        assertThatThrownBy(() -> cardService.activateCard("1234567890123456"))
                .isInstanceOf(CardAlreadyActivated.class)
                .hasMessageContaining("already activated");
    }

    @Test
    @DisplayName("Should update status to BLOCKED successfully")
    void shouldUpdateStatusToBlockedSuccessfully() {
        card.setStatus(CardStatus.ACTIVE);
        UpdateStatusCardRequest request = new UpdateStatusCardRequest("1234567890123456", CardStatus.BLOCKED);

        when(cardRepository.findByNumberHash(anyString())).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardMapper.toDtoResponse(any(Card.class))).thenReturn(cardResponse);

        CardResponse result = cardService.updateStatus(request);

        assertThat(result).isNotNull();
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    @DisplayName("Should throw exception when card not found for status update")
    void shouldThrowExceptionWhenCardNotFoundForStatusUpdate() {
        UpdateStatusCardRequest request = new UpdateStatusCardRequest("1234567890123456", CardStatus.ACTIVE);
        when(cardRepository.findByNumberHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.updateStatus(request))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessageContaining("Card not found");
    }

    @Test
    @DisplayName("Should find cards by user id")
    void shouldFindCardsByUserId() {
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        when(cardRepository.findAllByUserId(anyString(), any(Pageable.class))).thenReturn(cardPage);
        when(cardMapper.toYourDtoResponseList(anyList())).thenReturn(List.of(cardResponse));

        PageResponse<CardResponse> result = cardService.findYourCards("user-id", new PageModel(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
    }

    @Test
    @DisplayName("Should throw exception when no cards found for user")
    void shouldThrowExceptionWhenNoCardsFoundForUser() {
        Page<Card> emptyPage = new PageImpl<>(List.of());
        when(cardRepository.findAllByUserId(anyString(), any(Pageable.class))).thenReturn(emptyPage);

        assertThatThrownBy(() -> cardService.findYourCards("user-id", new PageModel(0, 10)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}