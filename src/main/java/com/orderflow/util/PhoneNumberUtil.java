package com.orderflow.util;

import lombok.experimental.UtilityClass;

/**
 * Utility methods for normalizing and validating phone numbers.
 */
@UtilityClass
public class PhoneNumberUtil {

    private static final String PLUS = "+";

    /**
     * Normalizes a phone number to E.164 format (no spaces, dashes, or parens).
     * Prepends "+" if not already present.
     *
     * @param phone raw phone number string
     * @return normalized E.164 phone number
     */
    public static String normalize(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number must not be blank");
        }
        String digits = phone.replaceAll("[^0-9+]", "");
        if (!digits.startsWith(PLUS)) {
            digits = PLUS + digits;
        }
        return digits;
    }

    /**
     * Returns the raw numeric wa_id (without the leading "+") expected by the WhatsApp API.
     *
     * @param phone E.164 phone number
     * @return wa_id numeric string
     */
    public static String toWaId(String phone) {
        return normalize(phone).replace(PLUS, "");
    }

    /**
     * Validates that a phone number has between 7 and 15 digits (ITU-T E.164 range).
     */
    public static boolean isValid(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        String digits = phone.replaceAll("[^0-9]", "");
        return digits.length() >= 7 && digits.length() <= 15;
    }
}
