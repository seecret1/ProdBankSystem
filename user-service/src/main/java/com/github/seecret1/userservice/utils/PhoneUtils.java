package com.github.seecret1.userservice.utils;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

/**
 * Утилита для работы с номерами телефонов
 */
@UtilityClass
public class PhoneUtils {

    private static final Pattern DIGITS_ONLY = Pattern.compile("\\D");

    /**
     * Очищает номер телефона от всех нецифровых символов
     */
    public String cleanPhoneNumber(String phone) {
        if (phone == null) {
            return null;
        }
        return DIGITS_ONLY.matcher(phone).replaceAll("");
    }

    /**
     * Форматирует номер телефона в международный формат
     * +X (XXX) XXX-XX-XX
     */
    public String formatInternationalPhone(String phone) {
        if (phone == null) {
            return null;
        }

        String cleaned = cleanPhoneNumber(phone);

        if (cleaned.length() == 11 && cleaned.startsWith("7")) {
            // Российский номер
            String number = cleaned.substring(1);
            return "+7 (" + number.substring(0, 3) + ") "
                    + number.substring(3, 6) + "-"
                    + number.substring(6, 8) + "-"
                    + number.substring(8, 10);
        } else if (cleaned.length() == 10) {
            // Номер без кода страны, добавляем +7
            return "+7 (" + cleaned.substring(0, 3) + ") "
                    + cleaned.substring(3, 6) + "-"
                    + cleaned.substring(6, 8) + "-"
                    + cleaned.substring(8, 10);
        } else if (cleaned.length() >= 11) {
            // Определяем код страны
            String countryCode = cleaned.substring(0, cleaned.length() - 10);
            String number = cleaned.substring(countryCode.length());
            return "+" + countryCode + " (" + number.substring(0, 3) + ") "
                    + number.substring(3, 6) + "-"
                    + number.substring(6, 8) + "-"
                    + number.substring(8, 10);
        }

        return phone;
    }

    /**
     * Форматирует номер телефона в простой формат
     * 7XXXXXXXXXX
     */
    public String formatSimple(String phone) {
        if (phone == null) {
            return null;
        }
        String cleaned = cleanPhoneNumber(phone);

        if (cleaned.length() == 10) {
            return "7" + cleaned;
        } else if (cleaned.length() == 11 && (cleaned.startsWith("7") || cleaned.startsWith("8"))) {
            return "7" + cleaned.substring(1);
        }

        return cleaned;
    }

    /**
     * Маскирует номер телефона, оставляя первые 2 и последние 2 цифры
     * +7 (9XX) XXX-XX-XX -> +7 (9**) ***-**-XX
     */
    public String maskPhoneWithPrefix(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }

        String cleaned = cleanPhoneNumber(phone);

        if (cleaned.length() <= 4) {
            return "*".repeat(cleaned.length());
        }

        // Оставляем первые 2 и последние 2 цифры
        String prefix = cleaned.substring(0, 2);
        String suffix = cleaned.substring(cleaned.length() - 2);
        int maskedLength = cleaned.length() - 4;

        String maskedNumber = prefix + "*".repeat(maskedLength) + suffix;

        // Форматируем замаскированный номер
        if (maskedNumber.length() == 11 && maskedNumber.startsWith("7")) {
            String number = maskedNumber.substring(1);
            return "+7 (" + number.substring(0, 3) + ") "
                    + number.substring(3, 6) + "-"
                    + number.substring(6, 8) + "-"
                    + number.substring(8, 10);
        }

        return maskedNumber;
    }

    private String formatPhone(String number, String format) {
        if (number.length() < 10) {
            return number;
        }

        return String.format(format,
                number.charAt(0), number.charAt(1), number.charAt(2),
                number.charAt(3), number.charAt(4), number.charAt(5),
                number.charAt(6), number.charAt(7),
                number.charAt(8), number.charAt(9)
        );
    }

    private String formatUnknownPhone(String phone) {
        if (phone.length() <= 4) {
            return phone;
        }

        int groupSize = Math.min(3, phone.length() / 3);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < phone.length(); i += groupSize) {
            if (i > 0) {
                result.append(" ");
            }
            int end = Math.min(i + groupSize, phone.length());
            result.append(phone, i, end);
        }

        return result.toString();
    }
}