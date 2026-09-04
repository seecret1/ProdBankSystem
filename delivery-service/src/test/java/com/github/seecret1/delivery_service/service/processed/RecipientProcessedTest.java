package com.github.seecret1.delivery_service.service.processed;

import com.github.seecret1.delivery_service.dto.order.OrderCardDeliveryDto;
import com.github.seecret1.delivery_service.entity.Recipient;
import com.github.seecret1.delivery_service.entity.enums.PersonType;
import com.github.seecret1.delivery_service.repository.RecipientRepository;
import com.github.seecret1.delivery_service.service.RecipientService;
import com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecipientProcessed Unit Tests")
class RecipientProcessedTest {

    @Mock
    private RecipientRepository recipientRepository;

    @Mock
    private RecipientService recipientService;

    @InjectMocks
    private RecipientProcessed recipientProcessed;

    private OrderCardDeliveryDto orderDto;
    private Recipient existingRecipient;
    private Recipient createdRecipient;
    private Recipient updatedRecipient;

    @BeforeEach
    void setUp() {
        orderDto = DeliveryTestDataFactory.validOrderCardDeliveryDto();
        existingRecipient = DeliveryTestDataFactory.defaultRecipient();
        createdRecipient = DeliveryTestDataFactory.defaultRecipient();
        updatedRecipient = DeliveryTestDataFactory.defaultRecipient();
        updatedRecipient.setContactPhone("+79990001122");
    }

    @Test
    @DisplayName("Should create new recipient when not found")
    void shouldCreateNewRecipientWhenNotFound() {
        when(recipientRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(recipientService.createNewRecipient(any())).thenReturn(createdRecipient);

        Recipient result = recipientProcessed.processDelivery(orderDto);

        assertThat(result).isEqualTo(createdRecipient);
        verify(recipientService).createNewRecipient(any());
        verify(recipientService, never()).updateByUserId(any());
    }

    @Test
    @DisplayName("Should reuse existing recipient when data unchanged")
    void shouldReuseExistingRecipientWhenDataUnchanged() {
        when(recipientRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingRecipient));

        Recipient result = recipientProcessed.processDelivery(orderDto);

        assertThat(result).isEqualTo(existingRecipient);
        verify(recipientService, never()).createNewRecipient(any());
        verify(recipientService, never()).updateByUserId(any());
    }

    @Test
    @DisplayName("Should update recipient when first name changed")
    void shouldUpdateWhenFirstNameChanged() {
        existingRecipient.getFullName().setFirstName("Petr");
        when(recipientRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingRecipient));
        when(recipientService.updateByUserId(any())).thenReturn(updatedRecipient);

        Recipient result = recipientProcessed.processDelivery(orderDto);

        assertThat(result).isEqualTo(updatedRecipient);
        verify(recipientService).updateByUserId(any());
        verify(recipientService, never()).createNewRecipient(any());
    }

    @Test
    @DisplayName("Should update recipient when last name changed")
    void shouldUpdateWhenLastNameChanged() {
        existingRecipient.getFullName().setLastName("Ivanov");
        when(recipientRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingRecipient));
        when(recipientService.updateByUserId(any())).thenReturn(updatedRecipient);

        Recipient result = recipientProcessed.processDelivery(orderDto);

        assertThat(result).isEqualTo(updatedRecipient);
        verify(recipientService).updateByUserId(any());
    }

    @Test
    @DisplayName("Should update recipient when middle name changed")
    void shouldUpdateWhenMiddleNameChanged() {
        existingRecipient.getFullName().setMiddleName("Alexeevich");
        when(recipientRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingRecipient));
        when(recipientService.updateByUserId(any())).thenReturn(updatedRecipient);

        Recipient result = recipientProcessed.processDelivery(orderDto);

        assertThat(result).isEqualTo(updatedRecipient);
        verify(recipientService).updateByUserId(any());
    }

    @Test
    @DisplayName("Should update recipient when contact phone changed")
    void shouldUpdateWhenContactPhoneChanged() {
        existingRecipient.setContactPhone("+79991112233");
        when(recipientRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingRecipient));
        when(recipientService.updateByUserId(any())).thenReturn(updatedRecipient);

        Recipient result = recipientProcessed.processDelivery(orderDto);

        assertThat(result).isEqualTo(updatedRecipient);
        verify(recipientService).updateByUserId(any());
    }

    @Test
    @DisplayName("Should update recipient when office id changed")
    void shouldUpdateWhenOfficeIdChanged() {
        existingRecipient.setOfficeId("office-999");
        when(recipientRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingRecipient));
        when(recipientService.updateByUserId(any())).thenReturn(updatedRecipient);

        Recipient result = recipientProcessed.processDelivery(orderDto);

        assertThat(result).isEqualTo(updatedRecipient);
        verify(recipientService).updateByUserId(any());
    }

    @Test
    @DisplayName("Should update recipient when person type changed")
    void shouldUpdateWhenPersonTypeChanged() {
        existingRecipient.setPersonType(PersonType.LEGAL);
        when(recipientRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingRecipient));
        when(recipientService.updateByUserId(any())).thenReturn(updatedRecipient);

        Recipient result = recipientProcessed.processDelivery(orderDto);

        assertThat(result).isEqualTo(updatedRecipient);
        verify(recipientService).updateByUserId(any());
    }

    @Test
    @DisplayName("Should not update when middle name is null in both dto and entity")
    void shouldNotUpdateWhenMiddleNameNullInBoth() {
        existingRecipient.getFullName().setMiddleName(null);
        orderDto.getFullName().setMiddleName(null);
        when(recipientRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingRecipient));

        Recipient result = recipientProcessed.processDelivery(orderDto);

        assertThat(result).isEqualTo(existingRecipient);
        verify(recipientService, never()).updateByUserId(any());
    }

    @Test
    @DisplayName("Should update when middle name changed from null to value")
    void shouldUpdateWhenMiddleNameChangedFromNullToValue() {
        existingRecipient.getFullName().setMiddleName(null);
        orderDto.getFullName().setMiddleName("NewMiddle");
        when(recipientRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingRecipient));
        when(recipientService.updateByUserId(any())).thenReturn(updatedRecipient);

        Recipient result = recipientProcessed.processDelivery(orderDto);

        assertThat(result).isEqualTo(updatedRecipient);
        verify(recipientService).updateByUserId(any());
    }

    @ParameterizedTest(name = "Should update when {0} changes")
    @MethodSource("recipientChangeScenarios")
    @DisplayName("Should update recipient for various field changes")
    void shouldUpdateForVariousFieldChanges(String scenario, Consumer<Recipient> modifier) {
        modifier.accept(existingRecipient);
        when(recipientRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingRecipient));
        when(recipientService.updateByUserId(any())).thenReturn(updatedRecipient);

        Recipient result = recipientProcessed.processDelivery(orderDto);

        assertThat(result).isEqualTo(updatedRecipient);
        verify(recipientService).updateByUserId(any());
    }

    private static Stream<Arguments> recipientChangeScenarios() {
        return Stream.of(
                Arguments.of("officeId to null", (Consumer<Recipient>) r -> r.setOfficeId(null)),
                Arguments.of("officeId from null", (Consumer<Recipient>) r -> {
                    r.setOfficeId(null);
                    DeliveryTestDataFactory.validOrderCardDeliveryDto().getOfficeId();
                })
        );
    }
}
