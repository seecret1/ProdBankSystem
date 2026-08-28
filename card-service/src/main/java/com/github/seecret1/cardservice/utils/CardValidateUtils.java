package com.github.seecret1.cardservice.utils;

import com.github.seecret1.cardservice.dto.request.CardRequest;
import com.github.seecret1.cardservice.dto.user.UserResponse;
import com.github.seecret1.cardservice.entity.Card;
import com.github.seecret1.cardservice.entity.enums.CardStatus;
import com.github.seecret1.cardservice.exception.CardExistsException;
import com.github.seecret1.cardservice.exception.CardExpiryDateException;
import com.github.seecret1.cardservice.exception.CardStatusUpdateException;
import com.github.seecret1.cardservice.exception.ExtendedException;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@UtilityClass
public class CardValidateUtils {

    public boolean checkCardStatus(Card card, CardStatus status) {
        if (card.getStatus() == status) return false;

        if (status == CardStatus.ACTIVE &&
                card.getDateExpiry().isBefore(LocalDate.now())) {
            throw new CardStatusUpdateException("The card status cannot be active");
        }
        if (status == CardStatus.EXPIRED &&
                card.getDateExpiry().isAfter(LocalDate.now())) {
            throw new CardStatusUpdateException("The card status cannot be expired");
        }
        if ((status == CardStatus.EXTENDED || status == CardStatus.ACTIVE)
        ) {
            throw new CardStatusUpdateException("Cannot change status of card with negative balance");
        }

        return true;
    }

    public void checkCardValid(Card card, LocalDate dateExpiry) {

        if (!card.getDateExpiry().isBefore(dateExpiry))
            throw new CardStatusUpdateException("The card status cannot be EXTENDED");

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardStatusUpdateException("The card status BLOCKED");
        }

        if (card.getStatus() == CardStatus.EXPIRED ||
                card.getStatus() == CardStatus.ACTIVE ||
                card.getStatus() == CardStatus.EXTENDED) {
            card.setDateExpiry(dateExpiry);
            card.setStatus(CardStatus.EXTENDED);
        }
    }

    public void checkValidateOnCreated(Optional<Card> card, CardRequest request) {
        if (card.isPresent()) {
            throw new CardExistsException(
                    "Card with number " + CardMaskUtils.maskCardNumber(request.number()) + " already exists!"
            );
        }

        if (request.dateExpiry().isBefore(LocalDate.now())) {
            throw new CardExpiryDateException(
                    "Date expiry is before now!"
            );
        }
    }
}
