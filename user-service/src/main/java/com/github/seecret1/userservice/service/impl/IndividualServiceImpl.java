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

    private final EncryptionUtils encryptionUtils;

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
        String phoneEncrypt = encryptionUtils.encrypt(phoneNumber);
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

        checkPassportAndPhone(request);
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
        checkPassportAndPhone(individual, request);

        var user = individual.getUser();

        if (user.getStatus() == UserStatus.INACTIVE) {
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
        }

        individualMapper.update(individual, request);
        individualRepository.save(individual);
        return individualMapper.toResponseDto(individual);
    }

    private void checkPassportAndPhone(Individual individual, IndividualRequest request) {
        String newPassportNumber = request.passportNumber();
        if (newPassportNumber != null && !newPassportNumber.isEmpty()) {
            String newPassportEncrypted = encryptionUtils.encrypt(newPassportNumber);
            String currentPassportEncrypted = individual.getPassportNumber();
            if (!newPassportEncrypted.equals(currentPassportEncrypted)) {
                if (individualRepository.existsIndividualByPassportNumber(newPassportEncrypted)) {
                    throw new IndividualDataExistsException(
                            "Passport number already exists"
                    );
                }
            }
        }

        String newPhoneNumber = request.phoneNumber();
        if (newPhoneNumber != null && !newPhoneNumber.isEmpty()) {
            String newPhoneEncrypted = encryptionUtils.encrypt(newPhoneNumber);
            String currentPhoneEncrypted = individual.getPhoneNumber();
            if (!newPhoneEncrypted.equals(currentPhoneEncrypted)) {
                if (individualRepository.existsIndividualByPhoneNumber(newPhoneEncrypted)) {
                    throw new IndividualDataExistsException(
                            "Phone number already exists"
                    );
                }
            }
        }
    }

    private void checkPassportAndPhone(IndividualRequest request) {
        String passportNumber = request.passportNumber();
        if (passportNumber != null && !passportNumber.isEmpty()) {
            String passportEncrypted = encryptionUtils.encrypt(passportNumber);
            if (individualRepository.existsIndividualByPassportNumber(passportEncrypted)) {
                throw new IndividualDataExistsException("Passport number already exists");
            }
        }
        String phoneNumber = request.phoneNumber();
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            String phoneEncrypted = encryptionUtils.encrypt(phoneNumber);
            if (individualRepository.existsIndividualByPhoneNumber(phoneEncrypted)) {
                throw new IndividualDataExistsException("Phone number already exists");
            }
        }
    }

    private static void loggingIndividualInfo(IndividualRequest request) {
        log.info("Body: phoneNumber={}, passportNumber={}",
                PhoneUtils.maskPhoneWithPrefix(request.phoneNumber()),
                PassportMaskUtils.maskPassportFull(request.passportNumber()));
    }
}
