package com.github.seecret1.office_service.utils;

import com.github.seecret1.common.dto.ErrorResponse;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@UtilityClass
public class ExceptionHandlerUtils {

    public static ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String message
    ) {
        return ResponseEntity
                .status(status)
                .body(ErrorResponse
                        .builder()
                        .status(status.value())
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build()
                );
    }
}
