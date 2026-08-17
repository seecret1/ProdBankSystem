package com.github.seecret1.delivery_service.service.processed;

import com.github.seecret1.delivery_service.dto.order.OrderCardDeliveryDto;
import com.github.seecret1.delivery_service.dto.user.RecipientDto;
import com.github.seecret1.delivery_service.entity.Recipient;
import com.github.seecret1.delivery_service.repository.RecipientRepository;
import com.github.seecret1.delivery_service.service.RecipientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipientProcessed {

    private final RecipientRepository recipientRepository;

    private final RecipientService recipientService;

    public Recipient processDelivery(OrderCardDeliveryDto dto) {
        log.info("Process delivery recipient: {}", dto);
        var recipientDto = getRecipientDto(dto);

        var recipient = recipientRepository.findByUserId(dto.getUserId())
                .orElseGet(() -> {
                    log.info("Creating new recipient for userId: {}", dto.getUserId());
                    return recipientService.createNewRecipient(recipientDto);
                });

        if (hasDataChanged(recipient, recipientDto)) {
            log.info("Updating recipient for userId: {}", dto.getUserId());
            return recipientService.updateByUserId(recipientDto);
        }
        log.info("Data unchanged for userId: {}, reusing", dto.getUserId());
        return recipient;
    }

    private boolean hasDataChanged(Recipient recipient, RecipientDto dto) {
        return !Objects.equals(recipient.getFullName().getFirstName(), dto.getFullName().getFirstName())
                || !Objects.equals(recipient.getFullName().getLastName(), dto.getFullName().getLastName())
                || !Objects.equals(recipient.getFullName().getMiddleName(), dto.getFullName().getMiddleName())
                || !Objects.equals(recipient.getContactPhone(), dto.getContactPhone())
                || !Objects.equals(recipient.getOfficeId(), dto.getOfficeId())
                || recipient.getPersonType() != dto.getPersonType();
    }

    private RecipientDto getRecipientDto(OrderCardDeliveryDto dto) {
        return RecipientDto.builder()
                .personType(dto.getPersonType())
                .contactPhone(dto.getContactPhone())
                .fullName(dto.getFullName())
                .userId(dto.getUserId())
                .officeId(dto.getOfficeId())
                .build();
    }
}
