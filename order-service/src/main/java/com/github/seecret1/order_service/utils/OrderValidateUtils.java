package com.github.seecret1.order_service.utils;

import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.dto.user.PersonInfo;
import com.github.seecret1.order_service.exception.OrderValidException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import static com.github.seecret1.order_service.utils.Constant.COUNTRY_RUS_CODE;

@Slf4j
@UtilityClass
public class OrderValidateUtils {

    public boolean validateCard(PersonInfo personInfo, OrderCardDto event) {
        if (personInfo.userId() == null || personInfo.userId().isBlank() ||
                personInfo.address() == null || !personInfo.countryCode().equals(COUNTRY_RUS_CODE)) {

            log.error("PersonInfo: userId={}, address={}, countryCode={}", personInfo.userId(), personInfo.address(), personInfo.countryCode());
            throw new OrderValidException("Order card by ID: %s by traceId: %s not validate",
                    event.getCardId(), event.getTraceId()
            );
        }
        return true;
    }
}
