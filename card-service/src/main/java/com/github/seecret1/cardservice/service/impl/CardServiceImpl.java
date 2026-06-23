package com.github.seecret1.cardservice.service.impl;

import com.github.seecret1.cardservice.client.UserServiceClient;
import com.github.seecret1.cardservice.dto.request.CardRequest;
import com.github.seecret1.cardservice.dto.request.ExtendCardRequest;
import com.github.seecret1.cardservice.dto.request.UpdateStatusCardRequest;
import com.github.seecret1.cardservice.dto.response.CardResponse;
import com.github.seecret1.cardservice.entity.Card;
import com.github.seecret1.cardservice.entity.enums.CardStatus;
import com.github.seecret1.cardservice.exception.*;
import com.github.seecret1.cardservice.mapper.CardMapper;
import com.github.seecret1.cardservice.model.CardFilterModel;
import com.github.seecret1.cardservice.repository.CardRepository;
import com.github.seecret1.cardservice.repository.specification.CardSpecification;
import com.github.seecret1.cardservice.service.CardService;
import com.github.seecret1.cardservice.service.InternalCardService;
import com.github.seecret1.cardservice.utils.AuthUtils;
import com.github.seecret1.cardservice.utils.CardHashUtils;
import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.InvalidParameterException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService, InternalCardService {

    private final UserServiceClient userServiceClient;

    private final CardRepository cardRepository;

    private final CardMapper cardMapper;

    private final AuthUtils authUtils;

    @Override
    @Transactional(readOnly = true)
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
    public CardResponse findByCriterial(String criterial) {
        log.info("Find card by criterial: {}", criterial);

        var card = findCardByCriterial(criterial);
        authUtils.checkCardAccess(card);

        log.debug("Find by criterial card: {}", card);
        return cardMapper.toYourDtoResponse(card);
    }

    @Override
    @Transactional(readOnly = true)
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
    public CardResponse create(CardRequest request) {
        String criterial = request.userEmail();
        log.info("Creating a user card, criterial: {}", criterial);

        var user = userServiceClient.findUserByCriterial(criterial);

        if (user == null) throw new EntityNotFoundException("User not found by criterial " + criterial);

        if (request.dateExpiry().isBefore(LocalDate.now())) {
            throw new InvalidParameterException(
                    "Date expiry is before now!"
            );
        }
        try {
            var card = cardRepository.save(cardMapper.toEntity(request, user.id()));

            log.debug("Created card: {}", card);
            log.info("Create card successful");

            return cardMapper.toDtoResponse(card);

        } catch (DataIntegrityViolationException ex) {
            throw new CardExistsException(
                    "Card with number " + request.number() + " already exists!"
            );
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CardResponse updateStatus(UpdateStatusCardRequest request) {
        String number = request.number();
        log.info("Update status for card: {}", number);

        String hash = CardHashUtils.hash(number);
        var card = cardRepository.findByNumberHash(hash)
                .orElseThrow(() -> new CardNotFoundException(
                        "Card not found by number: " + number
                ));

        var status = request.status();
        boolean check = checkCardStatus(card, status);

        if (!check) return cardMapper.toDtoResponse(card);
        card.setStatus(status);

        log.debug("Update card status: {}", card);
        cardRepository.save(card);
        log.info("Update card successful");
        return cardMapper.toDtoResponse(card);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CardResponse extendCard(ExtendCardRequest request) {
        String number = request.number();
        log.info("Extend the validity period of the card: {}", number);

        String hash = CardHashUtils.hash(number);
        var card = cardRepository.findByNumberHash(hash)
                .orElseThrow(() -> new CardNotFoundException(
                        "Card not found by number: " + number
                ));

        checkCardValid(card, request);

        log.debug("Extend card status: {}", card.getStatus());
        cardRepository.save(card);
        log.info("Extend card successful");
        return cardMapper.toDtoResponse(card);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void softDelete(String userId, String criterial) {
        log.info("Soft delete card by criterial: {}", criterial);

        var card = findCardByCriterial(criterial);
        if (card.getDeleted() == true) {
            throw new CardDeletedException("Card already deleted by criterial " + criterial);
        }

        var user = userServiceClient.findUserByCriterial(userId);
        log.debug("Find user {} from the server", user.username());

        card.softDelete(user.username());
        cardRepository.save(card);
        log.info("Soft delete card successful");
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void hardDelete(String criterial) {
        log.info("Hard delete card by criterial: {}", criterial);
        var card = findCardByCriterial(criterial);
        cardRepository.delete(card);
        log.info("Hard delete card successful");
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Page<Card> findExpiryCards(LocalDate expirationDate, Pageable pageable) {
        log.info("Find expiry cards before period: {}", expirationDate);
        return cardRepository.findExpiryCards(expirationDate, pageable);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Page<Card> findDeletedCards(Instant deletedAt, Pageable pageable) {
        log.info("Find deleted cards before period: {}", deletedAt);
        return cardRepository.findDeletedCards(deletedAt, pageable);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Page<Card> findExpiryActiveCards(LocalDate expirationDate, Pageable pageable) {
        log.info("Find updated status cards before period: {}", expirationDate);
        return cardRepository.findExpiryActiveCards(expirationDate, pageable);
    }

    private Card findCardByCriterial(String criterial) {
        log.debug("Searching card by criterial: {}", criterial);

        if (criterial != null && criterial.length() == 36) {
            Optional<Card> byId = cardRepository.findById(criterial);
            if (byId.isPresent()) {
                log.debug("Card found by ID: {}", criterial);
                return byId.get();
            }
        }

        String hash = CardHashUtils.hash(criterial);
        Optional<Card> byHash = cardRepository.findByNumberHash(hash);
        if (byHash.isPresent()) {
            log.debug("Card found by number hash: {}", criterial);
            return byHash.get();
        }

        throw new CardNotFoundException("Card not found by criterial: " + criterial);
    }

    private boolean checkCardStatus(Card card, CardStatus status) {
        if (card.getStatus() == status) return false;

        if (status == CardStatus.ACTIVE &&
                card.getDateExpiry().isBefore(LocalDate.now())) {
            throw new CardStatusUpdateException("The card status cannot be active");
        }
        if (status == CardStatus.EXPIRED &&
                card.getDateExpiry().isAfter(LocalDate.now())) {
            throw new CardStatusUpdateException("The card status cannot be expired");
        }
        if (status == CardStatus.EXTENDED) {
            throw new CardStatusUpdateException("Please use another method: extendCard");
        }

        return true;
    }

    private void checkCardValid(Card card, ExtendCardRequest request) {

        if (!card.getDateExpiry().isBefore(request.dateExpiry()))
            throw new CardStatusUpdateException("The card status cannot be EXTENDED");

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardStatusUpdateException("The card status BLOCKED");
        }
        if (card.getBalance().compareTo(BigDecimal.ZERO) < 0)
            throw new ExtendedException("Card status cannot be EXTENDED");

        if (card.getStatus() == CardStatus.EXPIRED ||
                card.getStatus() == CardStatus.ACTIVE ||
                card.getStatus() == CardStatus.EXTENDED) {
            card.setDateExpiry(request.dateExpiry());
            card.setStatus(CardStatus.EXTENDED);
        }
    }
}
