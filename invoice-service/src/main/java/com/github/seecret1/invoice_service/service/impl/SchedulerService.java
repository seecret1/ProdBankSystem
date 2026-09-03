package com.github.seecret1.invoice_service.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class SchedulerService {

//    @Override
//    @Transactional
//    public void refreshSpendingLimit() {
//        int pageNumber = 0;
//        int totalUpdated = 0;
//
//        try {
//            while (true) {
//                Pageable pageable = PageRequest.of(pageNumber, pageSize);
//                Page<Card> page = internalCardService.findAllActiveCard(pageable);
//
//                if (page.isEmpty()) {
//                    log.debug("No more active cards to update");
//                    break;
//                }
//                List<Card> expiredCards = page.getContent();
//                log.debug("List active cards size={}, pageNumber={}", expiredCards.size(), pageNumber);
//
//                int updatedInPage = updatedSpendingLimit(expiredCards);
//                totalUpdated += updatedInPage;
//
//                if (page.isLast()) break;
//
//                pageNumber++;
//            }
//            log.info("Scheduler completed. Total updated active cards: {}", totalUpdated);
//        } catch (Exception e) {
//            log.error("Error during scheduled updated: {}", e.getMessage(), e);
//        }
//    }
//
//    private int updatedSpendingLimit(List<Card> cards) {
//        int updated = 0;
//        for (var card : cards) {
//            try {
//                cardService.refreshSpendingLimit(card.getId(), card.getType());
//                log.debug("Updated spending limit card: id={}, number={}, spendingLimit={}",
//                        card.getId(), CardMaskUtils.maskCardNumber(card.getNumber()), card.getSpendingLimit());
//                updated++;
//
//            } catch (Exception e) {
//                log.error("Error updated limit card: id={}, error={}", card.getId(), e.getMessage());
//            }
//        }
//        return updated;
//    }
}
