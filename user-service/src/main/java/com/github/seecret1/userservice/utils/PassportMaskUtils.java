package com.github.seecret1.userservice.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PassportMaskUtils {

    /**
     * Маскирует номер паспорта
     * 1234 567890 -> 1234 ******
     * 1234-567890 -> 1234-******
     * 1234567890 -> 1234******
     */
    public static String maskPassport(String passport) {
        if (passport == null || passport.length() < 4) {
            return passport;
        }

        // Удаляем все нецифровые символы
        String digits = passport.replaceAll("\\D", "");

        if (digits.length() < 10) {
            // Если меньше 10 цифр, маскируем всё, кроме первых 2
            return maskShortPassport(passport);
        }

        // Берем серию (первые 4 цифры)
        String series = digits.substring(0, 4);

        // Определяем формат (есть пробел или тире)
        boolean hasSpace = passport.contains(" ");
        boolean hasDash = passport.contains("-");

        if (hasSpace) {
            return series + " ******";
        } else if (hasDash) {
            return series + "-******";
        } else {
            return series + "******";
        }
    }

    /**
     * Маскирует паспорт с частичным скрытием серии
     * 1234 567890 -> 12** ******
     */
    public static String maskPassportFull(String passport) {
        if (passport == null || passport.length() < 4) {
            return passport;
        }

        String digits = passport.replaceAll("\\D", "");

        if (digits.length() < 10) {
            return maskShortPassport(passport);
        }

        String series = digits.substring(0, 4);
        String maskedSeries = series.substring(0, 2) + "**";

        boolean hasSpace = passport.contains(" ");
        boolean hasDash = passport.contains("-");

        if (hasSpace) {
            return maskedSeries + " ******";
        } else if (hasDash) {
            return maskedSeries + "-******";
        } else {
            return maskedSeries + "******";
        }
    }

    /**
     * Маскирует паспорт, оставляя только последние 2 цифры номера
     * 1234 567890 -> 1*** *****90
     */
    public static String maskPassportWithSuffix(String passport) {
        if (passport == null || passport.length() < 4) {
            return passport;
        }

        String digits = passport.replaceAll("\\D", "");

        if (digits.length() < 10) {
            return maskShortPassport(passport);
        }

        String series = digits.substring(0, 4);
        String number = digits.substring(4, 10);

        String maskedSeries = series.charAt(0) + "***";
        String maskedNumber = "****" + number.substring(number.length() - 2);

        boolean hasSpace = passport.contains(" ");
        boolean hasDash = passport.contains("-");

        if (hasSpace) {
            return maskedSeries + " " + maskedNumber;
        } else if (hasDash) {
            return maskedSeries + "-" + maskedNumber;
        } else {
            return maskedSeries + maskedNumber;
        }
    }

    /**
     * Маскирует короткий паспорт (загранпаспорт)
     * 12 3456789 -> 12 *******
     */
    public static String maskForeignPassport(String passport) {
        if (passport == null || passport.length() < 2) {
            return passport;
        }

        String digits = passport.replaceAll("\\D", "");

        if (digits.length() < 9) {
            return maskShortPassport(passport);
        }

        String series = digits.substring(0, 2);
        boolean hasSpace = passport.contains(" ");

        if (hasSpace) {
            return series + " *******";
        } else {
            return series + "*******";
        }
    }

    private static String maskShortPassport(String passport) {
        if (passport == null || passport.length() <= 2) {
            return passport;
        }

        String digits = passport.replaceAll("\\D", "");
        if (digits.length() <= 2) {
            return "*".repeat(digits.length());
        }

        String prefix = digits.substring(0, 2);
        String masked = "*".repeat(digits.length() - 2);

        boolean hasSpace = passport.contains(" ");
        boolean hasDash = passport.contains("-");

        if (hasSpace) {
            return prefix + " " + masked;
        } else if (hasDash) {
            return prefix + "-" + masked;
        } else {
            return prefix + masked;
        }
    }
}