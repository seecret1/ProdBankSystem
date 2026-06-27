package com.github.seecret1.cardservice.service;

import com.github.seecret1.cardservice.entity.Card;
import com.github.seecret1.cardservice.entity.enums.CardStatus;
import com.github.seecret1.cardservice.repository.CardRepository;
import com.github.seecret1.cardservice.service.impl.CardSchedulerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardSchedulerService Unit Tests")
class CardSchedulerServiceImplTest {

    @Mock
    private CardService cardService;

    @Mock
    private InternalCardService internalCardService;

    @Mock
    private CardRepository cardRepository;

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
        card.setStatus(CardStatus.EXPIRED);
        card.setDateExpiry(LocalDate.now().minusYears(3));
        card.setDeleted(true);
        card.setDeletedAt(Instant.now().minusSeconds(30 * 24 * 60 * 60));
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
    @DisplayName("Should handle empty page when removing deleted cards")
    void shouldHandleEmptyPageWhenRemovingDeletedCards() {
        when(internalCardService.findDeletedCards(any(Instant.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        schedulerService.removeDeletedCards();

        verify(internalCardService, times(1)).findDeletedCards(any(Instant.class), any(Pageable.class));
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
    @DisplayName("Should handle empty page when updating status")
    void shouldHandleEmptyPageWhenUpdatingStatus() {
        when(internalCardService.findExpiryActiveCards(any(LocalDate.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        schedulerService.updateStatusExpiryCards();

        verify(internalCardService, times(1)).findExpiryActiveCards(any(LocalDate.class), any(Pageable.class));
        verify(cardService, never()).updateStatus(any());
    }

    @Test
    @DisplayName("Should handle exception when updating status")
    void shouldHandleExceptionWhenUpdatingStatus() {
        when(internalCardService.findExpiryActiveCards(any(LocalDate.class), any(Pageable.class)))
                .thenThrow(new RuntimeException("Database error"));

        schedulerService.updateStatusExpiryCards();
        verify(cardService, never()).updateStatus(any());
    }
}