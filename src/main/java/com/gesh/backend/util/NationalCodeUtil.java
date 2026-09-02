package com.gesh.backend.util;


public final class NationalCodeUtil {

    private NationalCodeUtil() {
    }

    public static boolean isValid(String code) {
        if (code == null) {
            return false;
        }
        String trimmed = code.trim();

        if (!trimmed.matches("\\d{10}")) {
            return false;
        }

        if (trimmed.chars().distinct().count() == 1) {
            return false;
        }

        int checkDigit = Character.getNumericValue(trimmed.charAt(9));
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(trimmed.charAt(i)) * (10 - i);
        }

        int remainder = sum % 11;
        return remainder < 2 ? checkDigit == remainder : checkDigit == 11 - remainder;
    }
}
