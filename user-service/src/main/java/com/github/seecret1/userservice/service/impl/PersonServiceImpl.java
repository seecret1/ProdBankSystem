package com.github.seecret1.userservice.service.impl;

import com.github.seecret1.userservice.dto.response.PersonInfo;
import com.github.seecret1.userservice.mapper.AddressMapper;
import com.github.seecret1.userservice.mapper.PersonMapper;
import com.github.seecret1.userservice.repository.IndividualRepository;
import com.github.seecret1.userservice.repository.UserRepository;
import com.github.seecret1.userservice.service.PersonService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    @Value("${services.api-key}")
    private String internalApiKey;

    private final UserRepository userRepository;

    private final AddressMapper addressMapper;

    @Override
    @Transactional(readOnly = true)
    public PersonInfo getPersonInfo(String userId, String apiKey) {
        log.info("Check api key with internal api key");
        if (!internalApiKey.equals(apiKey)) {
            throw new SecurityException("Invalid internal API key");
        }
        var individual = userRepository.findById(userId)
                .orElseThrow(EntityNotFoundException::new)
                .getIndividual();
        var address = individual.getAddress();

        var addressResponse = addressMapper.fromAddress(address);
        return PersonMapper.toDto(individual.getUser().getId(), addressResponse, address.getCountry().getCode());
    }
}
