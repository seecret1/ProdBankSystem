package com.github.seecret1.userservice.service.impl;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.userservice.dto.request.IndividualRequest;
import com.github.seecret1.userservice.dto.response.IndividualDto;
import com.github.seecret1.userservice.dto.response.IndividualResponse;
import com.github.seecret1.userservice.entity.Individual;
import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.entity.UserStatus;
import com.github.seecret1.userservice.exception.PersonException;
import com.github.seecret1.userservice.exception.UserNotFoundException;
import com.github.seecret1.userservice.mapper.AddressMapper;
import com.github.seecret1.userservice.mapper.IndividualMapper;
import com.github.seecret1.userservice.mapper.UserMapper;
import com.github.seecret1.userservice.repository.IndividualRepository;
import com.github.seecret1.userservice.service.IndividualService;
import com.github.seecret1.userservice.service.InternalUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndividualServiceImpl implements IndividualService {

    private final IndividualRepository individualRepository;
    private final IndividualMapper individualMapper;
    private final InternalUserService internalUserService;
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;

    @Override
    @Transactional
    public IndividualResponse recordPersonalData(String userId, IndividualRequest request) {
        User user = internalUserService.findUserEntityById(userId);

        if (individualRepository.existsByUserId(userId)) {
            throw new PersonException("Individual profile already exists for user id=[%s]", userId);
        }
        if (user.getStatus() != UserStatus.PENDING_PROFILE) {
            throw new PersonException(
                    "User id=%s cannot complete profile, status=%s",
                    userId,
                    user.getStatus()
            );
        }

        Individual individual = individualMapper.toEntity(request);
        linkUserAndIndividual(user, individual);
        applyAddress(user, request);
        user.setStatus(UserStatus.ACTIVE);

        individualRepository.save(individual);

        log.info("IN - recordPersonalData: individual for user [{}] created", user.getEmail());
        return individualMapper.toResponseDto(individual);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<IndividualResponse> findByEmails(Set<String> emails, PageModel pageModel) {
        Pageable pageable = pageModel.toPageRequest();
        var page = individualRepository.findAllByEmails(emails, pageable);
        return new PageResponse<>(
                page.getTotalElements(),
                page.getTotalPages(),
                individualMapper.toResponseDto(page.getContent())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public IndividualDto findById(String id) {
        var individual = findIndividual(id);
        log.info("IN - findById: individual with id = [{}] successfully found", id);
        return individualMapper.toDto(individual);
    }

    @Override
    @Transactional
    public IndividualResponse update(String id, IndividualRequest request) {
        var individual = findIndividual(id);
        individualMapper.update(individual, request);
        userMapper.update(individual.getUser(), request);
        individualRepository.save(individual);
        return individualMapper.toResponseDto(individual);
    }

    @Override
    @Transactional
    public void softDelete(String id) {
        log.info("IN - softDelete: individual with id=[{}]", id);
        individualRepository.softDelete(id);
    }

    @Override
    @Transactional
    public void hardDelete(String id) {
        var individual = findIndividual(id);
        log.info("IN - hardDelete: individual with id=[{}]", id);
        individualRepository.delete(individual);
    }

    private Individual findIndividual(String id) {
        return individualRepository.findById(id)
                .orElseThrow(() -> new PersonException("Individual not found by id=[%s]", id));
    }

    private static void linkUserAndIndividual(User user, Individual individual) {
        individual.setUser(user);
        user.setIndividual(individual);
    }

    private void applyAddress(User user, IndividualRequest request) {
        if (user.getAddress() == null) {
            user.setAddress(addressMapper.toAddress(request.address()));
        } else {
            addressMapper.update(user, request);
        }
    }
}
