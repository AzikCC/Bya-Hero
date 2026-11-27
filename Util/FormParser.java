package com.byahero.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Turns form text like "plate=ABC-123&bay=2" into a Map.
 * Browsers send form data and URL queries in this format.
 */
public class FormParser {

    public static Map<String, String> parse(String text) {
        Map<String, String> result = new HashMap<String, String>();
        if (text == null || text.isEmpty()) {
            return result;
        }

        String[] pairs = text.split("&");
        for (int i = 0; i < pairs.length; i++) {
            // split at the first "=" only: "plate=ABC-123" -> ["plate", "ABC-123"]
            String[] parts = pairs[i].split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = "";
            if (parts.length == 2) {
                value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            }
            if (!key.isEmpty()) {
                result.put(key, value);
            }
        }
        return result;
    }

    /** Gets one value from the map. Returns "" if it is missing. */
    public static String get(Map<String, String> params, String key) {
        String value = params.get(key);
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
