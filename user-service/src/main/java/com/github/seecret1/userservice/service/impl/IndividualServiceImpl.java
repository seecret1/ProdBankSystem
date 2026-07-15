package com.github.seecret1.userservice.service.impl;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.userservice.dto.request.IndividualRequest;
import com.github.seecret1.userservice.dto.response.IndividualResponse;
import com.github.seecret1.userservice.entity.Individual;
import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.entity.enums.UserStatus;
import com.github.seecret1.userservice.exception.PersonException;
import com.github.seecret1.userservice.exception.IndividualDataExistsException;
import com.github.seecret1.userservice.mapper.IndividualMapper;
import com.github.seecret1.userservice.repository.IndividualRepository;
import com.github.seecret1.userservice.repository.UserRepository;
import com.github.seecret1.userservice.service.IndividualService;
import com.github.seecret1.userservice.service.InternalUserService;
import com.github.seecret1.userservice.utils.AuthUtil;
import com.github.seecret1.userservice.utils.EncryptionUtils;
import com.github.seecret1.userservice.utils.PassportMaskUtils;
import com.github.seecret1.userservice.utils.PhoneUtils;
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
    @Cacheable(value = "${app.cache.cache-names.individualById}", key = "#id")
    @Transactional(readOnly = true)
    public IndividualResponse findById(String id) {
        var individual = findIndividual(id);
        log.info("IN - findById: individual with id: [{}] successfully found", id);
        return individualMapper.toResponseDto(individual);
    }

    @Override
    @Cacheable(value = "${app.cache.cache-names.individualByPhoneNumber}", key = "#phoneNumber")
    @Transactional(readOnly = true)
    public IndividualResponse findByPhoneNumber(String phoneNumber) {
        String phoneEncrypt = EncryptionUtils.encrypt(phoneNumber);
        var individual = individualRepository.findByPhoneNumber(phoneEncrypt)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Individual not found by phone number: " + phoneNumber
                ));
        log.info("IN - findByPhoneNumber: individual with phone number: [{}] successfully found", phoneNumber);
        return individualMapper.toResponseDto(individual);
    }

    @Override
    @CacheEvict(
            value = {
                    "${app.cache.cache-names.individualAll}",
                    "${app.cache.cache-names.individualById}",
                    "${app.cache.cache-names.individualByPhoneNumber}",
                    "${app.cache.cache-names.userAll}",
                    "${app.cache.cache-names.userActiveAll}",
                    "${app.cache.cache-names.userFilter}",
                    "${app.cache.cache-names.userById}",
                    "${app.cache.cache-names.userByEmail}",
                    "${app.cache.cache-names.userByUsername}"
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

        loggingIndividualInfo(request);

        log.info("IN - recordPersonalData: individual for user [{}] created", user.getEmail());
        return individualMapper.toResponseYourDto(individual);
    }

    @Override
    @CacheEvict(
            value = {
                    "${app.cache.cache-names.individualAll}",
                    "${app.cache.cache-names.individualByPhoneNumber}"
            }, allEntries = true
    )
    @CachePut(value = "${app.cache.cache-names.individualById}", key = "#id")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public IndividualResponse update(String id, IndividualRequest request) {
        var individual = findIndividual(id);
        AuthUtil.checkUserPersonalData(individual.getUser());
        return updateIndividual(individual, request);
    }

    @Override
    @CacheEvict(
            value = {
                    "${app.cache.cache-names.individualAll}",
                    "${app.cache.cache-names.individualByPhoneNumber}"
            }, allEntries = true
    )
    @CachePut(value = "${app.cache.cache-names.individualById}", key = "#result.id()")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public IndividualResponse updateYour(String userId, IndividualRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found by id: " + userId
                ));
        AuthUtil.checkUserPersonalData(user);
        loggingIndividualInfo(request);
        var individual = user.getIndividual();
        return updateIndividual(individual, request);
    }

    @Override
    @CacheEvict(
            value = {
                    "${app.cache.cache-names.individualAll}",
                    "${app.cache.cache-names.individualById}",
                    "${app.cache.cache-names.individualByPhoneNumber}"
            },
            allEntries = true
    )
    @Transactional
    public void softDelete(String id) {
        log.info("IN - softDelete: individual by id=[{}]", id);
        individualRepository.softDelete(id);
    }

    @Override
    @CacheEvict(
            value = {
                    "${app.cache.cache-names.individualAll}",
                    "${app.cache.cache-names.individualById}",
                    "${app.cache.cache-names.individualByPhoneNumber}"
            },
            allEntries = true
    )
    @Transactional
    public void hardDelete(String id) {
        var individual = findIndividual(id);
        log.info("IN - hardDelete: individual by id=[{}]", id);
        individualRepository.delete(individual);
    }

    private Individual findIndividual(String id) {
        return individualRepository.findById(id)
                .orElseThrow(() -> new PersonException(
                        "Individual not found by id=[%s]", id
                ));
    }

    private IndividualResponse updateIndividual(Individual individual, IndividualRequest request) {
        String newPassport = request.passportNumber();
        String newPhone = request.phoneNumber();
        String currentPassport = individual.getPassportNumber();
        String currentPhone = individual.getPhoneNumber();

        if (!newPassport.equals(currentPassport)) {
            if (individualRepository.existsIndividualByPassportNumber(newPassport)) {
                throw new IndividualDataExistsException(
                        "Passport number already exists for another user"
                );
            }
            individual.setPassportNumber(newPassport);
        }
        if (!newPhone.equals(currentPhone)) {
            if (individualRepository.existsIndividualByPhoneNumber(newPhone)) {
                throw new IndividualDataExistsException(
                        "Phone number already exists for another user"
                );
            }
            individual.setPhoneNumber(newPhone);
        }

        individualMapper.update(individual, request);
        individualRepository.save(individual);
        return individualMapper.toResponseDto(individual);
    }

    private static void loggingIndividualInfo(IndividualRequest request) {
        log.info("Body: phoneNumber={}, passportNumber={}",
                PhoneUtils.maskPhoneWithPrefix(request.phoneNumber()),
                PassportMaskUtils.maskPassportFull(request.passportNumber()));
    }
}
