package com.github.seecret1.userservice.service.impl;

import com.github.seecret1.userservice.dto.FullNameDto;
import com.github.seecret1.userservice.dto.response.PersonInfo;
import com.github.seecret1.userservice.mapper.AddressMapper;
import com.github.seecret1.userservice.mapper.PersonMapper;
import com.github.seecret1.userservice.repository.UserRepository;
import com.github.seecret1.userservice.service.PersonService;
import com.github.seecret1.userservice.utils.EncryptionUtils;
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

    private final EncryptionUtils encryptionUtils;

    @Override
    @Transactional(readOnly = true)
    public PersonInfo getPersonInfo(String userId, String apiKey) {
        log.info("Check api key with internal api key");
        if (!internalApiKey.equals(apiKey)) {
            throw new SecurityException("Invalid internal API key");
        }
        var user = userRepository.findById(userId)
                .orElseThrow(EntityNotFoundException::new);
        var individual = user.getIndividual();
        var address = individual.getAddress();

        var addressResponse = addressMapper.fromBaseAddress(address);
        FullNameDto fullName = new FullNameDto(user.getFirstName(), user.getLastName(), user.getMiddleName());
        return PersonMapper.toDto(
                individual.getUser().getId(),
                fullName,
                encryptionUtils.decrypt(individual.getPhoneNumber()),
                addressResponse
        );
    }
}
