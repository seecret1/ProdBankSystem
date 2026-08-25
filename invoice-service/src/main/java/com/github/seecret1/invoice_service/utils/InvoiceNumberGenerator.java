package com.github.seecret1.invoice_service.utils;

import lombok.experimental.UtilityClass;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class InvoiceNumberGenerator {

    private static final String DATE_PATTERN = "yyyyMMdd";
    private static final AtomicInteger counter = new AtomicInteger(1);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    /**
     * Генерирует номер счета с префиксом и датой
     * Пример: CRD-20260825-0001
     */
    public String generateWithPrefix(String prefix) {
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        long number = counter.getAndIncrement();
        return String.format("%s-%s-%04d", prefix, datePart, number);
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