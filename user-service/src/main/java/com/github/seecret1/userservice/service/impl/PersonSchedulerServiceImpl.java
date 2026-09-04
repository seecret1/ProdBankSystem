package com.github.seecret1.userservice.service.impl;

import com.github.seecret1.userservice.config.custom.PassportProperties;
import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.entity.enums.UserStatus;
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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonSchedulerServiceImpl implements PersonSchedulerService {

    private final PassportProperties passProps;

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
                Page<User> page = userRepository.findUsersUpdatePassport(
                        pageable,
                        passProps.getFirstAge(),
                        passProps.getSecondAge()
                );

                if (page.isEmpty()) {
                    log.debug("No more users who need to update the passport data");
                    break;
                }
                List<User> users = page.getContent();
                log.debug("List passport validity cards size={}, pageNumber={}", users.size(), pageNumber);
                for (var user : users) {
                    int age = Period.between(user.getBirthDate(), LocalDate.now()).getYears();

                    if (age == passProps.getFirstAge() || age == passProps.getSecondAge()) {
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
    public void deactivateUsersIfPassportExpired() {
        log.debug("Notify users who missed passport renewal deadline");

        int pageNumber = 0;
        int totalNotificationCounter = 0;

        try {
            while (true) {
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                Page<User> page = userRepository.findUsersWithExpiredPassport(
                        pageable,
                        passProps.getFirstAge(),
                        passProps.getSecondAge(),
                        passProps.getDaysThreshold()
                );

                if (page.isEmpty()) {
                    log.debug("No more users who need to update the passport data");
                    break;
                }
                List<User> users = page.getContent();
                List<User> updatedUsers = new ArrayList<>(users.size());
                log.debug("List passport missed validity cards size={}, pageNumber={}", users.size(), pageNumber);
                for (var user : users) {
                    user.setStatus(UserStatus.INACTIVE);
                    updatedUsers.add(user);

                    // TODO: отправить уведомление об обновлении паспортных данных
                    //  Использовать notification-service
                    totalNotificationCounter++;
                }

                userRepository.saveAll(updatedUsers);

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
                Page<User> page = userRepository.findUsersWhoMissedPassportRenewalDeadline(
                        pageable,
                        passProps.getFirstAge(),
                        passProps.getSecondAge(),
                        passProps.getDaysNotified()
                );

                if (page.isEmpty()) {
                    log.debug("No more users who need to update the passport data");
                    break;
                }
                List<User> users = page.getContent();
                log.debug("List passport missed validity cards size={}, pageNumber={}", users.size(), pageNumber);
                for (var user : users) {
                    // TODO: отправить уведомление об обновлении паспортных данных
                    //  Использовать notification-service
                    totalNotificationCounter++;
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
