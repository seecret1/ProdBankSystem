package com.github.seecret1.office_service.exception.handler;

import com.github.seecret1.common.dto.ErrorResponse;
import com.github.seecret1.office_service.exception.OfficeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.github.seecret1.office_service.utils.ExceptionHandlerUtils.buildResponse;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(OfficeException.class)
    public ResponseEntity<ErrorResponse> handleOfficeException(
            Exception ex
    ) {
        log.error("GlobalRestControllerAdvice -> OfficeException: {}", ex.getMessage());
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}
