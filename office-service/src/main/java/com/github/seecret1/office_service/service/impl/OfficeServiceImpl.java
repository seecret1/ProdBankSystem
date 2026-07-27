package com.github.seecret1.office_service.service.impl;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.office_service.dto.request.OfficeCreateRequest;
import com.github.seecret1.office_service.dto.response.OfficeResponse;
import com.github.seecret1.office_service.mapper.OfficeMapper;
import com.github.seecret1.office_service.repository.OfficeRepository;
import com.github.seecret1.office_service.service.OfficeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfficeServiceImpl implements OfficeService {

    @Value("${services.api-key}")
    private String internalApiKey;

    private final OfficeRepository officeRepository;

    private final OfficeMapper officeMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OfficeResponse> findAll(PageModel pageModel) {
        log.info("Use findAll office: {}", pageModel);
        var page = officeRepository.findAll(pageModel.toPageRequest());
        log.debug("Page offices: totalElements: {}, totalPages: {}, content: {}",
                page.getTotalElements(), page.getTotalPages(), page.getContent());
        return new PageResponse<>(
                page.getTotalElements(),
                page.getTotalPages(),
                officeMapper.toDto(page.getContent())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OfficeResponse findById(String id) {
        log.info("Find office by id: {}", id);
        var office = officeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Office not found by id: " + id
                ));
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
        log.debug("Page offices by city: totalElements: {}, totalPages: {}, content: {}",
                page.getTotalElements(), page.getTotalPages(), page.getContent());
        return new PageResponse<>(
                page.getTotalElements(),
                page.getTotalPages(),
                officeMapper.toDto(page.getContent())
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OfficeResponse create(OfficeCreateRequest request) {
        log.info("Create office by request: {}", request);

        var office = officeMapper.toEntity(request);
        officeRepository.save(office);
        log.debug("Office successfully created");
        return officeMapper.toDto(office);
    }
}
