package com.orderflow.util;

import lombok.experimental.UtilityClass;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Utility methods for converting between Unix epoch timestamps and {@link LocalDateTime}.
 */
@UtilityClass
public class DateTimeUtil {

    private static final ZoneId UTC = ZoneId.of("UTC");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    /**
     * Converts a Unix epoch timestamp (seconds) to {@link LocalDateTime} in UTC.
     *
     * @param epochSeconds Unix timestamp in seconds
     * @return LocalDateTime in UTC
     */
    public static LocalDateTime fromEpochSeconds(long epochSeconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), UTC);
    }

    /**
     * Converts a Unix epoch timestamp string to {@link LocalDateTime} in UTC.
     *
     * @param epochSecondsStr string representation of Unix timestamp
     * @return LocalDateTime in UTC, or null if input is null or blank
     */
    public static LocalDateTime fromEpochString(String epochSecondsStr) {
        if (epochSecondsStr == null || epochSecondsStr.isBlank()) {
            return null;
        }
        try {
            return fromEpochSeconds(Long.parseLong(epochSecondsStr));
        } catch (NumberFormatException e) {
            return LocalDateTime.now(UTC);
        }
    }

    /**
     * Converts a {@link LocalDateTime} to a Unix epoch timestamp in seconds (UTC).
     */
    public static long toEpochSeconds(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.UTC).getEpochSecond();
    }

    /**
     * Returns the current UTC time.
     */
    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(UTC);
    }
}
