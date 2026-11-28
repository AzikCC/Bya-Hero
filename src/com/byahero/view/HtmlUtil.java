package com.byahero.view;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/** Helpers for putting text safely inside HTML. */
public class HtmlUtil {

    /**
     * Replaces special characters so user text cannot break the page.
     * Example: "<b>" becomes "&lt;b&gt;" and is shown as plain text.
     */
    public static String escape(String text) {
        if (text == null) {
            return "";
        }
        text = text.replace("&", "&amp;");
        text = text.replace("<", "&lt;");
        text = text.replace(">", "&gt;");
        text = text.replace("\"", "&quot;");
        text = text.replace("'", "&#39;");
        return text;
    }

    /** Makes text safe to put inside a web address, like "Baguio City" -> "Baguio+City". */
    public static String urlEncode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    /** Makes the first letter big and the rest small, like "ordinary" -> "Ordinary". */
    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    /** Formats a number with 2 decimals, like 96.0 -> "96.00". */
    public static String money(double amount) {
        return String.format(Locale.US, "%.2f", amount);
    }

    /** Builds <option> tags for a dropdown. The "selected" one is pre-chosen. */
    public static String options(List<String> items, String selected) {
        return options(items, selected, false);
    }

    /** Same, but can show nice labels ("Ordinary") while the value stays the same ("ORDINARY"). */
    public static String options(List<String> items, String selected, boolean prettyLabels) {
        StringBuilder html = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            String label = item;
            if (prettyLabels) {
                label = capitalize(item);
            }
            html.append("<option value=\"").append(escape(item)).append("\"");
            if (item.equalsIgnoreCase(selected)) {
                html.append(" selected");
            }
            html.append(">").append(escape(label)).append("</option>");
        }
        return html.toString();
    }
}
