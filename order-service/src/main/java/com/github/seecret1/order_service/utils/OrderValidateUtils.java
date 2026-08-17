package com.github.seecret1.order_service.utils;

import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.dto.user.PersonInfo;
import com.github.seecret1.order_service.exception.OrderValidException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class OrderValidateUtils {

    public boolean validateCard(PersonInfo personInfo, OrderCardDto event) {
        if (personInfo.userId() == null || personInfo.userId().isBlank()) {

            log.error("PersonInfo: userId={}, address={}", personInfo.userId(), personInfo.address());
            throw new OrderValidException("Order card by ID: %s by traceId: %s not validate",
                    event.getCardId(), event.getTraceId()
            );
        }
        return true;
    }
}
