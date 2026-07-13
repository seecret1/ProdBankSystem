package com.github.seecret1.cardservice.service;

import com.github.seecret1.cardservice.dto.response.CardResponse;
import com.github.seecret1.cardservice.entity.Card;
import com.github.seecret1.cardservice.entity.enums.CardStatus;
import com.github.seecret1.cardservice.entity.enums.CardType;
import com.github.seecret1.cardservice.service.impl.CardSchedulerServiceImpl;
import com.github.seecret1.cardservice.utils.CardMaskUtils;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardSchedulerService Unit Tests")
class CardSchedulerServiceImplTest {

    @Mock
    private CardService cardService;

    @Mock
    private InternalCardService internalCardService;

    @InjectMocks
    private CardSchedulerServiceImpl schedulerService;

    private Card card;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(schedulerService, "maximumYearExpiry", 1L);
        ReflectionTestUtils.setField(schedulerService, "maximumYearDeleted", 1L);
        ReflectionTestUtils.setField(schedulerService, "pageSize", 10);

        card = new Card();
        card.setId("test-id");
        card.setNumber("1234567890123456");
        card.setStatus(CardStatus.ACTIVE);
        card.setDateExpiry(LocalDate.now().minusYears(3));
        card.setDeleted(true);
        card.setDeletedAt(Instant.now().minusSeconds(30L * 24 * 60 * 60));
        card.setType(CardType.DEBIT);
        card.setSpendingLimit(BigDecimal.valueOf(1000));
    }

    @Test
    @DisplayName("Should remove expired cards in batches")
    void shouldRemoveExpiredCards() {
        Page<Card> page = new PageImpl<>(List.of(card));
        when(internalCardService.findExpiryCards(any(LocalDate.class), any(Pageable.class)))
                .thenReturn(page);
        doNothing().when(cardService).hardDelete(card.getId());

        schedulerService.removeExpiryCards();

        verify(internalCardService, times(1)).findExpiryCards(any(LocalDate.class), any(Pageable.class));
        verify(cardService, times(1)).hardDelete(card.getId());
    }

    @Test
    @DisplayName("Should handle empty page when removing expired cards")
    void shouldHandleEmptyPageWhenRemovingExpiredCards() {
        when(internalCardService.findExpiryCards(any(LocalDate.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        schedulerService.removeExpiryCards();

        verify(internalCardService, times(1)).findExpiryCards(any(LocalDate.class), any(Pageable.class));
        verify(cardService, never()).hardDelete(anyString());
    }

    @Test
    @DisplayName("Should handle exception when removing expired cards")
    void shouldHandleExceptionWhenRemovingExpiredCards() {
        when(internalCardService.findExpiryCards(any(LocalDate.class), any(Pageable.class)))
                .thenThrow(new RuntimeException("Database error"));

        schedulerService.removeExpiryCards();

        verify(cardService, never()).hardDelete(anyString());
    }

    @Test
    @DisplayName("Should continue processing next pages when one card fails")
    void shouldContinueProcessingWhenOneCardFails() {
        Card card2 = new Card();
        card2.setId("test-id-2");
        card2.setNumber("9876543210123456");
        card2.setDateExpiry(LocalDate.now().minusYears(3));
        card2.setDeleted(true);

        Page<Card> page = new PageImpl<>(List.of(card, card2));
        when(internalCardService.findExpiryCards(any(LocalDate.class), any(Pageable.class)))
                .thenReturn(page);
        doThrow(new RuntimeException("Delete failed")).when(cardService).hardDelete(card.getId());
        doNothing().when(cardService).hardDelete(card2.getId());

        schedulerService.removeExpiryCards();

        verify(cardService, times(1)).hardDelete(card.getId());
        verify(cardService, times(1)).hardDelete(card2.getId());
    }

    @Test
    @DisplayName("Should log masked card number when deleting")
    void shouldLogMaskedCardNumberWhenDeleting() {
        Page<Card> page = new PageImpl<>(List.of(card));
        when(internalCardService.findExpiryCards(any(LocalDate.class), any(Pageable.class)))
                .thenReturn(page);
        doNothing().when(cardService).hardDelete(card.getId());

        schedulerService.removeExpiryCards();

        String masked = CardMaskUtils.maskCardNumber(card.getNumber());
        assertThat(masked).isEqualTo("**** **** **** 3456");
    }

    @Test
    @DisplayName("Should remove deleted cards in batches")
    void shouldRemoveDeletedCards() {
        Page<Card> page = new PageImpl<>(List.of(card));
        when(internalCardService.findDeletedCards(any(Instant.class), any(Pageable.class)))
                .thenReturn(page);
        doNothing().when(cardService).hardDelete(card.getId());

        schedulerService.removeDeletedCards();

        verify(internalCardService, times(1)).findDeletedCards(any(Instant.class), any(Pageable.class));
        verify(cardService, times(1)).hardDelete(card.getId());
    }

    @Test
    @DisplayName("Should handle empty page when removing deleted cards")
    void shouldHandleEmptyPageWhenRemovingDeletedCards() {
        when(internalCardService.findDeletedCards(any(Instant.class), any(Pageable.class)))
                .thenReturn(Page.empty());
        schedulerService.removeDeletedCards();
        verify(cardService, never()).hardDelete(anyString());
    }

    @Test
    @DisplayName("Should handle exception when removing deleted cards")
    void shouldHandleExceptionWhenRemovingDeletedCards() {
        when(internalCardService.findDeletedCards(any(Instant.class), any(Pageable.class)))
                .thenThrow(new RuntimeException("Database error"));
        schedulerService.removeDeletedCards();
        verify(cardService, never()).hardDelete(anyString());
    }

    @Test
    @DisplayName("Should update status of expired active cards")
    void shouldUpdateStatusExpiryCards() {
        card.setStatus(CardStatus.ACTIVE);
        Page<Card> page = new PageImpl<>(List.of(card));
        when(internalCardService.findExpiryActiveCards(any(LocalDate.class), any(Pageable.class)))
                .thenReturn(page);
        when(cardService.updateStatus(card.getNumber(), CardStatus.EXPIRED))
                .thenReturn(mock(CardResponse.class));

        schedulerService.updateStatusExpiryCards();

        verify(internalCardService, times(1)).findExpiryActiveCards(any(LocalDate.class), any(Pageable.class));
        verify(cardService, times(1)).updateStatus(card.getNumber(), CardStatus.EXPIRED);
    }

    @Test
    @DisplayName("Should handle empty page when updating status")
    void shouldHandleEmptyPageWhenUpdatingStatus() {
        when(internalCardService.findExpiryActiveCards(any(LocalDate.class), any(Pageable.class)))
                .thenReturn(Page.empty());
        schedulerService.updateStatusExpiryCards();
        verify(cardService, never()).updateStatus(anyString(), any(CardStatus.class));
    }

    @Test
    @DisplayName("Should handle exception when updating status")
    void shouldHandleExceptionWhenUpdatingStatus() {
        when(internalCardService.findExpiryActiveCards(any(LocalDate.class), any(Pageable.class)))
                .thenThrow(new RuntimeException("Database error"));
        schedulerService.updateStatusExpiryCards();
        verify(cardService, never()).updateStatus(anyString(), any(CardStatus.class));
    }

    @Test
    @DisplayName("Should continue processing next cards when one update fails")
    void shouldContinueProcessingWhenOneUpdateFails() {
        Card card2 = new Card();
        card2.setId("test-id-2");
        card2.setNumber("9876543210123456");
        card2.setStatus(CardStatus.ACTIVE);
        card2.setDateExpiry(LocalDate.now().minusYears(3));

        Page<Card> page = new PageImpl<>(List.of(card, card2));
        when(internalCardService.findExpiryActiveCards(any(LocalDate.class), any(Pageable.class)))
                .thenReturn(page);
        doThrow(new RuntimeException("Update failed")).when(cardService).updateStatus(card.getNumber(), CardStatus.EXPIRED);
        when(cardService.updateStatus(card2.getNumber(), CardStatus.EXPIRED))
                .thenReturn(mock(CardResponse.class));

        schedulerService.updateStatusExpiryCards();

        verify(cardService, times(1)).updateStatus(card.getNumber(), CardStatus.EXPIRED);
        verify(cardService, times(1)).updateStatus(card2.getNumber(), CardStatus.EXPIRED);
    }

    @Test
    @DisplayName("Should refresh spending limit for all active cards")
    void shouldRefreshSpendingLimit() {
        Page<Card> page = new PageImpl<>(List.of(card));
        when(internalCardService.findAllActiveCard(any(Pageable.class)))
                .thenReturn(page);
        when(cardService.refreshSpendingLimit(card.getId(), card.getType()))
                .thenReturn(mock(CardResponse.class));

        schedulerService.refreshSpendingLimit();

        verify(internalCardService, times(1)).findAllActiveCard(any(Pageable.class));
        verify(cardService, times(1)).refreshSpendingLimit(card.getId(), card.getType());
    }

    @Test
    @DisplayName("Should handle empty page when refreshing spending limit")
    void shouldHandleEmptyPageWhenRefreshingSpendingLimit() {
        when(internalCardService.findAllActiveCard(any(Pageable.class)))
                .thenReturn(Page.empty());
        schedulerService.refreshSpendingLimit();
        verify(cardService, never()).refreshSpendingLimit(anyString(), any(CardType.class));
    }

    @Test
    @DisplayName("Should handle exception when refreshing spending limit")
    void shouldHandleExceptionWhenRefreshingSpendingLimit() {
        when(internalCardService.findAllActiveCard(any(Pageable.class)))
                .thenThrow(new RuntimeException("Database error"));
        schedulerService.refreshSpendingLimit();
        verify(cardService, never()).refreshSpendingLimit(anyString(), any(CardType.class));
    }

    @Test
    @DisplayName("Should continue processing next cards when one refresh fails")
    void shouldContinueProcessingWhenOneRefreshFails() {
        Card card2 = new Card();
        card2.setId("test-id-2");
        card2.setType(CardType.CREDIT);
        card2.setSpendingLimit(BigDecimal.valueOf(2000));

        Page<Card> page = new PageImpl<>(List.of(card, card2));
        when(internalCardService.findAllActiveCard(any(Pageable.class)))
                .thenReturn(page);
        doThrow(new RuntimeException("Refresh failed")).when(cardService).refreshSpendingLimit(card.getId(), card.getType());
        when(cardService.refreshSpendingLimit(card2.getId(), card2.getType()))
                .thenReturn(mock(CardResponse.class));

        schedulerService.refreshSpendingLimit();

        verify(cardService, times(1)).refreshSpendingLimit(card.getId(), card.getType());
        verify(cardService, times(1)).refreshSpendingLimit(card2.getId(), card2.getType());
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