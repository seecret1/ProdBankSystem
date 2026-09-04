package com.github.seecret1.delivery_service.service.impl;

import com.github.seecret1.delivery_service.dto.user.RecipientDto;
import com.github.seecret1.delivery_service.entity.Recipient;
import com.github.seecret1.delivery_service.exception.RecipientUpdateException;
import com.github.seecret1.delivery_service.mapper.RecipientMapper;
import com.github.seecret1.delivery_service.repository.RecipientRepository;
import com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecipientService Unit Tests")
class RecipientServiceImplTest {

    @Mock
    private RecipientRepository recipientRepository;

    @Mock
    private RecipientMapper recipientMapper;

    @InjectMocks
    private RecipientServiceImpl recipientService;

    private RecipientDto recipientDto;
    private Recipient recipient;

    @BeforeEach
    void setUp() {
        recipientDto = DeliveryTestDataFactory.defaultRecipientDto();
        recipient = DeliveryTestDataFactory.defaultRecipient();
    }

    @Test
    @DisplayName("Should create new recipient")
    void shouldCreateNewRecipient() {
        when(recipientMapper.toEntity(recipientDto)).thenReturn(recipient);
        when(recipientRepository.save(recipient)).thenReturn(recipient);

        Recipient result = recipientService.createNewRecipient(recipientDto);

        assertThat(result).isEqualTo(recipient);
        verify(recipientMapper).toEntity(recipientDto);
        verify(recipientRepository).save(recipient);
        verify(recipientRepository, never()).update(recipient);
    }

    @Test
    @DisplayName("Should update recipient by userId")
    void shouldUpdateRecipientByUserId() {
        Recipient updatedRecipient = DeliveryTestDataFactory.defaultRecipient();
        updatedRecipient.setContactPhone("+79990001122");

        when(recipientMapper.toEntity(recipientDto)).thenReturn(recipient);
        when(recipientRepository.findByUserId(USER_ID)).thenReturn(Optional.of(updatedRecipient));

        Recipient result = recipientService.updateByUserId(recipientDto);

        assertThat(result).isEqualTo(updatedRecipient);
        verify(recipientRepository).update(recipient);
        verify(recipientRepository).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("Should throw RecipientUpdateException when recipient not found after update")
    void shouldThrowWhenRecipientNotFoundAfterUpdate() {
        when(recipientMapper.toEntity(recipientDto)).thenReturn(recipient);
        when(recipientRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipientService.updateByUserId(recipientDto))
                .isInstanceOf(RecipientUpdateException.class)
                .hasMessageContaining("Recipient by userId: " + USER_ID + " not found");

        verify(recipientRepository).update(recipient);
    }

    @Test
    @DisplayName("Should map dto to entity before save on create")
    void shouldMapDtoBeforeSaveOnCreate() {
        when(recipientMapper.toEntity(recipientDto)).thenReturn(recipient);
        when(recipientRepository.save(recipient)).thenReturn(recipient);

        recipientService.createNewRecipient(recipientDto);

        verify(recipientMapper).toEntity(recipientDto);
    }

    @Test
    @DisplayName("Should map dto to entity before update")
    void shouldMapDtoBeforeUpdate() {
        when(recipientMapper.toEntity(recipientDto)).thenReturn(recipient);
        when(recipientRepository.findByUserId(USER_ID)).thenReturn(Optional.of(recipient));

        recipientService.updateByUserId(recipientDto);

        verify(recipientMapper).toEntity(recipientDto);
        verify(recipientRepository).update(recipient);
    }
}
