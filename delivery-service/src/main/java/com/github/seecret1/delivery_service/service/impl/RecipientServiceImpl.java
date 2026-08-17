package com.github.seecret1.delivery_service.service.impl;

import com.github.seecret1.delivery_service.dto.user.RecipientDto;
import com.github.seecret1.delivery_service.entity.Recipient;
import com.github.seecret1.delivery_service.exception.RecipientUpdateException;
import com.github.seecret1.delivery_service.mapper.RecipientMapper;
import com.github.seecret1.delivery_service.repository.RecipientRepository;
import com.github.seecret1.delivery_service.service.RecipientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipientServiceImpl implements RecipientService {

    private final RecipientRepository recipientRepository;

    private final RecipientMapper recipientMapper;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Recipient updateByUserId(RecipientDto recipientDto) {
        log.debug("updateByUserId: {}", recipientDto);
        var recipient = recipientMapper.toEntity(recipientDto);
        recipientRepository.update(recipient);
        return recipientRepository.findByUserId(recipient.getUserId())
                .orElseThrow(() -> new RecipientUpdateException(
                        "Recipient by userId: %s not found", recipient.getUserId()
                ));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Recipient createNewRecipient(RecipientDto recipientDto) {
        log.debug("createNewRecipient: {}", recipientDto);
        var recipient = recipientMapper.toEntity(recipientDto);
        return recipientRepository.save(recipient);
    }
}
