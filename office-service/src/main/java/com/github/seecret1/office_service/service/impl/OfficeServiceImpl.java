package com.github.seecret1.office_service.service.impl;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.office_service.dto.request.OfficeCreateRequest;
import com.github.seecret1.office_service.dto.request.OfficeUpdateRequest;
import com.github.seecret1.office_service.dto.response.OfficeFullResponse;
import com.github.seecret1.office_service.dto.response.OfficeResponse;
import com.github.seecret1.office_service.entity.Office;
import com.github.seecret1.office_service.feign.UserServiceFeignClient;
import com.github.seecret1.office_service.mapper.OfficeMapper;
import com.github.seecret1.office_service.repository.OfficeRepository;
import com.github.seecret1.office_service.service.OfficeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfficeServiceImpl implements OfficeService {

    @Value("${services.api-key}")
    private String internalApiKey;

    private final UserServiceFeignClient userServiceFeignClient;

    private final OfficeRepository officeRepository;

    private final OfficeMapper officeMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OfficeFullResponse> findAll(PageModel pageModel) {
        log.info("Use findAll office: {}", pageModel);
        var page = officeRepository.findAll(pageModel.toPageRequest());
        log.debug("Page offices: totalElements: {}, totalPages: {}",
                page.getTotalElements(), page.getTotalPages());
        return new PageResponse<>(
                page.getTotalElements(),
                page.getTotalPages(),
                officeMapper.toFullDto(page.getContent())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OfficeResponse findById(String id) {
        log.info("Find office by id: {}", id);
        var office = findOfficeEntityById(id);
        log.debug("Find office: {}", office);
        return officeMapper.toDto(office);
    }

    @Override
    public PageResponse<OfficeResponse> findOfficesByCity(
            String city,
            PageModel pageModel,
            String apiKey
    ) {
        log.info("Check api key with internal api key");
        if (!internalApiKey.equals(apiKey)) {
            throw new SecurityException("Invalid internal API key");
        }
        log.info("Find offices by city name: {}", city);
        var page = officeRepository.findOfficeByCity(city, pageModel.toPageRequest());
        log.debug("Page offices by city: totalElements: {}, totalPages: {}",
                page.getTotalElements(), page.getTotalPages());
        return new PageResponse<>(
                page.getTotalElements(),
                page.getTotalPages(),
                officeMapper.toDto(page.getContent())
        );
    }

    // TODO: заменить на работу с Kafka и работой с order-service
    //  также работать с *_OWNER (владельцем)
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public OfficeFullResponse create(String userId, OfficeCreateRequest request) {
        log.info("Create office by request: {}", request);

        var office = officeMapper.toEntity(request);
        office.setOwnerId(userId);
        officeRepository.save(office);
        log.debug("Office successfully created");
        return officeMapper.toFullDto(office);
    }

    // TODO: заменить на работу с Kafka и работой с order-service
    //  также работать с *_OWNER (владельцем)
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public OfficeResponse updateOffice(String id, OfficeUpdateRequest request) {
        log.info("Update office by request: {}", request);

        var office = findOfficeEntityById(id);
        var savedOffice = officeMapper.toEntity(office, request);

        officeRepository.save(savedOffice);
        log.debug("Office successfully updated: {}", savedOffice);
        return officeMapper.toDto(savedOffice);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public void blocked(String id) {
        log.info("Blocked/deactivate office by id: {}", id);

        var office = findOfficeEntityById(id);
        office.setActive(false);
        officeRepository.save(office);

        log.debug("Office successfully blocked/deactivated: {}", office);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public void delete(String userId, String id) {
        log.info("Find user by id: {}", userId);
        var user = userServiceFeignClient.findUserById(userId);

        String username = user.username();
        log.debug("Find user {} from the server", username);

        log.info("Delete office by id: {}", id);
        var office = findOfficeEntityById(id);
        office.softDelete(username);

        officeRepository.save(office);
        log.debug("Office successfully deleted: ID={}, deletedBy={}",
                office.getId(), office.getDeletedBy());
    }

    private Office findOfficeEntityById(String id) {
        return officeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Office not found by id: " + id
                ));
    }
}
