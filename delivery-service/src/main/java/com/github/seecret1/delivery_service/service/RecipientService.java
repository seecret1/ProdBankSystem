package com.github.seecret1.delivery_service.service;

import com.github.seecret1.delivery_service.dto.user.RecipientDto;
import com.github.seecret1.delivery_service.entity.Recipient;

public interface RecipientService {

    Recipient updateByUserId(RecipientDto recipientDto);

    Recipient createNewRecipient(RecipientDto recipientDto);
}
