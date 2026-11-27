package com.byahero.util;

/** Small helpers for checking and cleaning text. Used by models and services. */
public class Validator {

    /** Returns true if the text is null, empty, or only spaces. */
    public static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    /** Returns the text without extra spaces. A null becomes "". */
    public static String clean(String text) {
        if (text == null) {
            return "";
        }
        return text.trim();
    }

    /** Throws a friendly error if the (already cleaned) value is empty. */
    public static void requireField(String value, String fieldName) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Please fill in the " + fieldName + " field.");
        }
    }
}
