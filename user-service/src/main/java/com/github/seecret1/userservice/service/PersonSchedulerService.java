package com.github.seecret1.userservice.service;

public interface PersonSchedulerService {

    void checkPassportValidity();

    void checkPassportValidityWhoMissedPassportRenewalDeadline();
}
