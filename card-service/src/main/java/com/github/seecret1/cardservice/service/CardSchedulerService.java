package com.github.seecret1.cardservice.service;

public interface CardSchedulerService {

    void removeExpiryCardFromDb();

    void removeDeletedCardFromDb();
}
