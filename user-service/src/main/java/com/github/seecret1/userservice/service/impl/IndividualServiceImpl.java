package com.github.seecret1.userservice.service.impl;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.userservice.dto.request.IndividualRequest;
import com.github.seecret1.userservice.dto.response.IndividualResponse;
import com.github.seecret1.userservice.entity.Individual;
import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.entity.enums.UserStatus;
import com.github.seecret1.userservice.exception.PersonException;
import com.github.seecret1.userservice.exception.PersonExistsException;
import com.github.seecret1.userservice.mapper.IndividualMapper;
import com.github.seecret1.userservice.repository.IndividualRepository;
import com.github.seecret1.userservice.repository.UserRepository;
import com.github.seecret1.userservice.service.IndividualService;
import com.github.seecret1.userservice.service.InternalUserService;
import com.github.seecret1.userservice.utils.AuthUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndividualServiceImpl implements IndividualService {

    private final UserRepository userRepository;

    private final IndividualRepository individualRepository;

    private final IndividualMapper individualMapper;

    private final InternalUserService internalUserService;

    @Override
    @Cacheable(value = "${app.cache.cache-names.individualAll}", key = "#pageModel.toString()")
    @Transactional(readOnly = true)
    public PageResponse<IndividualResponse> findAll(PageModel pageModel) {
        log.info("Find all individuals");

        Pageable pageable = pageModel.toPageRequest();
        var page = individualRepository.findAll(pageable);
        log.debug("Find individuals list. page: {}, size list: {}, page list: {}",
                page.getTotalPages(), page.getTotalElements(), page.getContent());

        return new PageResponse<>(
                page.getTotalElements(),
                page.getTotalPages(),
                individualMapper.toResponseDto(page.getContent())
        );
    }

    @Override
    @Cacheable(value = "${app.cache.cache-names.individualByCriterial}", key = "#criterial")
    @Transactional(readOnly = true)
    public IndividualResponse findByCriterial(String criterial) {
        var individual = individualRepository.findByCriterial(criterial)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Individual not found by criterial: " + criterial
                ));
        log.info("IN - findByCriterial: individual with criterial: [{}] successfully found", criterial);
        return individualMapper.toResponseDto(individual);
    }

    @Override
    @CacheEvict(
            value = {
                    "${app.cache.cache-names.individualAll}",
                    "${app.cache.cache-names.individualByCriterial}",
                    "${app.cache.cache-names.userAll}",
                    "${app.cache.cache-names.userByCriterial}",
                    "${app.cache.cache-names.userFilter}"
            },
            allEntries = true
    )
    @Transactional
    public IndividualResponse recordPersonalData(String userId, IndividualRequest request) {
        User user = internalUserService.findUserEntityById(userId);

        if (individualRepository.existsByUserId(userId)) {
            throw new PersonException("Individual profile already exists for user id=[%s]", userId);
        }

        AuthUtil.userRecordPersonalData(user);

        Individual individual = individualMapper.toEntity(request);
        individual.setUser(user);
        user.setStatus(UserStatus.ACTIVE);

        individualRepository.save(individual);

        log.info("IN - recordPersonalData: individual for user [{}] created", user.getEmail());
        return individualMapper.toResponseDto(individual);
    }

    @Override
    @CachePut(value = "${app.cache.cache-names.individualByCriterial}", key = "#criterial")
    @CacheEvict(value = "${app.cache.cache-names.individualAll}", allEntries = true)
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public IndividualResponse update(String criterial, IndividualRequest request) {
        var individual = findIndividual(criterial);
        AuthUtil.checkUserPersonalData(individual.getUser());
        return updateIndividual(individual, request);
    }

    @Override
    @CachePut(value = "${app.cache.cache-names.individualByCriterial}", key = "#userId")
    @CacheEvict(value = "${app.cache.cache-names.individualAll}", allEntries = true)
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public IndividualResponse updateYour(String userId, IndividualRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found by id: " + userId
                ));
        AuthUtil.checkUserPersonalData(user);
        var individual = user.getIndividual();
        return updateIndividual(individual, request);
    }

    @Override
    @CacheEvict(
            value = {"${app.cache.cache-names.individualAll}",
                    "${app.cache.cache-names.individualByCriterial}"},
            allEntries = true
    )
    @Transactional
    public void softDelete(String criterial) {
        log.info("IN - softDelete: individual with criterial=[{}]", criterial);
        individualRepository.softDelete(criterial);
    }

    @Override
    @CacheEvict(
            value = {"${app.cache.cache-names.individualAll}",
                    "${app.cache.cache-names.individualByCriterial}"},
            allEntries = true
    )
    @Transactional
    public void hardDelete(String criterial) {
        var individual = findIndividual(criterial);
        log.info("IN - hardDelete: individual with criterial=[{}]", criterial);
        individualRepository.delete(individual);
    }

    private Individual findIndividual(String criterial) {
        return individualRepository.findByCriterial(criterial)
                .orElseThrow(() -> new PersonException(
                        "Individual not found by criterial=[%s]", criterial
                ));
    }

    private IndividualResponse updateIndividual(Individual individual, IndividualRequest request) {
        String newPassport = request.passportNumber();
        String newPhone = request.phoneNumber();
        String currentPassport = individual.getPassportNumber();
        String currentPhone = individual.getPhoneNumber();

        if (!newPassport.equals(currentPassport)) {
            if (individualRepository.existsIndividualByPassportNumber(newPassport)) {
                throw new PersonExistsException(
                        "Passport number already exists for another user"
                );
            }
            individual.setPassportNumber(newPassport);
        }
        if (!newPhone.equals(currentPhone)) {
            if (individualRepository.existsIndividualByPhoneNumber(newPhone)) {
                throw new PersonExistsException(
                        "Phone number already exists for another user"
                );
            }
            individual.setPhoneNumber(newPhone);
        }

        individualMapper.update(individual, request);
        individualRepository.save(individual);
        return individualMapper.toResponseDto(individual);
    }
}
