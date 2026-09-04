package com.github.seecret1.userservice.controller.publics;

import com.github.seecret1.userservice.dto.response.PersonInfo;
import com.github.seecret1.userservice.service.PersonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/person")
public class PersonPublicController {

    // TODO: вынести
    private static final String X_INTERNAL_API_KEY = "X-Internal-Api-Key";

    private final PersonService personService;

    @GetMapping("/services/{userId}")
    public PersonInfo getPersonInfo(
            @PathVariable String userId,
            @RequestHeader(value = X_INTERNAL_API_KEY, required = false) String apiKey
    ) {
        return personService.getPersonInfo(userId, apiKey);
    }
}
