package com.byahero.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Gives us the current date and time as text, like 2026-11-14 06:15:00. */
public class DateUtil {

    public static String now() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(format);
    }
}
