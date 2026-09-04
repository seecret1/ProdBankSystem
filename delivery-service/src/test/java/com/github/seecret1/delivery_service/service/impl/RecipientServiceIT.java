package com.github.seecret1.delivery_service.service.impl;

import com.github.seecret1.delivery_service.SpringBootApplicationTest;
import com.github.seecret1.delivery_service.dto.user.RecipientDto;
import com.github.seecret1.delivery_service.entity.Recipient;
import com.github.seecret1.delivery_service.exception.RecipientUpdateException;
import com.github.seecret1.delivery_service.repository.RecipientRepository;
import com.github.seecret1.delivery_service.service.RecipientService;
import com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RecipientService Integration Tests")
class RecipientServiceIT extends SpringBootApplicationTest {

    @Autowired
    private RecipientService recipientService;

    @Autowired
    private RecipientRepository recipientRepository;

    @Test
    @DisplayName("Should create new recipient with all fields")
    void shouldCreateNewRecipient() {
        RecipientDto recipientDto = DeliveryTestDataFactory.defaultRecipientDto();

        Recipient result = recipientService.createNewRecipient(recipientDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getUserId()).isEqualTo(recipientDto.getUserId());
        assertThat(result.getContactPhone()).isEqualTo(recipientDto.getContactPhone());
        assertThat(result.getOfficeId()).isEqualTo(recipientDto.getOfficeId());
        assertThat(result.getPersonType()).isEqualTo(recipientDto.getPersonType());
        assertThat(result.getDeleted()).isFalse();

        Recipient savedRecipient = recipientRepository.findByUserId(recipientDto.getUserId()).orElseThrow();
        assertThat(savedRecipient.getId()).isEqualTo(result.getId());
    }

    @Test
    @DisplayName("Should create multiple recipients with different user IDs")
    void shouldCreateMultipleRecipients() {
        RecipientDto recipient1 = RecipientDto.builder()
                .userId("user-1")
                .fullName(DeliveryTestDataFactory.defaultFullNameDto())
                .contactPhone("+79991111111")
                .officeId("office-001")
                .personType(com.github.seecret1.delivery_service.entity.enums.PersonType.PHYSICAL)
                .build();

        RecipientDto recipient2 = RecipientDto.builder()
                .userId("user-2")
                .fullName(DeliveryTestDataFactory.defaultFullNameDto())
                .contactPhone("+79992222222")
                .officeId("office-002")
                .personType(com.github.seecret1.delivery_service.entity.enums.PersonType.PHYSICAL)
                .build();

        Recipient result1 = recipientService.createNewRecipient(recipient1);
        Recipient result2 = recipientService.createNewRecipient(recipient2);

        assertThat(result1.getId()).isNotEqualTo(result2.getId());
        assertThat(result1.getUserId()).isEqualTo("user-1");
        assertThat(result2.getUserId()).isEqualTo("user-2");

        assertThat(recipientRepository.findByUserId("user-1")).isPresent();
        assertThat(recipientRepository.findByUserId("user-2")).isPresent();
    }

    @Test
    @DisplayName("Should update existing recipient by user ID")
    void shouldUpdateRecipientByUserId() {
        RecipientDto originalDto = DeliveryTestDataFactory.defaultRecipientDto();
        Recipient created = recipientService.createNewRecipient(originalDto);
        assertThat(created).isNotNull();

        String updatedPhone = "+79991234999";
        RecipientDto updateDto = RecipientDto.builder()
                .userId(DeliveryTestDataFactory.USER_ID)
                .fullName(DeliveryTestDataFactory.defaultFullNameDto())
                .contactPhone(updatedPhone)
                .officeId("office-updated")
                .personType(com.github.seecret1.delivery_service.entity.enums.PersonType.PHYSICAL)
                .build();

        Recipient result = recipientService.updateByUserId(updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(DeliveryTestDataFactory.USER_ID);
        assertThat(result.getContactPhone()).isEqualTo(updatedPhone);
        assertThat(result.getOfficeId()).isEqualTo("office-updated");
    }

    @Test
    @DisplayName("Should throw RecipientUpdateException when updating non-existent recipient")
    void shouldThrowExceptionWhenUpdatingNonExistentRecipient() {
        RecipientDto updateDto = RecipientDto.builder()
                .userId("non-existent-user-id")
                .fullName(DeliveryTestDataFactory.defaultFullNameDto())
                .contactPhone("+79991234567")
                .officeId("office-001")
                .personType(com.github.seecret1.delivery_service.entity.enums.PersonType.PHYSICAL)
                .build();

        assertThatThrownBy(() -> recipientService.updateByUserId(updateDto))
                .isInstanceOf(RecipientUpdateException.class)
                .hasMessageContaining("Recipient by userId: non-existent-user-id not found");
    }

    @Test
    @DisplayName("Should preserve recipient data after transactional boundary")
    void shouldPreserveRecipientDataAfterTransaction() {
        RecipientDto recipientDto = DeliveryTestDataFactory.defaultRecipientDto();
        Recipient created = recipientService.createNewRecipient(recipientDto);
        Long recipientId = created.getId();

        Recipient fetched = recipientRepository.findById(recipientId).orElseThrow();

        assertThat(fetched).isNotNull();
        assertThat(fetched.getId()).isEqualTo(recipientId);
        assertThat(fetched.getUserId()).isEqualTo(recipientDto.getUserId());
        assertThat(fetched.getContactPhone()).isEqualTo(recipientDto.getContactPhone());
    }

    @Test
    @DisplayName("Should find recipient by userId correctly")
    void shouldFindRecipientByUserId() {
        String testUserId = "test-user-find-12345";
        RecipientDto recipientDto = RecipientDto.builder()
                .userId(testUserId)
                .fullName(DeliveryTestDataFactory.defaultFullNameDto())
                .contactPhone("+79991234567")
                .officeId("office-001")
                .personType(com.github.seecret1.delivery_service.entity.enums.PersonType.PHYSICAL)
                .build();

        recipientService.createNewRecipient(recipientDto);

        Recipient found = recipientRepository.findByUserId(testUserId).orElseThrow();

        assertThat(found).isNotNull();
        assertThat(found.getUserId()).isEqualTo(testUserId);
        assertThat(found.getContactPhone()).isEqualTo("+79991234567");
    }

    @Test
    @DisplayName("Should update recipient multiple times atomically")
    void shouldUpdateRecipientMultipleTimes() {
        RecipientDto initialDto = DeliveryTestDataFactory.defaultRecipientDto();
        recipientService.createNewRecipient(initialDto);

        for (int i = 1; i <= 3; i++) {
            RecipientDto updateDto = RecipientDto.builder()
                    .userId(DeliveryTestDataFactory.USER_ID)
                    .fullName(DeliveryTestDataFactory.defaultFullNameDto())
                    .contactPhone("+7999" + String.format("%010d", i))
                    .officeId("office-" + i)
                    .personType(com.github.seecret1.delivery_service.entity.enums.PersonType.PHYSICAL)
                    .build();

            recipientService.updateByUserId(updateDto);
        }

        Recipient final_recipient = recipientRepository.findByUserId(DeliveryTestDataFactory.USER_ID).orElseThrow();
        assertThat(final_recipient.getContactPhone()).isEqualTo("+79990000000003");
        assertThat(final_recipient.getOfficeId()).isEqualTo("office-3");
    }
}

