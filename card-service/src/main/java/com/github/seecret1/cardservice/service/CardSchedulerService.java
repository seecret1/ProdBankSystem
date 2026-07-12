package com.github.seecret1.cardservice.service;

public interface CardSchedulerService {

    void removeExpiryCards();

    void removeDeletedCards();

    void updateStatusExpiryCards();

    void refreshSpendingLimit();
}
