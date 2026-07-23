package com.github.seecret1.userservice.service.impl;

import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.repository.UserRepository;
import com.github.seecret1.userservice.service.PersonSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonSchedulerServiceImpl implements PersonSchedulerService {

    private static final int AGE_20 = 20;
    private static final int AGE_45 = 45;

    @Value("${app.scheduler.pageSize}")
    private int pageSize;

    private final UserRepository userRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkPassportValidity() {
        log.debug("Notify users who need to update their passport data");

        int pageNumber = 0;
        int totalNotificationCounter = 0;

        try {
            while (true) {
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                Page<User> page = userRepository.findUsersUpdatePassport(pageable);

                if (page.isEmpty()) {
                    log.debug("No more users who need to update the passport data");
                    break;
                }
                List<User> users = page.getContent();
                log.debug("List passport validity cards size={}, pageNumber={}", users.size(), pageNumber);
                for (var user : users) {
                    int age = Period.between(user.getBirthDate(), LocalDate.now()).getYears();

                    if (age == AGE_20 || age == AGE_45) {
                        // TODO: отправить уведомление о скором обновлении паспортных данных
                        //  Использовать notification-service
                        totalNotificationCounter++;
                    }
                }

                if (page.isLast()) break;

                pageNumber++;
            }
            log.info("Scheduler completed. Total notification users: {}", totalNotificationCounter);
        } catch (Exception e) {
            log.error("Error during scheduled updated: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkPassportValidityWhoMissedPassportRenewalDeadline() {
        log.debug("Notify users who missed passport renewal deadline");

        int pageNumber = 0;
        int totalNotificationCounter = 0;

        try {
            while (true) {
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                Page<User> page = userRepository.findUsersWhoMissedPassportRenewalDeadline(pageable);

                if (page.isEmpty()) {
                    log.debug("No more users who need to update the passport data");
                    break;
                }
                List<User> users = page.getContent();
                log.debug("List passport missed validity cards size={}, pageNumber={}", users.size(), pageNumber);
                for (var user : users) {
                    int age = Period.between(user.getBirthDate(), LocalDate.now()).getYears();

                    if (age == AGE_20 || age == AGE_45) {
                        // TODO: отправить уведомление об обновлении паспортных данных
                        //  Использовать notification-service
                        totalNotificationCounter++;
                    }
                }

                if (page.isLast()) break;

                pageNumber++;
            }
            log.info("Scheduler completed. Total notification users: {}", totalNotificationCounter);
        } catch (Exception e) {
            log.error("Error during scheduled updated: {}", e.getMessage(), e);
        }
    }
}
