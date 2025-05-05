package com.example.event_scheduling.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {

    public Utils() {}

    public static LocalDateTime getUTCDate(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime localDateTime = LocalDateTime.parse(dateString, formatter);

        ZonedDateTime localZoned = localDateTime.atZone(ZoneId.systemDefault());
        ZonedDateTime utcZoned = localZoned.withZoneSameInstant(ZoneId.of("UTC"));

        return utcZoned.toLocalDateTime();
    }
}
