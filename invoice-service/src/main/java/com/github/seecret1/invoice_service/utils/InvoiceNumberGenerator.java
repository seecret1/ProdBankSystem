package com.github.seecret1.invoice_service.utils;

import lombok.experimental.UtilityClass;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class InvoiceNumberGenerator {

    private static final String DATE_TIME_PATTERN = "yyyyMMdd-HHmmssSSS";
    private static final AtomicInteger counter = new AtomicInteger(1);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    /**
     * Генерирует номер счета с префиксом, датой, временем и миллисекундами
     * Пример: CRD-20260825-143522123-0001
     */
    public String generateWithPrefix(String prefix) {
        String dateTimePart = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        int number = counter.getAndIncrement();
        return String.format("%s-%s-%04d", prefix, dateTimePart, number);
    }

    /**
     * Сбрасывает счетчик (для тестов или нового периода)
     */
    public void resetCounter() {
        counter.set(1);
    }

    /**
     * Устанавливает начальное значение счетчика
     */
    public void setCounterStart(int startValue) {
        counter.set(startValue);
    }

    /**
     * Получить текущее значение счетчика
     */
    public int getCurrentCounter() {
        return counter.get();
    }
}