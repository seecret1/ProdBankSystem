package com.github.seecret1.cardservice.service.impl;

import com.github.seecret1.cardservice.client.UserServiceClient;
import com.github.seecret1.cardservice.config.CardSpendingLimitsConfig;
import com.github.seecret1.cardservice.dto.request.CardRequest;
import com.github.seecret1.cardservice.dto.response.CardResponse;
import com.github.seecret1.cardservice.entity.Card;
import com.github.seecret1.cardservice.entity.enums.CardStatus;
import com.github.seecret1.cardservice.entity.enums.CardType;
import com.github.seecret1.cardservice.exception.*;
import com.github.seecret1.cardservice.kafka.service.OrderKafkaProducerService;
import com.github.seecret1.cardservice.mapper.CardMapper;
import com.github.seecret1.cardservice.model.CardFilterModel;
import com.github.seecret1.cardservice.repository.CardRepository;
import com.github.seecret1.cardservice.repository.specification.CardSpecification;
import com.github.seecret1.cardservice.service.CardService;
import com.github.seecret1.cardservice.utils.AuthUtils;
import com.github.seecret1.cardservice.utils.CardHashUtils;
import com.github.seecret1.cardservice.utils.CardMaskUtils;
import com.github.seecret1.cardservice.utils.CardValidateUtils;
import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
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

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final OrderKafkaProducerService orderKafkaProducerService;

    private final CardSpendingLimitsConfig cardSpendingLimitsConfig;

    private final UserServiceClient userServiceClient;

    private final CardRepository cardRepository;

    private final CardMapper cardMapper;

    private final AuthUtils authUtils;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "${app.cache.cache-names.cardAll}", key = "#pageModel.toString()")
    public PageResponse<CardResponse> findAll(PageModel pageModel) {
        log.info("Find all page cards");

        Pageable pageable = pageModel.toPageRequest();
        var pageResult = cardRepository.findAll(pageable);

        log.debug("Find cards list. page: {}, page size: {}, page elements: {}",
                pageResult.getTotalPages(), pageResult.getTotalElements(), pageResult.getContent());

        return new PageResponse<>(
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                cardMapper.toDtoResponseList(pageResult.getContent())
        );
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "${app.cache.cache-names.cardOnlyNotDeleted}", key = "#pageModel.toString()")
    public PageResponse<CardResponse> findOnlyNotDeleted(PageModel pageModel) {
        log.info("Find all not deleted page cards");

        Pageable pageable = pageModel.toPageRequest();
        var pageResult = cardRepository.findNotDeletedCards(pageable);

        log.debug("Find not deleted cards list. page: {}, page size: {}, page elements: {}",
                pageResult.getTotalPages(), pageResult.getTotalElements(), pageResult.getContent());

        return new PageResponse<>(
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                cardMapper.toDtoResponseList(pageResult.getContent())
        );
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "${app.cache.cache-names.cardFilter}", key = "#filter.toString()")
    public PageResponse<CardResponse> findByFilter(CardFilterModel filter) {
        log.info("Find card by filter: {}", filter);
        var page = cardRepository.findNotDeletedCards(
                CardSpecification.withFilter(filter),
                filter.getPage().toPageRequest()
        );
        log.debug("Find page cards by filter. Page: {}", page);
        return new PageResponse<>(
                page.getTotalElements(),
                page.getTotalPages(),
                cardMapper.toDtoResponseList(page.getContent())
        );
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "${app.cache.cache-names.cardById}", key = "#id")
    public CardResponse findById(String id) {
        log.info("Find card by id: {}", id);

        var card = findCardById(id);
        authUtils.checkCardAccess(card);

        log.debug("Find by ID card: {}", card);
        return cardMapper.toYourDtoResponse(card);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "${app.cache.cache-names.cardById}", key = "#id")
    public CardResponse findByIdDeletedOrNot(String id) {
        var card = findCardByIdDeletedOrNot(id);
        log.debug("Find by ID card: {}", card);
        return cardMapper.toYourDtoResponse(card);
    }

    @Override
    @Transactional
    @Cacheable(value = "${app.cache.cache-names.cardByNumber}", key = "#number")
    public CardResponse findByNumber(String number) {
        var card = findCardByNumber(number);
        authUtils.checkCardAccess(card);

        log.debug("Find by number card: {}", card);
        return cardMapper.toYourDtoResponse(card);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "${app.cache.cache-names.cardYour}", key = "#userId")
    public PageResponse<CardResponse> findYourCards(String userId, PageModel pageModel) {
        log.info("Find cards by user criterial: {}, page: {}, size: {}",
                userId, pageModel.getNumber(), pageModel.getSize());

        Pageable pageable = pageModel.toPageRequest();
        var page = cardRepository.findAllByUserId(userId, pageable);

        if (page.isEmpty()) {
            throw new EntityNotFoundException(
                    "User not found by criterial: " + userId + " or not cards from this users"
            );
        }
        log.debug("Found {} cards for user {}, total pages: {}",
                page.getContent().size(), userId, page.getTotalPages());

        return new PageResponse<>(
                page.getTotalElements(),
                page.getTotalPages(),
                cardMapper.toYourDtoResponseList(page.getContent())
        );
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CacheEvict(
            value = {
                    "${app.cache.cache-names.cardAll}",
                    "${app.cache.cache-names.cardOnlyNotDeleted}",
                    "${app.cache.cache-names.cardYour}",
                    "${app.cache.cache-names.cardFilter}",
                    "${app.cache.cache-names.cardByNumber}"
            },
            allEntries = true
    )
    @CachePut(value = "${app.cache.cache-names.cardById}", key = "#id")
    public CardResponse activateCard(String id) {
        log.info("Activate card by id: {}", id);

        // TODO: добавить проверки
        var card = findCardByIdUseLock(id);

        if (card.getStatus() == CardStatus.PENDING) {
            card.setStatus(CardStatus.ACTIVE);
            log.info("Successfully activate card: {}", id);
            return cardMapper.toYourDtoResponse(card);
        }
        else if (card.getStatus() == CardStatus.ACTIVE) {
            log.warn("Card already activated: {}", id);
            return cardMapper.toYourDtoResponse(card);
        }
        else throw new CardException("Card cannot be activated");
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CacheEvict(
            value = {
                    "${app.cache.cache-names.cardAll}",
                    "${app.cache.cache-names.cardOnlyNotDeleted}",
                    "${app.cache.cache-names.cardYour}",
                    "${app.cache.cache-names.cardFilter}",
                    "${app.cache.cache-names.cardById}",
                    "${app.cache.cache-names.cardByNumber}"
            },
            allEntries = true
    )
    public CardResponse create(String userId, CardRequest request) {
        log.info("Creating a user card, userId: {}", userId);

        var user = userServiceClient.findUserById(userId);

        if (user == null) throw new EntityNotFoundException("User not found by id " + userId);

        var card = cardRepository.findByNumberHash(CardHashUtils.hash(request.number()));

        if (card.isPresent()) {
            throw new CardExistsException(
                    "Card with number " + CardMaskUtils.maskCardNumber(request.number()) + " already exists!"
            );
        }

        if (request.dateExpiry().isBefore(LocalDate.now())) {
            throw new CardExpiryDateException(
                    "Date expiry is before now!"
            );
        }

        log.info("Init order created card");
        var orderCard = cardMapper.toEntity(request, user.id());

        cardRepository.save(orderCard);

        orderKafkaProducerService.sendNoWait(
                cardMapper.toOrderCardDto(orderCard, request.comment(), userId)
        );

        log.info("Save card with status PENDING");
        return cardMapper.toDtoResponse(orderCard);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CacheEvict(
            value = {
                    "${app.cache.cache-names.cardAll}",
                    "${app.cache.cache-names.cardOnlyNotDeleted}",
                    "${app.cache.cache-names.cardYour}",
                    "${app.cache.cache-names.cardFilter}",
                    "${app.cache.cache-names.cardByNumber}"
            },
            allEntries = true
    )
    @CachePut(value = "${app.cache.cache-names.cardById}", key = "#id")
    public CardResponse updateStatus(String id, CardStatus status) {
        log.info("Update status for card: {}", id);

        var card = findCardByIdUseLock(id);

        boolean check = CardValidateUtils.checkCardStatus(card, status);

        if (!check) return cardMapper.toDtoResponse(card);
        if (status == CardStatus.EXPIRED) {
            extendCard(id, card.getDateExpiry());
        }
        card.setStatus(status);

        log.debug("Update card status: {}", card);
        cardRepository.save(card);
        log.info("Update card successful");
        return cardMapper.toDtoResponse(card);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CacheEvict(
            value = {
                    "${app.cache.cache-names.cardAll}",
                    "${app.cache.cache-names.cardOnlyNotDeleted}",
                    "${app.cache.cache-names.cardYour}",
                    "${app.cache.cache-names.cardFilter}",
                    "${app.cache.cache-names.cardByNumber}"
            },
            allEntries = true
    )
    @CachePut(value = "${app.cache.cache-names.cardById}", key = "#id")
    public CardResponse extendCard(String id, LocalDate dateExpiry) {
        log.info("Extend the validity period of the card: {}", id);

        var card = findCardByIdUseLock(id);
        CardValidateUtils.checkCardValid(card, dateExpiry);

        log.debug("Extend card status: {}", card.getStatus());
        cardRepository.save(card);
        log.info("Extend card successful");
        return cardMapper.toDtoResponse(card);
    }

    @Override
    public CardResponse refreshSpendingLimit(String cardId, CardType cardType) {
        var card = findCardByIdUseLock(cardId);

        log.info("Refresh spending limit for card by id: {}", cardId);
        card.setSpendingLimit(cardSpendingLimitsConfig.getMaxLimitForType(cardType));
        var newCard = cardRepository.save(card);
        log.debug("Refreshed spending limit for card: {}", card);
        return cardMapper.toDtoResponse(newCard);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CacheEvict(
            value = {
                    "${app.cache.cache-names.cardAll}",
                    "${app.cache.cache-names.cardOnlyNotDeleted}",
                    "${app.cache.cache-names.cardYour}",
                    "${app.cache.cache-names.cardFilter}",
                    "${app.cache.cache-names.cardById}",
                    "${app.cache.cache-names.cardByNumber}"
            },
            allEntries = true
    )
    public void softDelete(String userId, String cardId) {
        log.info("Soft delete card by id: {}", cardId);

        var card = findCardByIdUseLock(cardId);
        var user = userServiceClient.findUserById(userId);
        log.debug("Find user {} from the server", user.username());

        card.softDelete(user.username());
        cardRepository.save(card);
        log.info("Soft delete card successful");
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CacheEvict(
            value = {
                    "${app.cache.cache-names.cardAll}",
                    "${app.cache.cache-names.cardOnlyNotDeleted}",
                    "${app.cache.cache-names.cardYour}",
                    "${app.cache.cache-names.cardFilter}",
                    "${app.cache.cache-names.cardById}",
                    "${app.cache.cache-names.cardByNumber}"
            },
            allEntries = true
    )
    public void hardDelete(String id) {
        log.info("Hard delete card by id: {}", id);
        var card = findCardByIdDeletedOrNot(id);
        cardRepository.delete(card);
        log.info("Hard delete card successful");
    }

    private Card findCardById(String id) {
        log.debug("Searching card by id: {}", id);

        return cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(
                        "Card not found by id: " + id
                ));
    }

    private Card findCardByIdUseLock(String id) {
        log.info("Find card by ID={} in update method", id);
        return cardRepository.findByIdUseLock(id)
                .orElseThrow(() -> new CardNotFoundException(
                        "Card not found by ID: " + id
                ));
    }

    private Card findCardByIdDeletedOrNot(String id) {
        log.info("Find card deleted or not by ID: {}", id);
        var card = cardRepository.findByIdDeletedOrNot(id)
                .orElseThrow(() -> new CardNotFoundException(
                        "Card not found by id: " + id
                ));
        if (card.getDeleted() == true) {
            throw new CardDeletedException(
                    "Card already deleted by id: %s and user: %s",
                    id, card.getDeletedBy()
            );
        }
        return card;
    }

    private Card findCardByNumber(String number) {
        String hash = CardHashUtils.hash(number);
        String maskedNumber = CardMaskUtils.maskCardNumber(number);
        log.info("Find card by number: {}", maskedNumber);

        return cardRepository.findByNumberHash(hash)
                .orElseThrow(() -> new CardNotFoundException(
                        "Card not found by number: " + maskedNumber
                ));
    }
}
