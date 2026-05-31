package com.github.seecret1.userservice.service.impl;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.userservice.dto.request.IndividualRequest;
import com.github.seecret1.userservice.dto.response.IndividualDto;
import com.github.seecret1.userservice.dto.response.IndividualResponse;
import com.github.seecret1.userservice.exception.PersonException;
import com.github.seecret1.userservice.mapper.IndividualMapper;
import com.github.seecret1.userservice.repository.IndividualRepository;
import com.github.seecret1.userservice.service.IndividualService;
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

    @Override
    public IndividualResponse register(IndividualRequest request) {
        var individual = individualMapper.toEntity(request);
        individualRepository.save(individual);
        log.info("IN - register: individual: [{}] successfully registered", individual.getUser().getEmail());
        return individualMapper.toResponseDto(individual);
    }

    @Override
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
    public IndividualDto findById(String id) {
        var individual = individualRepository.findById(id)
                .orElseThrow(() -> new PersonException("Individual not found by id=[%s]", id));
        log.info("IN - findById: individual with id = [{}] successfully found", id);
        return individualMapper.toDto(individual);
    }

    @Override
    public IndividualResponse update(String id, IndividualRequest request) {
        var individual = individualRepository.findById(id)
                .orElseThrow(() -> new PersonException("Individual not found by id=[%s]", id));
        individualMapper.update(individual, request);
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
        var individual = individualRepository.findById(id)
                .orElseThrow(() -> new PersonException("Individual not found by id=[%s]", id));
        log.info("IN - hardDelete: individual with id=[{}]", id);
        individualRepository.delete(individual);
    }
}
