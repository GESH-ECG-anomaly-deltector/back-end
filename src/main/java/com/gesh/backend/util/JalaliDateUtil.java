package com.gesh.backend.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class JalaliDateUtil {

    private static final int[] G_D_M = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
    private static final char[] PERSIAN_DIGITS = {'۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'};
    private static final ZoneId ZONE = ZoneId.systemDefault();

    private JalaliDateUtil() {
    }

    public static int[] toJalali(LocalDate date) {
        int gy = date.getYear();
        int gm = date.getMonthValue();
        int gd = date.getDayOfMonth();

        int jy;
        if (gy > 1600) {
            jy = 979;
            gy -= 1600;
        } else {
            jy = 0;
            gy -= 621;
        }
        int gy2 = gm > 2 ? gy + 1 : gy;
        long days = (365L * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) - 80 + gd + G_D_M[gm - 1];

        jy += 33 * (days / 12053);
        days %= 12053;
        jy += 4 * (days / 1461);
        days %= 1461;
        if (days > 365) {
            jy += (days - 1) / 365;
            days = (days - 1) % 365;
        }

        int jm, jd;
        if (days < 186) {
            jm = 1 + (int) (days / 31);
            jd = 1 + (int) (days % 31);
        } else {
            jm = 7 + (int) ((days - 186) / 30);
            jd = 1 + (int) ((days - 186) % 30);
        }
        return new int[]{jy, jm, jd};
    }

    public static String toPersianDigits(String input) {
        StringBuilder sb = new StringBuilder(input.length());
        for (char c : input.toCharArray()) {
            sb.append(c >= '0' && c <= '9' ? PERSIAN_DIGITS[c - '0'] : c);
        }
        return sb.toString();
    }

    public static String formatDate(LocalDate date) {
        int[] j = toJalali(date);
        return toPersianDigits(String.format("%04d/%02d/%02d", j[0], j[1], j[2]));
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        int[] j = toJalali(dateTime.toLocalDate());
        String s = String.format("%04d/%02d/%02d - %02d:%02d", j[0], j[1], j[2], dateTime.getHour(), dateTime.getMinute());
        return toPersianDigits(s);
    }

    public static String formatDateTime(Instant instant) {
        return formatDateTime(LocalDateTime.ofInstant(instant, ZONE));
    }

    public static String formatDateTimeCompact(LocalDateTime dateTime) {
        int[] j = toJalali(dateTime.toLocalDate());
        String s = String.format("%04d/%02d/%02d %02d:%02d", j[0], j[1], j[2], dateTime.getHour(), dateTime.getMinute());
        return toPersianDigits(s);
    }
}