package com.github.seecret1.office_service.dto.request;

import java.time.LocalTime;

public record ScheduleRequest(

        String day,

        LocalTime openingTime,

        LocalTime closingTime

) { }
